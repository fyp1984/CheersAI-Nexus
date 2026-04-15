package com.cheersai.nexus.user.service;

import com.cheersai.nexus.common.model.usermanagement.User;
import com.cheersai.nexus.user.config.BetaProvisionProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BetaProvisioningService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final long NODE_BRIDGE_EXTRA_TIMEOUT_SECONDS = 5L;
    private static final String NODE_HTTP_BRIDGE_SCRIPT = """
            let input = '';
            process.stdin.setEncoding('utf8');
            process.stdin.on('data', chunk => input += chunk);
            process.stdin.on('end', async () => {
              try {
                const cfg = JSON.parse(input || '{}');
                const method = (cfg.method || 'GET').toUpperCase();
                const url = cfg.url;
                const user = cfg.user || '';
                const password = cfg.password || '';
                const timeoutMs = Number(cfg.timeoutMs || 15000);
                const body = cfg.body ?? null;
                const contentType = cfg.contentType || '';

                if (!url) {
                  throw new Error('url is required');
                }

                const headers = { 'Accept': 'application/json' };
                if (user && password) {
                  headers['Authorization'] = 'Basic ' + Buffer.from(`${user}:${password}`).toString('base64');
                }
                if (body !== null && body !== undefined && body !== '') {
                  headers['Content-Type'] = contentType || 'application/json';
                }

                const init = { method, headers, signal: AbortSignal.timeout(timeoutMs) };
                if (body !== null && body !== undefined && body !== '') {
                  init.body = typeof body === 'string' ? body : JSON.stringify(body);
                }

                const response = await fetch(url, init);
                const text = await response.text();
                process.stdout.write(JSON.stringify({ status: response.status, body: text }));
              } catch (err) {
                process.stderr.write((err && err.stack) ? err.stack : String(err));
                process.exit(2);
              }
            });
            """;

    private final BetaProvisionProperties properties;
    private final ObjectMapper objectMapper;

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public ProvisioningResult provisionForActivation(User user) {
        if (!properties.isEnabled()) {
            return new ProvisioningResult("", "", null);
        }

        if (user == null) {
            throw new RuntimeException("用户不存在，无法执行开通流程");
        }
        if (!StringUtils.hasText(user.getEmail())) {
            throw new RuntimeException("用户缺少邮箱，无法执行开通流程");
        }

        validateRequiredConfig();

        String usernameCandidate = StringUtils.hasText(user.getUsername()) ? user.getUsername() : generateUsername(user.getEmail());
        String username = normalizeExternalUsername(usernameCandidate);
        String email = user.getEmail().trim().toLowerCase(Locale.ROOT);
        String displayName = StringUtils.hasText(user.getNickname()) ? user.getNickname() : username;

        String ssoInitialPassword = ensureSsoUser(username, email, displayName);
        bindSsoDefaultRole(username);
        ensureFilebayUser(username, email);
        String filebayRepo = safeTrim(properties.getFilebayDefaultRepo());
        ensureFilebayRepo(username);
        ensureFilebayMaskedDir(username);
        return new ProvisioningResult(username, filebayRepo, ssoInitialPassword);
    }

    public String resetSsoPassword(User user, String newPassword) {
        if (user == null) {
            throw new RuntimeException("用户不存在，无法重置 SSO 密码");
        }
        if (!StringUtils.hasText(newPassword)) {
            throw new RuntimeException("新密码不能为空");
        }
        if (!StringUtils.hasText(user.getEmail())) {
            throw new RuntimeException("用户缺少邮箱，无法重置 SSO 密码");
        }

        validateSsoConfig();

        String usernameCandidate = StringUtils.hasText(user.getUsername()) ? user.getUsername() : generateUsername(user.getEmail());
        String ssoUsername = normalizeExternalUsername(usernameCandidate);
        HttpCallResult resetResult = requestFormUrlEncoded(
                HttpMethod.POST,
                normalizeSsoBaseUrl(properties.getSsoBaseUrl()),
                "/api/set-password",
                properties.getSsoClientId(),
                properties.getSsoClientSecret(),
                Map.of(
                        "userOwner", safeTrim(properties.getSsoOwner()),
                        "userName", ssoUsername,
                        "oldPassword", "",
                        "newPassword", safeTrim(newPassword)
                )
        );
        ensureSsoCallSucceeded(resetResult, "SSO 重置密码失败");
        return ssoUsername;
    }

    private void validateRequiredConfig() {
        List<String> missing = new ArrayList<>();
        if (!StringUtils.hasText(properties.getSsoBaseUrl())) {
            missing.add("SSO_API_URL");
        }
        if (!StringUtils.hasText(properties.getSsoClientId())) {
            missing.add("SSO_PROVISION_CLIENT_ID");
        }
        if (!StringUtils.hasText(properties.getSsoClientSecret())) {
            missing.add("SSO_PROVISION_CLIENT_SECRET");
        }
        if (!StringUtils.hasText(properties.getFilebayBaseUrl())) {
            missing.add("FILEBAY_BASE_URL");
        }
        if (!StringUtils.hasText(properties.getFilebayAdminUsername())) {
            missing.add("FILEBAY_ADMIN_USERNAME");
        }
        if (!StringUtils.hasText(properties.getFilebayAdminPassword())) {
            missing.add("FILEBAY_ADMIN_PASSWORD");
        }
        if (!missing.isEmpty()) {
            throw new RuntimeException("开通配置缺失: " + String.join(", ", missing));
        }
    }

    private void validateSsoConfig() {
        List<String> missing = new ArrayList<>();
        if (!StringUtils.hasText(properties.getSsoBaseUrl())) {
            missing.add("SSO_API_URL");
        }
        if (!StringUtils.hasText(properties.getSsoClientId())) {
            missing.add("SSO_PROVISION_CLIENT_ID");
        }
        if (!StringUtils.hasText(properties.getSsoClientSecret())) {
            missing.add("SSO_PROVISION_CLIENT_SECRET");
        }
        if (!missing.isEmpty()) {
            throw new RuntimeException("SSO 配置缺失: " + String.join(", ", missing));
        }
    }

    private String ensureSsoUser(String username, String email, String displayName) {
        String subjectRef = subjectRef(username);
        HttpCallResult getUserResult = request(
                HttpMethod.GET,
                normalizeSsoBaseUrl(properties.getSsoBaseUrl()),
                "/api/get-user",
                properties.getSsoClientId(),
                properties.getSsoClientSecret(),
                Map.of("id", subjectRef),
                null
        );
        ensureSsoCallSucceeded(getUserResult, "SSO 查询用户失败");
        if (getUserResult.statusCode() == 200 && extractDataMap(getUserResult.body()).isPresent()) {
            return null;
        }

        String initialPassword = generatePassword();
        Map<String, Object> payload = new HashMap<>();
        payload.put("owner", safeTrim(properties.getSsoOwner()));
        payload.put("name", username);
        payload.put("displayName", displayName);
        payload.put("email", email);
        payload.put("password", initialPassword);
        payload.put("isEnabled", true);

        HttpCallResult addUserResult = request(
                HttpMethod.POST,
                normalizeSsoBaseUrl(properties.getSsoBaseUrl()),
                "/api/add-user",
                properties.getSsoClientId(),
                properties.getSsoClientSecret(),
                null,
                payload
        );

        if (looksLikeAlreadyExists(addUserResult)) {
            return null;
        }
        ensureSsoCallSucceeded(addUserResult, "SSO 创建用户失败");
        return initialPassword;
    }

    private void bindSsoDefaultRole(String username) {
        if (!StringUtils.hasText(properties.getSsoDefaultRole())) {
            return;
        }
        String roleId = safeTrim(properties.getSsoOwner()) + "/" + safeTrim(properties.getSsoDefaultRole());
        HttpCallResult getRoleResult = request(
                HttpMethod.GET,
                normalizeSsoBaseUrl(properties.getSsoBaseUrl()),
                "/api/get-role",
                properties.getSsoClientId(),
                properties.getSsoClientSecret(),
                Map.of("id", roleId),
                null
        );
        ensureSsoCallSucceeded(getRoleResult, "SSO 查询角色失败");

        Map<String, Object> roleData = extractDataMap(getRoleResult.body())
                .orElseThrow(() -> new RuntimeException("SSO 角色数据为空: " + roleId));

        String userRef = subjectRef(username);
        List<String> users = readStringList(roleData.get("users"));
        if (users.contains(userRef)) {
            return;
        }
        users.add(userRef);
        roleData.put("users", users);

        HttpCallResult updateRoleResult = request(
                HttpMethod.POST,
                normalizeSsoBaseUrl(properties.getSsoBaseUrl()),
                "/api/update-role",
                properties.getSsoClientId(),
                properties.getSsoClientSecret(),
                Map.of("id", roleId),
                roleData
        );
        ensureSsoCallSucceeded(updateRoleResult, "SSO 绑定角色失败");
    }

    private void ensureFilebayUser(String username, String email) {
        HttpCallResult getUserResult = request(
                HttpMethod.GET,
                safeTrim(properties.getFilebayBaseUrl()),
                "/api/v1/users/" + username,
                properties.getFilebayAdminUsername(),
                properties.getFilebayAdminPassword(),
                null,
                null
        );
        if (getUserResult.statusCode() == 200) {
            return;
        }

        Map<String, Object> payload = Map.of(
                "username", username,
                "email", email,
                "password", generatePassword(),
                "must_change_password", true,
                "visibility", "private",
                "send_notify", false
        );

        HttpCallResult createUserResult = request(
                HttpMethod.POST,
                safeTrim(properties.getFilebayBaseUrl()),
                "/api/v1/admin/users",
                properties.getFilebayAdminUsername(),
                properties.getFilebayAdminPassword(),
                null,
                payload
        );
        if (!is2xx(createUserResult.statusCode()) && !looksLikeAlreadyExists(createUserResult)) {
            throw new RuntimeException("FileBay 创建用户失败: HTTP " + createUserResult.statusCode());
        }
    }

    private void ensureFilebayRepo(String username) {
        String repo = safeTrim(properties.getFilebayDefaultRepo());
        HttpCallResult getRepoResult = request(
                HttpMethod.GET,
                safeTrim(properties.getFilebayBaseUrl()),
                "/api/v1/repos/" + username + "/" + repo,
                properties.getFilebayAdminUsername(),
                properties.getFilebayAdminPassword(),
                null,
                null
        );
        if (getRepoResult.statusCode() == 200) {
            return;
        }

        Map<String, Object> payload = Map.of(
                "name", repo,
                "private", true,
                "auto_init", true,
                "default_branch", safeTrim(properties.getFilebayDefaultBranch())
        );

        HttpCallResult createRepoResult = request(
                HttpMethod.POST,
                safeTrim(properties.getFilebayBaseUrl()),
                "/api/v1/admin/users/" + username + "/repos",
                properties.getFilebayAdminUsername(),
                properties.getFilebayAdminPassword(),
                null,
                payload
        );
        if (!is2xx(createRepoResult.statusCode()) && !looksLikeAlreadyExists(createRepoResult)) {
            throw new RuntimeException("FileBay 创建仓库失败: HTTP " + createRepoResult.statusCode());
        }
    }

    private void ensureFilebayMaskedDir(String username) {
        String repo = safeTrim(properties.getFilebayDefaultRepo());
        String maskedDir = safeTrim(properties.getFilebayDefaultMaskedDir()).replaceAll("^/+|/+$", "");
        if (!StringUtils.hasText(maskedDir)) {
            maskedDir = "masked";
        }
        String keepPath = maskedDir + "/.keep";

        HttpCallResult getContentResult = request(
                HttpMethod.GET,
                safeTrim(properties.getFilebayBaseUrl()),
                "/api/v1/repos/" + username + "/" + repo + "/contents/" + keepPath,
                properties.getFilebayAdminUsername(),
                properties.getFilebayAdminPassword(),
                null,
                null
        );
        if (getContentResult.statusCode() == 200) {
            return;
        }

        Map<String, Object> payload = Map.of(
                "message", "init " + maskedDir + " directory",
                "content", Base64.getEncoder().encodeToString("# keep\n".getBytes(StandardCharsets.UTF_8)),
                "branch", safeTrim(properties.getFilebayDefaultBranch())
        );

        HttpCallResult initDirResult = request(
                HttpMethod.POST,
                safeTrim(properties.getFilebayBaseUrl()),
                "/api/v1/repos/" + username + "/" + repo + "/contents/" + keepPath,
                properties.getFilebayAdminUsername(),
                properties.getFilebayAdminPassword(),
                null,
                payload
        );
        if (!is2xx(initDirResult.statusCode()) && !looksLikeAlreadyExists(initDirResult)) {
            throw new RuntimeException("FileBay 初始化目录失败: HTTP " + initDirResult.statusCode());
        }
    }

    private String generateUsername(String email) {
        String normalized = safeTrim(email).toLowerCase(Locale.ROOT);
        String base = normalized.replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
        if (!StringUtils.hasText(base)) {
            base = "beta_user";
        }
        String suffix = UUID.nameUUIDFromBytes(normalized.getBytes(StandardCharsets.UTF_8))
                .toString().replace("-", "").substring(0, 6);
        String trimmedBase = base.length() > 32 ? base.substring(0, 32) : base;
        return (trimmedBase + "_" + suffix).toLowerCase(Locale.ROOT);
    }

    private String normalizeExternalUsername(String username) {
        String normalized = safeTrim(username).toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_-]+", "_")
                .replaceAll("[_-]{2,}", "_")
                .replaceAll("^[_-]+|[_-]+$", "");
        if (!StringUtils.hasText(normalized)) {
            return "beta_user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        }
        if (normalized.length() > 40) {
            normalized = normalized.substring(0, 40).replaceAll("^[_-]+|[_-]+$", "");
            if (!StringUtils.hasText(normalized)) {
                return "beta_user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
            }
        }
        return normalized;
    }

    private String generatePassword() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return "Aa1!" + uuid.substring(0, 12);
    }

    private String subjectRef(String username) {
        return safeTrim(properties.getSsoOwner()) + "/" + username;
    }

    private String normalizeSsoBaseUrl(String baseUrl) {
        String normalized = safeTrim(baseUrl).replaceAll("/+$", "");
        if (normalized.endsWith("/api")) {
            return normalized.substring(0, normalized.length() - 4);
        }
        return normalized;
    }

    private HttpCallResult request(
            HttpMethod method,
            String baseUrl,
            String path,
            String basicUser,
            String basicPassword,
            Map<String, Object> queryParams,
            Object requestBody
    ) {
        String normalizedBase = safeTrim(baseUrl).replaceAll("/+$", "");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(normalizedBase + path);
        if (queryParams != null) {
            queryParams.forEach((key, value) -> builder.queryParam(key, value));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (StringUtils.hasText(basicUser) && StringUtils.hasText(basicPassword)) {
            headers.setBasicAuth(basicUser, basicPassword, StandardCharsets.UTF_8);
        }

        HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new TimeoutRequestFactory(Duration.ofSeconds(properties.getHttpTimeoutSeconds())));
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }
        });
        String url = builder.build(true).toUriString();

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.build(true).toUri(),
                    method,
                    entity,
                    String.class
            );
            return new HttpCallResult(response.getStatusCode().value(), response.getBody());
        } catch (Exception ex) {
            log.warn("beta provisioning request failed in java client: method={}, url={}, error={}", method, url, ex.getMessage());
            try {
                HttpCallResult fallbackResult = requestViaNodeBridge(method, url, basicUser, basicPassword, requestBody, null);
                log.info("beta provisioning request succeeded via node bridge: method={}, url={}, status={}",
                        method, url, fallbackResult.statusCode());
                return fallbackResult;
            } catch (Exception nodeEx) {
                log.error("beta provisioning request failed in node bridge: method={}, url={}, error={}",
                        method, url, nodeEx.getMessage());
                throw new RuntimeException("开通请求失败: " + nodeEx.getMessage());
            }
        }
    }

    private HttpCallResult requestFormUrlEncoded(
            HttpMethod method,
            String baseUrl,
            String path,
            String basicUser,
            String basicPassword,
            Map<String, String> formData
    ) {
        String normalizedBase = safeTrim(baseUrl).replaceAll("/+$", "");
        String url = normalizedBase + path;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (StringUtils.hasText(basicUser) && StringUtils.hasText(basicPassword)) {
            headers.setBasicAuth(basicUser, basicPassword, StandardCharsets.UTF_8);
        }

        String body = buildFormBody(formData);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new TimeoutRequestFactory(Duration.ofSeconds(properties.getHttpTimeoutSeconds())));
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }
        });

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
            return new HttpCallResult(response.getStatusCode().value(), response.getBody());
        } catch (Exception ex) {
            log.warn("beta provisioning form request failed in java client: method={}, url={}, error={}", method, url, ex.getMessage());
            try {
                HttpCallResult fallbackResult = requestViaNodeBridge(
                        method,
                        url,
                        basicUser,
                        basicPassword,
                        body,
                        MediaType.APPLICATION_FORM_URLENCODED_VALUE
                );
                log.info("beta provisioning form request succeeded via node bridge: method={}, url={}, status={}",
                        method, url, fallbackResult.statusCode());
                return fallbackResult;
            } catch (Exception nodeEx) {
                log.error("beta provisioning form request failed in node bridge: method={}, url={}, error={}",
                        method, url, nodeEx.getMessage());
                throw new RuntimeException("开通请求失败: " + nodeEx.getMessage());
            }
        }
    }

    private String buildFormBody(Map<String, String> formData) {
        if (formData == null || formData.isEmpty()) {
            return "";
        }
        return formData.entrySet().stream()
                .map(entry -> encodeFormItem(entry.getKey()) + "=" + encodeFormItem(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String encodeFormItem(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private HttpCallResult requestViaNodeBridge(
            HttpMethod method,
            String url,
            String basicUser,
            String basicPassword,
            Object requestBody
    ) throws Exception {
        return requestViaNodeBridge(method, url, basicUser, basicPassword, requestBody, null);
    }

    private HttpCallResult requestViaNodeBridge(
            HttpMethod method,
            String url,
            String basicUser,
            String basicPassword,
            Object requestBody,
            String contentType
    ) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("node", "-e", NODE_HTTP_BRIDGE_SCRIPT);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        Map<String, Object> payload = new HashMap<>();
        payload.put("method", method.name());
        payload.put("url", url);
        payload.put("user", safeTrim(basicUser));
        payload.put("password", safeTrim(basicPassword));
        payload.put("timeoutMs", Math.max(1000, properties.getHttpTimeoutSeconds() * 1000L));
        payload.put("body", requestBody);
        payload.put("contentType", safeTrim(contentType));

        try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(objectMapper.writeValueAsString(payload));
        }

        boolean finished = process.waitFor(
                properties.getHttpTimeoutSeconds() + NODE_BRIDGE_EXTRA_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        );
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Node HTTP 桥接超时");
        }

        String output = readAll(process.getInputStream());
        if (process.exitValue() != 0) {
            throw new RuntimeException("Node HTTP 桥接失败: " + abbreviate(output, 300));
        }

        JsonNode root = objectMapper.readTree(output);
        int status = root.path("status").asInt(0);
        if (status <= 0) {
            throw new RuntimeException("Node HTTP 桥接返回无效状态: " + abbreviate(output, 300));
        }
        return new HttpCallResult(status, root.path("body").asText(""));
    }

    private String readAll(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    private String abbreviate(String input, int maxLength) {
        if (!StringUtils.hasText(input)) {
            return "";
        }
        String text = input.trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private boolean is2xx(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private void ensureSsoCallSucceeded(HttpCallResult result, String operationName) {
        if (!is2xx(result.statusCode())) {
            throw new RuntimeException(operationName + ": HTTP " + result.statusCode());
        }
        Map<String, Object> root = readJsonMap(result.body());
        if (root == null || !root.containsKey("status")) {
            return;
        }
        String status = String.valueOf(root.get("status"));
        if ("ok".equalsIgnoreCase(status)) {
            return;
        }
        String message = root.get("msg") == null ? "" : String.valueOf(root.get("msg")).trim();
        if (!StringUtils.hasText(message)) {
            message = safeTrim(result.body());
        }
        throw new RuntimeException(operationName + ": " + message);
    }

    private boolean looksLikeAlreadyExists(HttpCallResult result) {
        if (result.statusCode() == 409 || result.statusCode() == 422) {
            return true;
        }
        String body = safeTrim(result.body()).toLowerCase(Locale.ROOT);
        return body.contains("already exists")
                || body.contains("has been taken")
                || body.contains("duplicate")
                || body.contains("already");
    }

    private Map<String, Object> readJsonMap(String body) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        try {
            return objectMapper.readValue(body, MAP_TYPE);
        } catch (Exception ignore) {
            return null;
        }
    }

    private java.util.Optional<Map<String, Object>> extractDataMap(String body) {
        if (!StringUtils.hasText(body)) {
            return java.util.Optional.empty();
        }
        try {
            Map<String, Object> root = objectMapper.readValue(body, MAP_TYPE);
            Object data = root.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Map<String, Object> mapped = dataMap.entrySet().stream()
                        .filter(item -> item.getKey() instanceof String)
                        .collect(Collectors.toMap(
                                item -> (String) item.getKey(),
                                Map.Entry::getValue
                        ));
                return java.util.Optional.of(mapped);
            }
            if (root.containsKey("id")) {
                return java.util.Optional.of(root);
            }
        } catch (Exception ignore) {
            // ignore parse failure and return empty
        }
        return java.util.Optional.empty();
    }

    private List<String> readStringList(Object source) {
        if (!(source instanceof List<?> list)) {
            return new ArrayList<>();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private record HttpCallResult(int statusCode, String body) {
    }

    public record ProvisioningResult(
            String ssoUsername,
            String filebayRepo,
            String ssoInitialPassword
    ) {
    }
}

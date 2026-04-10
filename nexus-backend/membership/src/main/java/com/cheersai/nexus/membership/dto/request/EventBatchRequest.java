package com.cheersai.nexus.membership.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EventBatchRequest {

    @NotNull(message = "事件列表不能为空")
    private List<@Valid EventItemRequest> events;
}

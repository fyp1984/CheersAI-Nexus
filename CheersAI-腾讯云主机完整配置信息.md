### 一、实例基础信息

- **实例ID**：ins-ctrcjim8
    
- **实例名称**：uat-nexus.cheersai.cloud
    
- **状态**：运行中
    
- **操作系统**：TencentOS Server 4 for x86_64
    
- **实例规格**：4核8GB（S8.LARGE8）
    
- **可用区**：广州地域-6号可用区（ap-guangzhou-6）
    
- **生命周期**：包年包月（创建于2026-03-23，到期于2026-04-23）
    

### 二、网络配置详情

- **公网IP**：175.178.236.183（带宽1Mbps）
    
- **内网IP**：172.16.0.2
    
- **网络架构**：默认VPC（Default-VPC）→ 默认子网（Default-Subnet）
    
- **安全组**：sg-bf126b0j（已开放SSH 22端口、HTTP 80/443端口及自定义应用端口）
    

### 三、存储信息

- **系统盘**：50GB 云固态盘（CLOUD_BSSD），挂载于根目录
    

### 四、登录与权限配置

- **默认登录用户**：root（端口22，密码需通过密钥或安全方式获取）
    
- **专用部署用户**：nexus（已创建，家目录`/home/nexus`，权限归属`nexus:nexus`）
    

### 五、部署准备目录

- **应用部署目录**：`/home/nexus/app`（已创建，权限设置为755，归属`nexus:nexus`，用于存放应用包及运行文件）
    

### 六、后续部署指引

1. **应用上传**：通过SCP将应用包（如.jar/.war）传输至`/home/nexus/app`目录；
    
2. **权限校验**：确保nexus用户对部署目录有读写执行权限（`chown -R nexus:nexus /home/nexus/app`）；
    
3. **启动配置**：可在`/home/nexus`下创建启动脚本（如`start.sh`），配置环境变量与应用启动命令；
    
4. **域名验证**：新域名`uat-nexus.cheersai.cloud`已指向服务器公网IP（175.178.236.183），可通过`curl http://uat-nexus.cheersai.cloud:应用端口`验证服务状态；
    
5. **日志查看**：通过`tail -f /home/nexus/app/app.log`查看应用运行日志。
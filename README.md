# 后端项目启动与配置说明

---

## 1. 环境要求

- **JDK**：1.8 及以上（建议 Oracle JDK 1.8.0_201+ 或 OpenJDK 8+）
- **Maven**：3.6 及以上
- **MySQL**：5.7/8.0（建议 8.0，需支持 InnoDB）
- **Redis**：5.x/6.x
- **文件存储**：MinIO（任意可用版本，建议 2022+）或阿里云OSS
- **IDE**：IntelliJ IDEA（推荐）或 Eclipse
- **操作系统**：Windows/Linux/MacOS 均可

---

## 2. 依赖安装与项目构建

1. **克隆代码**
   ```bash
   git clone <your-repo-url>
   cd lesson
   ```
2. **安装依赖并编译**
   ```bash
   mvn clean package -DskipTests
   ```
   > 如首次构建较慢，请确保 Maven 能正常联网下载依赖。

---

## 3. application.yml 关键配置说明

### 3.1 服务端口与环境
```yaml
server:
  port: 8088 # 服务端口   前端默认反向代理端口，如果不懂，请不要随意更改
spring:
  profiles:
    active: dev # 激活的环境
```

### 3.2 数据库（MySQL）
```yaml
spring:
  datasource:
    druid:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://<your-mysql-host>:3306/lesson?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
      username: <your-mysql-username>
      password: <your-mysql-password>
```
- **注意事项**：
    - `<your-mysql-host>` 替换为实际 MySQL 地址（如 127.0.0.1 或远程 IP）。
    - `lesson` 数据库需提前创建。
    - 用户需有该库的读写权限。

### 3.3 Redis
```yaml
spring:
  redis:
    host: <your-redis-host>
    port: 6379
    password: <your-redis-password>
    database: 0
```
- **注意事项**：
    - `<your-redis-host>` 通常为 127.0.0.1 或服务器 IP。
    - 若无密码可留空。

### 3.4 文件存储配置（MinIO/阿里云OSS）

**MinIO配置**
```yaml
storage:
  type: minio  # 存储类型
  minio:
    endpoint: http://<your-minio-host>:<minio-port>
    accessKey: <your-minio-access-key>
    secretKey: <your-minio-secret-key>
    bucketName: <your-minio-bucket>
```

**阿里云OSS配置**
```yaml
storage:
  type: oss  # 存储类型
  oss:
    endpoint: oss-cn-beijing.aliyuncs.com  # OSS区域端点
    accessKeyId: <your-oss-access-key>      # 阿里云AccessKey
    accessKeySecret: <your-oss-secret-key>  # 阿里云SecretKey
    bucketName: <your-oss-bucket>           # OSS存储桶名称
```

**注意事项**：
- **MinIO**：服务需提前启动，`bucketName`需提前在MinIO控制台创建
- **阿里云OSS**：需提前创建OSS存储桶，配置CORS跨域规则
- **一键切换**：通过`storage.type`配置切换存储服务（minio/oss）

### 3.5 JWT 配置
```yaml
lesson:
  jwt:
    secret-key: <your-jwt-secret>
    ttl: 7200000 # token 有效期（毫秒）
    token-name: online-token    // token 名称  与前端一致，不要随更修改
```
- **建议**：`secret-key` 请设置为复杂字符串，勿泄露。

### 3.6 Swagger/Knife4j 配置
```yaml
swagger:
  enabled: true
spring:
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
```
- **说明**：
    - `enabled: true` 开发环境建议开启，生产环境建议关闭。
    - `pathmatch` 配置是 Spring Boot 2.6+ 必需，否则 Swagger 页面 404。

### 3.7 拦截器放行路径（Swagger/静态资源）
```yaml
lesson:
  interceptor:
    exclude-paths:
      - /swagger-ui.html
      - /swagger-ui/**
      - /swagger-resources/**
      - /webjars/**
      - /v2/api-docs
      - /v2/api-docs/**
      - /v3/api-docs
      - /v3/api-docs/**
      - /doc.html
```
- **说明**：
    - 这些路径必须放行，否则 Swagger/Knife4j 页面和接口文档无法访问。

---

## 4. 数据库初始化

1. **创建数据库**
   ```sql
   CREATE DATABASE lesson DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```
2. **导入表结构和初始数据**
    - 使用 Navicat、DBeaver、MySQL Workbench 或命令行导入 `lesson.sql`（如有）。
    - 确保所有表、索引、初始数据导入无误。

---

## 5. 文件存储配置与使用

### 5.1 MinIO 配置与使用

1. **启动 MinIO 服务**
   ```bash
   minio server /data --console-address ":9001"
   ```
2. **创建 bucket**
    - 访问 MinIO 控制台（如 http://localhost:9001），新建 bucket（如 `edu`）。
3. **配置参数**
    - 在 `application.yml` 填写 endpoint、accessKey、secretKey、bucketName。
4. **常见问题**
    - 上传失败：检查 endpoint、密钥、bucket 是否正确，MinIO 服务是否启动。

### 5.2 阿里云OSS 配置与使用

1. **创建OSS存储桶**
    - 登录阿里云控制台，进入OSS服务
    - 创建存储桶，选择合适的地域（如华北1-北京）
    - 设置存储桶权限为"公共读"或"私有"
    - 记录存储桶名称、地域、AccessKey等信息

2. **配置CORS跨域规则**
   ```java
   // 使用项目提供的OssCorsTest测试类自动配置
   @Test
   public void testSetOssCors() throws Exception {
       // 自动设置CORS规则，支持前端直传
       SetBucketCORSRequest request = new SetBucketCORSRequest(bucketName);
       // 配置允许的域名、方法、头部等
   }
   ```

3. **配置参数**
    - 在 `application.yml` 填写 endpoint、accessKeyId、accessKeySecret、bucketName
    - 确保AccessKey有足够的权限（建议使用RAM子账号）

4. **常见问题**
    - 上传失败：检查AccessKey权限、CORS配置、存储桶权限
    - 跨域问题：确保已正确配置CORS规则
    - 权限问题：检查AccessKey是否有效，是否有对应存储桶的操作权限

---

## 6. Redis 配置与使用

1. **启动 Redis 服务**
   ```bash
   redis-server
   ```
2. **配置 host、port、password**
    - 在 `application.yml` 填写对应参数。
3. **常见问题**
    - 连接失败：检查端口、密码、网络连通性。

---

## 7. 启动项目

1. **IDEA 启动**
    - 运行 `com.lesson.LessonApplication` 主类。
2. **命令行启动**
   ```bash
   mvn spring-boot:run
   ```
3. **启动成功后**，控制台会显示端口、环境、数据库等信息。

---

## 8. 访问接口文档

- **Knife4j UI**（推荐）：
  [http://localhost:8088/doc.html](http://localhost:8088/doc.html)
- **Swagger UI**：
  [http://localhost:8088/swagger-ui/index.html](http://localhost:8088/swagger-ui/index.html)

---

## 9. 常见问题排查

### 9.1 数据库连接失败
- 检查 MySQL 是否启动，端口、用户名、密码、数据库名是否正确。
- 检查防火墙、网络连通性。

### 9.2 Redis 连接失败
- 检查 Redis 是否启动，端口、密码是否正确。

### 9.3 文件存储上传失败
**MinIO上传失败**
- 检查 MinIO 是否启动，endpoint、密钥、bucket 是否正确。

**阿里云OSS上传失败**
- 检查AccessKey权限、CORS配置、存储桶权限是否正确
- 确认OSS存储桶已创建且AccessKey有足够权限
- 检查网络连接和OSS服务状态

### 9.4 Swagger/Knife4j 页面 404
- 检查拦截器放行路径、静态资源映射、依赖是否引入、yml 是否有 pathmatch 配置。

### 9.5 删除章节报"未知错误"
- 通常是数据依赖，需先删除相关依赖数据或优化后端异常提示。

### 9.6 端口冲突
- 检查 8088 端口是否被占用，可在 `application.yml` 修改端口，前端也需要修改。

---

## 10. 生产环境安全建议

- **关闭 Swagger/Knife4j**：生产环境建议 `swagger.enabled: false`，避免接口暴露。
- **敏感信息保护**：数据库、MinIO、Redis 密码等敏感信息请勿提交到代码仓库。
- **定期备份数据库和 MinIO 数据**。
- **开启 HTTPS、配置防火墙、限制管理端口访问**。

---

## 11. 其他说明

- **如需自定义配置，请参考 `application.yml` 注释和本说明文档。**
- **如遇到无法解决的问题，请联系项目维护者或在 README 中补充。**

---

## 12. 不可随意修改的配置说明

在实际部署和开发过程中，以下配置项**不可随意修改**，否则可能导致系统异常、数据丢失或安全风险：

### 12.1 数据库表结构
- **表结构、字段名**：
    - 不可随意更改表名、字段名。
    - 更改后会导致 ORM 映射、业务逻辑失效，甚至数据丢失。
    - 如需变更，必须同步修改实体类、Mapper、Service 及相关 SQL 脚本，并做好数据迁移。

### 12.2 存储服务配置
- **storage.type**：
    - 控制使用MinIO还是阿里云OSS，不可随意更改。
    - 更改后需要确保对应的存储服务已正确配置。

- **bucketName**：
    - 项目中 bucketName（如 `edu`）与代码、前端、数据库强相关。
    - 随意更改会导致文件上传/下载失败，历史文件无法访问。
    - 如需更改，需同步修改所有相关配置和历史数据。

- **OSS AccessKey**：
    - 阿里云OSS的AccessKey和SecretKey，用于身份验证。
    - 更改后需要确保新的AccessKey有足够的权限。
    - 建议使用RAM子账号，避免使用主账号AccessKey。

### 12.3 JWT 密钥
- **lesson.jwt.secret-key**：
    - 用于生成和校验用户登录 token。
    - 更改后所有已发放 token 立即失效，用户需重新登录。
    - 切勿泄露或随意更改。

### 12.4 拦截器放行路径
- **lesson.interceptor.exclude-paths**：
    - 这些路径用于放行 Swagger、静态资源、登录等接口。
    - 随意删除或更改会导致接口文档无法访问、前端页面加载异常、登录功能失效等。
    - 如需调整，需充分测试所有相关功能。

### 12.5 服务端口
- **server.port**：
    - 端口如被占用可更改，但需同步通知前端、运维、API 调用方。
    - 生产环境建议固定端口，避免频繁变更。

### 12.6 生产环境安全配置
- **swagger.enabled**、数据库/MinIO/Redis/OSS 密码等敏感信息**：
    - 生产环境必须关闭 Swagger/Knife4j，保护敏感信息。
    - 密码、密钥等敏感信息不可随意暴露或更改。
    - OSS AccessKey建议使用RAM子账号，定期轮换密钥。

### 12.7 Token 名称（token-name）
- **lesson.jwt.token-name**：
    - 该配置为前后端约定的 token 名称（如 `online-token`），前端请求头需携带同名 token。
    - **不可随意修改**，否则前端无法正常传递和识别 token，导致鉴权失败。
    - 如需修改，必须前后端同时调整并充分测试。

### 12.8 拦截器白名单（exclude-paths）
- **lesson.interceptor.exclude-paths**：
    - 白名单路径与前端路由、接口强相关。
    - 随意调整会导致前端部分页面、接口无法访问或被误拦截。
    - 新增/删除白名单路径时，需与前端开发充分沟通并联调。


## 13. 必须导入的基础数据说明

本项目依赖于数据库中的**动态导航栏、课程分类、角色、权限等基础数据**，这些数据通常在前端页面动态渲染和权限控制中使用。

- **首次部署或初始化项目时，必须导入完整的数据库表结构和初始数据**（如 `lesson.sql` 脚本）。
- 包括但不限于：
    - 导航栏表（如 `nav_menu`、`menu` 等）
    - 分类表（如 `course_category`、`category` 等）
    - 角色、权限相关表
    - 其他系统基础配置表
- **如未导入这些数据，前端页面的导航栏、分类、权限等功能将无法正常显示和使用。**
- 如需自定义导航栏、分类等内容，请在数据库中维护相关表数据，并确保与前端路由、接口保持一致。

---

> 本文档适用于 lesson 后端项目，适合新手快速上手和排查问题。

---

# 权限管理系统说明

## 概述

本项目实现了一个基于注解的权限管理系统，用于控制用户对论坛帖子、回复、问答问题、问答回答等资源的增删改权限。

## 核心组件

### 1. 权限注解 (@CheckPermission)

```java
@CheckPermission(resourceType = "forum_topic", operation = "delete")
public Result<String> deleteTopic(@PathVariable Integer id) {
    // 方法实现
}
```

**参数说明：**
- `resourceType`: 资源类型，支持的值：
    - `forum_topic`: 论坛帖子
    - `forum_reply`: 论坛回复
    - `qa_question`: 问答问题
    - `qa_answer`: 问答回答
    - `notification`: 通知（用户消息）
- `operation`: 操作类型，支持的值：
    - `update`: 修改操作
    - `delete`: 删除操作
- `allowAdmin`: 是否允许管理员操作所有资源（默认true）

### 2. 权限切面 (PermissionAspect)

自动拦截带有`@CheckPermission`注解的方法，进行权限验证。

**权限验证逻辑：**
1. 检查用户是否登录
2. 检查用户是否存在
3. 如果是管理员且允许管理员操作，直接放行
4. 否则检查资源是否属于当前用户
5. 对于`notification`类型，只有通知归属用户本人才能操作（如删除）

### 3. 权限异常 (PermissionDeniedException)

当权限验证失败时抛出此异常，会被全局异常处理器捕获并返回友好的错误信息。

## 使用方法

### 1. 在Controller方法上添加注解

```java
@PutMapping("/topics")
@CheckPermission(resourceType = "forum_topic", operation = "update")
public Result<String> updateTopic(@RequestBody ForumTopicsDTO forumTopicsDTO) {
    forumService.updateTopic(forumTopicsDTO);
    return Result.success("修改成功");
}

@DeleteMapping("/topics/{id}")
@CheckPermission(resourceType = "forum_topic", operation = "delete")
public Result<String> deleteTopic(@PathVariable Integer id) {
    forumService.deleteTopic(id);
    return Result.success("删除成功");
}
```

### 2. 确保DTO包含ID字段

为了权限验证能够正确获取资源ID，DTO类需要包含对应的ID字段：

```java
@Data
public class ForumTopicsDTO implements Serializable {
    private Integer topicId; // 用于权限验证
    private String topicTitle;
    private String topicContent;
    private String topicCategory;
}

// 通知无需DTO，直接用主键ID即可
```

### 3. 管理员权限

管理员（`userType = "ADMIN"`）默认可以操作所有资源。如果需要限制管理员的某些操作，可以设置`allowAdmin = false`：

```java
@CheckPermission(resourceType = "forum_topic", operation = "delete", allowAdmin = false)
public Result<String> deleteTopic(@PathVariable Integer id) {
    // 只有资源所有者可以删除，管理员也不行
}
```

## 已实现的权限控制

### 论坛模块
- ✅ 修改帖子：只有帖子作者或管理员可以修改
- ✅ 删除帖子：只有帖子作者或管理员可以删除
- ✅ 修改回复：只有回复作者或管理员可以修改
- ✅ 删除回复：只有回复作者或管理员可以删除

### 问答模块
- ✅ 修改问题：只有问题作者或管理员可以修改
- ✅ 删除问题：只有问题作者或管理员可以删除
- ✅ 修改回答：只有回答作者或管理员可以修改
- ✅ 删除回答：只有回答作者或管理员可以删除

### 通知模块
- ✅ 删除通知：只有通知归属用户本人可以删除

## 外键约束处理

### 论坛模块
- 删除帖子时，自动删除该帖子下的所有回复
- 删除回复时，自动更新帖子的回复数

### 问答模块
- 删除问题时，自动删除该问题下的所有回答

## 错误处理

当权限验证失败时，系统会返回以下错误信息：

```json
{
    "code": 0,
    "msg": "权限不足，无法执行此操作",
    "data": null
}
```

## 日志记录

权限验证过程会记录详细的日志信息，包括：
- 权限验证通过/失败
- 管理员操作
- 资源ID和用户ID信息

## 扩展说明

### 添加新的资源类型

1. 在`PermissionAspect`的`getResourceIdFromArgs`方法中添加新的资源类型处理
2. 在`checkResourcePermission`方法中添加新的权限检查逻辑
3. 确保对应的实体类有正确的用户ID字段

通知（notification）类型已内置支持，无需额外DTO，直接用主键ID。

### 添加新的操作类型

直接在注解中使用新的操作类型即可，系统会自动记录到日志中。

## 注意事项

1. 确保所有需要权限验证的方法都添加了`@CheckPermission`注解
2. 确保DTO类包含正确的ID字段用于权限验证
3. 管理员权限是全局的，请谨慎使用
4. 权限验证依赖于JWT token中的用户ID，确保用户已正确登录

## 文件结构

```
lesson-common/src/main/java/com/lesson/
├── annotation/
│   └── CheckPermission.java          # 权限注解
└── exception/
    └── PermissionDeniedException.java # 权限异常

lesson-server/src/main/java/com/lesson/
├── aspect/
│   └── PermissionAspect.java         # 权限切面
└── handler/
    └── GlobalExceptionHandler.java   # 全局异常处理
```

## 测试示例

### 正常情况
```bash
# 用户删除自己的帖子
DELETE /user/forum/topics/1
Authorization: Bearer <user_token>

# 管理员删除任何帖子
DELETE /user/forum/topics/1
Authorization: Bearer <admin_token>

# 用户删除自己的通知
DELETE /user/notification/1
Authorization: Bearer <user_token>
```

### 权限不足
```bash
# 用户删除别人的帖子
DELETE /user/forum/topics/2
Authorization: Bearer <user_token>

# 用户删除别人的通知
DELETE /user/notification/2
Authorization: Bearer <user_token>

# 返回错误
{
    "code": 0,
    "msg": "权限不足，无法执行此操作",
    "data": null
}
``` 
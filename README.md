# 在线教育系统

## 项目简介

这是一个在线教育系统

## 功能特性

### 核心功能
- ✅ **用户注册登录** - 完整的用户认证系统
- ✅ **动态导航栏** - 基于用户类型的权限控制
- ✅ **分类搜索** - 课程分类筛选功能
- ✅ **响应式设计** - 支持PC和移动端

### 技术架构
- **后端**: Spring Boot + MyBatis-Plus + Redis + JWT
- **前端**: Vue 2 + Element-UI + Vuex + Axios
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.0

## 项目结构

```
lesson/
├── lesson-common/          # 公共模块
├── lesson-pojo/           # 实体类模块
├── lesson-server/         # 后端服务模块
├── school-homework-ui/    # 前端Vue项目
└── lesson.sql            # 数据库脚本
```

## 快速开始

### 1. 环境要求
- JDK 8+
- Node.js 14+
- MySQL 8.0
- Redis 6.0

### 2. 数据库配置
```sql
-- 导入数据库脚本
source lesson.sql
```

### 3. 后端启动
```bash
cd lesson-server
mvn spring-boot:run
```

### 4. 前端启动
```bash
cd school-homework-ui
# 建议不要直接使用 cnpm 安装依赖，会有各种诡异的 bug。可以通过如下操作解决 npm 下载速度慢的问题
npm install --registry=https://registry.npmmirror.com

# 启动服务
npm run dev
```

## 接口文档

### 用户认证接口
- `POST /user/register` - 用户注册
- `POST /user/login` - 用户登录
- `GET /user/getUserInfo` - 获取用户信息

### 导航菜单接口
- `GET /system/nav-menu?userType=xxx` - 获取导航菜单

### 分类管理接口
- `GET /course/category/list` - 获取课程分类列表

## 用户类型

- **STUDENT** - 学生用户
- **TEACHER** - 教师用户  
- **ADMIN** - 管理员用户

## 开发说明

### 后端开发
- 控制器位于 `lesson-server/src/main/java/com/lesson/controller/`
- 服务层位于 `lesson-server/src/main/java/com/lesson/service/`
- 数据访问层位于 `lesson-server/src/main/java/com/lesson/mapper/`

### 前端开发
- 页面组件位于 `school-homework-ui/src/views/`
- API接口位于 `school-homework-ui/src/api/`
- 状态管理位于 `school-homework-ui/src/store/`

## 部署说明

### 生产环境部署
1. 配置数据库连接
2. 配置Redis连接
3. 打包后端项目
4. 构建前端项目
5. 配置Nginx反向代理

## 许可证

MIT License

## 联系方式

如有问题，请联系开发团队。 

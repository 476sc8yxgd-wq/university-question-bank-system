# 大学题目资料库管理系统

一个功能完整的大学题目资料库管理系统，支持题目的录入、管理、搜索、导出等功能，并提供用户权限管理和统计报表功能。

## 功能特性

- **用户管理**：管理员、教师、学生三种角色，支持用户权限控制
- **题目管理**：支持题目的增删改查，包括题目内容、选项、答案、解析等
- **分类管理**：支持题目分类和难度级别管理
- **题目搜索**：支持按关键词、分类、难度等多维度搜索
- **题目导出**：支持导出为 Word 文档格式
- **批量导入**：支持从 Word 文档批量导入题目
- **统计报表**：提供题目数量、用户数量等统计信息
- **系统设置**：支持数据库连接配置和系统参数设置

## 技术栈

- **编程语言**：Java 8+
- **GUI 框架**：Swing
- **数据库**：PostgreSQL (Supabase)、SQLite (可选)
- **数据访问**：JDBC、REST API
- **构建工具**：Maven
- **文档解析**：Apache POI

## 项目结构

```
大学题目资料库管理系统/
├── src/main/java/com/university/questionbank/
│   ├── Main.java                 # 主程序入口
│   ├── config/                   # 配置类
│   │   └── DatabaseConfig.java   # 数据库配置
│   ├── dao/                      # 数据访问接口
│   ├── dao/impl/                 # 数据访问实现
│   ├── model/                    # 数据模型
│   ├── service/                  # 业务逻辑层
│   ├── gui/                      # GUI 界面
│   └── util/                     # 工具类
├── pom.xml                       # Maven 配置文件
└── README.md                     # 项目说明文档
```

## 环境要求

- JDK 8 或更高版本
- Maven 3.6+
- PostgreSQL 数据库（推荐使用 Supabase）
- 或者 SQLite（本地模式）

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd 大学题目资料库管理系统
```

### 2. 配置数据库

#### 使用 Supabase (推荐)

1. 创建 Supabase 项目
2. 在 Supabase Dashboard 中创建表：users, roles, questions, question_categories, question_difficulties
3. 复制数据库连接信息
4. 修改 `src/main/resources/database.properties` 文件：

```properties
db.host=your-project.supabase.co
db.port=5432
db.database=postgres
db.username=postgres
db.password=your-password
```

#### 使用 SQLite (本地模式)

SQLite 模式无需配置数据库连接信息，系统会自动创建本地数据库文件。

### 3. 编译项目

使用 Maven：

```bash
mvn clean package
```

或使用提供的批处理文件（Windows）：

```bash
完整编译.bat
```

### 4. 运行项目

#### REST API 模式（推荐）

```bash
启动应用-Supabase-REST.bat
```

#### JDBC 模式

```bash
启动应用-JDBC.bat
```

#### SQLite 模式

```bash
启动应用-SQLite.bat
```

## 默认账号

系统初始管理员账号：
- 用户名：`admin`
- 密码：`admin123`

**首次登录后请立即修改密码！**

## 依赖库

项目使用了以下第三方库（已包含在 `lib/` 目录中）：

- gson-2.10.1.jar - JSON 处理
- postgresql-42.7.3.jar - PostgreSQL JDBC 驱动
- sqlite-jdbc-3.45.2.0.jar - SQLite JDBC 驱动
- poi-5.2.5.jar - Office 文档处理
- poi-ooxml-5.2.5.jar - Office OpenXML 支持
- jbcrypt-0.4.jar - 密码加密
- commons-io-2.16.1.jar - 文件操作工具

## 数据库模式

### 用户表 (users)
- id: 用户ID
- username: 用户名
- password: 密码（加密）
- role_id: 角色ID
- student_id: 学号（可选）
- real_name: 真实姓名
- email: 邮箱
- created_at: 创建时间
- updated_at: 更新时间

### 角色表 (roles)
- id: 角色ID
- name: 角色名称（admin/teacher/student）
- display_name: 显示名称

### 题目表 (questions)
- id: 题目ID
- content: 题目内容
- option_a: 选项A
- option_b: 选项B
- option_c: 选项C
- option_d: 选项D
- answer: 正确答案
- explanation: 解析
- category_id: 分类ID
- difficulty_id: 难度ID
- created_by: 创建者ID
- created_at: 创建时间
- updated_at: 更新时间

## 功能说明

### 管理员功能
- 用户管理（增删改查）
- 角色权限管理
- 分类管理
- 题目管理（全部权限）
- 统计报表
- 系统设置

### 教师功能
- 题目管理（增删改查）
- 分类管理
- 题目导入导出
- 题目搜索

### 学生功能
- 题目浏览
- 题目搜索
- 题目导出

## 常见问题

### 1. JDBC 连接失败

如果遇到 "尝试连线已失败" 错误，可能是网络原因导致 5432 端口被阻。建议使用 REST API 模式。

### 2. 编译错误

确保所有依赖 JAR 文件已下载到 `lib/` 目录，并运行 `完整编译.bat`。

### 3. 数据库连接配置

请检查 `database.properties` 文件中的数据库连接信息是否正确。

## 许可证

本项目仅供学习和研究使用。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题，请通过 GitHub Issues 联系。

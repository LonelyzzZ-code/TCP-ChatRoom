# TCP 一对一聊天室

> Java 大作业 — 基础版（一对一聊天）

基于 **Java Swing + TCP Socket + 多线程** 的本地聊天程序，支持服务端与客户端双向实时通信。

## 功能特性

- ✅ Swing GUI 图形界面（服务端 / 客户端各一个独立窗口）
- ✅ 实时双向文字消息收发
- ✅ UTF-8 编码，中文完美支持
- ✅ 多线程设计：收发独立，界面不卡顿
- ✅ 客户端断开后服务端支持重连
- ✅ 窗口关闭自动释放网络资源（优雅退出）

## 项目结构

```
TCP/
├── src/
│   ├── server/
│   │   └── ChatServer.java    # 服务端
│   └── client/
│       └── ChatClient.java    # 客户端
├── README.md
├── requirements.txt
└── .gitignore
```

## 运行环境

| 要求 | 说明 |
|------|------|
| JDK | 8 及以上 |
| 外部依赖 | 无（纯 Java 标准库） |
| 平台 | Windows / macOS / Linux |

## 快速开始

### 1. 编译

```bash
javac -encoding UTF-8 src/server/ChatServer.java src/client/ChatClient.java -d out
```

### 2. 运行

**先启动服务端：**

```bash
java -cp out server.ChatServer
```

**再启动客户端：**

```bash
java -cp out client.ChatClient
```

### 3. 开始聊天

两个窗口启动后即可互发消息，输入框回车或点击「发送」按钮均可。

## 技术要点

| 技术点 | 说明 |
|--------|------|
| 网络通信 | TCP Socket（`ServerSocket` / `Socket`），本地 `localhost:8888` |
| 编码处理 | `StandardCharsets.UTF_8`，避免中文乱码 |
| 多线程 | Accept 线程、Receive 线程、Send 线程各自独立 |
| UI 线程安全 | 子线程通过 `SwingUtilities.invokeLater()` 更新界面 |
| 资源管理 | 窗口关闭 → WindowListener → 关闭流 → 关闭 Socket → 退出 |

## 运行顺序

```
┌──────────┐     ┌──────────┐
│ 服务端    │ ←→  │ 客户端    │
│ :8888    │     │          │
└──────────┘     └──────────┘
  先启动           后连接
```

## 许可证

仅供学习和练习使用。

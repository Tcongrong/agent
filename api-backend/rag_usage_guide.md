# RAG知识库使用指南

本文档详细介绍如何给知识库上传文件以及如何调用接口进行问答。

## 一、知识库文件上传

### 1. 上传接口概述

系统提供了两个主要的文档上传接口：

- **单个文件上传**：`/import/single`
- **批量文件上传**：`/import/batch`

这些接口位于 `DocumentImportController` 类中，需要特定的角色权限（ADMIN或TEACHER）才能访问。

### 2. 支持的文件格式

系统支持以下文件格式：
- PDF文件 (.pdf)
- 文本文件 (.txt)
- Markdown文件 (.md)
- Word文档 (.docx)

### 3. 单个文件上传

#### API详情

- **URL**: `/import/single`
- **方法**: POST
- **请求体**: `ImportDocumentRequest`
- **权限要求**: ADMIN或TEACHER角色

#### 请求参数格式

```json
{
  "filePath": "D:\\path\\to\\your\\document.pdf",
  "metadata": {
    "documentType": "操作系统",
    "source": "考研资料",
    "description": "操作系统考研复习笔记"
  }
}
```

#### 导入流程

1. 系统验证文件是否存在且类型是否支持
2. 根据文件类型调用相应的处理方法提取内容
3. 将文档内容分割成多个块（chunks）
4. 为每个块生成向量表示
5. 将向量和元数据存储到向量数据库
6. 返回导入结果，包括分块数和导入的ID列表

### 4. 批量文件上传

#### API详情

- **URL**: `/import/batch`
- **方法**: POST
- **请求体**: `BatchImportRequest`
- **权限要求**: ADMIN或TEACHER角色

#### 请求参数格式

```json
{
  "directoryPath": "D:\\path\\to\\your\\documents\\folder",
  "metadata": {
    "documentType": "操作系统",
    "source": "考研资料",
    "description": "操作系统考研复习资料合集"
  }
}
```

#### 批量导入流程

1. 系统递归扫描指定目录，找出所有支持的文件格式
2. 使用线程池并行处理多个文件，提高导入效率
3. 对每个文件执行与单个文件相同的导入流程
4. 返回所有文件的导入结果列表

## 二、问答接口调用

系统提供了两个主要的问答接口，根据是否是操作系统相关问题，自动决定是否使用RAG增强回答。

### 1. 单轮对话接口

#### API详情

- **URL**: `/api/ai/test/chat`
- **方法**: POST
- **请求体**: `TestChatRequest`

#### 请求参数格式

```json
{
  "prompt": "什么是进程调度？",
  "systemMessage": "你是一个操作系统专家，擅长解答操作系统相关问题。"
}
```

#### 工作流程

1. 系统从请求头获取当前用户ID
2. 创建新的聊天会话
3. 自动检测问题是否与操作系统相关（通过关键词匹配）
4. 如果是操作系统相关问题，则使用RAG服务增强回答
5. 否则，使用标准的深度求索服务回答
6. 保存用户消息和AI响应到数据库
7. 更新会话标题
8. 返回包含会话ID、问题和回答的响应

### 2. 多轮对话接口

#### API详情

- **URL**: `/api/ai/test/multi-chat`
- **方法**: POST
- **请求体**: `MultiChatRequest`

#### 请求参数格式

```json
{
  "sessionId": 123, // 可选，如果提供则继续已有会话
  "messages": [
    {
      "role": "user",
      "content": "什么是进程？"
    },
    {
      "role": "assistant",
      "content": "进程是程序在执行过程中的实例..."
    },
    {
      "role": "user",
      "content": "进程和线程有什么区别？"
    }
  ],
  "systemMessage": "你是一个操作系统专家，擅长解答操作系统相关问题。"
}
```

#### 工作流程

1. 系统从请求头获取当前用户ID
2. 如果提供了会话ID，则使用已有会话；否则创建新会话
3. 构建消息列表用于API调用
4. 检查最新的用户问题是否与操作系统相关
5. 如果是操作系统相关问题，则使用RAG服务进行多轮增强回答
6. 否则，使用标准的深度求索多轮对话服务
7. 保存新的用户消息和AI响应到数据库
8. 返回包含会话ID、问题历史和最新回答的响应

## 三、操作系统相关问题检测

系统通过关键词匹配的方式自动检测问题是否与操作系统相关。主要关键词包括：

- 操作系统、OS、进程、线程、调度
- 内存、存储、文件系统、虚拟内存
- 死锁、互斥、同步、并发
- 中断、系统调用、内核、用户态、内核态
- 进程管理、内存管理、设备管理
- 考研、计算机基础、计算机组成
- 指令系统、CPU、处理器、缓存、页表、段表

当问题中包含这些关键词时，系统会自动使用RAG服务，从知识库中检索相关信息来增强回答。

## 四、完整使用示例

### 示例1：上传单个文档并进行问答

#### 步骤1：上传文档

```bash
curl -X POST http://localhost:8080/import/single \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "filePath": "D:\\materials\\os_notes.pdf",
    "metadata": {
      "documentType": "操作系统",
      "source": "考研资料"
    }
  }'
```

#### 步骤2：提问

```bash
curl -X POST http://localhost:8080/api/ai/test/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "prompt": "简述操作系统中的死锁概念及其必要条件",
    "systemMessage": "你是一个操作系统专家"
  }'
```

### 示例2：批量上传文档并进行多轮对话

#### 步骤1：批量上传

```bash
curl -X POST http://localhost:8080/import/batch \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "directoryPath": "D:\\materials\\os_books",
    "metadata": {
      "documentType": "操作系统",
      "source": "教材"
    }
  }'
```

#### 步骤2：开始多轮对话

```bash
# 第一轮
curl -X POST http://localhost:8080/api/ai/test/multi-chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "messages": [
      {
        "role": "user",
        "content": "什么是虚拟内存？"
      }
    ],
    "systemMessage": "你是一个操作系统专家"
  }'

# 第二轮（使用返回的sessionId）
curl -X POST http://localhost:8080/api/ai/test/multi-chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "sessionId": 123,  # 替换为实际返回的会话ID
    "messages": [
      {
        "role": "user",
        "content": "什么是虚拟内存？"
      },
      {
        "role": "assistant",
        "content": "虚拟内存是操作系统使用的一种内存管理技术..."
      },
      {
        "role": "user",
        "content": "它有哪些优缺点？"
      }
    ],
    "systemMessage": "你是一个操作系统专家"
  }'
```

## 五、注意事项

1. **文件路径格式**：Windows系统下，文件路径中的反斜杠需要使用双反斜杠（\\）转义

2. **权限要求**：文件上传接口需要ADMIN或TEACHER角色，确保请求头中包含有效的JWT token

3. **关键词识别**：系统自动通过关键词识别操作系统相关问题，确保问题中包含相关术语以获得RAG增强回答

4. **会话管理**：多轮对话需要保持会话ID以维持上下文

5. **文件大小限制**：过大的文件可能导致处理时间过长，建议对大型文档进行适当分割

6. **中文编码**：确保所有文件使用UTF-8编码以正确处理中文内容

## 六、故障排除

1. **上传失败**：
   - 检查文件路径是否正确
   - 确认文件类型是否受支持
   - 验证用户权限是否足够

2. **问答不准确**：
   - 确认问题是否包含操作系统相关关键词
   - 检查知识库中是否有足够的相关文档
   - 尝试调整问题表述，使其更明确地指向特定的操作系统概念
# RAG服务接口调度流程

本文档详细介绍了RAG（检索增强生成）相关服务接口的调用关系和工作流程，基于项目中实现的功能模块。

## 核心服务组件

### 1. VectorStoreService

**主要功能**：向量存储服务，负责文档嵌入向量的存储和相似度检索

**关键方法**：
- `insertDocuments(List<String> texts, List<Map<String, Object>> metadatas)`：插入文档向量到Milvus
- `searchSimilar(String query)`：根据查询文本检索相似文档

**调用关系**：
- 被RagService调用，用于存储和检索文档
- 使用EmbeddingService生成向量

### 2. EmbeddingService

**主要功能**：文本嵌入服务，负责将文本转换为向量表示

**关键方法**：
- `embed(String text)`：为单个文本生成嵌入向量
- `batchEmbed(List<String> texts)`：批量为多个文本生成嵌入向量

**调用关系**：
- 被VectorStoreService调用，用于生成文档和查询的向量表示

### 3. DeepSeekService

**主要功能**：与DeepSeek大语言模型的交互服务

**关键方法**：
- `chat(String prompt, String systemMessage)`：单轮对话
- `multiChat(List<Map<String, String>> messages, String systemMessage)`：多轮对话

**调用关系**：
- 被RagService调用，用于生成最终回答
- 被AITestController直接调用，用于非RAG增强的标准对话

### 4. RagService

**主要功能**：检索增强生成服务，整合向量检索和语言模型

**关键方法**：
- `answerWithRag(String query, Long userId)`：单轮RAG增强回答
- `answerMultiWithRag(List<Map<String, String>> messages, String systemMessage, Long userId)`：多轮RAG增强回答

**调用关系**：
- 被AITestController调用
- 内部调用VectorStoreService进行文档检索
- 内部调用DeepSeekService生成回答

### 5. DocumentProcessingService

**主要功能**：文档处理服务，负责PDF等文档的解析和文本提取

**关键方法**：
- `extractTextFromPDF(String filePath)`：从PDF文件提取文本

**调用关系**：
- 被DocumentImportController调用，用于处理上传的文档

## 接口调度流程

### 1. 文档导入流程

```
DocumentImportController → DocumentProcessingService → EmbeddingService → VectorStoreService
```

**详细步骤**：
1. 客户端上传文档到DocumentImportController
2. DocumentImportController调用DocumentProcessingService解析文档
3. DocumentProcessingService使用PDFBox提取文本内容
4. 提取的文本通过EmbeddingService转换为向量
5. 向量和元数据存储到VectorStoreService（Milvus）

### 2. RAG增强查询流程

**单轮查询**：
```
AITestController → RagService.answerWithRag → VectorStoreService.searchSimilar → DeepSeekService.chat
```

**多轮对话**：
```
AITestController → RagService.answerMultiWithRag → VectorStoreService.searchSimilar → DeepSeekService.multiChat
```

**详细步骤**：
1. 客户端发送查询到AITestController
2. AITestController检测是否为操作系统相关问题
3. 对于操作系统相关问题，调用RagService.answerWithRag或answerMultiWithRag
4. RagService处理流程：
   - 从最新用户消息中提取查询
   - 调用VectorStoreService.searchSimilar检索相关文档
   - 构建增强的上下文和提示词
   - 调用DeepSeekService生成回答
5. 非操作系统问题，直接调用DeepSeekService生成回答
6. 将回答返回给客户端

## 服务调用参数说明

### RagService方法参数

1. **单轮RAG增强**：
   - `query`：用户查询文本
   - `userId`：用户ID

2. **多轮RAG增强**：
   - `messages`：对话历史消息列表
   - `systemMessage`：系统提示词
   - `userId`：用户ID

### DeepSeekService方法参数

1. **单轮对话**：
   - `prompt`：提示词
   - `systemMessage`：系统提示词（可为null）

2. **多轮对话**：
   - `messages`：消息列表，每个消息为Map，包含role和content
   - `systemMessage`：系统提示词（可为null）

### VectorStoreService方法参数

1. **插入文档**：
   - `texts`：文本列表
   - `metadatas`：元数据列表

2. **搜索相似**：
   - `query`：查询文本

## 代码示例

### 1. 单轮RAG查询示例

```java
// 在Controller中的调用
String query = "什么是进程调度？";
Long userId = 1L;
String response = ragService.answerWithRag(query, userId);
```

### 2. 多轮RAG对话示例

```java
// 在Controller中的调用
List<Map<String, String>> messages = new ArrayList<>();
messages.add(Map.of("role", "user", "content", "操作系统的主要功能是什么？"));
messages.add(Map.of("role", "assistant", "content", "操作系统有五大功能..."));
messages.add(Map.of("role", "user", "content", "详细解释一下进程管理"));

String systemMessage = "你是一个操作系统专家";
Long userId = 1L;
String response = ragService.answerMultiWithRag(messages, systemMessage, userId);
```

### 3. 文档导入示例

```java
// 客户端上传文件后，在Controller中的处理
String filePath = "path/to/uploaded/file.pdf";
List<String> texts = documentProcessingService.extractTextFromPDF(filePath);
List<Map<String, Object>> metadatas = texts.stream().map(text -> 
    Map.of("source", "pdf", "uploadTime", System.currentTimeMillis())
).collect(Collectors.toList());

vectorStoreService.insertDocuments(texts, metadatas);
```

## 注意事项

1. 所有服务都使用Spring的依赖注入进行管理，通过`@Autowired`进行装配
2. VectorStoreService使用Milvus进行向量存储，已适配SDK 2.3.4版本
3. 在调用RAG服务时，系统会自动检测问题是否与操作系统相关，只有相关问题才会触发RAG增强
4. 文档相似度搜索结果会根据配置的阈值进行过滤，低于阈值的结果不会被用于上下文构建
5. 所有服务都包含完善的异常处理和日志记录，确保系统稳定性

## 配置说明

RAG服务的关键配置项：

- `rag.context.max_size`：上下文最大长度，默认1000
- `rag.context.min_score`：最小相似度分数，默认0.3
- `rag.prompt.prefix`：提示词前缀，默认"# 操作系统考研问答\n\n"
- `rag.prompt.context_template`：上下文模板
- `rag.prompt.query_template`：查询模板

通过修改这些配置，可以调整RAG服务的行为和性能。
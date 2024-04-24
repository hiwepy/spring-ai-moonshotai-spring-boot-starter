# spring-ai-moonshotai-spring-boot-starter

 > 基于 [Moonshot AI（月之暗面）](https://www.moonshot.cn) 和 Spring AI 的 Spring Boot Starter 实现

### Moonshot AI

Moonshot的文本生成模型（指moonshot-v1）是训练用于理解自然语言和书面语言的，它可以根据输入生成文本输出。对模型的输入也被称为“prompt”。通常我们建议您提供明确的指令以及给出一些范例，来让模型能够完成既定的任务，设计 prompt 本质上就是学会如何“训练”模型。moonshot-v1模型可以用于各种任务，包括内容或代码生成、摘要、对话、创意写作等。

- 官网地址：[https://www.moonshot.cn](https://www.moonshot.cn)
- API文档：[https://platform.moonshot.cn/docs/api-reference](https://platform.moonshot.cn/docs/api-reference)

#### 支持的功能包括：

- 支持文本生成（Chat Completion API）
- 支持多轮对话（Chat Completion API），支持返回流式输出结果
- 支持函数调用（Function Calling）

#### 模型列表

你可以使用 Moonshot AI 的 [List Models API](https://platform.moonshot.cn/docs/api-reference#list-models) 来获取当前可用的模型列表。

当前，支持的模型有：

- **moonshot-v1-8k**: 它是一个长度为 8k 的模型，适用于生成短文本。
- **moonshot-v1-32k**: 它是一个长度为 32k 的模型，适用于生成长文本。
- **moonshot-v1-128k**: 它是一个长度为 128k 的模型，适用于生成超长文本。

以上模型的区别在于它们的最大上下文长度，这个长度包括了输入消息和生成的输出，在效果上并没有什么区别。这个主要是为了方便用户选择合适的模型。

### Maven

``` xml
<dependency>
	<groupId>com.github.teachingai</groupId>
	<artifactId>spring-ai-moonshotai-spring-boot-starter</artifactId>
	<version>${project.version}</version>
</dependency>
```

### Sample

使用示例请参见 [Spring AI Examples](https://github.com/teachingai/spring-ai-examples)

### License

[Apache License 2.0](LICENSE)

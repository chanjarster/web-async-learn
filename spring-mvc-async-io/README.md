# Spring MVC Async IO

在Spring MVC里可以用以下返回值来做异步处理：

1. Callable<?>
1. DeferredResult<?>
1. ListenableFuture<?> or CompletableFuture<?>/CompletionStage<?>
1. ResponseBodyEmitter
1. SseEmitter
1. StreamingResponseBody

但是这些使用的都是Servlet 3.0 Async Processing机制，那么如何才在Spring MVC中使用Servlet 3.1 Async IO（\[Read|Write\]Listener）呢？本文在这里介绍一些方法。

TODO

## 相关资料


* [Is Servlet 3.1 (Read|Write)Listener supported by DeferredResult in Spring 4?][ref-6]

[ref-6]: https://stackoverflow.com/questions/28828355/is-servlet-3-1-readwritelistener-supported-by-deferredresult-in-spring-4

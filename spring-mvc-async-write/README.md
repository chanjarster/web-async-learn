# Spring MVC Async Write

## ResponseBodyEmitter

> A ``ResponseBodyEmitter`` can be returned to write multiple objects to the response asynchronously; also supported as the body within a ``ResponseEntity``.

ResponseBodyEmitterReturnValueHandler

HTTP Streaming
https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-ann-async-http-streaming

## SseEmitter

> An ``SseEmitter`` can be returned to write Server-Sent Events to the response asynchronously; also supported as the body within a ``ResponseEntity``.

ResponseBodyEmitterReturnValueHandler

HTTP Streaming With Server-Sent Events
https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-ann-async-sse

## StreamingResponseBody

> A ``StreamingResponseBody`` can be returned to write to the response OutputStream asynchronously; also supported as the body within a ``ResponseEntity``.

StreamingResponseBodyReturnValueHandler

[HTTP Streaming Directly To The OutputStream][HTTP Streaming Directly To The OutputStream]



## 对于Servlet Async IO的支持


## 相关资料


* [Spring Web MVC Doc - Supported method return values][ref-1]
* [HTTP Streaming][ref-2]
* [HTTP Streaming With Server-Sent Events][ref-3]
* [HTTP Streaming Directly To The OutputStream][ref-4]
* [Spring MVC 3.2 Preview: Techniques for Real-time Updates][ref-5]

[ref-1]: https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-ann-return-types
[ref-2]: https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-ann-async-http-streaming
[ref-3]: https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-ann-async-sse
[ref-4]: https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-ann-async-output-stream
[ref-5]: https://spring.io/blog/2012/05/08/spring-mvc-3-2-preview-techniques-for-real-time-updates/


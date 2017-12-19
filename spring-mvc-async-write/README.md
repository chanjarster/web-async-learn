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

HTTP Streaming Directly To The OutputStream
https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-ann-async-output-stream


## 对于Servlet Async IO的支持


## 相关资料


* [Spring Web MVC Doc - Supported method return values][ref-1]
* [Spring Web MVC Doc - Asynchronous Request Processing][ref-2]
* [Spring Web MVC Doc - Configuring Asynchronous Request Processing][ref-3]
* [Spring MVC 3.2 Preview: Introducing Servlet 3, Async Support][ref-4]
* [Configuring Spring MVC Async Threads][ref-5]
* [Is Servlet 3.1 (Read|Write)Listener supported by DeferredResult in Spring 4?][ref-6]

[ref-1]: https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-ann-return-types
[ref-2]: https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-ann-async
[ref-3]: https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-ann-async-configuration
[ref-4]: https://spring.io/blog/2012/05/07/spring-mvc-3-2-preview-introducing-servlet-3-async-support
[ref-5]: http://www.clianz.com/2016/02/24/configuring-spring-mvc-async-threads/
[ref-6]: https://stackoverflow.com/questions/28828355/is-servlet-3-1-readwritelistener-supported-by-deferredresult-in-spring-4
[ref-7]: https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-ann-async-configuration-spring-mvc

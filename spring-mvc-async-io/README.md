# Spring MVC Async IO

在[前一篇文章][ref-spring-mvc-async-proc]里介绍了Spring MVC对于Servlet 3.0 Async Processing的支持。
同时也提到了Spring MVC并没有提供对Servlet 3.1 Async IO的直接支持，本文介绍一些在Spring MVC中使用到Servlet 3.1 Async Processing的方法。

在开始讲解前要先了解Spring MVC是如何做Async Processing的，这里涉及到一个关键类`WebAsyncManager`。

## WebAsyncManager

在[前一篇文章][ref-spring-mvc-async-proc]里提到过以下几种`AsyncHandlerMethodReturnValueHandler`：

1. `CallableMethodReturnValueHandler`
1. `DeferredResultMethodReturnValueHandler`
1. `ResponseBodyEmitterReturnValueHandler`
1. `StreamingResponseBodyReturnValueHandler`

仔细看这些Handler的代码会发现最终都使用`WebAsyncManager`执行异步处理。

`WebAsyncManager`执行的大致动作有这么几个：

1. 调用`ServletRequest.startAsync()`
1. 使用`AsyncTaskExecutor`开启另一个线程执行任务
1. 调用各种Interceptor的回调方法，比如`CallableProcessingInterceptor`和`DeferredResultProcessingInterceptor`

下面以`Callable<?>`为例，详细讲解一下`WebAsyncManager`的执行过程（下面的T-*代表不同的线程）：

1. T-http-1: `WebAsyncManager.startCallableProcessing`
1. T-http-1: `CallableInterceptorChain.applyBeforeConcurrentHandling` -> `CallableProcessingInterceptor.beforeConcurrentHandling`
1. T-http-1: `WebAsyncManager.startAsyncProcessing` -> `AsyncWebRequest.startAsync` -> `ServletRequest.startAsync`
1. T-http-1: `AsyncTaskExecutor.submit(Callable)`
1. T-mvc-async: `CallableInterceptorChain.applyPreProcess` -> `CallableProcessingInterceptor.preProcess`
1. T-mvc-async: `Callable.run`，等待执行完毕
1. T-mvc-async: `CallableInterceptorChain.applyPostProcess` -> `CallableProcessingInterceptor.postProcess`
1. T-mvc-async: `WebAsyncManager.setConcurrentResultAndDispatch` -> `AsyncWebRequest.dispatch` -> `ServletRequest.dispatch`
1. T-http-2: DispatchServlet ...
1. T-http-2: `AsyncWebRequest.onComplete` -> `CallableInterceptorChain.triggerAfterCompletion` -> `CallableProcessingInterceptor.afterCompletion`

上面的结果可以通过观察源代码、访问[http://localhost:8080/callable-trace][url-callable-trace]查看日志，下面是日志样例（注意观察`CallableProcessingLogger`）：

```
2017-12-25 16:58:09.632 DEBUG 19794 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : DispatcherServlet with name 'dispatcherServlet' processing GET request for [/callable-trace]
2017-12-25 16:58:09.632 DEBUG 19794 --- [nio-8080-exec-1] s.w.s.m.m.a.RequestMappingHandlerMapping : Looking up handler method for path /callable-trace
2017-12-25 16:58:09.632 DEBUG 19794 --- [nio-8080-exec-1] s.w.s.m.m.a.RequestMappingHandlerMapping : Returning handler method [public java.util.concurrent.Callable<java.lang.String> me.chanjar.learning.CallableTraceController.hello()]
2017-12-25 16:58:09.632 DEBUG 19794 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Last-Modified value for [/callable-trace] is: -1
2017-12-25 16:58:09.633  INFO 19794 --- [nio-8080-exec-1] orConfiguration$CallableProcessingLogger : beforeConcurrentHandling
2017-12-25 16:58:09.633 DEBUG 19794 --- [nio-8080-exec-1] o.s.w.c.request.async.WebAsyncManager    : Concurrent handling starting for GET [/callable-trace]
2017-12-25 16:58:09.633 DEBUG 19794 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Leaving response open for concurrent processing
2017-12-25 16:58:09.633  INFO 19794 --- [ my-mvc-async-1] orConfiguration$CallableProcessingLogger : preProcess
2017-12-25 16:58:10.636  INFO 19794 --- [ my-mvc-async-1] orConfiguration$CallableProcessingLogger : postProcess
2017-12-25 16:58:10.636 DEBUG 19794 --- [ my-mvc-async-1] o.s.w.c.request.async.WebAsyncManager    : Concurrent result value [Hi from CallableTraceController. Current Thread: my-mvc-async-2] - dispatching request to resume processing
2017-12-25 16:58:10.636 DEBUG 19794 --- [nio-8080-exec-2] o.s.web.servlet.DispatcherServlet        : DispatcherServlet with name 'dispatcherServlet' resumed processing GET request for [/callable-trace]
2017-12-25 16:58:10.637 DEBUG 19794 --- [nio-8080-exec-2] s.w.s.m.m.a.RequestMappingHandlerMapping : Looking up handler method for path /callable-trace
2017-12-25 16:58:10.637 DEBUG 19794 --- [nio-8080-exec-2] s.w.s.m.m.a.RequestMappingHandlerMapping : Returning handler method [public java.util.concurrent.Callable<java.lang.String> me.chanjar.learning.CallableTraceController.hello()]
2017-12-25 16:58:10.637 DEBUG 19794 --- [nio-8080-exec-2] o.s.web.servlet.DispatcherServlet        : Last-Modified value for [/callable-trace] is: -1
2017-12-25 16:58:10.637 DEBUG 19794 --- [nio-8080-exec-2] s.w.s.m.m.a.RequestMappingHandlerAdapter : Found concurrent result value [Hi from CallableTraceController. Current Thread: my-mvc-async-2]
2017-12-25 16:58:10.638 DEBUG 19794 --- [nio-8080-exec-2] m.m.a.RequestResponseBodyMethodProcessor : Written [Hi from CallableTraceController. Current Thread: my-mvc-async-2] as "text/plain" using [org.springframework.http.converter.StringHttpMessageConverter@70444987]
2017-12-25 16:58:10.639 DEBUG 19794 --- [nio-8080-exec-2] o.s.web.servlet.DispatcherServlet        : Null ModelAndView returned to DispatcherServlet with name 'dispatcherServlet': assuming HandlerAdapter completed request handling
2017-12-25 16:58:10.639 DEBUG 19794 --- [nio-8080-exec-2] o.s.web.servlet.DispatcherServlet        : Successfully completed request
2017-12-25 16:58:10.639  INFO 19794 --- [nio-8080-exec-2] orConfiguration$CallableProcessingLogger : afterCompletion
```

## 将ReadListener加到MVC异步处理过程

先来回顾一下使用`ReadListener`的大致步骤：

```java

protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

  AsyncContext asyncCtx = req.startAsync();
  ServletInputStream is = req.getInputStream();
  is.setReadListener(new ReadListener() {

    @Override
    public void onDataAvailable() {

        while (is.isReady() && !is.isFinished()) {
          int length = is.read(buffer);
          if (length == -1 && is.isFinished()) {
            asyncCtx.complete();
            return;
          }
          // 处理buffer
        }

    }

    @Override
    public void onAllDataRead() {
      // ...
    }

    @Override
    public void onError(Throwable t) {
      // ...
    }
  });

}
```

所以如果要使用`ReadListener`，那么能够切入的点有两个：第5步和第7步。

如果在第5步`CallableProcessingInterceptor.preProcess`里使用`ReadListener`则：

* 在`ReadListener.onAllDataRead`或者`onError`之前，`Callable<?>`必须阻塞。
* `ReadListener`和`Callable<?>`需要有通信机制。
* `Callable<?>`可以根据不同情况返回不同的结果。

如果在第7步`CallableProcessingInterceptor.postProcess`里使用`ReadListener`则：

* `Callable<?>`事实上变成事先返回结果，而不是等`ReadListener.onAllDataRead`或者`onError`。
* `CallableProcessingInterceptor.postProcess`必须阻塞
* `ReadListener`和`Callable<?>`无法存在通信机制。


`spring.http.multipart.resolve-lazily=true`，否则被Multipart***事先消费掉inputStream中的内容。

curl -X POST  -F "bigfile=@src/main/resources/bigfile" --limit-rate 10k http://localhost:8080/async-io-read-hook-pre-process


Tomcat 8.5.24及之前存在[BUG 61932][tc-issue-61932]，会导致出现。。。。。。TODO。。。。。。

## 将WriteListener加到MVC异步处理过程

TODO

## 相关资料


* [Is Servlet 3.1 (Read|Write)Listener supported by DeferredResult in Spring 4?][ref-6]
* [Slides - Servlet 3.1 Async IO][ref-2]
* [WebAsyncManager][ref-1]
* [AsyncStateMachine][ref-3]

[ref-1]: https://docs.spring.io/spring/docs/4.3.x/javadoc-api/org/springframework/web/context/request/async/WebAsyncManager.html
[ref-2]: https://www.slideshare.net/SimoneBordet/servlet-31-async-io
[ref-3]: https://tomcat.apache.org/tomcat-8.5-doc/api/org/apache/coyote/AsyncStateMachine.html
[ref-6]: https://stackoverflow.com/questions/28828355/is-servlet-3-1-readwritelistener-supported-by-deferredresult-in-spring-4

[url-callable-trace]: http://localhost:8080/callable-trace
[ref-spring-mvc-async-proc]: ../servlet-async-processing/README.md
[tc-issue-61932]: https://bz.apache.org/bugzilla/show_bug.cgi?id=61932

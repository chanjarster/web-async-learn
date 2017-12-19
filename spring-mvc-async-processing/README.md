# Spring MVC Async Processing

## Callable<?>

> A ``Callable<?>`` can be returned when the application wants to produce the return value asynchronously in a thread managed by Spring MVC.

用于异步返回结果，使用的是Spring MVC的``AsyncTaskExecutor``，Spring MVC使用``CallableMethodReturnValueHandler``负责处理它。

下面是例子[CallableController][src-CallableController]：

```java
@RestController
public class CallableController {

  @RequestMapping("callable-hello")
  public Callable<String> hello() {
    return () -> new SlowJob("CallableController").doWork();
  }
}
```

用浏览器访问：[http://localhost:8080/callable-hello][callable-hello] 查看返回结果。

## DeferredResult<?>

> A ``DeferredResult<?>`` can be returned when the application wants to produce the return value from a thread of its own choosing.

用于异步返回结果，使用的是client code自己的thread，Spring MVC使用``DeferredResultMethodReturnValueHandler``负责处理它。

下面是例子[DeferredResultController][src-DeferredResultController]：

```java
@RestController
public class DeferredResultController {

  @Autowired
  @Qualifier("customExecutorService")
  private ExecutorService executorService;

  @RequestMapping("deferred-result-hello")
  public DeferredResult<String> hello() {
    DeferredResult<String> deferredResult = new DeferredResult<>();
    executorService.submit(() -> {
      try {
        deferredResult.setResult(new SlowJob("DeferredResultController").doWork());
      } catch (Exception e) {
        deferredResult.setErrorResult(e);
      }

    });
    return deferredResult;
  }

}
```

在这个例子里使用了ExecutorService（见[ExecutorServiceConfiguration][src-ExecutorServiceConfiguration]），你也可以根据实际情况采用别的机制来给``DeferredResult.setResult``。

用浏览器访问：[http://localhost:8080/deferred-result-hello][deferred-result-hello] 查看返回结果。

## ListenableFuture<?> or CompletableFuture<?>/CompletionStage<?>

> A ``ListenableFuture<?>`` or ``CompletableFuture<?>``/``CompletionStage<?>`` can be returned when the application wants to produce the value from a thread pool submission.

用于异步返回结果，使用client code自己的thread pool，Spring MVC使用``DeferredResultMethodReturnValueHandler``负责处理它。

下面是例子[ListenableFutureController][src-ListenableFutureController]：

```java
@RestController
public class ListenableFutureController {

  @Autowired
  @Qualifier("customExecutorService")
  private ExecutorService executorService;

  @RequestMapping("listenable-future-hello")
  public ListenableFutureTask<String> hello() {

    ListenableFutureTask<String> listenableFutureTask = new ListenableFutureTask<>(
        () -> new SlowJob("ListenableFutureController").doWork());
    executorService.submit(listenableFutureTask);
    return listenableFutureTask;
  }

}
```

用浏览器访问：[http://localhost:8080/listenable-future-hello][listenable-future-hello] 查看返回结果。

下面是例子[CompletionFutureController][src-CompletionFutureController]

```java
@RestController
public class CompletionFutureController {

  @RequestMapping("completable-future-hello")
  public CompletableFuture<String> hello() {

    return CompletableFuture
        .supplyAsync(() -> new SlowJob("CompletionFutureController").doWork());
  }

}
```

用浏览器访问：[http://localhost:8080/completable-future-hello][completable-future-hello] 查看返回结果。

## 配置MVC Async

### AsyncTaskExecutor

Spring MVC执行异步操作需要用到``AsyncTaskExecutor``，这个可以在用``WebMvcConfigurer.configureAsyncSupport``方法来提供（[相关文档][ref-7]）。
如果不提供，则使用``SimpleAsyncTaskExecutor``，``SimpleAsyncTaskExecutor``不使用thread pool，因此推荐提供自定义的``AsyncTaskExecutor``。

需要注意的是``@EnableAsync``也需要用到``AsyncTaskExecutor``，不过Spring MVC和它用的不是同一个。
顺带一提，``EnableAsync``默认也使用``SimpleAsyncTaskExecutor``，可以使用``AsyncConfigurer.getAsyncExecutor``方法来提供一个自定义的``AsyncTaskExecutor``。

例子见：[MvcAsyncTaskExecutorConfigurer][src-MvcAsyncTaskExecutorConfigurer]。

### Interceptors

* ``AsyncHandlerInterceptor``，使用``WebMvcConfigurer.addInterceptors``注册
* ``CallableProcessingInterceptor[Adapter]``，使用``WebMvcConfigurer.configureAsyncSupport``注册
* ``DeferredResultProcessingInterceptor[Adapter]``，使用``WebMvcConfigurer.configureAsyncSupport``注册

官方文档：[Intercepting Async Requests][ref-8]

## WebAsyncManager

``WebAsyncManager``是Spring MVC管理async processing的中心类，如果你可以阅读它的源码来更多了解Spring MVC对于async processing的底层机制。

## 参考资料

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
[ref-8]: https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-ann-async-interception
[src-MvcAsyncTaskExecutorConfigurer]: src/main/java/me/chanjar/learning/config/MvcAsyncTaskExecutorConfigurer.java
[src-ExecutorServiceConfiguration]: src/main/java/me/chanjar/learning/config/ExecutorServiceConfiguration.java
[src-CallableController]: src/main/java/me/chanjar/learning/CallableController.java
[src-DeferredResultController]: src/main/java/me/chanjar/learning/DeferredResultController.java
[src-ListenableFutureController]: src/main/java/me/chanjar/learning/ListenableFutureController.java
[src-CompletionFutureController]: src/main/java/me/chanjar/learning/CompletionFutureController.java
[callable-hello]: http://localhost:8080/callable-hello
[completable-future-hello]: http://localhost:8080/completable-future-hello
[deferred-result-hello]: http://localhost:8080/deferred-result-hello
[listenable-future-hello]: http://localhost:8080/listenable-future-hello

# Servlet 3.0 Async Processing

Servlet 3.0 开始提供了AsyncContext用来支持异步处理请求，那么异步处理请求到底能够带来哪些好处？

Web容器一般来说处理请求的方式是：为每个request分配一个thread。我们都知道thread的创建不是没有代价的，Web容器的thread pool都是有上限的。
那么一个很容易预见的问题就是，在高负载情况下，thread pool都被占着了，那么后续的request就只能等待，如果运气不好客户端会报等待超时的错误。
在AsyncContext出现之前，解决这个问题的唯一办法就是扩充Web容器的thread pool。

但是这样依然有一个问题，考虑以下场景：

有一个web容器，线程池大小200。有一个web app，它有两个servlet，Servlet-A处理单个请求的时间是10s，Servlet-B处理单个请求的时间是1s。
现在遇到了高负载，有超过200个request到Servlet-A，如果这个时候请求Servlet-B就会等待，因为所有HTTP thread都已经被Servlet-A占用了。
这个时候工程师发现了问题，扩展了线程池大小到400，但是负载依然持续走高，现在有400个request到Servlet-A，Servlet-B依然无法响应。

看到问题了没有，因为**HTTP thread和Worker thread耦合在了一起（就是同一个thread）**，所以导致了当大量request到一个耗时操作时，就会将HTTP thread占满，导致整个Web容器就会无法响应。

但是如果使用AsyncContext，我们就可以将耗时的操作交给另一个thread去做，这样HTTP thread就被释放出来了，可以去处理其他请求了。

> 注意，只有使用AsyncContext才能够达到上面所讲的效果，如果直接new Thread()或者类似的方式的，HTTP thread并不会归还到容器。

下面是一个官方的例子：

```java
@WebServlet(urlPatterns={"/asyncservlet"}, asyncSupported=true)
public class AsyncServlet extends HttpServlet {
   /* ... Same variables and init method as in SyncServlet ... */

   @Override
   public void doGet(HttpServletRequest request, 
                     HttpServletResponse response) {
      response.setContentType("text/html;charset=UTF-8");
      final AsyncContext acontext = request.startAsync();
      acontext.start(new Runnable() {
         public void run() {
            String param = acontext.getRequest().getParameter("param");
            String result = resource.process(param);
            HttpServletResponse response = acontext.getResponse();
            /* ... print to the response ... */
            acontext.complete(); 
         }
      });
   }
}
```

## 陷阱

在这个官方例子里，每个HTTP thread都会开启另一个Worker thread来处理请求，然后把HTTP thread就归还给Web容器。但是看`AsyncContext.start()`方法的javadoc：

> Causes the container to dispatch a thread, possibly from a managed thread pool, to run the specified Runnable.

实际上这里并没有规定Worker thread到底从哪里来，也许是HTTP thread pool之外的另一个thread pool？还是说就是HTTP thread pool？

[The Limited Usefulness of AsyncContext.start()][4]文章里写道：不同的Web容器对此有不同的实现，不过Tomcat实际上是利用HTTP thread pool来处理`AsyncContext.start()`的（见[AsyncStateMachine.java#L429][AsyncStateMachine.java_L429]）。

这也就是说，我们原本是想释放HTTP thread的，但实际上并没有，因为有HTTP thread依然被用作Worker thread，只不过这个thread和接收请求的HTTP thread不是同一个而已。

这个结论我们也可以通过[AsyncServlet1][src-AsyncServlet1]和[SyncServlet][src-SyncServlet]的Jmeter benchmark看出来，两者的throughput结果差不多。启动方法：启动[Main][src-Main]，然后利用Jmeter启动[benchmark.jmx][src-benchmark.jmx]（Tomcat默认配置下HTTP thread pool=200）。

## 使用ExecutorService

前面看到了Tomcat并没有单独维护Worker thread pool，那么我们就得自己想办法搞一个，见[AsyncServlet2][src-AsyncServlet2]，它使用了一个带Thread pool的ExecutorService来处理AsyncContext。

## 其他方式

所以对于AsyncContext的使用并没有固定的方式，你可以根据实际需要去采用不同的方式来处理，为此你需要一点Java concurrent programming的知识。

## 对于性能的误解

AsyncContext的目的并不是为了**提高性能**，也并不直接提供性能提升，它提供了把HTTP thread和Worker thread解藕的机制，从而提高Web容器的**响应能力**。

不过AsyncContext在某些时候的确能够提高性能，但这个取决于你的代码是怎么写的。
比如：Web容器的HTTP thread pool数量200，某个Servlet使用一个300的Worker thread pool来处理AsyncContext。
相比Sync方式Worker thread pool=HTTP thread pool=200，在这种情况下我们有了300的Worker thread pool，所以肯定能够带来一些性能上的提升（毕竟干活的人多了）。

相反，如果当Worker thread的数量<=HTTP thread数量的时候，那么就不会得到性能提升，因为此时处理请求的瓶颈在Worker thread。
你可以修改[AsyncServlet2][src-AsyncServlet2]的线程池大小，把它和[SyncServlet][src-SyncServlet]比较benchmark结果来验证这一结论。

**一定不要认为Worker thread pool必须比HTTP thread pool大**，理由如下：

1. 两者职责不同，一个是Web容器用来接收外来请求，一个是处理业务逻辑
1. thread的创建是有代价的，如果HTTP thread pool已经很大了再搞一个更大的Worker thread pool反而会造成过多的Context switch和内存开销
1. AsyncContext的目的是将HTTP thread释放出来，避免被操作长期占用进而导致Web容器无法响应

所以在更多时候，Worker thread pool不会很大，而且会根据不同业务构建不同的Worker thread pool。
比如：Web容器thread pool大小200，一个慢速Servlet的Worker thread pool大小10，这样一来，无论有多少请求到慢速操作，它都不会将HTTP thread占满导致其他请求无法处理。


## 相关资料

* [Java EE 7 Tutorial: Java Servlet Technology - Asynchronous Processing](https://docs.oracle.com/javaee/7/tutorial/servlets012.htm)
* [The Limited Usefulness of AsyncContext.start()][4]

 [4]: https://dzone.com/articles/limited-usefulness
 [src-Main]: src/main/java/me/chanjar/learning/Main.java
 [src-SyncServlet]: src/main/java/me/chanjar/learning/SyncServlet.java
 [src-AsyncServlet1]: src/main/java/me/chanjar/learning/AsyncServlet1.java
 [src-AsyncServlet2]: src/main/java/me/chanjar/learning/AsyncServlet2.java
 [src-benchmark.jmx]: benchmark.jmx
 [AsyncStateMachine.java_L429]: https://github.com/apache/tomcat85/blob/TOMCAT_8_5_23/java/org/apache/coyote/AsyncStateMachine.java#L429

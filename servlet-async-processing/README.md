# Servlet 3.0 Async Processing

Servlet 3.0 开始提供了AsyncContext用来支持异步处理请求，那么异步处理请求到底能够带来哪些好处？

Web容器一般来说处理请求的方式是：为每个request分配一个thread。我们都知道thread的创建不是没有代价的，Web容器的thread pool都是有上限的。
那么一个很容易预见的问题就是，在高负载情况下，thread pool都被占着了，那么后续的request就只能等待，如果运气不好客户端会报等待超时的错误。
在AsyncContext出现之前，解决这个问题的唯一办法就是扩充Web容器的thread pool。

但是这样依然有一个问题，考虑以下场景：

有一个web容器，线程池大小200。有一个web app，它有两个servlet，Servlet-A处理单个请求的时间是10s，Servlet-B处理单个请求的时间是1s。
现在遇到了高负载，有超过300个request到Servlet-A，如果这个时候请求Servlet-B就会等待，因为所有container request processing thread都已经被Servlet-A占用了。
这个时候工程师发现了问题，扩展了线程池大小到500，但是负载依然持续走高，现在有600个request到Servlet-A，Servlet-B依然无法响应。

看到问题了没有，因为**container request processing thread和worker thread耦合在了一起**，所以导致了当大量request到一个耗时操作时，整个Web容器就会无法响应。

但是如果使用AsyncContext，我们就可以将耗时的操作交给另一个thread去做，这样container request processing thread就被释放出来了，可以去处理其他请求了。

> 注意，只有使用AsyncContext才能够达到上面所讲的效果，如果直接new Thread()或者类似的方式的化，request processing thread并不会归还到容器。

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

这个例子里可以看到，AsyncContext使用了另一个线程去处理request，这样container request processing thread就归还给container了。

这个例子很简单，也很easy，但是有一个隐藏的缺陷，就是如果我们为每个请求额外开启一个worker thread，那么开销就实在是太大了。
每个thread的创建都是有开销的，thread的创建也不是无限的，现在就相当于在高负载情况下有2倍的线程在工作（container request processing thread和worker thread），
如此一来就很容易占尽系统资源出现``OOM (could not create native thread)``。
好一点的做法就是使用thread pool、ExecutorService框架、BlockingQueue来异步地处理请求。

所以，AsyncContext的目的并不是为了**提高性能**，而是提供一个把container request processing thread和worker thread解藕的机会，从而提高系统的响应能力。

不过AsyncContext在某些时候的确能够提高性能，但这个取决于你的代码是怎么写的。比如：

1. Web容器的thread pool数量200，某个Servlet使用一个300的thread pool来处理AsyncContext。
1. Web容器的thread pool数量200，某个Servlet使用官方例子来处理AsyncContext。

上面这两种情况下，因为获得了额外的worker thread，所以肯定能够带来一些性能上的提升（相比传统做法，worker thread的数量就是200）。

如果你的代码这么写就不会得到额外的性能提升：

1. Web容器的thread pool数量200，某个Servlet将所有AsyncContext放到queue里，然后使用单个线程消费queue

那么原来并行的操作（200线程）就变成了单线程操作，那么性能反而会降低许多。

相关资料：

1. [Java EE 7 Tutorial: Java Servlet Technology - Asynchronous Processing](https://docs.oracle.com/javaee/7/tutorial/servlets012.htm)
1. [Java EE 7 Tutorial: Java Servlet Technology - Nonblocking I/O](https://docs.oracle.com/javaee/7/tutorial/servlets013.htm)
1. [Slides - Servlet 3.1 Async IO](https://www.slideshare.net/SimoneBordet/servlet-31-async-io)


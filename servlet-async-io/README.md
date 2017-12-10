# Servlet 3.1 Async IO

Servlet Async Processing提供了一种异步请求处理的手段，能够让你将Http thread从慢速处理中释放出来出来其他请求，提高系统的响应度。

但是光有Async Processing是不够的，因为整个请求-响应过程的速度快慢还牵涉到了客户端的网络情况，如果客户端网络情况糟糕，其上传和下载速度都很慢，那么同样也会长时间占用Http Thread使其不能被释放出来。

于是Servlet 3.1提供了Async IO机制，使得从Request中读、往Response里写变成异步动作。

## Async Read

我们先来一段客户端上传速度慢的例子，[AsyncReadServlet.java][src-AsyncReadServlet]：

```java
@WebServlet(value = "/async-read", asyncSupported = true)
public class AsyncReadServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    System.out.println("Servlet thread: " + Thread.currentThread().getName());
    AsyncContext asyncCtx = req.startAsync();
    ServletInputStream is = req.getInputStream();
    is.setReadListener(new ReadListener() {
      private int totalReadBytes = 0;

      @Override
      public void onDataAvailable() {
        System.out.println("ReadListener thread: " + Thread.currentThread().getName());

        try {
          byte buffer[] = new byte[1 * 1024];
          int readBytes = 0;
          while (is.isReady() && !is.isFinished()) {
            int length = is.read(buffer);
            if (length == -1 && is.isFinished()) {
              asyncCtx.complete();
              System.out.println("Read: " + readBytes + " bytes");
              System.out.println("Total Read: " + totalReadBytes + " bytes");
              return;
            }
            readBytes += length;
            totalReadBytes += length;

          }
          System.out.println("Read: " + readBytes + " bytes");

        } catch (IOException ex) {
          ex.printStackTrace();
          asyncCtx.complete();
        }
      }

      @Override
      public void onAllDataRead() {
        try {
          System.out.println("Total Read: " + totalReadBytes + " bytes");
          asyncCtx.getResponse().getWriter().println("Finished");
        } catch (IOException ex) {
          ex.printStackTrace();
        }
        asyncCtx.complete();
      }

      @Override
      public void onError(Throwable t) {
        System.out.println(ExceptionUtils.getStackTrace(t));
        asyncCtx.complete();
      }
    });

  }

}
```

我们利用`curl`的`--limit-rate`选项来模拟慢速上传``curl -X POST  -F "bigfile=@src/main/resources/bigfile" --limit-rate 5k http://localhost:8080/async-read``

然后观察服务端的打印输出：

```
Servlet thread: http-nio-8080-exec-3
ReadListener thread: http-nio-8080-exec-3
Read: 16538 bytes
ReadListener thread: http-nio-8080-exec-4
Read: 16384 bytes
ReadListener thread: http-nio-8080-exec-5
Read: 16384 bytes
ReadListener thread: http-nio-8080-exec-7
Read: 16384 bytes
ReadListener thread: http-nio-8080-exec-6
Read: 16384 bytes
ReadListener thread: http-nio-8080-exec-8
Read: 16384 bytes
ReadListener thread: http-nio-8080-exec-9
Read: 16384 bytes
ReadListener thread: http-nio-8080-exec-10
Read: 2312 bytes
ReadListener thread: http-nio-8080-exec-1
Read: 48 bytes
Total Read: 117202 bytes
```

可以从输出看到除了doGet和第一次进入onDataAvailable是同一个Http thread之外，后面的read动作都发生在另外的Http thread里。
这是因为客户端的数据推送速度太慢了，容器先将Http thread收回，当容器发现可以读取到新数据的时候，再分配一个Http thread去读InputStream，如此循环直到全部读完为止。

注意：`HttpServletRequest.getInputStream()`和`getParameter*()`不能同时使用。

## Async Write

再来一段客户端下载慢的例子，[AsyncWriteServlet.java][src-AsyncWriteServlet]：

```java
@WebServlet(value = "/async-write", asyncSupported = true)
public class AsyncWriteServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    System.out.println("Servlet thread: " + Thread.currentThread().getName());
    AsyncContext asyncCtx = req.startAsync();
    ServletOutputStream os = resp.getOutputStream();
    InputStream bigfileInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("bigfile");

    os.setWriteListener(new WriteListener() {

      @Override
      public void onWritePossible() throws IOException {

        int loopCount = 0;
        System.out.println("WriteListener thread: " + Thread.currentThread().getName());
        while (os.isReady()) {
          loopCount++;
          System.out.println("Loop Count: " + loopCount);
          byte[] bytes = readContent();
          if (bytes != null) {
            os.write(bytes);
          } else {
            closeInputStream();
            asyncCtx.complete();
            break;
          }
        }
      }

      @Override
      public void onError(Throwable t) {

        try {
          os.print("Error happened");
          os.print(ExceptionUtils.getStackTrace(t));
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          closeInputStream();
          asyncCtx.complete();
        }

      }

      private byte[] readContent() throws IOException {
        byte[] bytes = new byte[1024];
        int readLength = IOUtils.read(bigfileInputStream, bytes);
        if (readLength <= 0) {
          return null;
        }
        return bytes;
      }

      private void closeInputStream() {
        IOUtils.closeQuietly(bigfileInputStream);
      }
    });

  }

}
```

同样利用`curl`做慢速下载，``curl --limit-rate 5k http://localhost:8080/async-write``

接下来看以下服务端打印输出：

```
Servlet thread: http-nio-8080-exec-1
WriteListener thread: http-nio-8080-exec-1
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-2
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-3
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-4
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-5
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-6
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-7
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-8
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-9
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-10
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-1
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-2
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-3
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-4
Write bytes: 8192
WriteListener thread: http-nio-8080-exec-5
Write bytes: 2312
```

PS. 后发现即使没有添加`--limit-rate`参数，也会出现类似于上面的结果。

## Jmeter

上面两个例子使用的是`curl`来模拟，我们也提供了Jmeter的benchmark。

需要注意的是，必须在user.properties文件所在目录启动Jmeter，因为这个文件里提供了模拟慢速连接的参数`httpclient.socket.http.cps=5120`。然后利用Jmeter打开benchmark.xml。


## 相关资料

* [Java EE 7 Tutorial: Java Servlet Technology - Nonblocking I/O][ref-1]
* [Slides - Servlet 3.1 Async IO][ref-2]
* [Non-blocking I/O using Servlet 3.1: Scalable applications using Java EE 7][ref-3]
* [How to simulate network bandwidth in JMeter?][ref-4]
* [Configuring JMeter][ref-5]
* [Servlet 3.1 Asynchronous IO and Jetty-9.1][ref-6]

  [ref-1]: https://docs.oracle.com/javaee/7/tutorial/servlets013.htm
  [ref-2]: https://www.slideshare.net/SimoneBordet/servlet-31-async-io
  [ref-3]: https://blogs.oracle.com/arungupta/non-blocking-io-using-servlet-31:-scalable-applications-using-java-ee-7-totd-188
  [ref-4]: https://wiki.apache.org/jmeter/Controlling%20Bandwidth%20in%20JMeter%20to%20simulate%20different%20networks
  [ref-5]: http://jmeter.apache.org/usermanual/get-started.html#configuring_jmeter
  [ref-6]: https://webtide.com/servlet-3-1-async-io-and-jetty/
  [src-AsyncReadServlet]:  src/main/java/me/chanjar/learning/AsyncReadServlet.java
  [src-AsyncWriteServlet]: src/main/java/me/chanjar/learning/AsyncWriteServlet.java

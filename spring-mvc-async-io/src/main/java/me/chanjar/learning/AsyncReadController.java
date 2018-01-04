package me.chanjar.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptorAdapter;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;

import javax.servlet.AsyncContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.Callable;

@RestController
public class AsyncReadController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncReadController.class);

  @RequestMapping("async-io-read-hook-pre-process")
  public Callable<String> hookPreProcess(ServletRequest request) throws Exception {

    InnerCallable callable = new InnerCallable();

    WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
    asyncManager.registerCallableInterceptor("some-key", new InnerCallableProcessingInterceptor(callable));
    return callable;
  }

  private static class InnerCallable implements Callable<String> {

    private String result;

    private Exception exception;

    @Override
    public String call() throws Exception {
      synchronized (this) {
        this.wait();
      }
      if (exception != null) {
        throw exception;
      }
      return result;
    }

    public synchronized void onSuccess(String result) {
      this.result = result;
      this.notify();
    }

    public synchronized void onError(Throwable e) {
      if (e instanceof Exception) {
        this.exception = (Exception) e;
      } else {
        this.exception = new RuntimeException(e);
      }
      this.notify();
    }
  }

  private static class InnerCallableProcessingInterceptor extends CallableProcessingInterceptorAdapter {

    private final InnerCallable callable;

    public InnerCallableProcessingInterceptor(InnerCallable callable) {
      this.callable = callable;
    }

    @Override
    public <T> void preProcess(NativeWebRequest request, Callable<T> task) throws Exception {

      HttpServletRequest nativeRequest = (HttpServletRequest) request.getNativeRequest();
      AsyncContext asyncContext = nativeRequest.getAsyncContext();
      ServletInputStream is = nativeRequest.getInputStream();
      is.setReadListener(new ReadListener() {
        private int totalReadBytes = 0;

        @Override
        public void onDataAvailable() throws IOException {
          LOGGER.info("ReadListener onDataAvailable");

          try {
            byte buffer[] = new byte[1 * 1024];
            int readBytes = 0;
            while (is.isReady() && !is.isFinished()) {
              int length = is.read(buffer);
              if (length == -1 && is.isFinished()) {
                LOGGER.info("Read: " + readBytes + " bytes");
                LOGGER.info("Total Read: " + totalReadBytes + " bytes");
                return;
              }
              readBytes += length;
              totalReadBytes += length;

            }
            LOGGER.info("Read: " + readBytes + " bytes");

          } catch (IOException ex) {
            throw new RuntimeException(ex);
          }
        }

        @Override
        public void onAllDataRead() throws IOException {
          LOGGER.info("ReadListener onAllDataRead");
          is.close();
          String msg = "ReadListener thread: " + Thread.currentThread().getName() + " succeeded";
          callable.onSuccess(msg);
        }

        @Override
        public void onError(Throwable throwable) {
          LOGGER.error("ReadListener onError");
          callable.onError(throwable);
        }
      });
    }

  }
}

package me.chanjar.learning;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

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

        int writeBytes = 0;
        System.out.println("WriteListener thread: " + Thread.currentThread().getName());
        while (os.isReady()) {
          byte[] bytes = new byte[1024];
          int readBytes = readContent(bytes);
          if (readBytes > 0) {
            os.write(bytes);
            writeBytes += readBytes;
          } else {
            closeInputStream();
            asyncCtx.complete();
            break;
          }
        }
        System.out.println("Write bytes: " + writeBytes);

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

      private int readContent(byte[] buffer) throws IOException {
        int readLength = IOUtils.read(bigfileInputStream, buffer);
        return readLength;
      }

      private void closeInputStream() {
        IOUtils.closeQuietly(bigfileInputStream);
      }
    });

  }

}

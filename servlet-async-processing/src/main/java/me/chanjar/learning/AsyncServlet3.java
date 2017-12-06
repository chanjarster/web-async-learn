package me.chanjar.learning;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

@WebServlet(value = "/async-servlet-3", asyncSupported = true)
public class AsyncServlet3 extends HttpServlet {

  private final ArrayBlockingQueue<SlowJob.SlowJobRunner> queue = new ArrayBlockingQueue<>(200);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    AsyncContext asyncCtx = req.startAsync();
    try {
      queue.put(new SlowJob.SlowJobRunner(new SlowJob("async servlet3"), asyncCtx));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  @Override
  public void init() throws ServletException {
    super.init();

    new Thread(() -> {
      while (true) {

        SlowJob.SlowJobRunner slowJobRunner = null;
        try {
          slowJobRunner = queue.take();
          slowJobRunner.run();
        } catch (InterruptedException e) {
          break;
        }

      }

    }).start();

  }
}

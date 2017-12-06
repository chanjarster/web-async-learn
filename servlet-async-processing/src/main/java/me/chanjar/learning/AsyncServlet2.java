package me.chanjar.learning;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(value = "/async-servlet-2", asyncSupported = true)
public class AsyncServlet2 extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    AsyncContext asyncCtx = req.startAsync();
    asyncCtx.start(new SlowJob.SlowJobRunner(new SlowJob("async servlet2"), asyncCtx));

  }

}

package me.chanjar.learning;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/sync-servlet")
public class SyncServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    PrintWriter writer = resp.getWriter();
    String result = new SlowJob("sync servlet").doWork();
    writer.println(result);
    writer.flush();

  }

}

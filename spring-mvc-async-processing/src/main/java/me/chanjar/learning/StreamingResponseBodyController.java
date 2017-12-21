package me.chanjar.learning;

import me.chanjar.learning.slowjob.SlowJob;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;

@RestController
public class StreamingResponseBodyController {

  @RequestMapping("streaming-response-body-hello")
  public StreamingResponseBody hello() {

    return outputStream -> {
      String hello = new SlowJob("CallableController").doWork();
      outputStream.write(hello.getBytes());
      outputStream.flush();
    };

  }
}

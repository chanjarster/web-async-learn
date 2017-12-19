package me.chanjar.learning;

import me.chanjar.learning.slowjob.SlowJob;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

@RestController
public class CallableController {

  @RequestMapping("callable-hello")
  public Callable<String> hello() {
    return () -> new SlowJob("CallableController").doWork();
  }
}

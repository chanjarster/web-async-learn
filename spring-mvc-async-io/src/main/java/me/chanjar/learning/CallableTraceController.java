package me.chanjar.learning;

import me.chanjar.learning.slowjob.SlowJob;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

@RestController
public class CallableTraceController {

  @RequestMapping("callable-trace")
  public Callable<String> hello() {
    return () -> new SlowJob("CallableTraceController").doWork();
  }

}

package me.chanjar.learning.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.concurrent.Callable;

@Configuration
public class AsyncInterceptorConfiguration extends WebMvcConfigurerAdapter {

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {

    configurer.setDefaultTimeout(30000L);
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setThreadNamePrefix("my-mvc-async-");
    taskExecutor.setCorePoolSize(400);
    taskExecutor.setMaxPoolSize(400);
    taskExecutor.setQueueCapacity(400);
    taskExecutor.initialize();
    configurer.setTaskExecutor(taskExecutor);

    configurer.registerCallableInterceptors(new CallableProcessingLogger());
  }

  private static class CallableProcessingLogger implements CallableProcessingInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallableProcessingLogger.class);

    @Override
    public <T> void beforeConcurrentHandling(NativeWebRequest request, Callable<T> task) throws Exception {
      LOGGER.info("beforeConcurrentHandling");
    }

    @Override
    public <T> void preProcess(NativeWebRequest request, Callable<T> task) throws Exception {
      LOGGER.info("preProcess");

    }

    @Override
    public <T> void postProcess(NativeWebRequest request, Callable<T> task, Object concurrentResult) throws Exception {
      LOGGER.info("postProcess");
    }

    @Override
    public <T> Object handleTimeout(NativeWebRequest request, Callable<T> task) throws Exception {
      LOGGER.info("handleTimeout");
      return null;
    }

    @Override
    public <T> void afterCompletion(NativeWebRequest request, Callable<T> task) throws Exception {
      LOGGER.info("afterCompletion");

    }
  }

}

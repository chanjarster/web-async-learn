package me.chanjar.learning.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MvcAsyncTaskExecutorConfigurer extends WebMvcConfigurerAdapter {

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

  }

}

package com.planbow.app;


import jakarta.servlet.MultipartConfigElement;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.RestTemplate;
import java.util.Random;

@SpringBootApplication
@Log4j2
@EnableAsync
@ComponentScan(value = {
        "com.planbow.controllers",
        "com.planbow.services",
        "com.planbow.repository",
        "com.planbow.utility",
        "com.planbow.datasource.hibernate",
        "com.planbow.datasource.mongodb",
        "com.planbow.datasource.storage"
})
@EnableScheduling
public class PlanbowApiApplication {

    public static void main(String[] args) {
        log.info("Starting planbow-api");
        SpringApplication.run(PlanbowApiApplication.class, args);
    }


    @Bean
    public RestTemplate restTemplate() {
        log.info("Initializing RestTemplate bean");
        return new RestTemplate();
    }

    @Bean
    public Random random(){
        return new Random();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(500));
        factory.setMaxRequestSize(DataSize.ofMegabytes(500));
        return factory.createMultipartConfig();
    }

}

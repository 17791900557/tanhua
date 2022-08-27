package com.tanhua.dubbo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync//开启spring提供的多线程
public class DubboMongoApplication {

    public static void main(String[] args) {
        SpringApplication. run(DubboMongoApplication.class,args);
    }
}
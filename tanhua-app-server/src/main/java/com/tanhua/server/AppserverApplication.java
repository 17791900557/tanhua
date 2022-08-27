package com.tanhua.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@EnableCaching//开启缓存注解
//排除mongo的自动配置在项目中，添加了mongo的依赖的话，
// springboot就会自动去连接本地的mongo，
// 由于他连接不上会导致出错。
public class AppserverApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppserverApplication.class,args);
    }

}

package com.hasaker.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @package com.hasaker.post
 * @author 余天堂
 * @create 2020/2/9 15:11
 * @description PostApplication
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class PostApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostApplication.class, args);
    }
}
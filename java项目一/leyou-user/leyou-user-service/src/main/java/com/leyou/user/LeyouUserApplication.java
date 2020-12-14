package com.leyou.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.leyou.user.mapper")
public class LeyouUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeyouUserApplication.class);
    }
}

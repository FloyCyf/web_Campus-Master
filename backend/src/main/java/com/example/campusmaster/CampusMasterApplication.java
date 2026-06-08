
package com.example.campusmaster;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 校园万事达互助众包任务平台启动类
 */
@SpringBootApplication
@MapperScan("com.example.campusmaster.mapper")
@EnableTransactionManagement
public class CampusMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusMasterApplication.class, args);
    }
}

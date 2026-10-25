package br.com.uniamerica.sgd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SgdApplication {

    public static void main(String[] args) {
        SpringApplication.run(SgdApplication.class, args);
    }
}

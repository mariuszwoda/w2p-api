package pl.where2play.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableMethodSecurity
@SpringBootApplication
public class W2pApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(W2pApiApplication.class, args);
    }

}
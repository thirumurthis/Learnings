//usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25

//DEPS org.springframework.boot:spring-boot-dependencies:4.1.0@pom
//DEPS org.springframework.boot:spring-boot-starter-web

package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
@RestController
public class SpringRestApi {

    public static void main(String[] args) {
        SpringApplication.run(SpringRestApi.class, args);
    }

    @GetMapping("/")
    public String sayHello(
        @RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hello, " + name + "!";
    }
}
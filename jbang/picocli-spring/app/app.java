///usr/bin/env jbang "$0" "$@" ; exit $?

package app;

//SOURCES support/*
//JAVA 24

//DEPS org.springframework.boot:spring-boot-dependencies:3.4.5@pom
//DEPS org.springframework.boot:spring-boot-starter-web
//DEPS org.springframework.boot:spring-boot-starter-actuator
//DEPS info.picocli:picocli-spring-boot-starter:4.7.7

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import app.support.optionshelper;
import picocli.CommandLine.IFactory;
import picocli.CommandLine;


@SpringBootApplication
@ComponentScan(basePackages = {"app","support"})
public class app implements CommandLineRunner{
    private IFactory factory;
    private optionshelper options;

    app(IFactory factory, optionshelper options){
        this.factory = factory;
        this.options = options;
    }

    @Override
    public void run(String... args) throws Exception{
        new CommandLine(options, factory).execute(args);
    }

    public static void main(String... args){
        System.exit(SpringApplication.exit(SpringApplication.run(app.class, args)));
    }
}
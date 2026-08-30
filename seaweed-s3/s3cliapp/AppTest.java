//usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25

//SOURCES support/*

/* - passing the application yaml to the spring app */
//FILES application.yaml

//DEPS org.springframework.boot:spring-boot-dependencies:4.1.0@pom
//DEPS org.springframework.boot:spring-boot-starter-web
//DEPS org.springframework.boot:spring-boot-starter-validation

//DEPS software.amazon.awssdk:s3:2.29.0
//DEPS software.amazon.awssdk:apache-client:2.46.7
//DEPS org.slf4j:slf4j-api:2.0.17
//DEPS info.picocli:picocli-spring-boot-starter:4.7.7

package s3cliapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

import s3cliapp.support.cliOptions;


@SpringBootApplication
@ComponentScan(basePackages = {"s3cliapp","support"})
public class AppTest implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AppTest.class);
 
    private final IFactory factory;
    private final cliOptions options;

    AppTest(IFactory factory, cliOptions options){
       this.options = options;
       this.factory = factory;
    }

    @Override
    public void run(String... args) throws Exception{
        new CommandLine(options, factory).execute(args);
    }

    public static void main(String... args){
        System.exit(SpringApplication.exit(SpringApplication.run(AppTest.class, args)));
    }
}
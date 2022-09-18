### Reactive Spring boot application example:

- Create the basic project structure using the `https://start.spring.io` 
- Choose the `Spring Reactive Web`, `Lombok`

Spring Reactive stream API contains below API

#### Mono:
  `Mono` - publisher that can emit 0 or 1 object
  - We can use mono in the repository layer like below, where it can handle 0 or 1 record
```java

public Mono<Customer> findCustomerById(long id){
   // check the data in the database
   Customer customer = repository
   if (customerExists){
       return Mono.just(Customer);
   }else{
       return Mono.empty();
   }
}
```
  `Flux` - publisher that can emit 0 to many object as reactive sequence

```java
public Flux<Customer> findAllCustomer() {
    return Flux.just(
        new customer("Tom", "S"),
        new Customer("Ram", "D")
    );
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.7.3</version>
		<relativePath/>
	</parent>
	<groupId>com.reactive.demo</groupId>
	<artifactId>reactive-example</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>reactive-example</name>
	<description>Demo project for Spring Boot</description>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webflux</artifactId>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>io.projectreactor</groupId>
			<artifactId>reactor-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
		</plugins>
	</build>
</project>
```
- controller 

```java 
package com.reactive.demo.reactiveexample.controller;

import com.reactive.demo.reactiveexample.dto.Vehicles;
import com.reactive.demo.reactiveexample.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class VehicleController {

    @Autowired
    VehicleService vehicleService;

    @GetMapping("/list")
    public List<Vehicles> getVehicleList(){
        log.info("invoked controller url /list non stream");
        return vehicleService.getVehicleList();
    }

    @GetMapping(value="/stream",produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Vehicles> geVehiclesStream(){
        log.info("invoked controller stream uri /stream");
        Flux<Vehicles> result = vehicleService.getVehiclesStream();
        log.info("result from the service - "+result.toStream());
        return result;
    }
}
```

- Simple service layer

```java
package com.reactive.demo.reactiveexample.service;

import com.reactive.demo.reactiveexample.dao.VehicleDao;
import com.reactive.demo.reactiveexample.dto.Vehicles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class VehicleService {

    @Autowired
    VehicleDao vehicleDao;

    public List<Vehicles> getVehicleList(){
        return vehicleDao.fetchVehicles();
    }

    public Flux<Vehicles> getVehiclesStream(){
        return vehicleDao.fetchVehiclesStream();
    }
}
```
- Dao class to mock the data load 
```java
package com.reactive.demo.reactiveexample.dao;

import com.reactive.demo.reactiveexample.dto.Vehicles;
import com.reactive.demo.reactiveexample.service.VehicleService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class VehicleDao {


    //induce delay
    private static void sleepForSecond(int i){
        try {
            Thread.sleep(1000);
        }catch (InterruptedException exe){
            exe.printStackTrace();
        }
    }

    public List<Vehicles> fetchVehicles(){

        List<Vehicles> input = createOutput();
        return IntStream.rangeClosed(1,20)
                .peek(VehicleDao::sleepForSecond)
                .map(i -> new Random().nextInt(input.size()))
                .mapToObj(item -> input.get(item))
                .collect(Collectors.toList());
    }


    public Flux<Vehicles> fetchVehiclesStream(){

        List<Vehicles> input = createOutput();
        return Flux.range(1,20)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(item -> System.out.println("Processing :- "+item))
                .map(i -> new Random().nextInt(input.size()))
                .map(item ->input.get(item));
    }

    public List<Vehicles> createOutput() {
        List<Vehicles> vList = new ArrayList<>();

        Vehicles v1 = new Vehicles(4, "Toyota", "Corolla", 2004);
        Vehicles v2 = new Vehicles(4, "Toyota", "Corolla", 2005);
        Vehicles v3 = new Vehicles(4, "Toyota", "Corolla", 2006);
        Vehicles v4 = new Vehicles(4, "Toyota", "Corolla", 2007);
        Vehicles v5 = new Vehicles(4, "Toyota", "Corolla", 2008);
        Vehicles v6 = new Vehicles(4, "Toyota", "Corolla", 2009);
        Vehicles v7 = new Vehicles(4, "Toyota", "Corolla", 2010);
        Vehicles v8 = new Vehicles(4, "Toyota", "Corolla", 2011);
        Vehicles v9 = new Vehicles(4, "Toyota", "Corolla", 2012);
        Vehicles v10 = new Vehicles(4, "Toyota", "Corolla", 2013);

        vList.add(v1);
        vList.add(v2);
        vList.add(v3);
        vList.add(v4);
        vList.add(v5);
        vList.add(v6);
        vList.add(v7);
        vList.add(v8);
        vList.add(v9);
        vList.add(v10);
        return vList;
    }

}
```
- Simple data POJO

```java
package com.reactive.demo.reactiveexample.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Vehicles {

    private int wheels;
    private String make;
    private String model;
    private int year;
}
```

Note:
  - Including `spring-boot-starter-web` dependency will use tomcat server, this doesn't work when deploying the code.
  - `spring-boot-starter-webflux` will include Netty server, which works in this case.

reference:
 [1](https://dimitr.im/difference-between-mono-and-flux)

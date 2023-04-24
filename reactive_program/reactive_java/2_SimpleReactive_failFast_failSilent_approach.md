- This code contains both project and test class
- First we include the data, subscriber and publisher in project src main
- Test for Fast fail and Fail silent is demonstrated in the Test class

```java
package com.reactive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode
public class ItemData {

    private final String name;
    private final Float price;
}
```
- subscriber
```java
package com.reactive;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

//LifeCycle of item Data subscriber
@Slf4j
@Data
public class ItemSubscriber implements Flow.Subscriber<ItemData> {

    private final AtomicInteger messageLimiter;
    private final boolean failQuiet;
    private Flow.Subscription subscription;

    //This is used for testing, which will aggregate
    // the messages
    public List<ItemData> consumedItems = new LinkedList<>();

    public ItemSubscriber(Integer unboundedLimit){
        this(unboundedLimit,false);
    }

    public ItemSubscriber(Integer unboundedLimit, boolean failQuiet){
        this.messageLimiter = new AtomicInteger(unboundedLimit);
        this.failQuiet = failQuiet;
    }
    public static ItemSubscriber createUnbounded(){
        return new ItemSubscriber(Integer.MAX_VALUE);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        log.info("subscribed... ");
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(ItemData item) {
        messageLimiter.decrementAndGet();
        log.info("onNext Got "+item);

        if (isItemForbidden(item.getName())){
            if(!failQuiet){
                throw new IllegalArgumentException("item forbidden");
            }
            log.info("failed quietly");
        }else{
            consumedItems.add(item);
        }

        if(messageLimiter.get()>0){
            subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.info("on Error");

    }

    @Override
    public void onComplete() {
        log.info("on completed");
    }

    private boolean isItemForbidden(String name){
        return name.contains("?");
    }
}
```

- publisher

```java
package com.reactive;

import java.util.concurrent.SubmissionPublisher;

// we will leverage submissionPublisher
// NOTE: if we directly implement the Publisher (like subscriber)
// we need to implement using publisher, we need to handle everything
// related to Subscriber, like create new list of subscriber.
// then when the event arrives we need to implement the logic to iterate
// every subscriber and publish the event to every subscriber
// this is bit cumbersome to do so we can utilize SubmissionPublisher
// this class contains lot of additional function.
// Since the SubmissionPublisher has complex logic we use
public class ItemPublisher extends SubmissionPublisher<ItemData> {}
```

- Test class

```java
package com.reactive;

import static org.assertj.core.api.Assertions.*;

import static org.awaitility.Awaitility.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ItemReactiveFlowTest {

    @Test
    public void withPublisher_subscribeIt_andConsumeAllElements(){
        ItemPublisher itemPublisher = new ItemPublisher();
        ItemSubscriber subscriber = new ItemSubscriber(2,true);
        itemPublisher.subscribe(subscriber);
        List<ItemData> items = List.of(
                new ItemData("Remote",12.0F),
                new ItemData("Tv",299.0F));

        //assert
        //only one subscriber
        assertThat(itemPublisher.getNumberOfSubscribers()).isEqualTo(1);
        items.forEach(itemPublisher::submit);
        itemPublisher.close();

        await().atMost(1000, TimeUnit.MILLISECONDS).untilAsserted(
            () -> assertThat(subscriber.getConsumedItems()).containsExactlyElementsOf(items));
    }
}
```
- Error handling test case

```java
package com.reactive;


import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

//1. Fail fast subscriber - in case of any error cancel further event request
//2. Fail Silently Subscriber - we ignore th exception and processing events further
public class ErrorHandlingTest {

    @Test
    //Fail silently
    void withPublisher_WhenErrorSubscribing_FailSilentlyShouldContinue(){
        ItemPublisher itemPublisher = new ItemPublisher();
        ItemSubscriber subscriber = new ItemSubscriber(2,true);
        itemPublisher.subscribe(subscriber);
        List<ItemData> items = List.of(
                new ItemData("?Remote",12.0F),
                new ItemData("Tv",299.0F));

        //assert
        //only one subscriber
        assertThat(itemPublisher.getNumberOfSubscribers()).isEqualTo(1);
        items.forEach(itemPublisher::submit);
        itemPublisher.close();

        await().atMost(1000, TimeUnit.MILLISECONDS).untilAsserted(
                () -> assertThat(subscriber.getConsumedItems()).containsExactly(items.get(1)));
    }

    @Test
    //Fail Fast Subscriber
    void withPublisher_ifSubscriberFail_StopFurtherProcessing(){

        ItemPublisher itemPublisher = new ItemPublisher();
        //Fail set to false so exception thrown
        ItemSubscriber subscriber = new ItemSubscriber(2,false);
        itemPublisher.subscribe(subscriber);
        List<ItemData> items = List.of(
                new ItemData("?Remote",12.0F),
                new ItemData("Tv",299.0F));

        //assert
        //only one subscriber
        assertThat(itemPublisher.getNumberOfSubscribers()).isEqualTo(1);
        items.forEach(itemPublisher::submit);
        itemPublisher.close();

        await().atMost(1000, TimeUnit.MILLISECONDS).untilAsserted(
                () -> assertThat(subscriber.getConsumedItems()).isEmpty());

    }
}
```

- dependecy pom.xml, includes Lomobok and slf4j

```xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.reactive</groupId>
  <artifactId>reactive</artifactId>
  <version>1.0-SNAPSHOT</version>

  <name>reactive</name>
  <!-- FIXME change it to the project's website -->
  <url>http://www.example.com</url>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.awaitility</groupId>
      <artifactId>awaitility</artifactId>
      <version>4.2.0</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-engine</artifactId>
      <version>5.9.1</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.assertj</groupId>
      <artifactId>assertj-core</artifactId>
      <version>3.24.2</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>1.18.26</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>2.0.7</version>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-simple</artifactId>
      <version>2.0.7</version>
    </dependency>
  </dependencies>

  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>3.11.0</version>
          <configuration>
            <source>17</source>
            <target>17</target>
          </configuration>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>
```

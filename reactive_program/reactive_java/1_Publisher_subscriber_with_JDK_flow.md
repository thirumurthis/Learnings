
- Below uses the JDK API to implement the Reactive programming, below are all Test with examples.

- Publisher vs Subscriber - SubmissionPublisher - an implementation of the Publisher in java that can be used for testing.
- Hot Publisher - which doesn't care about the back pressure, sometimes the producer might lead to OOM (Out of memory) issue
- Cold Publisher - incorporates the back pressure, where we limit the number of message to process by the subscriber. This is done using the subscription

```java
package com.reactive;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.*;
import static org.assertj.core.api.Assertions.assertThat;


public class SubscriberVsPublisherTest {

    @Test
    public void testUsePublisherToSendEvents() throws InterruptedException{

        AtomicInteger atomicInteger = new AtomicInteger();
        //This is the JDK provided simple implementation of the publisher
        //which we can leverage to publish message to the Flow without worrying
        //about details.
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

        //Below is the subsciber which will be catching the events from the
        //flow producer by the submissionpublisher.
        Flow.Subscriber<String> subscriber = new Flow.Subscriber<>(){


            //below implemented methods are the callbacks
            @Override
            //this method will be called during subscription
            public void onSubscribe(Flow.Subscription subscription) {
                System.out.println("onSubscribe "+subscription);
                //Set with the Max value
                //For the subscription we need to tell how many messages
                //we want to process at the beginning
                subscription.request(Integer.MAX_VALUE);
            }

            @Override
            public void onNext(String item) {
                System.out.println("on next "+ item);
                atomicInteger.incrementAndGet();
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("on error "+throwable.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("on complete ");
            }
        };

        List<String> items = List.of("item-1","item-2");
        publisher.subscribe(subscriber);
        items.forEach(publisher::submit);
        publisher.close();

        await().atMost(20000, TimeUnit.MILLISECONDS)
                .untilAsserted(
                () ->   assertThat(atomicInteger.get()).isEqualTo(2)
        );
    }
}
```

- Hot Publisher

```java
package com.reactive;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class HotPublisherTest {

        @Test
        public void testUsePublisherToSendEvents() throws InterruptedException {

            AtomicInteger atomicInteger = new AtomicInteger();
            //This is the JDK provided simple implementation of the publisher
            //which we can leverage to publish message to the Flow without worrying
            //about details.
            SubmissionPublisher<Integer> hotPublisher = new SubmissionPublisher<>();

            //Below is the subsciber which will be catching the events from the
            //flow producer by the submissionPublisher.
            Flow.Subscriber<Integer> subscriber = new Flow.Subscriber<>() {


                //below implemented methods are the callbacks
                @Override
                //this method will be called during subscription
                public void onSubscribe(Flow.Subscription subscription) {
                    System.out.println("onSubscribe " + subscription);
                    //Below indicates that the subscription will
                    //handle as many event as possible till the Integer.MAX,
                    // it also doesn't state at which time it process
                    subscription.request(Integer.MAX_VALUE);
                }

                @Override
                public void onNext(Integer item) {
                    System.out.println("on next " + item);
                    atomicInteger.incrementAndGet();
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("on error " + throwable.getMessage());
                }

                @Override
                public void onComplete() {
                    System.out.println("on complete ");
                }
            };

            //Create a stream of infinte value
            Stream<Integer> infiniteProducer = Stream.iterate(0,i->i+2);
            //NOTE:
            // Hotpublisher keeps on pushing the data not worrying about
            // the subscriber reading it at which rate (or speed of event consumption)
            // publisher doesn't have no idea of how to limit, like process 10 limit
            // and then publish 10 limit
            // Since this can't be limit so it is called the hot publisher.
            hotPublisher.subscribe(subscriber);
            infiniteProducer.forEach(hotPublisher::submit);
            hotPublisher.close();

            await().atMost(10_000, TimeUnit.MILLISECONDS)
                    .untilAsserted(
                            () -> assertThat(atomicInteger.get()).isEqualTo(1000)
                    );
        }
}
```

- Cold Publsher with back pressure
 - though 9 items are streames, since the subscription requests one item the output is limited to only 1.
```java
package com.reactive;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ColdPublisherTest {

        @Test
        public void testUsePublisherToSendEvents() throws InterruptedException {

        AtomicInteger atomicInteger = new AtomicInteger();
        //This is the JDK provided simple implementation of the publisher
        //which we can leverage to publish message to the Flow without worrying
        //about details.
        SubmissionPublisher<Integer> hotPublisher = new SubmissionPublisher<>();

        //Below is the subsciber which will be catching the events from the
        //flow producer by the submissionPublisher.
        Flow.Subscriber<Integer> subscriber = new Flow.Subscriber<>() {


            //below implemented methods are the callbacks
            @Override
            //this method will be called during subscription
            public void onSubscribe(Flow.Subscription subscription) {
                System.out.println("onSubscribe " + subscription);

                //only one item is requested from the pulbisher
                // so the producer is not unbounded
                // So in this case the producer won't get out of memory
                // COLD publisher - can request message/event on demand
                // In Hot Publisher- since we can't limit the number of
                // messages sent by publisher this ultimately leads to OOM (out of memory)
                // In Cold publisher - the subscription is set 1 here
                // so the producer can buffer at publisher side and no OOM issue
                subscription.request(1);
            }

            @Override
            public void onNext(Integer item) {
                // on next event can request next event
                // once the current event is completed.
                System.out.println("on next " + item);
                atomicInteger.incrementAndGet();
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("on error " + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("on complete ");
            }
        };

        //Create a stream of finite value
        Stream<Integer> infiniteProducer = Stream.of(1,2,3,4,5,6,7,8,9);
        //NOTE:
        // In cold publisher, though the producer stream send 9 events
        // only 1 will be delivered to subscriber, since we set request 1
        // the others will not be processed.
        // So the subscription tells which controls which is known as back pressure
        // handling
        hotPublisher.subscribe(subscriber);
        infiniteProducer.forEach(hotPublisher::submit);
        hotPublisher.close();

        await().atMost(1000, TimeUnit.MILLISECONDS)
                .untilAsserted(
                        () -> assertThat(atomicInteger.get()).isEqualTo(1)
                );
    }
}
```

- Maven dependency to execute above code.

```xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.reactive</groupId>
  <artifactId>reactive</artifactId>
  <version>1.0-SNAPSHOT</version>

  <name>reactive</name>
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

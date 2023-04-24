- Below class creates an over producing publisher of the ItemData, refer other links in this folder for pojo

```java
package com.reactive.backpressure;

import com.reactive.ItemData;

import java.util.UUID;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class OverProducingPublisher extends SubmissionPublisher<ItemData> {

    public void start(){
        // this stream is a lazy stream
        Stream<ItemData> itemsStream = Stream.generate(
                ()-> new ItemData(UUID.randomUUID().toString(),
                        ThreadLocalRandom.current().nextFloat())
        );
        // activate the stream
        itemsStream.limit(100_000).forEach(this::submit);
    }
}
```

- Simple publisher with Unbounded publishing and with limiting subscription
```java
package com.reactive.backpressure;

import com.reactive.ItemData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
public class ItemDataUboundedSubscriber implements Flow.Subscriber<ItemData> {

    private final AtomicInteger messageLimiter;
    private boolean failQuietly;
    private Flow.Subscription subscription;

    List<ItemData> consumedItems = new LinkedList<>();

    public static ItemDataUboundedSubscriber createUnbounded(){
        return new ItemDataUboundedSubscriber(Integer.MAX_VALUE);
    }

    public ItemDataUboundedSubscriber(Integer upperBound){
        this(upperBound,false);
    }
    public ItemDataUboundedSubscriber(Integer upperBound, boolean failQuietly){
        // this is used for how much message to consume.
        this.messageLimiter = new AtomicInteger(upperBound);
        this.failQuietly= failQuietly;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
    this.subscription = subscription;
    subscription.request(1);
    }

    @Override
    public void onNext(ItemData item) {
        // this value can be used to swap
        int toIncrement =   messageLimiter.decrementAndGet();
        log.info("onNext Got "+item);

        if (isItemForbidden(item.getName())){
            if(!failQuietly){
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
    public void onError(Throwable throwable) {}

    @Override
    public void onComplete() {}

    private boolean isItemForbidden(String name){
        return name.contains("?");
    }
}
```

-Test case which uses the Thread to invoke the over producing publisher to demostrate the back pressure
 - The first test case will overwhelm the subscriber since there is no limit on the subscriber
 - The second test case will limit the number of subscription, though the publisher produces 100K messages, only 3 will be consumed and producer will buffer only if subscriber needed

```java
package com.reactive;

import com.reactive.backpressure.ItemDataUboundedSubscriber;
import com.reactive.backpressure.OverProducingPublisher;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class HotPublisherBackPressureTest {
    @Test
    public void floodWithDataWhenSubscribed(){
        OverProducingPublisher publisher = new OverProducingPublisher();
        ItemDataUboundedSubscriber subscriber
                = ItemDataUboundedSubscriber.createUnbounded();
        publisher.subscribe(subscriber);

        // a new thread is created
        new Thread(()->{
            //below will invoke the publisher start function
            publisher.start();
            // below will close once the publicser start is returned
            publisher.close();
        }).start();

        Awaitility.await().atMost(1000, TimeUnit.MILLISECONDS)
                .untilAsserted(
                        ()-> Assertions.assertThat(
                                subscriber.getConsumedItems().size()
                                ).isGreaterThan(100)
                );
    }

    @Test
    public void applyBackPressureOnOverProducingPublisher(){
        OverProducingPublisher publisher = new OverProducingPublisher();
        // back pressure count 3 is applied so only consumes three message
        // since we have used AtomicInteger messageLimiter to count
        ItemDataUboundedSubscriber subscriber = new ItemDataUboundedSubscriber(3);

        publisher.subscribe(subscriber);
        // since we have limited with 3, though the publisher start can produde
        //100000 message, these will be in memory but buffered only 3
        // the message are not sent to the subscriber and consumer not receive 3
        // and stops the process
        // a new thread is created
        new Thread(()->{
            //below will invoke the publisher start function
            publisher.start();
            // below will close once the publicser start is returned
            publisher.close();
        }).start();

        Awaitility.await().atMost(1000, TimeUnit.MILLISECONDS)
                .untilAsserted(
                        ()-> Assertions.assertThat(
                                subscriber.getConsumedItems().size()
                        ).isEqualTo(3)
                );
    }
}
```

- Generic class to transform the data and extend the publisher

Note:-
 - Pom.xml is same as the exemple with [failFase and failSilent](https://github.com/thirumurthis/Learnings/blob/master/reactive_program/reactive_java/SimpleReactive_failFast_failSilent_approach.md)

```java
package com.reactive;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Function;

// R- Return data type
@Slf4j
public class ItemTransformProcessor<R> extends SubmissionPublisher<R> implements Flow.Processor<ItemData,R> {

    private Function<ItemData, R> function;
    private Flow.Subscription subscription;

    public ItemTransformProcessor(Function<ItemData, R> function){
        super();
        this.function = function;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(ItemData item) {
        submit(function.apply(item));
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error(throwable.getMessage());
    }

    @Override
    public void onComplete() {
        log.info("on Completed");
        // calling below so it will close
        close(); // we need to invoke the close() from submission subscriber
    }
}
```

- subscriber that will take the string value of the item data based on the function passed to transformprocessor

```java
package com.reactive;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
// we are transforming to a string
public class ItemSubscriberForStringTransform implements Flow.Subscriber<String> {

    private final AtomicInteger messageLimiter;
    private Flow.Subscription subscription;
    public List<String> consumedItems = new LinkedList<>();
    public ItemSubscriberForStringTransform(Integer unboundedLimit){
        this.messageLimiter = new AtomicInteger(unboundedLimit);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        log.info("subscribed... ");
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(String item) {
        messageLimiter.decrementAndGet();
        log.info("onNext Got "+item);
        consumedItems.add(item);

        if(messageLimiter.get()>0){
            subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.info("on Error. stopped processing");
    }

    @Override
    public void onComplete() {
        log.info("on completed");
    }
}
```

- publisher

```java
package com.reactive;

import java.util.concurrent.Flow;
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

- Using the ItemTransformProcessor to extend for different covnertion in this case for integer transform
- This can transform the item data price to int value (can also multply, etc).
```java
package com.reactive;

import java.util.function.Function;

//Since the ItemTransformProcessor is more generic, we can use String as well.
public class ConvertToIntegerTransformProcessor extends ItemTransformProcessor<Integer>{

    public static ConvertToIntegerTransformProcessor create(){
        return new ConvertToIntegerTransformProcessor(
                itemData -> itemData.getPrice().intValue());
    }

    //we have private constructor, so we are using a static method
    private ConvertToIntegerTransformProcessor(Function<ItemData,Integer> function){
        super(function);
    };
}
```

- Testing the flow 
- subscribe and transform items and consume all the elements.

```java
package com.reactive;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ItemTransformProcessorTest {

    @Test

    void withPublisher_subscriber_transformProcessor_and_consumeAllItems(){

        ItemPublisher publisher = new ItemPublisher();
        //In this transform processor we are getting the name
        // of the item and passing the function lambda
        //Transform processor is a publisher which is
        //able to produce or emit message
        ItemTransformProcessor<String> transformProcessor
                = new ItemTransformProcessor<>(ItemData::getName);
        ItemSubscriberForStringTransform subscriber =
                new ItemSubscriberForStringTransform(3);

        List<ItemData> items = List.of(
                new ItemData("Tv",399.00F),
                new ItemData("Remote",15.00F)
        );

        List<String> expectValue = List.of("Tv","Remote");

        //Susbscribe the transformer
        publisher.subscribe(transformProcessor);
        // transformer subscribe to the subscriber
        transformProcessor.subscribe(subscriber);
        //events published
        items.forEach(publisher::submit);
        publisher.close();

        Awaitility.await().atMost(1000, TimeUnit.MILLISECONDS)
                .untilAsserted(()-> Assertions.assertThat(subscriber.getConsumedItems())
                        .containsExactlyElementsOf(expectValue));
    }
}
```

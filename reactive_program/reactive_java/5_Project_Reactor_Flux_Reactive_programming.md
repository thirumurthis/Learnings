- Add below dependency to the pom.xml

```xml
    <dependency>
      <groupId>io.projectreactor</groupId>
      <artifactId>reactor-core</artifactId>
      <version>3.2.6.RELEASE</version>
    </dependency>
    <dependency>
      <groupId>io.projectreactor</groupId>
      <artifactId>reactor-test</artifactId>
      <version>3.4.22</version>
    </dependency>
```

- Below is the text class that uses the Reactor Flux for producing event

```java
package com.reactive.projectreactor;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReactorFluxTests {

    @Test
    public void withFlux_onSubscribing_streamInfo(){
        List<Integer> items = new ArrayList<>();

        // Every process is executed in the same thread.
        Flux.just(1,2,3,4)
                .log()
                .map(itm -> {
                    log.info("{}:{}",itm,Thread.currentThread());
                    return itm*2;
                })
                .subscribe(items::add);

        Assertions.assertThat(items).containsExactly(2,4,6,8);
    }

    @Test
    void withFlux_onZipping_thenCombine(){
        List<String> items = new ArrayList<>();

        Flux.just(1,2,3,4)
                .log()
                .map(itm -> itm *2)
                //complex operator
                //by creating a flux publisher which is a lazy reactive publisher
                //that create 0 to max range
                //every item processed by the map above will be joined
                // by the subsequent element of the zipwith publisher
                // and emits the one and two, to create a string which will
                // be emitted - for each map output one string will be emitted
                .zipWith(Flux.range(0,Integer.MAX_VALUE)
                        .log(),(one,two)->String.format("Flux1: %d, Flux2: %d",one,two)
                )
                .subscribe(items::add);

        Assertions.assertThat(items).containsExactly(
                "Flux1: 2, Flux2: 0",
                "Flux1: 4, Flux2: 1",
                "Flux1: 6, Flux2: 2",
                "Flux1: 8, Flux2: 3");
    }

    @Test
    void withFlux_onApplyingBackpressure_publishElementsInBatches(){
        List<Integer> items = new ArrayList<>();

        Flux.just(1,2,3,4)
                .log()
                .map(i-> i *2)
                .onBackpressureBuffer()
                .subscribe(new Subscriber<Integer>() {
                    private Subscription s;
                    int onNextAmount;
                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(2);
                    }

                    @Override
                    public void onNext(Integer item) {
                        items.add(item);
                        onNextAmount++;
                        // below will request for batch of 2
                        if(onNextAmount % 2 == 0){
                            s.request(2);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {}

                    @Override
                    public void onComplete() {}
                });

        Assertions.assertThat(items).containsExactly(2,4,6,8);
    }
}
```

visitor pattern 

```java
package com.dp.visitor;

public class Country {

    public <R> R accept(Visitor<R> visitor){
        return visitor.visit(this);
    }

    @Override
    public String toString(){
        return "Country ";
    }
}
```

```java
package com.dp.visitor;

public class State {

    public <R> R accept(Visitor<R> visitor){
       return visitor.visit(this);
    }

    @Override
    public String toString(){
        return "State ";
    }
}
```

```java
package com.dp.visitor;

public class Street {

    public <R> R accept(Visitor<R> visitor){
       return visitor.visit(this);
    }

    @Override
    public String toString(){
        return "Street ";
    }
}
```

```java
package com.dp.visitor;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class Vehicle {

    @Getter
    @Setter
    private Country country = new Country();

    @Getter
    @Setter
    private Street[] street = {new Street(),new Street()};

    @Getter
    @Setter
    private State state = new State();

    //public <R> R accept(Visitor<R> visitor, Collector<? super R, ? extends Object, ? extends Object> collector){
    // the collector can return any type of object

    // in below we return the RR
    public <R, RR> RR accept(Visitor<R> visitor, Collector<? super R, ? extends Object, RR> collector){

        R r1 = this.country.accept(visitor);
        R r2 =this.state.accept(visitor);
        R r3 = this.street[0].accept(visitor);
        R r4 = this.street[1].accept(visitor);
        R r5 = visitor.visit(this);

        return Stream.of(r1,r2,r3,r4,r5).collect(collector);
    }

    @Override
    public String toString(){
        return "vehicle";
    }
}
```

```java
package com.kafka.example.firstapp.dp.visitor;

public interface Visitor<R> {
    R visit(Vehicle vehicle);
    R visit(Country country);
    R visit(State state);
    R visit(Street street);
}
```

```java
package com.dp.visitor;

import java.util.stream.Collectors;

public class TravelVisitor {

    public static void main(String[] args) {

        Vehicle vehicle = new Vehicle();
        var visitor = new Visitor<String>(){
            @Override
            public String visit(Vehicle vehicle) {
                return "0. "+vehicle;
            }

            @Override
            public String visit(Country country) {
                return  "1. "+country;
            }

            @Override
            public String visit(State state) {
                return "2. "+state;
            }

            @Override
            public String visit(Street street) {
                return "3. "+street;
            }

        };

        String visited = vehicle.accept(visitor, Collectors.joining(" | "));
        System.out.println("output:- "+visited);
        
        List<String> resultList =vehicle.accept(visitor,Collectors.toList());
        resultList.forEach(System.out::println);
    }
}
```

### Progress 2 

```java
package com.dp.visitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TravelVisitor {

    public static void main(String[] args) {

        Vehicle output = new Vehicle();

        Map<Class<?>, Function<Object,String >> registry = new HashMap<>();

        registry.put(Vehicle.class, vehicle -> " at "+vehicle);
        registry.put(Country.class, country -> " at "+country);
        registry.put(State.class, state -> " at "+state);
        registry.put(Street.class, street -> " at "+street);

        Visitor<String> visitor = e -> registry.get(e.getClass()).apply(e);
        String response = visitor.visit(output);

        System.out.println("visit "+response);

       // String visited = vehicle.accept(visitor, Collectors.joining(" | "));
       // System.out.println("output:- "+visited);

       // List<String> resultList = vehicle.accept(visitor,Collectors.toList());
       // resultList.forEach(System.out::println);
    }
}
```

```java
package com.dp.visitor;

import lombok.Getter;
import lombok.Setter;

public class Vehicle {

    @Getter
    @Setter
    private Country country = new Country();

    @Getter
    @Setter
    private Street[] street = {new Street(),new Street()};

    @Getter
    @Setter
    private State state = new State();

    private <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString(){
        return "vehicle";
    }
}
```

### Progress 3

```java
public class TravelVisitor {

    public static void main(String[] args) {

        Vehicle output = new Vehicle();
        Map<Class<?>, Function<Object,String >> registry = new HashMap<>();
        
        BiConsumer<Class<?>,Function<Object,String>> biConsumer =
                (type,function) -> registry.put(type,function);

        biConsumer.accept(Vehicle.class, vehicle -> " at "+vehicle);
        biConsumer.accept(Country.class, country -> " at "+country);
        biConsumer.accept(State.class, state -> " at "+state);
        biConsumer.accept(Street.class, street -> " at "+street);

        Visitor<String> visitor = e -> registry.get(e.getClass()).apply(e);
        String response = visitor.visit(output);

        System.out.println("visit "+response);

       // String visited = vehicle.accept(visitor, Collectors.joining(" | "));
       // System.out.println("output:- "+visited);

       // List<String> resultList = vehicle.accept(visitor,Collectors.toList());
       // resultList.forEach(System.out::println);
    }
}
```

### Progress 4
- Create the sepearate interface for consumer

```java
package com.dp.visitor;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface VisitorBuilder<R> extends BiConsumer<Class<?>, Function<Object,R>> {

    //create a default method since we cannot overload
    //the BiConsumer directly
    default void register(Class<?> type, Function<Object, R> function){
        this.accept(type,function);

    }
}
```
### progress 5

```java
public class TravelVisitor {

    public static void main(String[] args) {

        Vehicle output = new Vehicle();
        Map<Class<?>, Function<Object,String >> registry = new HashMap<>();

        VisitorBuilder<String> visitorBuilder =
                 (type,function) -> registry.put(type,function);

        Consumer<VisitorBuilder<String>> consumer =
                builder -> {
                    builder.register(Vehicle.class, vehicle -> " at "+vehicle);
                    builder.register(Country.class, country -> " at "+country);
                    builder.register(State.class, state -> " at "+state);
                    builder.register(Street.class, street -> " at "+street);
                };
                
        consumer.accept(visitorBuilder);
        
        //Below code can be refactored a bit using consumer
        // below code can go inside that consumer
        /*
        visitorBuilder.register(Vehicle.class, vehicle -> " at "+vehicle);
        visitorBuilder.register(Country.class, country -> " at "+country);
        visitorBuilder.register(State.class, state -> " at "+state);
        visitorBuilder.register(Street.class, street -> " at "+street);
         */

        Visitor<String> visitor = e -> registry.get(e.getClass()).apply(e);
        String response = visitor.visit(output);

        System.out.println("visit "+response);
    }
}
```

### progress 6
- We can still convert the consumer to a sperate interface

```java
package com.dp.visitor;

import java.util.function.Consumer;

public interface VisitorInitializer<R> extends Consumer<VisitorBuilder<R>> {

    default void init(VisitorBuilder<R> builder){
        this.accept(builder);
    }
}
```

```java
public class TravelVisitor {

    public static void main(String[] args) {

        Vehicle output = new Vehicle();
        Map<Class<?>, Function<Object,String >> registry = new HashMap<>();

        VisitorBuilder<String> visitorBuilder =
                 (type,function) -> registry.put(type,function);

        VisitorInitializer<String> consumer =
                builder -> {
                    builder.register(Vehicle.class, vehicle -> " at "+vehicle);
                    builder.register(Country.class, country -> " at "+country);
                    builder.register(State.class, state -> " at "+state);
                    builder.register(Street.class, street -> " at "+street);
                };

        consumer.init(visitorBuilder);

        Visitor<String> visitor = e -> registry.get(e.getClass()).apply(e);
        String response = visitor.visit(output);

        System.out.println("visit "+response);
   }
}
```

## progress 

- Extract to a new interface for visitor 

```java
package com.dp.visitor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface Visitor<R> {
    R visit(Object e);

    static <R> Visitor<R> of(VisitorInitializer<R> visitorInitializer){
        Map<Class<?>, Function<Object,R>> registry = new HashMap<>();
        VisitorBuilder<R> visitorBuilder =
                (type,function)-> registry.put(type,function);
        visitorInitializer.init(visitorBuilder);
        return e -> registry.get(e.getClass()).apply(e);
    }
}
```
-----------------------
# final version

```java

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class TravelVisitor {

    public static void main(String[] args) {

        Vehicle output = new Vehicle();
        VisitorInitializer<String> visitorInitializer =
                builder -> {
                    builder.register(Vehicle.class, vehicle -> " at "+vehicle);
                    builder.register(Country.class, country -> " at "+country);
                    builder.register(State.class, state -> " at "+state);
                    builder.register(Street.class, street -> " at "+street);
                };

        Visitor<String> visitor = Visitor.of(visitorInitializer);

        String response = visitor.visit(output);

        System.out.println("visit "+response);

        String response1 = visitor.visit(output.getCountry());
        System.out.println("print "+response1);
    }
}
```
-
```java
public class Country {

    @Override
    public String toString(){
        return "Country ";
    }
}
public class State {
    @Override
    public String toString(){
        return "State ";
    }
}
public class Street {

    @Override
    public String toString(){
        return "Street ";
    }
}
```

```java
public class TravelVisitor {

    public static void main(String[] args) {

        Vehicle output = new Vehicle();
        VisitorInitializer<String> visitorInitializer =
                builder -> {
                    builder.register(Vehicle.class, vehicle -> " at "+vehicle);
                    builder.register(Country.class, country -> " at "+country);
                    builder.register(State.class, state -> " at "+state);
                    builder.register(Street.class, street -> " at "+street);
                };

        Visitor<String> visitor = Visitor.of(visitorInitializer);

        String response = visitor.visit(output);

        System.out.println("visit "+response);

        String response1 = visitor.visit(output.getCountry());
        System.out.println("print "+response1);
   }
}
```
-
```java
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

public class Vehicle {

    @Getter
    @Setter
    private Country country = new Country();

    @Getter
    @Setter
    private Street[] street = {new Street(),new Street()};

    @Getter
    @Setter
    private State state = new State();

    @Override
    public String toString(){
            return "Vehicle{" +
                    "Country=" + country +
                    ", state=" + state +
                    ", street=" + Arrays.toString(street) +
                    '}';
    }
}
```

```java
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface Visitor<R> {
    R visit(Object e);

    static <R> Visitor<R> of(VisitorInitializer<R> visitorInitializer){
        Map<Class<?>, Function<Object,R>> registry = new HashMap<>();
        VisitorBuilder<R> visitorBuilder =
                (type,function)-> registry.put(type,function);
        visitorInitializer.init(visitorBuilder);
        return e -> registry.get(e.getClass()).apply(e);
    }
}
```

```java
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface VisitorBuilder<R> extends BiConsumer<Class<?>, Function<Object,R>> {

    //create a default method since we cannot overload
    //the BiConsumer directly
    default void register(Class<?> type, Function<Object, R> function){
        this.accept(type,function);
    }
}
```

```java
import java.util.function.Consumer;

public interface VisitorInitializer<R> extends Consumer<VisitorBuilder<R>> {

    default void init(VisitorBuilder<R> builder){
        this.accept(builder);
    }
}
```

Reference [1](https://github.com/JosePaumard/devoxx-belgium-2019-visitor-lambda)

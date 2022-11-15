## Best Practice using Lambda

- Don't mutate the status when using the lambda.
- Don't make the lambda to be big, better use method and refer it using method reference
- Returing the stream is intermidiate state, so be carefull when returing stream.
   - During creation we can return the stream.
   - Stream are intermidiate and evaluate lazily.
- Order do matters 
```java
   list.stream().distinct().limit().map(Employee::name).toList(); // this might produce incorrect result.
```
```java
   //Ex:
   Intestream.iterate(0,i-> (i+1) %2)
             .distinct()
             .limit(5)
             .forEach(System.out::println);//This will run but the correct order limit () first and distinct() then.
```
- So adding a parallel after the iterate will spin up lots of thread and forks
- Make sure to use parallel, only if it necessary for huge operation. Not always parallel makes process fast.

- Dont use infinite stream, use Intstream.range(), IntStream.rangeClosed() make sure the stream closes based on the use case.

- Using consumer in case if there task performed with similar steps but varies only on one step

- Say in database, we need to find the items based on id, edit it and save it.
- we can edit different properties say color, items etc.
- we can create a tempalte for the steps and pass the edit as a consumer

```java
 public void templateAction(long id, Consumer<Product> action){
  Cart cart= findProductById(productId);
  //edit
  action.accept(cart);
  //save the transaction
  //notify - other operation
  }
  
  public void updateColor(long id, Product product){
     templateAction(id, action -> action.updateColor()); // just a representation of the idea
  }  
```

- Exception handling in Lambdas
  - don't use the try.. catch in lambdas.
  - there are open source libraries like fiber.
  - We can pefrom lambda with exception handled without any impact
  - `try` is another library which is similar to the `Either` type defined below.

```java
package org.lambda;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class LambdaException {

    public static void main(String[] args) {

        List<Employee> employeeList = List.of(
                Employee.builder().name("demo1").department("no").build(),
                Employee.builder().name("demo2").build(),
                Employee.builder().name("demo3").department("no").build(),
                Employee.builder().name("demo4").department("no").build(),
                Employee.builder().name("demo5").department("no").build()
        );

        boolean doNotExecute = false;
        if(doNotExecute)
        employeeList.stream()
                .map(LambdaException::doOperation)
                .forEach(System.out::println);

        if(doNotExecute)
        //using the funtional interface wrapped around
        employeeList.stream()
                .map(wrap(LambdaException::doOperation))
                .forEach(System.out::println);

        //The problem with the above approach is that the exception
        //will be throws and the lambda pipeline will fail
        // say in an use case if we are processing millions of record
        // and the exception occurs processing 70% of the data we don't
        // want to rerun the process again from the start.
        // we need a better way to handle exceptions

        // To resolve this we can use Either type (not available natively in java)
        // we need to create this Either type
        // Either <L,R> its either left or right
        //      Either<L,R>
        //      /        \
        //  Left<L>      Right<R>
        // with generics we can use
        //          Either<Exception,Object>
        //         /                     \
        //        Left<Exception>         Right<Object>
        // when we create the Either and apply on the evaluation
        // this will provides with the Either object, where we can decide to
        // use the left or right

        //Try -> is writted in Fiber library similar to the Either

        employeeList.stream()
                //.peek(System.out::println)
                //.map(Either.liftWithValue(item -> doOperation(item)))
                .map(Either.liftWithValue(LambdaException::doOperation))
                //.peek(item -> {
                  //  if(item.isLeft()) System.out.println("L "+item);
                  //  if(item.isRight()) System.out.println("R "+item);
                //})
                //.forEach(item -> System.out.println(item.toString()));
                .forEach(System.out::println);

    }

    static Employee doOperation(Employee employee) throws CustomException{
            if (Objects.isNull(employee.department())){
                throw new CustomException("less than 1");
            }
        return employee;
    }

    // as a workaround we can add an functional interface
    // for handling exception
    @FunctionalInterface
    public interface WrapCException<T,R>{
        R apply(T t) throws Exception;
    }

    // Generic wrapper over the exception
    public static <T,R> Function<T,R> wrap(WrapCException<T,R> handleException){
        return item -> {
            try{
                return handleException.apply(item);
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        };
    }
}

@Builder
//@RequiredArgsConstructor
record Employee(String name, String department){}

class CustomException extends RuntimeException{
    public  CustomException(String message, Throwable exception){
        super(message,exception);
    }
    public CustomException(String message){
        super(message);
    }
}
```
- simple pair reference
```java
package org.lambda;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class Pair<L, R> {
    public final L left;
    public final R right;

    public static <L, R> Pair<L, R> create(L left, R right) {
        return new Pair<>(left, right);
    }
}
```
- Either type

```java
package org.lambda;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class Either <L,R>{
    private final L left;
    private final R right;

    private Either(L left, R right){
        this.left = left;
        this.right = right;
    }

    public static <L,R> Either<L,R> Left(L value) {
       // System.out.println("constructor invoked L "+value.toString());
        return new Either<>(value,null);
    }
    public static <L,R> Either<L,R> Right(R value) {
       // System.out.println("constructor invoked R "+value.toString());
        return new Either<>(null,value);
    }

    public Optional<L> getLeft(){ return Optional.ofNullable(left);}
    public Optional<R> getRight(){return Optional.ofNullable(right);}

    public boolean isLeft(){return Objects.nonNull(left);}
    public boolean isRight(){return Objects.nonNull(right);}

    public Stream<Either> stream() {return Stream.of(this);}

    public <T> Optional<T> mapLeft(Function< ? super L, T> mapper){
        if(isLeft()){
            return Optional.of(mapper.apply(left));
        }
        return Optional.empty();
    }

    public <T> Optional<T> mapRight(Function< ? super R, T> mapper){
        if(isRight()){
            return Optional.of(mapper.apply(right));
        }
        return Optional.empty();
    }

    public <T> Stream<T> map(Function<? super R,T> mapper){
        return mapRight(mapper).stream();
    }

    public String toString(){
        if(isLeft()){
            return "Left("+left+")";
        }
        return "Right("+right+")";
    }

    public static <T,R> Function<T, Either<?,?>> lift(WrapFunction<T,R> function){
        return t -> {
            try{
                return Either.Right(function.check(t));
            }catch(Exception ex){
                return Either.Left(ex);
            }
        };
    }

    //Function<T, Either> liftWithvalue(WrapFunction<Object,String> func)
    // return the function which will be applied
    
    public  static <T,RR> Function<T,Either<? ,?>> liftWithValue(WrapFunction<T,RR> function){
        return t -> {
            try{
                return Either.Right(function.check(t));
            }catch(Exception ex){
                return Either.Left(new Pair<>(ex, t));
            }
        };
    }
}
```
- output 

```
Right(Employee[name=demo1, department=no])
Left(Pair(fst=org.lambda.CustomException: less than 1, snd=Employee[name=demo2, department=null]))
Right(Employee[name=demo3, department=no])
Right(Employee[name=demo4, department=no])
Right(Employee[name=demo5, department=no])
```


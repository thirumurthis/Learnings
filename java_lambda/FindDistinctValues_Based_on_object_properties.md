
### Fetch unique values from the Vector object using lambdas.

- Object which needs to be filtered

```java
//using lombok
@AllArgumentConstructor
@Getter
@Setter
public class MessageObject {
	
	private String variable1;
	private String variable2;
	private String variable3;
	private String variable4;
}
```

- Main class which performs the filter using lambda
- In this case using a vector object to store list of Message object and fitler based on the properties or field values
```java

import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ExampleMain {

	public static void main(String args[]) {
		
		Vector<Object> input = createVectorMessageObjet();
		
    //passing the concatenated String as keu for distinctbyKey predicate function
		List<MessageObject> output = input.stream()
				.map(a->(MessageObject)a)
				.filter(distinctByKey(key -> key.getVariable1()+key.getVariable2()+key.getVariable3()+key.getVariable4()))
				.distinct().collect(Collectors.toList());
		
		output.forEach(a -> System.out.println(a.getVariable1()+" | "
				+ a.getVariable2()+" | "
				+ a.getVariable3()+" | "
				+ a.getVariable4()));
	}

  /* stateful filter pridcate */
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
	    Set<Object> seen = ConcurrentHashMap.newKeySet();
	    return t -> seen.add(keyExtractor.apply(t));
	}
	
  /* for hash map we can use below */
  public static <T> Predicate<T> distinctByKeyForMap(Function<? super T, ?> keyExtractor) {
  
    Map<Object, Boolean> seen = new ConcurrentHashMap<>(); 
    return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;  
  }

  /* Create object to mock up the filtering scenario */
  private static Vector<Object> createVectorMessageObjet() {
        MessageObject message1 = new MessageObject("var1","var2","var3","var4");
        MessageObject message2 = new MessageObject("var1","var2","var3","var4");
	MessageObject message3 = new MessageObject("var1","var2","var3","var4");
	MessageObject message4 = new MessageObject("var10","var20","var3","var4");
	MessageObject message5 = new MessageObject("var1","var2","var30","var40");
		
	Vector<Object> out = new Vector<>();
	out.add(message1);
	out.add(message2);
	out.add(message3);
	out.add(message4);
	out.add(message5);
	return out;
   }
}
```

Output:

```
var1 | var2 | var3 | var4
var10 | var20 | var3 | var4
var1 | var2 | var30 | var40
```

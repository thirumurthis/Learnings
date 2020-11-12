- Convert the list of string to map.
  - the map should contain key as string and value as length of string.
  - the key string's first char should be upper case
  
- Logic filter to allow only string length greater then 1, mapping the string to enable 1st char as upper case and appending to rest of the string.
- Edge case, the string with length 1, also works. The order is not preserved in this case.
  
```java
package com.program;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateMapWithCharPassed {

    public static void main(String...args){
        CreateMapWithCharPassed obj = new CreateMapWithCharPassed();
        List<String> input = Arrays.asList("hello","evil","hell");
        System.out.print(obj.processLogic(input));
    }

    public Map<String,Integer> processLogic(List<String> input){

        Map<String, Integer> output = new HashMap<String, Integer>();

        output = input.stream().filter(item -> item.length()>=1)
                .map(item -> item.substring(0,1).toUpperCase()+""+item.substring(1,item.length()))
                .collect(Collectors.toMap(item-> item,item -> item.length()));
        return  output;
      }
}
```

Output:
```
{Hello=5, Evil=4, Hell=4}

```

#### How to fetch the ascii values provided a string using java lambdas

```java
	public static void main(String[] args) {
		String romanStr = "IVXLD";
		List<Integer> charlist= romanStr.chars().boxed().collect(Collectors.toList()); //boxed used on primitive data type
		System.out.println(charlist);		
	}
```
- output:
```
[73, 86, 88, 76, 68]
```

### How to sort and group a list of string based on the first char.
  - String list, `{"thor","cptain america","captain marvel"}`, the output to be `{c = ["captain america","captain marvel"], t = ["thor"]}`

```java
public static void main(String[] args) {
		List<String> input = Arrays.asList("thor","captain marvel","captain america","iron man"); 
		
		HashMap<Character,List<String> > res = input.stream()
                                                .sorted()
                                                .collect(Collectors.groupingBy(item -> item.charAt(0),LinkedHashMap::new,Collectors.toList()));
		
		res.forEach((k,v)->{
			System.out.println(k+" : "+v.stream().collect(Collectors.joining(",")));
 	});
	}
```
 - output:
```
c : captain america,captain marvel
i : iron man
t : thor
```

#####  For one of requirement we had to obtain the file path of the json file from jar.

Structure of the jar
```
com
  |
   - test
  |       |
  |        - Main.class
  |
   - resources
          |
           - data.json
```

The path of the data.json file is not accurate, since the daa `file:/C:/Thiru/program1.jar!/resources/data.json` in case of windows

```java
//using systeclassloader
 URL u = ClassLoader.getSystemClassLoader().getResource("resources/data.json" );

//using thread class loader
URL u2 = Thread.currentThread().getContextClassLoader().getResource("resources/Specialnode.json");

```

Reading the content of the file directly and using that to parse using objectmapper() jackson

```java
//using stream, this can be used to read the content of the file NOT the file path
InputStream input = MainClass.class.getResourceAsStream("/resources/Specialnode.json" );
InputStreamReader inReader = new InputStreamReader(input);
ObjectMapper objmapper = new ObjectMapper();
objmapper.readValue(inReader,Data.class); // this will parse and return the data
```

Converting file to Instream
```java
InStream input = new FileInputStream(New File("test/test-data.json"));
InputStreamReader inReader = new InputStreamReader(input);
...
```

In somecase below works
```java
 InputStream inputfile = this.getClass().getClassLoader()
                .getResourceAsStream("input.json");
        if(inputfile!=null) {
            InputStreamReader inReader = new InputStreamReader(inputfile);
            ..
```

### Camel Components
#### camel stream
  - Stream the data to console. Example route
```java
    // RouteBuilder info
    @Override
    public void configure() {
    from("file:data/inbox?noop=true").to("stream:out");  //noop don't delete the original file from input directory
    }
```
  - Stream to prompt input from user and print to a file
```java
    public void configure() {
      from("stream:in?promptMessage=Enter something:").to("file:data/outbox"); //The file name is not specified in here so a UID format file name will be created
    }
```
  - To specify the file, use `fileName` attribute in the to route. Since the file will get overwritten everytime, we can append date to the filename as below
```java
// in this case teh filename will be overritten when executing the route again
public void configure() {
   from("stream:in?promptMessage=Enter something:").to("file:data/outbox?fileName=userprompt.txt");
}

// below is to use date as filename
public void configure() {
from("stream:in?promptMessage=Enter something:").to("file:data/outbox?fileName=${date:now:yyyyMMdd-hh:mm:ss}.txt"); //filename will be the format 
                                                                               // date format can be any format supported by the java.txtSimpleDateFormat.
}
```
#### camel ftp
   - `ftp` component inherits all the feature of the `file` component with few more options
   - The ftp component is not part of the camel-core package, add below dependency
```xml
<dependency>
  <groupId>org.apache.camel</groupId>
  <artifactId>camel-ftp</artifactId>
  <version>${camel-version}</version>
</dependency>
```
- Sample demonstration of ftp component, we are using spring based DSL, since spring provides easy hook to start and stop the embedded FTP server.
```xml
 <!-- CREATE a camel contex and use main method to execute this -->
<route>
    <from uri="stream:in?promptMessage=Enter something:" />
    <to uri="ftp://rider:secret@localhost:21000/data/outbox"/>
</route>
```

#### camel file


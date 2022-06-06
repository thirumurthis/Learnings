Below is an example to add a MD5hash value ot header

```xml

<!-- declare the bean -->
<bean id="codecMD5util" class="org.apache.commons.codec.digest.DigestUtils"/>

<camelContext id="......>
  
  <camel:route id="fetchFileContent" autoStartup="true">
    <camel:from uri="file:///path/for/fetching/file?move=.completed&amp;moveFailed=.errored" />
    <camel:convertBodyTo type ="java.lang.byte[]"/>
    <camel:setHeader headerName="codecmd5">
       <camel:simple>${bean:codedMD5util?method=md5Hex}</camel:simple>
    </camel:setHeader>
    <camel:idompotentConsumer messageIdRepositoryRef="<create-bean-to-define-idempotency>" skipDuplicate="true">
    <camel:simple>header.codecmd5</camel:simple>
    <camel:setHeader headerName="OrderType"><constant>Draft</constant></camel:setHeader>
    <camel:to uri="<mock endpoint or vm:path, seda:paath>" />
    </camel:idompotentConsumer>
  
  </camel:route>


</camelContext>
```

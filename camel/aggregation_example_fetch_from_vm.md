Below is just the part of the route where the message is fetched from the vm endpoint

- So when the aggrgation size  is reached 100 the data will be send to consumer.


```xml
<bean class="com.demo.ListBasedAggreagationStrategy" id="ListAggerator" />
<route>
   <from uri="vm:inputep" />
    <aggregate strategyRef="ListAggerator" forceCompletionOnStop="true" completeAllonStop="true"
                 completionSize="100" completionTimeout="100" >
        <correlationExpression>
          <simple>all</simple>
      </correlationExpression>
      <to uri="file://print/to/path"/>  <!-- this can be another direct, vm or seda scheme -->
  </aggregate>
</route>
```

- Aggregator

```java
class ListAggrgator implements AggregationSrategy{
   
   @Override
   public Exchange aggregate(Exchange existingExchange, Exchange newExchange){
   
      Object newBody = newExchange.getIn().getBody();
      
      ArrayList<Object> list = null;
      
      //if the exchange doesn't have any aggrgation based on the object passed
      if(existingExchange == null){
        list = new ArrayList<Object>();
        list.add(inComing);
        newExchange.getIn().setBody(list);
        return newExchange;
      }else{
       list = existingExchange.getIn().getBody(ArrayList.class);
       list.add(newBody);
       return existingExchange;
   }

}
```

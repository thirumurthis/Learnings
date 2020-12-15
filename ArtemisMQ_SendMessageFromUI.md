### ArtemisMQ:

 - In order to send message from the UI console follow below steps:
   - Click the queue to which you wish to send message ( Queues tab)
   - Click the `Operations` link.
   - use the left side, borker tree node to choose the queue.
   - Search for  `sendMessage(java.util.Map....)` option
   - In the screen, pass in below values
```     
 Header : {} 
 Type : 3 
 Body : <<Content or message text>>
 User : <<username>>
 Password : <<password>> 
```
Note:
  Header - {} represents empty json, without this illegal arg exception will be thrown..
  Type - 3 (3 - is text format)

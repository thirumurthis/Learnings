Using the [link](https://paiza.io/projects/g7YPgzGhRFbiwDzzGro6oA) you can update the code and work on it.

`Xpath` understanding and manipulation
```ruby
require "mecab"
require 'rubygems'
require 'nokogiri'
require 'open-uri'
require 'enumerator'

url = 'http://www.asahi.com/'
text = String.new
#nokogiri = Nokogiri::XML.parse('<main><div id="HeadLine"><ul class="Lnk FstMod"><li><a>one<a></li></ul></div></main>')
text1 = '<main><div id="HeadLine"><ul class="Lnk FstMod"><li><a>one<a></li></ul></div></main>'
@xmlNoko = Nokogiri.XML(open("File1"))

#puts xmlNoko

doc = @xmlNoko.xpath("/")

bean = doc.at_xpath('//addr:address-setting[@match="#"]/*[last()]','addr' => 'urn:activemq:core')

# below will provide an array of the elements.
#bean = doc.at_xpath('//addr:address-setting[@match="#"]//*','addr' => 'urn:activemq:core')

puts bean
if bean.nil?
    puts "bean is null"
else 
   puts "bean is NOT NULL"
end

#bean2 = doc.css('//addrset:address-setting[@match="#"]/*',"addreset" => 'urn:activemq')
#if bean2.nil?
#    puts "bean2 is NULL"
#else
#    puts "bean2 is NOT null"
#end

bean1 = doc.xpath('//addr:address-setting[@match="#"]//*','addr' => 'urn:activemq:core')
if bean1.nil?
    puts "bean1 null"
else
    puts "bean1 NOT NULL"
   
   #// puts bean1
    bean1.each do |val|
#  puts val
     if val.to_s.include? '<auto-create-addresses>'     
         puts "matched the auto create address..."
         puts val
     end
   end

    newbean = bean1.at_xpath('/auto-create-addresses')
    puts newbean
    
    if newbean.nil?
        puts "auto-create-addresses is null"
    else
        puts "auto-create-addresses is NOT null  "
        puts newbean
        
        if newbean.content.eql? 'true'
            puts "value is true"
            newbean.content = 'hellotrue'
        else
            puts "value is false"
        end
            
    end
end
```

#### Create a file names File1 in the same online editor and save it (check the run as button)
```
<?xml version='1.0'?>
<configuration xmlns="urn:activemq"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xmlns:xi="http://www.w3.org/2001/XInclude"
               xsi:schemaLocation="urn:activemq /schema/artemis-configuration.xsd">

   <core xmlns="urn:activemq:core" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="urn:activemq:core ">

      <name>0.0.0.0</name>



      <address-settings>
         <!-- if you define auto-create on certain queues, management has to be auto-create -->
         <address-setting match="activemq.management#">
            <dead-letter-address>DLQ</dead-letter-address>
            <expiry-address>ExpiryQueue</expiry-address>
            <redelivery-delay>0</redelivery-delay>
            <!-- with -1 only the global-max-size is in use for limiting -->
            <max-size-bytes>-1</max-size-bytes>
            <message-counter-history-day-limit>10</message-counter-history-day-limit>
            <address-full-policy>PAGE</address-full-policy>
            <auto-create-queues>true</auto-create-queues>
            <auto-create-addresses>true2</auto-create-addresses>
            <auto-create-jms-queues>true</auto-create-jms-queues>
            <auto-create-jms-topics>true</auto-create-jms-topics>
         </address-setting>
         <!--default for catch all-->
         <address-setting match="#">
            <dead-letter-address>DLQ</dead-letter-address>
            <expiry-address>ExpiryQueue</expiry-address>
            <redelivery-delay>0</redelivery-delay>
            <!-- with -1 only the global-max-size is in use for limiting -->
            <max-size-bytes>-1</max-size-bytes>
            <message-counter-history-day-limit>10</message-counter-history-day-limit>
            <address-full-policy>PAGE</address-full-policy>
            <auto-create-queues>true</auto-create-queues>
            <auto-create-addresses>true1</auto-create-addresses>
            <auto-create-jms-queues>true</auto-create-jms-queues>
            <auto-create-jms-topics>true</auto-create-jms-topics>
         </address-setting>
      </address-settings>

   </core>
</configuration>

```
Screenshot of output:

 ![image](https://user-images.githubusercontent.com/6425536/86883252-7d72c880-c0a6-11ea-90e3-c1ac30bf7cb8.png)

```java
package com.simple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class SimpleDateExample {
	
	public static void main(String args[]) {
		String isoDate = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
		System.out.println(isoDate);//20201123
        isoDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
        System.out.println(isoDate);
        String isoDatetime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        System.out.println(isoDatetime);
        String indianFormat= LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        System.out.println(indianFormat);
        DateTimeFormatter french = DateTimeFormatter.ofPattern("d. MMMM yyyy",new Locale("fr"));
        String frenchDate = LocalDateTime.now().format(french);
        System.out.println("Date - french format: " + frenchDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        		.withLocale(new Locale("de"));
   		String germanDateTime = LocalDateTime.now().format(formatter);
   		System.out.println("Date - german format : " + germanDateTime);
   		String isoTime = LocalDateTime.now().format(DateTimeFormatter.ISO_TIME);
   		System.out.println("time in ISO TIME format : " + isoTime);
   		
   	 ZonedDateTime dateWithTimeZone = ZonedDateTime.of(LocalDateTime.now(),
   	        ZoneId.of("Europe/London"));
   	    String withTimeZone = dateWithTimeZone
   	        .format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
   	    System.out.println("date time with timezone : " + withTimeZone);
   	    		
   		//
   		LocalDate fromIsoDate = LocalDate.parse("2015-02-23");
   		LocalDate fromCustomPattern = LocalDate.parse("20-03-2017", 
   		                             DateTimeFormatter.ofPattern("dd-MM-yyyy"));
   		//LocalDate invalidDate = LocalDate.parse("16-02-2018"); // this will throw exception
   		
   	 
   	    // parsing text to LocalTime in Java 8
   	    LocalTime fromIsoTime = LocalTime.parse("12:07:43.048");
   	    LocalTime fromPattern = LocalTime.parse("11:06:32",
   	        DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println(fromIsoDate+" &&  "+fromPattern +" && "+ fromIsoTime);
   	    
   	    // converting String LocalDateTime and ZonedDateTime in Java 8
   	    LocalDateTime fromIsoDateTime = LocalDateTime
   	        .parse("2016-06-16T13:12:38.954");
   	    ZonedDateTime fromIsoZonedDateTime = ZonedDateTime
   	        .parse("2016-06-16T13:12:38.954+01:00[Europe/London]");
   	    System.out.println(fromIsoDateTime+" && "+fromIsoZonedDateTime);

	}

}

```

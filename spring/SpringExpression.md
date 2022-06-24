
```java

public class TestSpringExpression{
  
 public static void main(String args[]){
    
   MyPojo pojo = new MyPojo();
   pojo.setInput("hello");
   
   StandardEvaluationContext context = new StandardEvaluationContext();
   
   context.setVariable("myObj",pojo);
   
   ExpressionParser parser = new SpelExpressionParser();
   
   Expression s1 = parser.parseExpression("#myObj.input == 'hello'");
   
   MyPojo pj = (MyPojo)s1.getValue(context);
   System.out.println(pj.getInput());
   
   
 }
}


class MyPojo{
 
  private String input;
  private Date date;
  //getter setters
  
}
```


### Example:

```java
public class Data {

	 
	 private Object dataalue;
	 private Date dataDate;
	 private DateTime dataDateTime;
	// getters and setters
}
```
```java
package demo.spel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;


public class TestSpringExpression {

	public static void main(String[] args) {
		
		SampleModel mesgObj = populateValues();
		StandardEvaluationContext context = new StandardEvaluationContext();

		

		context.setVariable("data", mesgObj);
		ExpressionParser parser = new SpelExpressionParser();
		
		
		// Sample 1 : Max of two parameters
		Expression exp1 = parser.parseExpression("#data.max('IOT_Sensor1','IOT_Sensor2')");
		DataValue expressionResult1 = (DataValue)exp1.getValue(context);
		System.out.println("Maximum Value : "+expressionResult1.getDataValue()+" TimeStamp :"+expressionResult1.getDataDateTime());
		
		// Sample 2 : Difference of two parameters		
		Expression exp2 = parser.parseExpression("#data.dataValue('IOT-Sensor1') - #message.dataValue('IOT-Sensor2')");
		Object expressionResult2 = exp2.getValue(context);
		System.out.println(expressionResult2);
		
		
		// Sample 3: Difference of Maximum value of 2 parameters during TAFEOFF Phase 
		// Note :  For Arithmetic operations, use maxValue function, Since max function returns value with time stamp.
		Expression exp3 = parser.parseExpression("#message.dataValue('LEFT-device1') - #message.minValue('LEFT-device2')");
		Object expressionResult3 = exp3.getValue(context);
		System.out.println(expressionResult3);
			

	}

	
	public static SampleModel populateValues(){
		Map<String, List<ParameterValue>> allValues = new HashMap<>();
		// Example 1 : For ACMS DATA
		ParameterValue pValue = new ParameterValue();
		pValue.setParameterDate(new Date());
		pValue.setValue(100);
		List temp =  new ArrayList<ParameterValue>();
		temp.add(pValue);
		allValues.put("IOT_sensor_1", temp);
		
		ParameterValue pValue1 = new ParameterValue();
		pValue1.setParameterDate(new Date());
		pValue1.setValue(200);
		List temp1 =  new ArrayList<ParameterValue>();
		temp1.add(pValue1);
		allValues.put("IOT_sensor_2", temp1);
		
		
		
		
		
		// Example 1 : For ACMS DATA
		List temp3 =  new ArrayList<ParameterValue>();
		ParameterValue pValue3 = new ParameterValue();
		pValue3.setParameterDate(new Date());
		pValue3.setValue(100);
		pValue3.setParameterDate(new Date());
		pValue3.setValue(110);
		pValue3.setParameterDate(new Date());
		pValue3.setValue(120);
		
		temp3.add(pValue3);
		allValues.put("LEFT-Device1", temp3);
		
		temp3 =  new ArrayList<ParameterValue>();
		ParameterValue pValue4 = new ParameterValue();
		pValue4.setParameterDate(new Date());
		pValue4.setValue(120);
		pValue4.setValue(150);
		pValue4.setParameterDate(new Date());
		
		temp3.add(pValue4);
		allValues.put("LEFT-Device2", temp3);
		
		
		SampleModel mesgObj=new SampleModel();
		 mesgObj.setAllValues(allValues);
		 
		 return mesgObj;
		
		
	}
}

```

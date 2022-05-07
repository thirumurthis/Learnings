In this blog we will see how we can use Apache Common CLI (open source) to easily read the arguments passed to the Java program easily.

### Use Case
  - In Enterprise when building java that takes arguments and perform some business logic. We can use the Apache common CLI to minimize lots of manual codes.
  - I have used shell script to pass the argument to the java main class which is executes on schedule set in the crontab on Linux VM.

 - For demonstration purpose we use the Linux command that list the flies from the current directory `ls -lrt` on the below java program, `-lrt` is know as options or switch.

 - Here we will use similar options, in the java program. We will pass the input as java program arguments.

### Add the Apache Common CLI dependencies

```xml
<dependency>
    <groupId>commons-cli</groupId>
    <artifactId>commons-cli</artifactId>
    <version>1.5.0</version>
</dependency>
``` 
### Java program that uses the CLI library methods

- Lets create a simple class using the CLI libraries.
  - The `Options` object is used to define the requires arguments, which can be easily understood from the below code.
  - We will also be using `HelpFormatter` to display the added options and its description as help command.

```java
package com.demo.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class SimpleMain {

	private static String input;
	private static boolean l;
	private static boolean r;
	private static boolean t;
	
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();

		// create the Options
		Options options = new Options();
		options.addOption("i","input",true,"pass input value to argument");
		options.addOption("l","list",false,"use long list formatting");
		options.addOption("r","reverse",false,"reverse order while sorting");
		options.addOption("t",false,"Sort by modification time");
		options.addOption("h","help",false,"help for option");
		// Alternate way to create the options using the builder pattern
		options.addOption(Option.builder("s").longOpt("size")
		                                .desc("print allocated size of each file")
		                                .hasArg(false)
		                                .build());
		options.addOption(Option.builder("S").longOpt("sort")
                .desc("sort by file size")
                .hasArg(false)
                .build());

		// if we need to test some of the arguments from the java class we can pass
		// String[] cmdArgs = new String[]{ "--input=10" };
		
		// used to print the help using the CLI jar
		HelpFormatter formatter = new HelpFormatter();
		
		try {
		    // parse the command line arguments
			// passing the java arguments directly
		    CommandLine line = parser.parse(options, args);

		    // validate that input is set
		    if(line.hasOption("i") || line.hasOption("input")) {
		    	System.out.println("printing input :- "+line.getOptionValue("input"));
		    	
		    	//Fetch the value from the arguments
		    	input = line.getOptionValue("input");
		    }
		    if(line.hasOption("h") || line.hasOption("help")) {
		    	formatter.printHelp("help", options);
		    }
		    
		    if(line.hasOption("l") || line.hasOption("list")) {
		    	l=true;
		    }
		    if(line.hasOption("r") || line.hasOption("reverse")) {
		    	r=true;
		    }
		    if(line.hasOption("t")) {
		    	t=true;
		    }
		    // using the passed value to print the value, we can use this to perform business logic
		    if(r) {System.out.println("reverse passed");}
		    if(l) {System.out.println("list passed");}
		    if(t) {System.out.println("time passed");}
		}
		catch (ParseException exp) {
		    System.out.println("Unexpected exception:" + exp.getMessage());
		}
	}
```

### Output of the program for different program argument

   - using --help or -h as arguments to the java main class will print output as below

```
 $ java SimpleMain --help
usage: help
 -h,--help          help for option
 -i,--input <arg>   pass input value to argument
 -l,--list          use long list formatting
 -r,--reverse       reverse order while sorting
 -s,--size          print allocated size of each file
 -S,--sort          sort by file size
 -t                 Sort by modification time
```
  - Using -i or --input along with `-lrt` will display the output like below
```
$ java SimpleMain -i=10000 -lrt
1000
reverse passed
list passed
time passed
```

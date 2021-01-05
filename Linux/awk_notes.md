##### The more comman version of awk used now-a-days is gawk, use `$ awk` command to view display.

#### Basic command:
```sh
$ awk '{ print $0}' input.txt
one 1
two 2
three 3
```
 - input file content
```sh
$ cat input.txt
one 1
two 2
three 3
```

#### `awk` default seperator/delimitor is `space`.
```sh
$ awk '{print $2}' input.txt
1
2
3

// using the flag -F' ' -> sperates field based on space as well
```

#### using `tab` as sperator
```sh
$ awk -F t '{print $1}' input1.txt 
one

//input1.txt say has one<tab-space>1..
```

#### comma in the '{print $1, $2}' is just adding a space.
#### if the comma is removed, the string will be concatenated.
#### if we need to add a special char or string between the field then we can use
```sh
$ awk -F':' '{print $1"-"$2}' input3.txt
one-1
two-2

//input3.txt content
one:1
two:2
```
```sh
$ awk -F':' '{print $1,"-",$2}' input3.txt
one - 1
two - 2
// , in the print adds a space in output
```

### action in the awk command, is proceeded with '' 
#### command to print the number of feilds in each line.

```sh
$ awk '{print NF, $0}' input3.txt
1 one:1
1 two:2
```
```sh
$ awk -F':' '{print NF, $0}' input3.txt
2 one:1
2 two:2
```

#### pattern matching the content, prints only lines containing `one`
```sh
$ awk -F':' '/one/{print NF, $0}' input3.txt
2 one:1
```
```sh
$ cat input4.txt
one:1:ONE
two:2:TWO
three:3:next:will:be
Four:4
```
#### condition with print statement, lines having only 3 fields based on seperator :
```sh
$ awk -F':' 'NF==3{print NF,$0}' input4.txt
one:1:ONE
two:2:TWO

 // Note: the three and Four line not displayed.
```
#### condition without an action, in this case print, the default is print
```sh
$ awk -F':' 'NF>=3' input4.txt
three:3:next:will:be
```

#### `flags like -F, -f, -v` in the awk command
  - `-F` is used to specify the delimiter or seperator 
  - `-f` is used to provide the commands/action from a file. -f <filename>
  - `-v` is used to provide user defined variable.
  
##### using `-f` example:
```sh
$ cat swap
print $2, $1

$ awk -f swap
one two   <---- user input from console
two one    <----- output
<ctrl+D/C/Z> to quit
```

#### using the `-v` for user defined variable.
```sh
$ awk -v var1=10 '{print var1,$0}' input3.txt
10 one:1
10 two:2
```

```sh
$ awk -v hi=hello '{print var1,$0}' input3.txt
hello one:1
hello two:2
```

#### awk can be passed with `multiple file` at the same time
```sh
$ awk '{print $1}' file1 file2 file3
```

#### awk can use "\<" for input from a file.
```sh
$ awk '{print $0}' < input3.txt
```

#### awk can use "|" for input from command
```sh
$ uptime: awk '{print NF, $0}'
```

#### awk can output result to another command
```sh
$ awk -F':' '{print $1}' input2.txt | sort -n 
```


### Fundamental concept of awk is, each input is consists of `RECORDS` and each `Records` is divided into `FIELDS`
### By default, awk considers each line to be `RECORDS` and any white space is considered as end of one `FIELD` and begning of another `FIELD`.
### with the combination of SPACE and TAB, by default is treated as field seperator.

```sh
$ awk -F ABC '{print $2}'
oneABCtwoABCthree   <-- user input
two
```

#### How to specify two field seprators, (specified using '' quotes)
```sh
$ awk -F '[,!]' '{print $2}' 
one!two,three  <--- user input
two
```

#### How to assign a `Field sperator` within the awk programming. this where `FS` comes to play.
##### NOTE: awk splits the records and fields before calling the action, in here the '{FS...}'. This is the reason the first input is using default space as field serpator.
### `;` in action is used as command delimitor link in java, javascript.
```sh
$ awk '{FS=","; print $2}' 
one,two,three    <--- user input, NOTE: the default space separator is applied.

four,five,six    <--- sine the action is read the , space sperator is applied.
five
```

###### To fix the above issue, we can use the `BEGIN` pattern.
```sh
$ awk 'BEGIN{FS=","} {print $2}'
one,two,three
two
four,five,six
five
```

### What happens in case there file has single line without `new line` indicator, but some other indicator like #.
- content of input5.txt
```
laptop,computer,desktop#rom,ram,memory#television,radio,telphone#
```
##### How to seperate use the awk to sperate the above sample, where Record seprator is "#" and field sperator is ",".
```sh
$ awk 'BEGIN{RS="#";FS=","} {print $2}' input5.txt
computer
ram
radio
$ echo "one,two#three,four,five#six,seven" | awk 'BEGIN{RS="#";FS=","} {print $2}'
two
four
seven
```
 - NOTE: when the `RS=""`, blank or empty string, any sequence of blank line is used as seprator.
 - For example input6.txt
 ```
 renton
 washigton
 
 
 seattle
 washington
 
 SFO
 california
 ```
 
 ```sh
 $ awk 'BEGIN{RS="";FS="\n"} {print city=$1;state=$2; print city,",",state}' input6.txt
 renton,washington
 seattle,washignton
 SFO,california
 ```
 
-  NOTE: 
  - the `print` statement uses default FIELD seperator single space (wherever we use ',' or comma) 
  - and RECORD Seperator, which is new line.
  - This can be overrided by using __`OFS`__ and __`ORS`__
  ```sh
  $ awk 'BEGIN{OFS="@";ORS="#"} {print $1,$2} input6.txt
  renton@washington#seattle@washington#SFO@california#
  // Note, that the "{print $1,$2}' - the "," now uses OFS - @ 
  ```
  
#### Convert a file with three values in file to output tab sperated data
```sh
$ cat input7.txt
name1,city1,state1
name2,city2,state1
name3,city3,state2

$ awk -F"," 'BEGIN{OFS="\t";} {print $1,$2} input7.txt
name1     city1    state1 
name2     city2    state1 
name3     city3    state2 
```
  
### Built in variables
  - FS - input Field Separator
  - RS - input Record Separator
  - OFS - output Field Separator
  - ORS - output Record Separator
  - NF - Number of Fields (`this is informational, meaning cannot be assigned with another value like FS or RS`)
  - NR - Number of Records
  - FILENAME - File name itself
  - FNR - Number of records in that file
  - set of field variables 
     - $0 - prints the whole records.
     - $N, N= 1,2,3... (number of field)

```sh
$ awk '{print NR, $0} input7.txt
1 name1,city1,state1
2 name2,city2,state1
3 name3,city3,state2
```

```sh
$ awk 'NR==3{print NR, $0} input7.txt 
3 name3,city3,state2
// Prints the 3rd line in the file.
```
- Note: if there are more than 1 file, awk will concatenate from other files to the NR will be continuos 1 2 3 4...

```sh
$ cat input8.txt
one
two 
three

$ cat input9.txt
eight
nine
ten
// NR - will be contiuous, FNR is file level NR
$ awk '{print NR, FILENAME, FNR, $0}' input8.txt input9.txt
1 input8.txt 1 one
2 input8.txt 2 two
3 input8.txt 3 three
4 input9.txt 1 eight
5 input9.txt 2 nine
6 input9.txt 3 ten
```

#### Using the `$NF`, dollar on a built in variable, which prints the last field in the file.
```sh
$ cat input10.sh
one two
three four five
six

$ awk '{print $NF}' input10.sh
two
five
six
// prints the last field in the records/ each line
```

#### Using expression using `()` and print the penultimate string ( the field before the very last field)
```sh
$ awk '{print $(NF-1)}' input10.sh
one
four
six
// NOTE: there shouldn't be any blank lines, if any handle it using inital patterns if needed

// NOTE: using $NF-1 without () or brackets, the literal value is printed. the numerci value of word is 0,
//       i.e. $NF is one its numeric value is 0, so -1 will be printed.
```

#### reading the field values form the input, and  printing the field values 
```sh
$ awk '{print $($1) }'
1 one two three
1
2 one two three
one
3 one two three 
two
// the first input is the field value and prints that value
```

#### Assigning a new values to the field values, the below will updates only in the memory.
```sh
$ cat input11.txt
one two three four
five six
eight nine hundered

$ awk '{$2="TEMP"; print} input11.txt
one TEMP three four
five TEMP
eight TEMP hundered
```
##### Note: What happens if we use substitute a field value that is not present in the records.
```sh

$ awk '{$5=FIVE; print}' input11.txt
one two three four FIVE
five six    FIVE
eight nine hundered  FIVE

// The length of the line is extended. NR will display 5 now
$ awk 'BEGIN{OFS="@";print}' input11.txt           <--- to visually see the length
```

##### Check: what happens to below command
```sh
$ awk '{$0="one two three"; print NF,$2}' input11.txt
```

### User defined variables in awk
 - no need to create and define a variable in awk (sometimes this causes issue, the mistaken field will be crated as variable by awk)

```sh
$ awk '{var1=$1; var2=$2; print var1,var3}'
one two
one
one three
one
// Note: the var3 is not defined, so the value is blank in the output
```
 
 - awk variable are case senstivity.
 
 ```sh
  $ awk '{ a=$1; A=$2; print a,A}'
  one two
  one two
 ```
 #### NOTE : \ is used for escaping
 
 - How does awk resolve data type like integer, string, floating point
    - awk treats the variable as number or string based on the context.
    - any word or string stored in variable and operated, the word variable will be treated as 0. eg: a=1; b="temp"; print a+b; -> will yeild 1
    - integer and floating values are automatically convereted
    - To treat a number to a string, concatenate it with "" or empty string
    - order of execution of operation using BODMAS, awk first performs the oepration and then concatenates value. eg: a=1;b=2;c=3;print a b * c => will yeild 16 
    - All awk variable is having GLOBAL scope. any value set to the variable retains the values throghout the program.

```sh
$ awk '{a=1;b=4; print a+b}'
             <---- hit enter or any input, the result will be addition
5

$ awk '{a=1;b=4; print a b}'

14   <---- this string is concatenated since no operator is used.

$ awk '{a=1;b="temp"; print a+b}'

1   <---- the b="temp" is 0

$ awk '{a=1;b=2; print a/b}'

0.5

$ awk '{print "one" + 0}'    /// This is to convert a string to integer where one is treated as 0 since one is considered as number

0

$ awk '{print "1" ""}'

1    <--- this is a string from the context not integer or number since we are concatenating an empty string.

$ awk '{a=1;b=2;c=3; print a b * c}'

16

$ awk '{a=1;b=2;c=3; print (a b) *c}'   <--- here we are using paranthesis to say perform concatenation first, then multiply

36 

/// below demostrates how awk converts number to string
$ awk '{print "\"" $1 "\"+ 0 = " $1 +0 }' 

""+0 = 0
123
"123"+0 = 123
6.6
"6.6"+0 = 6.6
some15
"some15"+0 = 0    <------------------- any input starting with character is treated as word and 0 is subsituted.
15test
"15test"+0 = 15  <-------------------- any input starting with number is treated as number any string after the number is not used, so 15 is displayed.
```

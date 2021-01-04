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
  $ awk 'BEGIN{OFS=",";ORS="#"} {print $1,$2} input6.txt
  renton,washington#seattle,washington#SFO,california#
  ```
  
  

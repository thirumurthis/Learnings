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

# To use the arguments of the previous or last command in shell

###### when using the `!$` the last argument of previous command is substituted
```
$ echo a b c d
a b c d

$ echo !$
echo d
d
```
##### when using the `!^` the first argument of previous command is substituted
```
$echo a b c d
a b c d

$ echo !^
echo a
a

$ echo a b c d
a b c d
$ ls !^
ls a 
<corresponding ls output>
```

##### when using the `!!:n` (n - is number 1-n) the n-th argument of previous command is substituted
```
$ echo 1 2 3 4 5 6
1 2 3 4 5 6 

$ echo !!:4
ehco 4
4

$ echo 1 2 3 4 5 6
1 2 3 4 5 6 
$ ls !!:3
ls 3
<corrsponding output of ls command>
```

# To search through the `history` command `cntrl` + `R` and `cntrl` + `G`
```
$ 
(reverse-i-search)`':

(reverse-i-search)`ll': ll -lrt
```
 - <Press:> `cntrl` + `G` to close 
 - <Press:> `Enter` to execute the history command at shell cursor
 
# Copy word (`cntrl`+`w`) and paste  (`cntrl`+`y`) last copied content
```
$ ls -lrt 
<press cntrl + w from cursor position, notice -lrt getting removed>
<press cntrl + y from cursor position, notice -lrt getting printed>
```


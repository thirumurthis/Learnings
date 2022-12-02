makefile:

# 1. Accessing environment variable  

Any variable set in the environment when GNU make is started witll be available as make varible inside makefile.

`$(info)` to print the value of variable 
`$(origin)` - will display from where the value is from

 - tab space is required
```
.PHONY: demo
demo:
	echo "print info"
	$(info $(JAVA_HOME) $(origin JAVA_HOME))
```

- output:

```
PS C:> make demo
C:\Program Files\Java\jdk-17.0.1\ environment
echo "print info"
print info
```

#2

- In the Makefile we can override the variable. if we don't want to override the environment variable locally though it is assigned in the Makefile, we need to use -e or --environment-overrides, and the `$(origin)` which displays `environment override`

- `override` will be displayed if actually the value is overridden. 

- in below are overrding the JAVA_HOME

```
JAVA_HOME="java"

.PHONY: demo
demo:
	@echo "print info"
	$(info $(JAVA_HOME) $(origin JAVA_HOME))
```

- ouput with `make demo`

```
> make demo
"java" file
print info
```

- output with `make -e demo`

```
> make -e demo
C:\Program Files\Java\jdk-17.0.1\ environment override
print info
```

# 2.1 to check from where the variable set from environment, file, command line, override

```
override JAVA_HOME="java"

.PHONY: demo
demo:
	@echo "print info"
	$(info $(JAVA_HOME) $(origin JAVA_HOME))
```

- output 

```
2> make -e demo
"java" override
print info
```

# 2.2 
  - with demo passed from the command line works in the git bash
```
override JAVA_HOME="java"

.PHONY: demo
demo:
	@echo "print info"
	$(info $(JAVA_HOME) $(origin JAVA_HOME))
	$(info $(DEMO) $(origin DEMO))
```

- output 
```
$ DEMO=d make -e DEMO=overit demo
"java" override
overit command line
print info
```

# 3. Set variables from outside make file

```
make DEMO=value_of_demo target
```

```
override DEMO=inFile

$(info $(DEMO) $(origin DEMO))

.PHONY: demo
demo:
	@echo "print info"
	$(info $(JAVA_HOME) $(origin JAVA_HOME))

.PHONY: all
all: ; @echo all target DEMO value is $(DEMO)
```

- output

```
$ make all
inFile override
all target DEMO value is inFile
```

# 4. Usign `ifndef`

```
override DEMO=inFile

$(info $(DEMO) $(origin DEMO))

ifndef BUILD_DEBUG
BUILD_DEBUG :=yes
endif

.PHONY: all
all: ; @echo "all target DEMO : BUILD_DEBUG value is $(DEMO) : $(BUILD_DEBUG)"

```

- output 
```
$ make all
inFile override
all target DEMO : BUILD_DEBUG value is inFile : yes
```

# 4.1 using `?` for variable, which is short for the `ifndef`

```
override DEMO=inFile

$(info $(DEMO) $(origin DEMO))

BUILD_DEBUG ?=yes

.PHONY: all
all: ; @echo "all target DEMO : BUILD_DEBUG value is $(DEMO) : $(BUILD_DEBUG)"
```

- output 

```
$ make all
inFile override
all target DEMO : BUILD_DEBUG value is inFile : yes
```

# 5. escape the value inside target recipe using `$`

```
DEMO=inFile

$(info $(DEMO) $(origin DEMO))

BUILD_DEBUG ?=yes

# since the DEMO is not defined in the environment variable
# below $$DEMO will print blank though the $ is escaped

.PHONY: all
all: ; @echo all target DEMO value is $$DEMO
```

- Output when the environment DEMO doesn't exists
```
$ make all
inFile file
all target DEMO value is
```

- Output, when environment value set with `export DEMO=envvalue` in terminal, the local inFile value will be printed

```
$ make all
inFile file
all target DEMO value is inFile
```

# 5.1 using export and unexport in the make file

```
DEMO=inFile
export FOO=valueinmakewithexport
$(info $(DEMO) $(origin DEMO))

# since the DEMO is not defined in the environment variable
# below $$DEMO will print blank though the $ is escaped

.PHONY: all
all: ; @echo "all target DEMO TEST value is $$DEMO | $$FOO"
```

- output

```
$ make
inFile file
all target DEMO TEST value is inFile | valueinmakewithexport
```

- With `unexport` we can unexport the exported FOO value

```
DEMO=inFile
export FOO=valueinmakewithexport
$(info $(DEMO) $(origin DEMO))
unexport FOO

.PHONY: all
all: ; @echo "all target DEMO TEST value is $$DEMO | $$FOO"
```

- output

```
$ make
inFile file
all target DEMO TEST value is inFile | valueinmakewithexport
```


# 5.2 export can be target specific
 - NOTE: we cannot use `unexport` on specific target. `all: unexport FOO` will throw error.

```
DEMO=inFile

all: export FOO=just for all

.PHONY: all
all: ; @echo "all target DEMO TEST value is $$DEMO | $$FOO"

```

- output:

```
$ make
all target DEMO TEST value is inFile | just for all
```

# 5.3 - other variables 
- make also adds a number of variables to the subprocess environment 
  - MAKEFLAGS - variable contains the flags specified in the command line
              - MAKEFLAGS contains flags formatted for GNU internal use 
              - **never use the `MAKEFLAGS`**
  - MFLAGS - like MAKEFLAGS, this variable contains the flags specified in the command line
           - is only there for historic reasons
           - if you need to use, we can use MFLAGS
  - MAKELEVEL - variable contains depth of recursive make calls, via $(MAKE) starting at zero.
  
# 6. The $(shell) environment
- before executing the below make, set the environment variable 
`export DEMO=envval`
 
```
DEMO=inFile

$(info $(shell printenv | grep DEMO))

.PHONY: all
all: ; @printenv | grep DEMO
```
- output

```
$ make
DEMO=envvalue
DEMO=inFile
```
- The above is the bug which is not fixed due to other reasons
- To fix the above issue, in the shell we pass the value using `$()`

```
export FOO=inFile

$(info $(shell FOO=$(FOO) printenv | grep FOO))

.PHONY: all
all: ; @printenv | grep FOO
```

- Output

```
$ make
FOO=inFile
FOO=inFile
```
- This works because the argument to `$(shell)` gets expanded before execution, that set the value to FOO above. This value is taken from the makefile. This will get messy if we have many environment variable.

- Another better solution is to write a function `env_shell` which does not export variables.
 - NOTE: below function was not working where the env on the temp is not loaded correctly. validate latter.

```
env_file=/tmp/env
# the echo $V$1$2 was added for debug
env_shell = $(shell rm -f $(env_file))$(foreach V,$1,$(shell echo $V,$2; export $V=$($V) >> $(env_file)))$(shell echo $2 >> $(env_file))$(shell /bin/bash -e $(env_file))

export FOO=inFile
$(info $(call env_shell,FOO,printenv | grep FOO))

.PHONY: all
all: ; @printenv | grep FOO
```

# 7 - Target Specific and pattern specific variables

Global scope and local scope

```
.PHONY: all foo bar baz

VAR = global scope

all: foo bar
all: ; @echo In $@ VAR is $(VAR)

foo: ; @echo In $@ VAR is $(VAR)

bar: VAR = local scope
bar: baz
bar: ; @echo In $@ VAR is $(VAR)

baz: ; @echo In $@ VAR is $(VAR)
```
 - The make file has 4 targets, all 4 targets are phoney since this doesn't make any files.
 - The targets are exected standard depth-first from left to right, since the all target dependency is foo which is printed, then bar depends on baz printed, then bar finally all.
 - since there i no local definition for VAR in the all target the global value is used.
 - the target-specific variables apply not just to a target, but to all the target's prequisites as well as their prerequisites (or dependency). Any target-specific variable scope is to entire tree of targets.

NOTE: the use of defining all the target upfront with PHONY
 
- Output

```
> make
In foo VAR is global scope
In baz VAR is local scope
In bar VAR is local scope
In all VAR is global scope
```

# 7.1 Pattern specific variable

 NOTE: `$@` displays the target name
```
.PHONY: all foo bar baz

VAR = global scope

all: foo bar
all: ; @echo In $@ VAR is $(VAR)

foo: ; @echo In $@ VAR is $(VAR)

bar: VAR = local scope
bar: baz
bar: ; @echo In $@ VAR is $(VAR)

baz: ; @echo In $@ VAR is $(VAR)


f%: VAR = starts with f
```

- `f%`, where % is wildcard. This applies to all target that starts with f
- output

```
> make
In foo VAR is starts with f
In baz VAR is local scope
In bar VAR is local scope
In all VAR is global scope
```

# 8 - version checking

```
.PHONY: all 
all: ; @echo $(MAKE_VERSION)
```

- output 

```
 make
4.0
```

- To determine if the version is higher than 3.80

```
need:=4.10

ok := $(filter $(need), $(firstword $(sort $(MAKE_VERSION) $(need))))

# if ok is not blank, the required version of make or later being used

.PHONY: all 
all: ; @echo "$(MAKE_VERSION) , $(ok)"
```
-suppose (and need:=3.80) running version is 3.81 the `$(sort $(MAKE_VERSION $(need)))` will be 3.80,3.81.
- the `firstword` of that will be 3.80, so `filter` will keep it 
- suppose running version is 3.70, then sort will be 3.70, 3.80, when filtering for 3.80 it will be blank.

-Output
- Say if the current version is 4.0, below is the output

```
> make
4.0 ,
```
- if we update the need to  4.0 or 3.80
```
> make
4.0 , 4.0
> make
4.0 , 3.80
```

# 8.1 .FEATURES

3.8.1
 - archives
 - check-symlink = The -L and --check-symlink-times flags
 - else-if - Else branches in non-nested form 
 - jobserver - building parallel job using server
 - order-only  - order-only prerequisites
 - second-expansion - double expansion 
 - target-sepcific 

3.8.2
 - oneshell - .ONESHELL special target
 - shortest-stem : using the shortest stem option when choosing between pattern rules that match a target
 - undefine - undefine directive
 
4.0
  - guile - if GNU make was built with GNU guile support this will be present and the $(guile) function will be supported
  - load - ability to load dynamic obects to enhance capabilities of make
  - output-sync - the -O (--output-sync) command option

- to check if the feature is available. 

```
is_feature = $(if $(filter $1,$(.FEATURES)),T)

.PHONY: is-archive-feature
is-archive-feature: ; @echo "archives are $(if $(call is_feature,archives),,not ) available"
```
- output 
```
$ make is-archive-feature
archives are  available
```
- To check if even .FEATURE is supported use below 
 
```

has_feature := $(if $(filter default,$(origin .FEATURES)),$(if $(.FEATURES),T))

.PHONY: is-feature
is-feature: ; @echo "has .FEATURES $(if $(call has_feature),,not) enabled"
```

- output 

```
$ make is-feature
has .FEATURES  enabled
```

# 9. `$(eval)` 
- Powerful make function
- The argument of $(eval) is expadned and then parsed as if it where part of the makefile.

- In the previous example to check the version, alternatively we could use following fragment of code that sets the eval_availabel to T only if $(eval) is implemented

```
$(eval eval_available :=T)

ifndef ($(eval_available),T)
  $(error This makefile only works with Make program that support $$(eval))
endif

is_feature = $(if $(filter $1,$(.FEATURES)),T)

.PHONY: is-archive-feature
is-archive-feature: ; @echo "archives are $(if $(call is_feature,archives),,not ) available"
```
- if $(eval) is not available make will look for a varlaibe called eval eval_available := T and try to get its value. This varliave doesn't exiss of course, so eval_available will be set to empty string
- Using the `ifndef` we generate a fata error if $(eval) isn't implemented
- output

```
$ make is-archive-feature
Makefile:4: *** This makefile only works with Make program that support $(eval).  Stop.
```

# 9.2 boolean values

- `$(if)` and `ifndef` construct treat the empty string and defined variable as false, and anything else as true.

> **NOTE:**
> - The `$(if)` function ie. `$(if X, if-part, else-part)` expands to `if-part` if X is not empty and `else-part` otherwise. When using `$(if)` the condition is expanded and the value after expansion is tested for emptiness

```

EMPTY = 

VAR = $(EMPTY)

$(if $(VAR),$(info if-part),$(info else-part))
```

- output
```
> make
else-part
make: *** No targets.  Stop.
```

- The `ifdef`

```
ifdef VAR
if-part
else
else-part
endif
```

- undefined variable conditional

```
VAR=

ifdev VAR
$(info VAR is defined)
else
$(info VAR is undefined)
endif
```
 - In an actual makefile the above might not provide intended result, we can ask for warnings of undefined variables with `--warn-undefined-variables` command line option.
  - `ifdef` doesn't expand the VAR, it sumply looks to see if it hs been defined to a non-empty value. Below code prints VAR as defined
 ```
 EMPTY = 
 VAR = $(EMPTY)
 ifdef VAR
 $(info VAR is defined)
 else
 $(info VAR is undefined)
 endif
 ```
 
 # 10. Truth values using function in it
 
```
# check the first argument if present returns T, (no value so blank)
checkTruth = $(if $1,T)
# above can be written like below as well
checkTruthDiffForm= $(if $1,T,)

# below first argument is space, it will not be trimmed
$(info $(call checkTruth, ))
$(info $(call checkTruth,true))
$(info $(call checkTruth,a b c))

checkNow = $(if $1,T,F)

$(info $(call checkNow,))
```

- output
```
> make
T
T
T
F
```

- assiating empty values and output

```
checkTruth = $(if $1,T)

$(call checkTruth,)
EMPTY =
$(info $(call checkTruth,$(EMPTY)))
VAR = $(EMPTY)
$(info $(call checkTruth,$(VAR)))
```
- Output (blank line)
```


```

# 10.1 or logical operator

or = $1$2

```
or = $(call checkTruth,$1$2)
# else

or = $(if $1$2,T).

$(info $(call or, , ))
$(info $(call or,T, ))
$(info $(call or, ,)) #no space on second arg
$(info $(call or,param one,argument two ))

EMPTY=

$(info $call or,$(EMPTY),)
```

# 10.2 and 

```
and = $(if $1,$(if $2,T))
```

# 10.3 not

```
not = $(if $1,,T)
```
# 10.4 nand, nor, xor

```
nand = $(if $1,$(if $2,,T),T)
nor= $(if $1$2,,T)
xor = $(if $1,$(if $2,,T),$(if $2,T))
```

# 10.5 Starting version 3.81+ built-in logical operator exists

```
nativeAnd := $(and T,T)
nativeOr := $(or T,T)

ifneq($(nativeAnd),T)
and = $(if $1,$(if $2,T))
endif

ifneq($(nativeOr),T)
or= $(if $1$2,T)
endif

$(info test it for T: $(call and,T,T))
```
- output 

```
test it for T: T
```

# 11. Command detection
- Say if we need to see if curl command is supported

```
HaveCurl := $(shell which curl)

ifndef HaveCurl
$(error curl is missing)
else
   $(info has curl installed)
endif
```
- output

```
> make
has curl installed
```

## 11.1 wrapping command detection with functions

```
assertCommandPresent = $(if $(shell which $1),,$(error '$1' missing required))

$(info $(call assertCommandPresent,wget))
$(info $(call assertCommandPresent,curly))  # this will throw error 
```

- output (first line is blank - since wget exists in the system)

```

Makefile:46: *** 'curly' missing required.  Stop.
```

## 11.2 To make the user function to check command execute only for that target

```
assertCommandPresent = $(if $(shell which $1),,$(error '$1' missing required))

all: ; @echo "starting all target"

download: export _check = $(call assertCommandPresent,curly)
download: ; @echo "download target"
```

- output

```
> make all
starting all target

> make download
Makefile:50: *** 'curly' missing required.  Stop.
```

# 13. Delayed variable assignment

 - `:=` operator (this evaluates the right side immediately, uses the resluting value to the variable)
 - `=` recursive operator ()
 
```
BAR = before
FOO := $(BAR) then rain
BAR = after

$(info $(FOO))  # immideate evaluation happened in :=

BAR = before
FOO = $(BAR) then rain
BAR = after

$(info $(FOO))  # lazy or delayed evaluation happened in =
```
- Output

```
> make
before then rain
after then rain
```

# 13. defining a constant value in variable
- Say if we need to compute a value once and keep it fixed 

```
SHALIST = $(shell find . -name '*.txt' | xargs shasum)
```
 - The SHALIST contains the name and SHA1 crytpographic has of evey .txt
 - if we use `=` in here and invoke at multiple places this will slow down, better to use `:=` like below
 
```
SHALIST = $(eval SHALIST := $(shell find . -name '*.txt' | xargs shasum))$(SHALIST)
```
- if $(SHALIST) is ever evaluated, the $(eval SHALIST := $(shell ....)) part gets evauated.
- Because `:=` is being used here.

```
SHALIST = $(eval SHALIST := $(shell find . -name '*.txt' | xargs shasum))$(SHALIST)

$(info before $(value SHALIST))
$(info SHALIST is $(SHALIST))
$(info after $(value SHALIST))
```

- output ( there were two txt file in the same folder)

```
before $(eval SHALIST := $(shell find . -name '*.txt' | xargs shasum))$(SHALIST)
SHALIST is 4b3a329a9b4a3f9867674679322a719770f2c0b0  ./Makefile_eval.txt
after 4b3a329a9b4a3f9867674679322a719770f2c0b0  ./Makefile_eval.txt
```

# 14 simple list manipulation

 - `$(firstword)` - get the first workd in a list
 - `$(words)` - counts the number of list elements
 - `$(word)` - extract the word at a specific index (couting from 1)
 - `$(wordlist)` - Extract a range of words from a list
 - `$(foreach)` - used to iterate over a list
 
```
LIST = a simple example of list
$(info first word in LIST is $(firstword $(LIST))))
```
- output
```
first word in LIST is a
```

```
lastword = $(if $1,$(word $(word $1),$1))
LIST= example program to check
$(info the last word in LIST $(call lastword,$(LIST)))
```
  - not in 3.81+ there is a `lastword` built-in function 
  
```
#$(wordlist, S , E ,LIST)

notFirst = $(wordlist 2,$(words $1),$1)
LIST = Simple sentence for example
$(info $(call notFirst,$(LIST)))
```
- output
```
sentence for example
```

```
notlast = $(wordlist 2,$(words $1),dummy $1)
LIST=take the first not the last
$(info $(call notlast,$(LIST)))
```
- output
```
take the first not the
```

# 15. user-defined function

```
make_date = $1/$2/$3

today := $(call make_date,12,5,2022)  # output: 12/5/2022
```
 - `$(0)` special argument contins the name of the function. (in above it is `make_date`)
 - `$(origin $1)` using the origin on any argnent of the function will return `automatic` like `$@`

```
unix_to_dos = $(subst /,\,$1)
```
- make does very little escaping 

```
swap = $2 $1

$(info $(call swap,first,argument,second)) # in this case make doesn't know first,argument is first value. it will assume just first as 1 argument and rest second(in this case argument,second)

# to overcome it

FIRSTARG := first,argument

SWAPPED := $(call swap,$(FIRST),second)

# approach 2

c := ,
SWAPPED := $(call swap,first$cargument,second)

# approach 3

, := ,
SWAPPED := $(call swap,first$(,)argument,second)
```

# 16. Call built-in function

```
$(call info,message)

# create map function from functional programming 
# to apply that to every memember of a list, like below
map = $(foreach a,$2,$(call $1,$a))
```

```
print_variable = $(info $1 ($value $1) -> $($(1)) )

print_variables = $(call map,print_variable,$1)
VAR1 = foo
VAR2 = $(VAR1)
VAR3 = $(VAR2) $(VAR1)

$(call print_variables,VAR1 VAR2 VAR3)
```

- Additional options in 3.81+
   - .FEATURES
   - .SECONDEXPANSION 
          - the issue with make is that the automatic variables are valid and assinged only when a rule command are run, they are not valid part of the rule definition.
          - example it is not possible to write , `foo: $@.c` to mean that `foo` should be made from `foo.c`. Though `$@` has the `foo` when the rule command executed. (after 3.81+, this is can be obtained with `$$@`) but in order to enabled this functionlity we need to define `.SECONDEXPANSION`
          - automatic variable like $$ will always be blank because they cannot be comupted when makefile being parsed.
          ```
          .SECONDEXPANSION:
          
          FOO = foo
          all: $$(FOO)
          all: ; @echo Making $@ from $?
          
          bar: ; @echo Making $@
          
          FOO = bar
          ```
          -- output
          ```
          > make
          Making bar
          Making all from bar
          ```
          - NOTE if .SECONDEXPANSION is enabled, using any file name with `$` will need to be escaped by writting `$$`
    - .INCLUDE_DIRS - contains the list of directory that make will search when looking for makefiles that are included using the include directive. (can be modified using -I command line option)
          - example, running `make -I /user/foo` on linux with following make file 
          ```
          $(info $(.INCLUDE_DIRS))
          all: ; @true
          ```
          - output
          ```
          /usr/foo /usr/local/inclure /usr/local/include..
          ```
    - .DEFAULT_GOAL
    - MAKE_RESTARTS - variable is the count of the number of times that make has restarted.
    - new function 
       - $(info text)
       - $(lastword LIST)
       - $(flavor VAR) - returns the flavor of variable (eirther recursive for recursively expanded or simple)
           ```
             REC= foo
             SIM := foo
             $(info REC is $(flavor REC))
             $(info SIM is $(flavor SIM))
             
             all: ; @true
           ```
        - $(or arg1,arg2..) & $(and arg1...)
        - $(abspath DIR)
          ```
           $(info $(abspath foo/.////////bar))
           
           all: ; @true
          ```
        -$(realpath DIR)
        
    - `$?` - automatic variable includes the name of all prequisites to a target caused a rebuild, even if they do not exst.
    - $(wildcard) function had always returned sorted list of files, so after 3.82 we need to use $(sort) wrapped over the $(wildcard) like `$(sort $(wildcart *.txt))
     - to use a = sign in a target define it first
     ```
     eq := =
     
     all: odd$(eq)name
     odd%:  @echo Make $@
     ```
     - after 3.82 variable name cannot contain any space. (note name not value)
     
     - new `--eval` command line option causes make to run its argument through $(eval) before parseing make files. 
     ```
     # using make --eval=FOO=bar
     
     all: ; @echo Foo has value $(FOO)
     ```
     - output
     ```
     Foo has value bar
     ```
     
     Guile function are supported in 4.0 version
     
     ```
     $(if $(guile (access? "foo.c" R_OK)), $(info foo.c exists))
     ```
     
     - `$(file)` function 
     ```
     LOG = make.log
     
     $(file > $(LOG),Start)
     
     all: part-one part-two
     
     part-one: part-three
        @$(file >> $(LOG),$@)
        @echo Make $@
        
     part-three
        @$(file >> $(LOG),$@)
        @echo Make $@
     ```
     

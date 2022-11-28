makefile:

Accessing environment variable

#1 

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

# 2.1

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

# 3: Set variables from outside make file

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
  
# 6 The $(shell) environment
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

# 7.1 - Pattern specific variable

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
 

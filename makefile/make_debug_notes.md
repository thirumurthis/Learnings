## make file debug options 

## 1. To print the value of the variable

```
Animal=$(C)$(D)
Wild=horse
C=Cat
D=Dog

print-%: ; @echo $* = $($*)
```

- Output:
```
$ make print-Animal
Animal = CatDog

$ make print-C
C = Cat

$ make print-C C=Cloth
C = Cloth
```

## 1.1 - With origin

```
Animal=$(C)$(D)
Wild=horse
C=Cat
D=Dog

print-%: ; @echo $* = '$($*)' from $(origin $*)
```
- Output

```
$ make print-C C=Cost
C = Cost from command line

$ make print-C
C = Cat from file
```

## 1.2 - To dump all variables

```
Animal=$(C)$(D)
Wild=Horse
C=Cat
D=Dog
print-%: ; @echo $* = '$($*)' from $(origin $*)

.PHONY: printvars
printvars:
        @$(foreach V,$(sort $(.VARIABLES)), \
        $(if $(filter-out environ% default automatic, $(origin $V)), \
        $(info $V=$($V) ($(value $V)))))
```

- output:
```
$ make printvars
.DEFAULT_GOAL=printvars (printvars)
Animal=CatDog ($(C)$(D))
C=Cat (Cat)
CURDIR=/cygdrive/c/thiru/learn/gnu_make/makeexample3 (/cygdrive/c/thiru/learn/gnu_make/makeexample3)
D=Dog (Dog)
GNUMAKEFLAGS= ()
MAKEFILE_LIST= Makefile ( Makefile)
MAKEFLAGS= ()
Wild=Horse (Horse)
make: Nothing to be done for 'printvars'.
```

## 1.3 Tracing variables

```
Animal=$(C)$(D)
Wild=Horse
C=Cat
D=Dog

print-%: ; @echo $* = '$($*)' from $(origin $*)

ifdef TRACE
.PHONY: _trace _value
_trace: ; @$(MAKE) --no-print-directory TRACE=$(TRACE)='$$(warning TRACE $(TRACE))$(shell $(MAKE) TRACE=$(TRACE) _value)'
_value: ; @echo '$(value $(TRACE))'
endif
```
- output

```
$ make TRACE=C
make: *** No rule to make target '_value'.  Stop.
Makefile:10: TRACE C
Makefile:10: TRACE C
Makefile:10: TRACE C
make[1]: *** No rule to make target '_value'.  Stop.
make[1]: TRACE C
Makefile:10: TRACE C=
Makefile:10: TRACE C=
Makefile:10: TRACE C=
make[2]: *** No rule to make target 'Entering'.  Stop.
make[2]: TRACE C=
make[3]: *** No rule to make target 'Entering'.  Stop.
Makefile:10: recipe for target 'trace' failed
make[2]: *** [trace] Error 2
Makefile:10: recipe for target 'trace' failed
make[1]: *** [trace] Error 2
Makefile:10: recipe for target 'trace' failed
make: *** [trace] Error 2
```

 - The tracing works with the special `$(warning)` function that outputs a warning message to `STDERR` and returns the empty strnt.
 
 So everytime the tracer code changes the `C=Cat` to `C=$(warning TRACE C)Cat`
 
 The _trace is the first target encountered, so this rule runs by default. The `$(shell)` in the _trace rule returns the make file with different goal. if we trace C, for exampel this $(shell) runs the command: `make TRACE=C _value`, the _value rule will run as defined.
 
 ## 2. defining target specfic variable
 
 ```
 all: FOO=foo
 all: a
 all: ; @echo $(FOO)
 
 a: ; @echo $(FOO)
 ifdef TRACE
.PHONY: _trace _value
_trace: ; @$(MAKE) --no-print-directory TRACE=$(TRACE)='$$(warning TRACE $(TRACE))$(shell $(MAKE) TRACE=$(TRACE) _value)'
_value: ; @echo '$(value $(TRACE))'
endif
 ```
 
 -output  (Note below is not the expected output, additional targets are getting executed)
```
$ make TRACE=FOO
Makefile:11: TRACE FOO
Makefile:11: TRACE FOO
Makefile:11: TRACE FOO
make[1]: TRACE FOO
Makefile:11: TRACE FOO=
Makefile:11: TRACE FOO=
Makefile:11: TRACE FOO=
make[2]: *** No rule to make target 'Entering'.  Stop.
make[2]: TRACE FOO=
make[3]: *** No rule to make target 'Entering'.  Stop.
Makefile:11: recipe for target '_trace' failed
make[2]: *** [_trace] Error 2
Makefile:11: recipe for target '_trace' failed
make[1]: *** [_trace] Error 2
Makefile:11: recipe for target '_trace' failed
make: *** [_trace] Error 2
```
NOTE: in GNU 4.0 we have the `--trace` option part of the make

## 3. Shell usage

- to enhance the output of GNU make we can redefine the `SHELL`, this is a built in variabel 
tat contains the name of the shell to use when make executes


```
SHELL += -x

.PHONY: all
all: bar

bar: ; @touch $@
```
- output

```
$ make all
+ touch bar
```

## 3.1 Addition Shell usage

```
OLD_SHELL := $(SHELL)

SHELL = $(warning Building $@)$(OLD_SHELL)

.PHONY: all
all: bar door
        @echo "all executed"

bar: ; @touch $@

door: ; @echo "door executed"
```

- output (Note, make doesnt' display the targe which updates the file in this case bar will be executed only for the first time)
```
$ make all
Makefile:11: Building door
door executed
Makefile:7: Building all
all executed
```

- ## 3.2 combining $(warning) and -x

```
OLD_SHELL := $(SHELL)

SHELL = $(warning Building $@$(if $<, (from $<)) $(if $?, ($?, newer)))$(OLD_SHELL) -x

.PHONY: all
all: bar door
        @echo "all executed"

bar: ; @touch $@

door: ; @echo "door executed"
```

- output:

```
$ make
Makefile:12: Building door
+ echo 'door executed'
door executed
Makefile:7: Building all (from door)  (door targ1, newer)
+ echo 'all executed'
all executed
```

## 4. assertion (from different library)

- assert function will ouput fatal error if its first argument is false
- assert is not a built-in function a user defined in the GMSL makefile

```
include gms1

FOO := foo
BAR := foo

$(call assert,$(call sne,$(FOO),4(BAR)),FOO and BAR should not be equal)
```

## interactive debugging using _BREAKPOINT

- `-d` in make provides detaisl but not much useful info
- `-p` option prints the internal data base rules.

below is supported from 3.80

```
VAR1 = hello
VAR2 = $(VAR1) people
all: VAR3 = $(VAR2)

all: foo bar
	$(__BREAKPOINT)
	@echo Making $@
foo bar:
	@echo building $@
```

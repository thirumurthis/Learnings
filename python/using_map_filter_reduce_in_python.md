
- Lambda in Python can only take expressions.

`lambda <parameter> : <body of the lambda>`

- filter, map are high order function, which are lazily evaluated.
- filter returns an iterable, when this is passed to list the iterable is evaluated and the output is printed.

##### using filter's
```py
nums = [10, 3, 7, 1, 9, 4, 2]
list(filter(lambda x: x%2 != 0 , nums))
# output: [3, 7, 1, 9]
```

##### using map's
```py
nums = [10, 3, 7, 1, 9, 4, 2]
list(map(lambda x: x **2, nums))
# output: [100, 9, 49, 1, 81, 16, 4]

```

#### mixing both filter and map

```py

## map(<lambda expression> , <lambda expression>)
### the nested operations performed first filter returns iterator that is passes as prameter to map
### map returned iterator is evaluated in the list by passing it as argument
list(map(lambda x: x**2, 
         filter(lambda x: x%2 != 0, nums)))
# output: [9, 49, 1, 81]         
```

### `functools` module has `reduce` function - this can also be used to reduce the input 
 - refer the functools docmentation


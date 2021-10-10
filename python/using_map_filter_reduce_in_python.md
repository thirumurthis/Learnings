
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

---------------

```py
# comparision

# below prints True
'Red' < 'orange'

# output True
# this is because the ascii of R is greater than o

## to check the ordinality of char to return the Ascii number

ord('R')

print(ord('R'),ord('o'),end= '  ')
# output 82 111

## To find the min of the string list
colors = ['Red','orange','Blue','green']

## note the key param mapped to lambda to perform lower case conversion (so provides alphabetically since its lowercase)
min(colors, key=lambda s: s.lower())

# output : Blue
```

### `reversed()` returns the iteratable and lazily executed
```py
numbers = [9,12,7,4,57,10]
reversed_num = [ item **2 for item in reversed(numbers)]
reversed_num
#output : [100, 3249, 16, 49, 144, 81]
```

### `zip` - takes multiple input sequence and combine them to tuple

```py
names=['Tim','Kim','Tom']
grades = [4,2,5]
## using zip will map Tim to 4, Kim to 2, etc as tuple
for name,gpa in zip(names,grades):
    print(f'Name={name}: GPA:{gpa}')

# output
"""
Name=Tim: GPA:4
Name=Kim: GPA:2
Name=Tom: GPA:5
"""
## the list can contain any number of arguments, not required that both list to be matched 
## zip stops when the shortest list is completed.

names=['Tim','Kim','Tom']
grades = [4,2,5,10,12]
## using zip will map Tim to 4, Kim to 2, etc as tuple
for name,gpa in zip(names,grades):
    print(f'Name={name}: GPA:{gpa}')
    

# output
"""
Name=Tim: GPA:4
Name=Kim: GPA:2
Name=Tom: GPA:5
"""
```

### two dimensional list
```
# representation nums = [[0,1,1],[1,0,1],[1,1,0]]
# accessing using nums[0][1] , nums[1][2]
 
nums2d = [[0,1,1],[1,0,1],[1,1,0]]
# """
# representation of the data
# 0 1 1
# 1 0 1
# 1 1 0
# """
## print the data in diagonal 2d array
for i in range(len(nums2d)):
    print(nums2d[i][i], end=' ')
```
Below libraries provides arrays which can be used for create 1 or 2 dimensional array.
- pandas
- numpy 

### Generator expression is similar to List comprehensions, but with performance improvement.
 - Generator expressions uses `()` unlike list comprehensions which uses `[]`.
 - The Generator expressions performs lazy execution

```py
## 
for item in (x for x in range(1,25) if x%2 == 0 ):
    print(item, end =' ')
    
gen_expression = (x for x in range(1,25) if x%2 == 0 )

gen_expression
## OUTPUTS: <generator object <genexpr> at 0x0000021755FFC820>

inputs = [10,3,7,1,9,4,2]
### triple = list((x**3 for x in inputs if x%2 == 0))  # no need for nested generator expression when passing to list
triple = list(x**3 for x in inputs if x%2 == 0)    
```

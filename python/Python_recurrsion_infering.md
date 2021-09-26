```python
def tri_recursion(k):
  if(k > 0):
    result = k + tri_recursion(k - 1)
    print(result)
  else:
    result = 0
  return result

print("\n\nRecursion Example Results")
tri_recursion(6)
```
- Output: 1 , 3 , 6, 10, 15, 21

```
6 + t(5)   = 6 + 15 = 21
     5 + t(4)  = 5 + 10 = 15
           4 + t(3)  =  4+ 6 =10 
                 3 + t(2)  = 3+ 3 =6
                      2 + t(1) = 2+1= 3
                           1 + t(0)= 1+0 = 1
```

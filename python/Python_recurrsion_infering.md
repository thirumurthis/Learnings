```py
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

------------------------
#### problem to find the sum of digits 

```py
def sumdigit(n):
    # base condition
    if n < 10:
        return n
    else:
        digitExceptLast = n // 10
        digitLast = n % 10
        return digitLast + sumdigit(digitExceptLast)
 
 print(sumdigit(14093))  
 # outputs 17
```
```
for input 123
       3 + S(12)             = 3 + (2 + 1) = 6 
           S(12) = 2 + S(1)  = 2 + (1) = 3
                       S(1)  = 1
            
   
```

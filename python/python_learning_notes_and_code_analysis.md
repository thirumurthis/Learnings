Below are list of code analyzer in python

- Pylint
- flake8
- mccabe
- pyflakes
- pep8

usually installed using `pip install <package>`, once installed use `pyflakes python-file-to-check.py`

## Scopes

### Function arguments
- avoid using mutable objects as default parameters in a function.
```py
def spam(key, value, list_=[], dict_={}):
    list_.append(value)
    dict_[key] = value
    print('List: %r' % list_)
    print('Dict: %r' % dict_)
spam('key 1', 'value 1')
spam('key 2', 'value 2')
```
-  **Expected: output **
```
List: ['value 1']
Dict: {'key 1': 'value 1'}
List: ['value 2']
Dict: {'key 2': 'value 2'}
```
 - **Actual: Output **
```
List: ['value 1']
Dict: {'key 1': 'value 1'}
List: ['value 1', 'value 2']
Dict: {'key 1': 'value 1', 'key 2': 'value 2'}
```
 - In this case better option is to use `None`
```py
def spam(key, value, list_=None, dict_=None):
    if list_ is None:
       list_ = []
    if dict_ is None:
       dict_ {}
    list_.append(value)
    dict_[key] = value
```
### Class properties Scopes
 - in below code, like functional arguments the list and dictionaries are shared in the class
 - A better alternative is to initialize the mutable objects within the __init__ method of the class.
```py
class Spam(object):
    list_ = []
    dict_ = {}
    def __init__(self, key, value):
        self.list_.append(value)
        self.dict_[key] = value
        
        print('List: %r' % self.list_)
        print('Dict: %r' % self.dict_)
        
Spam('key 1', 'value 1')
Spam('key 2', 'value 2')
```
 - **Actual Output:**
```
List: ['value 1']
Dict: {'key 1': 'value 1'}
List: ['value 1', 'value 2']
Dict: {'key 1': 'value 1', 'key 2': 'value 2'}
```
- Better approach is 
```py
class Spam(object):
    def __init__(self, key, value):
       self.list_ = [key]
       self.dict_ = {key: value}
       
       print('List: %r' % self.list_)
       print('Dict: %r' % self.dict_)
```

### Inheriteance
- When inheriting, the original class properties will stay unchanged even in subclass (unless overwritten)

```py 
class A (object):
   spam =1
   
# class inheriting A
# pass is just used to not perform any action, a class without any definition will throw error
class B(A):
   pass
 
 # spam is inherited from parent class
 >>> A.spam
 1
 >>> B.spam
 1
 
 # assing different value to spam
 >>> A.spam=10
 >>> A.spam
 10
 >>> B.spam
 10
```
 - Note: From above code, we modified A.spam, but this impacted B.spam. 
    - This can be solved, either by defining spam in each class sperately
    - If it is to be modifiable it is better to put it in an instance variable instead. Like `__init__(self,spam):..`

### Modifying variables in global scope
```py
spam =1
def eggs():
    print(f"spam {spam}")

eggs()

def eggs1():
    spam +=1
    print(f"spam {spam}")

eggs1()
"""
  ** Throw below error **
  Traceback (most recent call last):
  File "c:\thiru\learn\python\spark-program\python_example\SimpleSpam.py", line 25, in <module>
    eggs1()
  File "c:\thiru\learn\python\spark-program\python_example\SimpleSpam.py", line 22, in eggs1
    spam +=1
UnboundLocalError: local variable 'spam' referenced before assignment
"""
```
 - Note: above code thorws exception because 
     - The problem is that spam += 1 actually translates to spam = spam + 1, and anything containing spam = makes the variable local to your scope
     - Since the local variable is being assigned at that point, it has no value yet and you are trying to use it. 
     - For these cases, there is the **global** statement, although I would really recommend that you avoid globals altogether.
 
```py 
spam =1
def eggs():
    print(f"spam {spam}")

eggs()

def eggs1():
    global spam   # since included declared as global more like a static keywork in java
    spam +=1
    print(f"spam {spam}")

eggs1()
## works now 
```
### Conventions: to 
 - in some case to avoid overwriting global functions. 
 - The PEP8 convention for naming your functions—similar to built-in statements, functions, and variables—is to use a trailing underscore.
 ```py 
 list = [1,2,3]
 # instead of above, use something like below
 list_ = [1,2,3]
 
import = 'Some import'
'''
Traceback (most recent call last):
...
SyntaxError: invalid syntax
'''
```

## Modifying while iterating
  - while iterating through mutable objects such as lists, dicts, or sets, you cannot modify them
  - below code example will throw `RuntimeError`
```py
dict_ = {'spam': 'eggs'}
list_ = ['spam']
set_ = {'spam', 'eggs'}

for key in dict_:
   del dict_[key]
   
for item in list_:
   list_.remove(item)
   
for item in set_:
    set_.remove(item)
```
 - This can be **avoided** by copying the object. The most convenient option is by using the list function
```py 
dict_ = {'spam': 'eggs'}
list_ = ['spam']
set_ = {'spam', 'eggs'}

for key in list(dict_):
   del dict_[key]
   
for item in list(list_):
   list_.remove(item)
   
for item in list(set_):
   set_.remove(item)
```

## Exception handling:
  - in below code, teh use of `except ValueError as exception:` sets the exception as local scope, but the return exception doesn't have visiblity
```py
def spam(value):
   try:
      value = int(value)
   except ValueError as exception:
       print('We caught an exception: %r' % exception)
   return exception
   
spam('a')
'''
  There will be an exception 
  We caught an exception: ValueError("invalid literal for int() with base 10: 'a'")
Traceback (most recent call last):
  File "c:\thiru\learn\python\spark-program\python_example\SimpleSpam.py", line 37, in <module>
    spam('a')
  File "c:\thiru\learn\python\spark-program\python_example\SimpleSpam.py", line 35, in spam
    return exception
UnboundLocalError: local variable 'exception' referenced before assignment
'''
```
 - the above scenrario can be handled better with below approach
```py
def spam(value):
     exception = None   # local variable with the scope now visible
     try:
        value = int(value)
     except ValueError as exception:
          print('We caught an exception: %r' % exception)
          
     return exception
 
 spam('a')
 
 ## this works now 
```
- Another point to note:
   - We really need to save it explicitly because Python 3 automatically deletes anything saved with as variable at the end of the except statements. 
   - The reason for this is that exceptions in Python 3 contain a __traceback__ attribute. 
   - Having this attribute makes it much more difficult for the garbage collector to handle as it introduces a recursive self-referencing cycle 
   - To solve this, Python essentially does the following
   ```
   exception = None
    try:
      value = int(value)
    except ValueError as exception:
    try:
      print('We caught an exception: %r' % exception)
    finally:
      del exception
   ```
 - Better to use the below approach to allow variable to garbage collect
```py
def spam(value):
   exception = None
   try:
       value = int(value)
   except ValueError as e:
       exception = e
       print('We caught an exception: %r' % exception)
       
   return exception
```
 - The Python garbage collector is smart enough to understand that the variables are not visible anymore and will delete it eventually, but it can take a lot more time.

## Late Binding - `Closuer` __are a method of implementing local scopes in code__.
  - Closuers makes it possible to locally define variables without overriding variables in the parent (or global) scope and hide the variables from the outside scope later.
  - The problem with closures in Python is that Python tries to bind its variables as late as possible for performance reasons.
```py
nums = [lambda a: i * a for i in range(3)]

for num in nums:
   print(num(5))
```
 - Expected result:
```
0
5
10
```
 - Actual output:
```
10
10
10
```
 - similar to how class inheritance works with properties. Due to late binding, the variable i gets called from the surrounding scope at call time, and not when it's actually defined.
 - To solve the above issue , `the variable needs to be made local.`

 #### To perform immediate binding we can curry the function with partial
 
```py 
import functools
nums = [functools.partial(lambda i, a: i * a, i) for i in range(3)]

for num in nums:
   print ( num(5))
```
-  **A better solution would be to avoid binding problems altogether by not introducing extra scopes (the lambda), that use external variables.**
-  **IMPORTANT **: in the above lamba if both i and a were specified as arguments to lambda, this will not be a problem.

### Circular imports 
```py 
# file name eggs.py:
from spam import spam
def eggs():
     print('This is eggs')
     spam()

# file name spam.py:
from eggs import eggs
def spam():
     print('This is spam')

if __name__ == '__main__':
   eggs()
```
 - Above code will throw exception, this is because spam() imports
 - This issue requires restructuring the code, but in this case we can simply use module name to access the function

```py 
# file name eggs.py:
from spam import spam
def eggs():
     print('This is eggs')
     spam.spam()  # use the module imported 

# file name spam.py:
from eggs import eggs
def spam():
     print('This is spam')

if __name__ == '__main__':
   eggs()
```
  - Another alternate solution is to move the import within the function itself
```py
# file name eggs.py:
def eggs():
    from spam import spam    # import statement local to function
    print('This is eggs')
    spam()

# file name spam.py:
def spam():
    from eggs import eggs
    print('This is spam')

if __name__ == '__main__':
eggs()
```
 - Other possible approaches are `Dynamic imports` which is not recommended since they check the dependency at runtime.

## import collision
  - Most case if we use `from <module> import <package>` would solve.
  - So always the current package will loaded than the global package.




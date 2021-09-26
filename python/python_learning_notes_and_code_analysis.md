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
### Class properties
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

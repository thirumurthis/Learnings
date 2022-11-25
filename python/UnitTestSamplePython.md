

```py

class Operation :

    def __init__(self,operation,data=[]):
        self.operation = operation
        self.data = data

    def operate(self):
        print (self.operation)
        result = 1;
        if self.operation == "*" :
            result =1;
        elif self.operation == "+":
            result =0;
        else:
            raise(ValueError("operation not supporter"))

        for item in self.data:
            print(item,end=", ")
            if self.operation == "*" :
                result = result * item;            
            elif self.operation == "+" :
                result = result + item;
        return result
```

```py
import unittest
from SimpleProgram import Operation
from unittest.mock import Mock
from unittest.mock import MagicMock

class TestSimpleProgram(unittest.TestCase):

   def setUp(self):
      print("initalize...")
      pass

   def tearDown(self):
      print("cleanup")
      pass

   def test_Operation(self):
      operation = Operation("+",[1,2,3,4,5,6,7,8])
      add = operation.operate()
      self.assertEquals(add,(8*9)/2)

   def test_Operation_invalid(self):
      operation = Operation("/",[2,342,3])
      with self.assertRaises(ValueError) : 
         # TypeError, etc.
         operation.operate()
         
   def test_Operation_operate():
      operation = Operation("*",[2,3])
      operation.operate = MagicMock()
      operation.operate()
      operation.operate.assert_any_call()
  
# to execute the test case with python executable directly

if __name__ == "__main__":
     unittest.main()
```

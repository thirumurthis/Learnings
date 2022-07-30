
```py
input = "data-reports/123456abc/test.txt"
fn = input.split('/')[-1]
basepath= input.split('/')[-2]
print(f'file_name = {fn}')
print(f'base_path = {basepath}')


def getInfo (fn :str, filter_funct = None):
    if fn.endswith('/') or (filter_funct and not filter_funct(fn)):
        print ('specified file not found')
    else:
        print(f'file name matched :- {fn}')
   
def filter_func(n):
    return n.endswith("test1.txt")
    
getInfo(fn,filter_func)
```

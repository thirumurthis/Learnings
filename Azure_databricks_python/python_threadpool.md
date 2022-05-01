## using multi threaded example using python

 - in the databricks
```py
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime
```

```py
# Allow multiple requests be processed (within the resource limitation)
# pass as list of items
def process_requests(requests):
  # fixed number of capacity: 3 concurrent processes
  pool_num = 3
  
  futures = [] 
  results = [] 
  
  with ThreadPoolExecutor(pool_num) as pool:
    i = 1    
    # fork out the jobs/process that are going to run
    for request in requests:
        #  pass the function as argument.
        futures.append(pool.submit(process_function, i, request))
        i = i + 1

    # join all forked jobs
    for future in futures: 
      if future.result() is not None:
        results.append(future.result())
      else: 
        results.append(None)
  # 
  print("Time: {}. All jobs {} are completed.".format(datetime.utcnow(), results))
```

```py
# Asynchronous operation to process a request
def process_funct(num, request):
  print("Job Num: {}. Time: {}. Start job for request {}\n".format(num, datetime.now(), request))
  ## 
  ##   INVOKE ANY PROCESS IN HERE TO BE DONE.. whl library can also be used
  ##
  return num
```

### Finally invoke the ThreadpoolExecutor in loop
```py
for i in range(1,5):
  process_requests([item for item in range(0, i)])
```

###  output:

```
Job Num: 1. Time: 2022-05-01 06:17:40.366830. Start job for request 0

Time: 2022-05-01 06:17:40.367039. All jobs [1] are done.
Job Num: 1. Time: 2022-05-01 06:17:40.367341. Start job for request 0

Job Num: 2. Time: 2022-05-01 06:17:40.367577. Start job for request 1

Time: 2022-05-01 06:17:40.368928. All jobs [1, 2] are done.
Job Num: 1. Time: 2022-05-01 06:17:40.369178. Start job for request 0
Job Num: 2. Time: 2022-05-01 06:17:40.369343. Start job for request 1


Job Num: 3. Time: 2022-05-01 06:17:40.369678. Start job for request 2

Time: 2022-05-01 06:17:40.370049. All jobs [1, 2, 3] are done.
Job Num: 1. Time: 2022-05-01 06:17:40.370291. Start job for request 0
Job Num: 2. Time: 2022-05-01 06:17:40.370449. Start job for request 1


Job Num: 3. Time: 2022-05-01 06:17:40.370814. Start job for request 2
Job Num: 4. Time: 2022-05-01 06:17:40.370870. Start job for request 3


Time: 2022-05-01 06:17:40.371313. All jobs [1, 2, 3, 4] are done.
```

### Example on how the list iterarot prints values 
```py
for i in range(1,5):
   out=[item for item in range(0, i)]
   print(out)
```

### output - of above for loop and list itertor
```
[0]
[0, 1]
[0, 1, 2]
[0, 1, 2, 3]
```

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
def process_function(num, request):
  print("Job Num: {}. Time: {}. Start job for request {}".format(num, datetime.now(), request))
  ## 
  ##   INVOKE ANY PROCESS IN HERE TO BE DONE.. whl library can also be used
  ##
  print()
  return num
```

### Finally invoke the ThreadpoolExecutor in loop
```py
for i in range(1,5):
  process_requests([item for item in range(0, i)])
```

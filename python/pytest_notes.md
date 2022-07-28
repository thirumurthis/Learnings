

pytest notes:

```
import pytest

import utils import get_spark_session, get_info_from_env

@pytest.fixture(scope="function")
def setup_data():
    // define the necessary info to setup the 
    spark_session = get_spark_session()
    info1 = "test"
    yeild (info1)
    spark_session.stop()
```

- test_example_cases.py

```
import tempfile
import os
import pytest

from utils import get_info_from_env

@pytest.mark.skipif(
   get_info_from_env() is None, reason = "skipe integeration test"
   )
class TestSample:
  def test_init(self):
    # // some code
     
 # using tempfile
 
 def testusageoffile:
    with tempfile.NamedTemporaryFile(prefix="test", suffix=".orc") as tmp_file:
      try:
         tmp_file.wirte(b'this is a test')
         tmp_base_name = os.path.basename(tmp_file.name)
         print(f"blob name :- {tmp_base_name}")
         tmp_file.seek(0)
         print (f"content : {tmp_file.read()}")
         # now add some cases
       except Exception as e:
         print(e)
```

- utils.py

```
import tempfile
import shutil
import os
from pathlib import Path

import findspark
findspark.init()
import pyspark

from pyspark.sql import SparkSession

def get_info_from_env():
  try:
    return os.environ['MYAPP']
  except KeyError:
    print("environment variable for the application ")
    return None

def get_spark_session():
   try:
      spark
      if not spark is None and isinstance(spark, SparkSession):
         return spark
   except NameError:
      return SparkSession.builder.appName("my-custom-app").getOrCreate()
```

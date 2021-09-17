
```
from sys import argv
import yaml
import pandas as pd
import numpy as np
import logging
pd.options.mode.chained_assignment = None
from datetime import datetime, timedelta
import json
import math
from sklearn.preprocessing import StandardScaler
from sklearn.externals import joblib

class SimpleRead():

# calling function 
 config = self.load_config_yaml('configuration_v1.yaml')

#above class is just for wrapping below piece of code is how a yaml file iread
 # Define a function to load YAML config file
    def load_config_yaml(self, config_path):
        '''
        '''
        with open(config_path, 'r') as stream:
            self.config = yaml.load(stream)
        return self.config 
        
        
```

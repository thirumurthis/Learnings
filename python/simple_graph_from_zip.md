```python
import pandas as pd
from dateutil import parser
import time
import datetime

df1=pd.read_pickle("/path/to/the/.pkl/file")
print(df1.head())   # prints few top records
df1["newDate"]= [datetime.datetime.strptime(d, "%Y-%m-%d %H:%M:%s+%f").timestamp() for d in df1['datetime']]
# above is converting the date from the dataframe object

df1 = df1.loc[df1.id==100]  # matches the id from dataframe

df1.index = df1["data_datetime"]
del df1["data_datetime"] # delete not needed for ploting

df1.sort_values(["data_datetime"], axist=0, ascending=True, inplace=True)
df1[['dataval']].plot(); # plotting using pandas, here we plot it
print('completed.');
```

#### Drawing the graph directly from the file location which has a png file, in jypter 

```py
from IPython.display import, display
from pathlib import Path

directory_path='/path/where/the/png/files are present/'
fileList = []
pathlist= Path(directory_path).glob('*.png')
for path in pathlist:
   # since the path is not a string
   path_as_str = str(path)
   fileList.append(path_as_str)

for filename in fileList:
   display(Image(filename=filename))
```

#### Extrcting information from csv file and performing aggreation count

```py
import json
import pandas as pd
import re
import os
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
from datetime import datetime
plt.style.use('ggplot')
df = pd.read_csv("~/output_data.txt", header=None,delimiter=',')
df.columns = ['orderid', 'productid','data']
uniqorderid = df.orderid.unique();
count =0
print ('orderid, count')
for item in uniqorderid:
    count =0
    for item1 in df['orderid']:
        if item1 == item :
            #print ('>>>> {}'.format(count))
            count = count+1
    print('{}, {}'.format(item,count))
uniqueproductid=df.productid.unique()
print(uniqueproductid)
df2=pd.read_csv("~/copare_with_different_source.txt", header=None, delimiter=",") # src order info
df2.columns = ['orderId']
uniqueorderid= df2.orderId.unique()
#print(uniqueorderid)
print ('orderId, count')
cnt =0
for order in uniqueorderid:
    cnt =0
    for srcorder1 in df['orderid']:
        if order == srcorder1 :
            cnt=cnt+1
    if cnt > 0 :
        print('{},{}'.format(order,cnt))
#        row1 = df.loc[df['orderid'] == order]
#       print('{},{}'.format(row1['orderid'],row1['data']))
        #if srcorder1 == order:
        #print (' map : {},{}'.format(order,df.loc[df['orderid'] == order]))
print("process completed...")


'''
sample csv file or text file content is 

orderid productid  data
01        20        120
'''
```

#### more sample on plotting
```py
from pathlib import Path
import pandas as pd
from dateutil import parser
import time
import datetime
import matplotlib.pyplot as plt
import matplotlib.dates as mdates


directory_as_str='~/plotData/3/'
fileList =[]
pathlist = Path(directory_as_str).glob('*.csv')   # collects all csv file
for path in pathlist:
    path_in_str = str(path)
    #print(path_in_str)
    fileList.append(path_in_str)

for filename in fileList:
    param_df = pd.read_csv(filename, delimiter=',')
    param_df['datetime'] = pd.to_datetime(param_df['datetime'])
    dval = filename.split('_')[2]
    lent = len(param_df)
    param_df.sort_values(by=['datetime'])

    fig, ax = plt.subplots(figsize=(14,6))
    param_df.plot('datetime', 'sensorflotvalue', ax=ax)
    ax.xaxis.set_major_formatter(mdates.DateFormatter('%H:%M:%S'))
    plt.title('DataName: {}, Length {}, sensor_id : {}'.format('SENSORX',lent,idval))
    plt.show()

print('completed...')

'''
sample csv input 
id,datetime,sensortalue
6,2018-11-11 12:05:23,1.9843745231628418
18,2018-11-11 12:05:22,1.9687495231628418
30,2018-11-11 12:05:21,1.9843745231628418
42,2018-11-11 12:05:20,1.9843745231628418
54,2018-11-11 12:05:19,1.9999995231628418
66,2018-11-11 12:05:18,1.9843741655349731
78,2018-11-11 12:05:17,1.9687495231628418
90,2018-11-11 12:05:16,2.109374523162842
102,2018-11-11 12:05:15,2.203124523162842
114,2018-11-11 12:05:14,2.171874523162842
'''
```



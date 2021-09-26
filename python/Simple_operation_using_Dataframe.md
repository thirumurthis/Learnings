#### Jupyter notebook
```py
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
timeFmt = mdates.DateFormatter('%H:%M')
%matplotlib inline

import os
import re
import panda as pd

plt.style.use('ggplot')
```

```py
df = pd.read_csv('input_data.csv')
df = df[df['sensort_id'] == 100212.0]
print(len(df))
print(df['FAN_speed'].isna().sum())
df[~df['FAN_speed'].isna()].groupby('angle_or_phase').count()
```
```py
df.drop_duplicates().groupby('sensor_id').apply(len)
```
```py
df = pd.read_csv('input_data_2.csv', parse_dates=['time'])

parameter = 'fan_speed'

for key, grp in df.groupby('sensor_id'):
    print('Total data points: {}'.format(len(grp)))
    print('Null data points: {}'.format(grp[sensor].isna().sum()))
    grp = grp.drop_duplicates().dropna(subset=['time', sensor])
    grp.plot('time', sensor, figsize=(14,4), style='.', ms=0.5)
    plt.title("SensorID: {}  SensorStartTime: {}".format(key, grp['time'].min()))
    print(grp['time'].min(), grp['time'].max())
    plt.show()
```

```py
files = [f for f in os.listdir('.') if re.search('input.+\.csv', f)]

specific_id = 1000.0

for file in files:
    df = pd.read_csv(file, parse_dates=['time'])
    df = df[df['sensor_id'] == specific_id]
    df = df[['time', 'fan_speed']]
    num_row = len(df)
    if num_row > 0:
        print(file)
        print('Number of rows: {}'.format(len(df)))
        df = df.drop_duplicates().dropna()
#         fig, ax = plt.subplots(1,6)
        df.plot('time', 'fan_speed', style='.', ms=0.5)
        plt.show()
```

- The ipython editor can be started with matplotlib
```
> ipython --matplotlib
```
- We can also start the matplotlib even in an existing ipython session which is started using `ipython` command, using `magic` command.

```
## below % magic will enable to use matplotlib in the current session of iplython
%matplotlib
```
   - For Jypter notebook this would `%matplotlib inline`  (mostly import will work in this case)

-----------

```py
# use of plt alias is recommended by the documentation
import matplotlib.pyplot as plt

import numpy as np
import random

# sns alias
import seaborn as sns

## to plot the data 
title = f'Rolling six sided Die {len(rolls):,} Times'

## by default the seaborn will create whitebackground, 
## whitegrid will provide grid on white backgroung
sns.set_style('whitegrid')

## in case of ipython it will open a separate window
axes = sns.barplot(x=values,y=frequencies,palette='bright')

```


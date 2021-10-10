- The ipython editor can be started with matplotlib
```
> ipython --matplotlib
```
- We can also start the matplotlib even in an existing ipython session which is started using `ipython` command, using `magic` command.

```
## below % magic will enable to use matplotlib in the current session of ipython
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
- To work with the object returned
```

## axes object is returned and the is the window itself
axes.set_title(title)

axes.set(xlable='Die value', ylabel='frequencies')

## adding a ylim to add 110% space at the tap comparitive to the max bar
axes.set_ylim(top=max(frequencies)* 1.10)

## below peice of code will display the frequence and the % of frequence at each bar chart
## axes.patches -> is representation of the colours in the bar
## frequency:, -> the :, provides as many seprators
for bar, frequency in zip (axes.patches, frequencies):
    text_x = bar.get_x()+bar.get_width()/2.0
    text_y = bar.get_height()
    text = f'{frequency:,}\n{frequency / len(rolls):.3%}'
    axes.text(text_x,text_y,text,fontsize=11,ha='center',va='bottom')
```

![image](https://user-images.githubusercontent.com/6425536/136715532-ab9e32ea-00bc-4b26-aa68-5fe2573829d3.png)

-----
- to recall the # statement we can use `%recall #number-of-command-in-ipython-or-jupyter-notebook`
- `Cntr + r` = performs backward search

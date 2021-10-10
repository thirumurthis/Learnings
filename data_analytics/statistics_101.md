## Statistics
 
### Statistics - Measures of Central Tendency - Mean, Median and Mode
 - Mean - average
 - Median - middle value when value arranged in sorted order
 - Mode - the most frequently occuring value 
         - (exactly same number of occurance - no mode is found)
   
 
 Example: 
   - mean height of classes provided the list of heights in a class
   - most fequently purchased car brand (the mode) in a given country

Terminology:
 - `Population` - This a entire group of data is called.
 - `Sample` - a subset of the population of data.

### Statistics - Measure of dispersion 
   - Called measure of variability
   - Helps to understand how distributed the values are through out the range. Like the values are right or left side of the sectrum.
-  Two topics of measure of despersion:
  - `Variance` 
     - how to calculate the variance?
       - Find the mean of the data (average)
       - substract every/each data with the mean  (some will be positive or negative)
       - square each data will emphasizes on the outliers, mostly yields positive values. (squaring the difference)
       - finally sum the squared value data, to get the mean. This is called population variance (with the sample or complete set of data)
    Example: case to use variance
      - in case of credit card transaction, outlier may be fraud transaction,etc.
      - in some case the outliers can be ignored. These depends on the data itself
      
  - `Standard deviation` 
    - how to calculate the standard deviation?
      - This is the square root of variance
      - This tones down the effect of the outliers
      - this helps understanding how the data is spread around the mean.
    - Important observation:
      - smaller the value are for the variance and std devition, the closer to the mean.

### Using python for cacluating the `vairance` and `standard deviation`:
```py
import statistics

data_set=[1,3,4,2,6,5,3,4,5,2]

# population variance
statistics.pvariance(data_set)
#2.5

# this helps understanding how the data is spread around the mean.
statistics.pstdev(data_set)
# 1.5
```

 - `variance` vs `std. deviation`:
   - the units of `variance` is squared unit
   - the units of the `std. deviation` which is the sequre root of the variance which is the actual unit.


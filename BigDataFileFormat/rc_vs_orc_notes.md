RC - Row Columnar file (RC File)
ORC - Optimized Row columnar file (successor for RC file)

> Both are row store + column store

![image](https://user-images.githubusercontent.com/6425536/145654256-ba14c02d-b869-439b-96a4-23ce34b22f01.png)

For converting the row major data to RC file: 
RC file process,
   - First partitions each data horizontally (rows to some point)
   - Then it transforms the data chunk to columnar. 
 The Horizontal partition is called `Row Groups` 

![image](https://user-images.githubusercontent.com/6425536/145654305-466b0990-f93f-4561-a315-218c857b9307.png)


#### ORC Internals:

ORC file strucutre contains: [Link](https://cwiki.apache.org/confluence/display/hive/languagemanual+orc)
  - Stripes: which is the row data
     - Each Stripe contains 3 elements:
        - Index data
        - Row Data
        - Stripe footer - contains the meta data details about the stripe
  - File Footer: This contains the auxilary information lile list of stripes, number rows per stripe, each column's data type, column level aggregates, min, max and sum.
  - PostScript : Holds compression parameters and size of the compressed fotter at the end of the file.

Note: 
 - There will be onlye one `File Footer` and `Postscript` per ORC file.
 - There can be more than one Stripes per the ORC file.
 - The default size of the stripe is `250 MB`. This is large size to achive efficient reads from HDFS.

Details about the stripes:
 - Index Data contains:
     - min and max value of each column
     - row position within each column
 - Row Data: 
     - this is data for the indexed rows, basically group of rows for the mentioned row postitions in the `index data`
 - Stripe Footer:
     - contains directroy of stream location (serialized data)


```

| ID                 |  Name                   |   country               |  \
| (min=1, max=10000) |  (dictionary, min, max) |  (dictionary, min, max) |    ----------> Stripe index
|--------------------|-------------------------|-------------------------|  /
| 1                  |  tim                    |     USA                 |
...........
| 10000               |................ ........|........................| ------------------------> First 10,000 rows

| ID                 |  Name                   |   country               |  \
| (min=1, max=10000) |  (dictionary, min, max) |  (dictionary, min, max) |    ----------> Stripe index
|--------------------|-------------------------|-------------------------|  /
| 10001              |  Ram                    |     USA                 |
...........
| 20000              |................ ........|........................ | ------------------------> next 10,000 rows

...
...
```

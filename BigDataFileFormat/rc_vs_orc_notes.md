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

##### How does the where condition works in columnar store
   - From the snapshot below, the query checks for symbol RSC
      - In this case, the RC file doesn't decompress all the columns, it only decompress the Sybmol column in row group.
         - If the column has RSC, then it will decompress other columns.
         - If doesn't has the RSC it will NOT decompress other columns.
    - The above is called `Lazy Decompression` in RC file.
  
  Note: Where condition is called the `predicate`
  
![image](https://user-images.githubusercontent.com/6425536/145659628-bfabeaa8-c3d8-435d-b078-edd947a4e04c.png)

##### Disadvantages of RC file is it doesn't have any metadata about the column.
  - RC file basically did a horizontal partition and a vertical partition, to combine the row-major and column-major format.
  - RC file row group size is 4MB which is slower, higher the block size high read efficient.

#### ORC (Optimized Row Columnar) - from hortonworks

- Simple represetnation of the ORC file with elements (refer the notes below the screen shot for more details)
![image](https://user-images.githubusercontent.com/6425536/145660765-11f27e4a-4f7b-443e-89e5-f976d93c190d.png)

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

#### Simple physical representation of the ORC file 
  - Each stripe can only be 250 MB
```

| ID                 |  Name                   |   country               |  \
| (min=1, max=10000) |  (dictionary, min, max) |  (dictionary, min, max) |    ----------> Stripe index
|--------------------|-------------------------|-------------------------|  /
| 1                  |  tim                    |     USA                 |
...........
| 10000              |................ ........|........................| ------------------------> First 10,000 rows

| ID                 |  Name                   |   country               |  \
| (min=10000,        |  (dictionary, min, max) |  (dictionary, min, max) |    ----------> Stripe index
|   max=20000)       |                         |                         |
|--------------------|-------------------------|-------------------------|  /
| 10001              |  Ram                    |     USA                 |
...........
| 20000              |................ ........|........................ | ------------------------> next 10,000 rows

...
...
```

###### Below are configuration parameters: 
  (for example, id we need compression we can specify this formats or none, refer the above link for docs)

![image](https://user-images.githubusercontent.com/6425536/145658609-40309acf-1eb1-40cc-b672-bf88423516ed.png)

#### Serialization in the ORC 
   - The serialization in ORC depends on the data type. 
   - Serialization is uses in transfer of data from one node to another over network.
  - The serialization of Integer column is transfered as stream of integer values.
  - The serialization of String uses the `dictionary` mechanism
       - `dictionary` is used to form unique column values. And keeps only those unique values, duplicates not present. Look up will be faster.
       - The orc keeps a `dictionary` that is sorted to speed up predicate filtering. 
       - `dictionary` improves compression ratios.

##### Compression:
   - Streams are compressed using codel. (which is specified as a table property for all streams in table.)
   - compression is done incrementally as each block is produced, to optimize the memory use.
   - Codec can be Snappy, Zlib or none
   - Compressed blocks can be skipped over without having to be decompressed or scanning. Positions in the stream are reporesented by `block start location` and an `offset` into the block.

### Miscellaneous
##### How Index works in Database link 
   - Creating an index over the high-cardinality columns makes accessign a single row very fast (High-cardinality refers to columns with values that are very uncommon or unique)
   - Creating composite index on combined column also increase efficiency of query if we are using those column in query.
   - [Link](https://chartio.com/learn/databases/how-does-indexing-work/)



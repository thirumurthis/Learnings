RC - Row Columnar file (RC File)
ORC - Optimized Row columnar file (successor for RC file)

Reference to write ORC file in java [link](https://orc.apache.org/docs/core-java.html)

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
- Metadata about the column are also stored.
- The ORC file also performs horizontal and vertical partition to create the row and column foramt. 
       - In ORC the row group is called `Stripes` (stripe is horizontal partioned data)
       - In RC file the row group is 4MB, in ORC the `stripes` is 64 MB and above. (to take advantage of sequential reads)
- ORC maintains `file level statistics` (few index). `index in ORC is statistics about column` like min, max. 
   - File footer contains 
      - Stripe level index (The index (or statistics) will be at the individual stripe level)
      - File level index  (The index (or statistics) will be at the file level - combining all the stripes)
   - `Stripe` contains (more than one stripes can be in the ORC file)
     - Row level index: For every 10K rows there will be an index.
     - Then followed by the actual data.

- Simple represetnation of the ORC file with elements (refer the notes below the screen shot for more details)
![image](https://user-images.githubusercontent.com/6425536/145661815-f3379a44-f3ed-48d0-b4a4-f95dcc761626.png)

 - Say if we are firing a query `select * from table where volume > 100`.
    - when executing the query in hive environment 
       - First it looks at the `file level index` for the statistics of volume column, in this case the ORC file contains it so it will read it. If it is less than 100 no point in reading that file at all. [If the ORC file is 2G and we skip it, we no need to read the complete files. which provides better efficiency.]
       - Next it looks at the `stripe level index` for the statistics of volume column, since 100 is preset in stripe 1. so it will read data from that one and stripe 2.
       - Then check the `row level index` for the volume set of data to be read.

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

#### File Structure:

 - Break file into sets of row called Stripes
    - Default size is 250 MB
    - Large size enables efficient read of columns
 - Footer
    - contains list of Stripe location 
    - Self-described type
    - Count, min, max and sum of each column
 - postscript
    - Contains compression parameters
    - Size of compressed folder

#### Stripe Structure:

  - Data 
     - Composed of multiple stream per column
  - Index 
     - Required for skipping rows
     - Defaults to every 10,000 rows
     - Min and Max for each column 
  - Footer 
     - Directory of stream locations
     - Encoding of each column
      
![image](https://user-images.githubusercontent.com/6425536/145664122-650ad230-c4fe-44d1-915e-3f3a2dedc7e5.png)

##### Compound Type Serialization 

- List
    - Encode the number of items in each value
    - Uses the same run length encoding
    - Uses child writer for value
- Maps
    - Encode the number of items ¡n each value
    - Uses the same run length encoding
    - Uses the key and value child writers

#### Memory management

Managing Memory
  - The entire stripe needs to be buffered
  - ORC uses a memory manager
     - Scales down buffer sizes as number of writers increase
     - Configured using hive.exec.orc.memory.pool
     - Defaults to 50% of JVM heap size
  - It may improve performance to provide more memory to insert queries
     - Scaling down stripe size cuts disk IO size

Notes:
   - Metadata is stored using Protocol Buffers
        - Allows addition and removal of fields
   - Reader supports seeking to a given row #
   - Metadata tool (hive —service orcfiledump file.orc)
   - User metadata can be added at any time before the ORC writer is closed.

### Vectorization
  - Columnstore and vectorization are big break and adopter in DW DBMS
  - Vectorize inner loop 
     - operate on 1024 row batch at once
     - Batch column is a vector of a primitive type

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

### ORC file more info

![image](https://user-images.githubusercontent.com/6425536/145664565-8a5491ad-fb2f-4920-911f-003639113fbc.png)

![image](https://user-images.githubusercontent.com/6425536/145664578-b0a21157-5cea-4fe4-86ba-287a44043938.png)

#### FILE STRUCTURE: 
- The `file footer` contains:
   - Metadata — schema, file statistics
   - Stripe information — metadata and location of stripes
   - `Postscript` with the compression, buffer size, & file version
- ORC file data ¡s divided into stripes.
  - `Stripes` are self contained sets of rows organized by columns.
  - `Stripes` are the smallest unit of work for tasks.
  - Default is ~64MB, but often configured larger.

#### STRIPE STRUCTURE
  - Within a stripe, the metadata data is in the `stripe footer`.
     - List of streams
     - Column encoding information (eg. direct or dictionary)
  - Columns are written as a set of streams. There are 3 kinds:
     - Index streams
     - Data streams
     - Dictionary streams

![image](https://user-images.githubusercontent.com/6425536/145664824-0a99d848-50bc-4bb5-af04-0df6dc5e5168.png)

STREAMS
- Streams are an independent sequence of bytes
- Serialization into streams depends on column type & encoding
- Optional pipeline stages:
   - Run Length Encoding (RLE) — first pass integer compression
   - Generic compression — Zlib, Snappy, LZO, Zstd
   - Encryption - AES/CTR


### Miscellaneous
##### How Index works in Database link 
   - Creating an index over the high-cardinality columns makes accessign a single row very fast (High-cardinality refers to columns with values that are very uncommon or unique)
   - Creating composite index on combined column also increase efficiency of query if we are using those column in query.
   - [Link](https://chartio.com/learn/databases/how-does-indexing-work/)



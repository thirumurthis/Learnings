
##### Elastic Terminology with reference to Database.


| Elastic  |  Database relavent | Elastic Search descrption |
|---------|----------------|------------------|
| Data type| Data type | Applicable to document field. A type such as integer, text, keyword, date, range, geographical coordiantes, etc.|
| Document | Databae record | Json documents stored in Elastic search. Contains fields, and each field is of particular data type.|
| Type (*) | - | Deprecated in Es 7+. Previously, type was convinent way to group similar documents together.|
| Index  | Database table (preivously this was similar to Db, since type is deprecated. this is similar to **table**.| Index stores and manages documents of same type.|

**Node** - running instance of Elastic Search (ES).
**Cluster** - In Elastic Search single node is also a cluster. ES instance that search the same index.
**Shard/primary shard** - Part of an Index resides in one node of cluster.
**Replica/replica shard** - Addiational copy of shard stored on different node for high availablity.


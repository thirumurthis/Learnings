
Provide that we need to have access the DBMS_XPLAN table.

1. First query
  - Place the query within the bracket

```
explain plan for 
(query for which explain plan);

```

2. Execute the query plan from the DBMS_XPLAN.

```
select plan_table_output from table(dbms_xplan.DISPLAY());
```

Demo:

```
explain plan for
(select * from dual);
```
```
select plan_table_output from table(dbms_xplan.DISPLAY());
```
- Below is the explain plan:
- 
```
Plan hash value: 272002086
 
--------------------------------------------------------------------------
| Id  | Operation         | Name | Rows  | Bytes | Cost (%CPU)| Time     |
--------------------------------------------------------------------------
|   0 | SELECT STATEMENT  |      |     1 |     2 |     2   (0)| 00:00:01 |
|   1 |  TABLE ACCESS FULL| DUAL |     1 |     2 |     2   (0)| 00:00:01 |
--------------------------------------------------------------------------
```

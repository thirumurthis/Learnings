## The tables in the zitadel table

connect to the postgres pod

```
-- exec into the postgres pod
$ kubectl exec -i -t -n local db-postgresql-0 -c postgresql -- sh -c "clear; (bash || ash || sh)"
```

-- connect to the zitadel database using below command, when prompted use the user password xyz

```
/$ psql -U zitadel
```

- list of data base can be viewed using

```
/$ \l
```

- To change to database use

```
/$ \c zitadel
You are now connected to database "zitadel" as user "zitadel".
```

- Zitadel Tables that has the `current` tables

```
projections.current_sequences
notification.current_sequences
auth.current_Sequences
adminapi.current_sequences
```

- Failed events table `Failed` tables

```
projections.failed_events
notification.failed_events
auth.failed_events
adminapi.failed_events
```

- To list the tables that are not part of default postgres

```
SELECT * FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema';
```

- Below is the the list of tables that are owned by zitadel table owner

```
select * from pg_catalog.pg_tables where tableowner = 'zitadel';
```

Example output

```
zitadel=> select * from pg_catalog.pg_tables where tableowner = 'zitadel';
 schemaname  |             tablename             | tableowner | tablespace | hasindexes | hasrules | hastriggers | rowsecurity
-------------+-----------------------------------+------------+------------+------------+----------+-------------+-------------
 system      | encryption_keys                   | zitadel    |            | t          | f        | f           | f
 projections | failed_events                     | zitadel    |            | t          | f        | f           | f
 adminapi    | styling2                          | zitadel    |            | t          | f        | f           | f
 auth        | users2                            | zitadel    |            | t          | f        | f           | f
 adminapi    | locks                             | zitadel    |            | t          | f        | f           | f
 adminapi    | current_sequences                 | zitadel    |            | t          | f        | f           | f
 adminapi    | failed_events                     | zitadel    |            | t          | f        | f           | f
 adminapi    | styling                           | zitadel    |            | t          | f        | f           | f
 auth        | locks                             | zitadel    |            | t          | f        | f           | f

```

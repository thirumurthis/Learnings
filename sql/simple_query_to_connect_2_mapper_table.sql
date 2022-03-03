/*
Structure of table

Parent_table

Parent_id | name | record_type | metadata | version


Child_table

child_id | Parent_id | Parent_child_id | version 


 parent1
  |
   - child2
     |
      - child2.1
  |
   - child3

*/

select parent_id from child_table Where version = 0
start with child_id = (
    Select pt.child_id From parent_table pt, child_table ct
    where pt.parent_id = ct.parent_id
    and pt.name = '<value>' 
)
connect by prior parent_child_id = child_id;

-- using json _value oracle function to load and parse data from varchar which has json string
/* metadata  sample entry
{ "item":"entry1", "description": [ {"version":1, "text":"this is a demo"} ]}
*/
select parent_id,name, json_value(metadata, '$.description[0].version') from parent_table;

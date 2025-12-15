

SELECT NEXTVAL('order_seq') from dual;
SELECT NEXTVAL('order_status_seq') from dual;
commit;


--INSERT INTO ORDER_INFO
--(order_id, user_name, order_tag, item_name,item_quantity,created_by, created_at,updated_at, metadata, description)
--VALUES
--(nextval('ORDER_SEQ'), 'demo1','only_demo','pencil',20,'admin',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),'metadata','demo order info');

INSERT INTO ORDER_INFO (order_id, user_name, order_tag, item_name,item_quantity,created_by, created_at,updated_at, metadata, description, updated_by, current_status)
-- VALUES (ORDER_SEQ.nextval(), 'only_demo','pencil',20,'admin',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),'metadata','demo order info')
 SELECT nextval('ORDER_SEQ'), 'demo1','only_demo','pencil',20,'admin',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),'metadata','demo order info','admin','RECEIVED'
 WHERE NOT EXISTS (SELECT 1 FROM ORDER_INFO WHERE user_name = 'demo1' and order_tag = 'only_demo' and created_by = 'admin' and item_name = 'pencil' and item_quantity=20);

--INSERT INTO USER_INFO (user_id, user_name, first_name, last_name, user_type)
 --VALUES (USER_INFO_nextval(),'demo1','demo-fn','demo-ln','demo')
-- select nextval('USER_SEQ'),'demo1','demo-fn','demo-ln','demo'
-- ;--where not exists (select 1 from user_info where user_name = 'demo1' and first_name = 'demo-fn' and last_name = 'demo-ln' and user_type = 'demo');

--commit;

--INSERT INTO ORDER_USER_MAPPER (user_id,order_id)
-- VALUES (USER_SEQ.currval,ORDER_SEQ.currval);
--select currval('USER_SEQ'),currval('ORDER_SEQ')
--;--where not exists (select 1 from order_user_mapper where user_id = (select user_id from user_info where user_name = 'demo1' ));

--commit;
INSERT INTO ORDER_STATUS (order_status_id,user_name,order_id,status,updated_at,updated_by)
--VALUES (nextval('ORDER_STATUS_SEQ'),'demo1',currval('ORDER_SEQ'),'RECEIVED',CURRENT_TIMESTAMP(),'demo1');
select nextval('ORDER_STATUS_SEQ'),'demo1',currval('ORDER_SEQ'),'RECEIVED',CURRENT_TIMESTAMP(),'demo1'
where not exists (select 1 from order_status where order_id in (select order_id from order_info where user_name = 'demo1' and order_tag = 'only_demo')
and status = 'RECEIVED');

INSERT INTO ORDER_STATUS (order_status_id,user_name,order_id,status,updated_at,updated_by)
--VALUES (nextval('ORDER_STATUS_SEQ'),'demo1',currval('ORDER_SEQ'),'CREATED',CURRENT_TIMESTAMP(),'demo1');
select nextval('ORDER_STATUS_SEQ'),'demo1',currval('ORDER_SEQ'),'CREATED',CURRENT_TIMESTAMP(),'demo1'
where not exists (select 1 from order_status where order_id in (select order_id from order_info where user_name = 'demo1' and order_tag = 'only_demo')
and status = 'CREATED');

INSERT INTO ORDER_STATUS (order_status_id,user_name,order_id,status,updated_at,updated_by)
--VALUES (nextval('ORDER_STATUS_SEQ'),'demo1',currval('ORDER_SEQ'),'IN-PROGRESS',CURRENT_TIMESTAMP(),'demo1');
select nextval('ORDER_STATUS_SEQ'),'demo1',currval('ORDER_SEQ'),'IN-PROGRESS',CURRENT_TIMESTAMP(),'demo1'
where not exists (select 1 from order_status where order_id in (select order_id from order_info where user_name = 'demo1' and order_tag = 'only_demo')
and status = 'IN-PROGRESS');

INSERT INTO ORDER_STATUS (order_status_id,user_name,order_id,status,updated_at,updated_by)
--VALUES (nextval('ORDER_STATUS_SEQ'),'demo1',currval('ORDER_SEQ'),'DELIVERED',CURRENT_TIMESTAMP(),'demo1');
select nextval('ORDER_STATUS_SEQ'),'demo1',currval('ORDER_SEQ'),'DELIVERED',CURRENT_TIMESTAMP(),'demo1'
where not exists (select 1 from order_status where order_id in (select order_id from order_info where user_name = 'demo1' and order_tag = 'only_demo')
and status = 'DELIVERED');

INSERT INTO ORDER_STATUS (order_status_id,user_name,order_id,status,updated_at,updated_by)
--VALUES (nextval('ORDER_STATUS_SEQ'),'demo1',currval('ORDER_SEQ'),'COMPLETED',CURRENT_TIMESTAMP(),'demo1');
select nextval('ORDER_STATUS_SEQ'),'demo1',currval('ORDER_SEQ'),'COMPLETED',CURRENT_TIMESTAMP(),'demo1'
where not exists (select 1 from order_status where order_id in (select order_id from order_info where user_name = 'demo1' and order_tag = 'only_demo')
and status = 'COMPLETED');
--INSERt INTO ORDER_STATUS (user_name,order_id,status,updated_at,updated_by)
-- VALUES (USER_SEQ.currval,ORDER_SEQ.currval,'CREATED',CURRENT_TIMESTAMP,'demo1');
--select 'demo1', currval('ORDER_SEQ'),'CREATED',CURRENT_TIMESTAMP(),'demo1'
--; --where not exists (select 1 from order_status where user_id = (select user_id from user_info where user_name = 'demo1' ));

commit;
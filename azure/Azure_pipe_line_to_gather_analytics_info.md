Simple pipe line used to gather legacy application analytics data.

- The application was using plain vanila js and jsp (Servlet), based on the Azure Application insight tagging using javascript.
- After adding the code as mentioned per [link](https://docs.microsoft.com/en-us/azure/azure-monitor/app/javascript), with appropriate instrumentation id.

- we added the javascript function call to each tab and button hits.


Pipeline flow:

```

  Application -->  Application  ------> Blob storage --------> Stream Analytics                     -----> MS SQL DB ------> Power BI
                     Insights                                      job  (output query foramatted)
```

Note:
   - MS Sql database was used, only to persist the data, since Application Insights holds data only for 90 days. 

Since i was working on the linux machine, had to use visual studio code sql client plugin.

### Simple Stream analytics query

```
// input event was configured as customAppEvent
With analyticData AS (
 select conext.data.eventTime as event_time,
 udf.sortedDimension(context.custom.dimensions).screen as screen
 from customAppEvent                     //<input-event-blob-storage-input-name-in-stream-job>
 where context.data.eventTime is not null
) select * into analyticsSqlTable from analyticData;  

//output configured to sql database , named as analtuicsSqltable
```

```
curl -w 'HTTP_CODE=%{http_code}' --silent -XGET 'elastic_local.com:9200/index_20191111/_search?pretty' -H 'Content-Type: application/json' --output elastic-out -d 
'{
       "aggs": {
        "avg_user_pct" : { "avg" : { "field" : "system.cpu.user.pct" }},
	      "avg_sys_pct" : { "avg" : { "field" : "system.cpu.system.pct" }}
      },
      "query": {
            "bool": {
              "must": {"prefix": { "hostname": "system.cpu.user.pct"} },
              "should": [
                {"term": { "name": "machine-process"} }
              ],
              "filter": [
                {
                  "range": {
                    "@timestamp": {
                      "from": "now-5m"
                    }
                  }
                }
              ]
            }
      }
    }'
```

Metrics beat to flow the cpu process details.
curl -w => is to format


### Other generic query to fetch metricbeat data 
```
GET /metricbeat-*/_search?pretty
{
  "query": {
    "bool": {
      "must": {
        "match": {
          "fields.env": "TEST"
        }
      },
      "filter": [
        {
          "range": {
            "@timestamp": {
              "from": "now-10m",
              "to": "now"
            }
          }
        }
      ]
    }
  }
}
```

response:
```
{
  "took" : 521,
  "timed_out" : false,
  "_shards" : {
    "total" : 236,
    "successful" : 236,
    "skipped" : 217,
    "failed" : 0
  },
  "hits" : {
    "total" : 37860,
    "max_score" : 3.8524792,
    "hits" : [
      {
        "_index" : "metricbeat-6.8.1-2019.11.19",
        "_type" : "doc",
        "_id" : "xxxxxxxxx",
        "_score" : 3.8524792,
        "_source" : {
          "@timestamp" : "2019-11-19T19:30:09.426Z",
          "beat" : {
            "name" : "hostname.local",
            "hostname" : "hostname.local",
            "version" : "6.8.1"
          },
          "host" : {
            "name" : "hostname.local"
          },
          "metricset" : {
            "name" : "cpu",
            "module" : "system",
            "rtt" : 416
          },
          "system" : {
            "cpu" : {
              "irq" : {
                "pct" : 0
              },
              "softirq" : {
                "pct" : 0.0081
              },
              "steal" : {
	      .....
```

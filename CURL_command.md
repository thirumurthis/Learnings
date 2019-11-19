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

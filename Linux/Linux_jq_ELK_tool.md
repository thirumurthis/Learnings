Jq -> [JQ](https://stedolan.github.io/jq/manual/#Basicfilters) is a Linux/Unix tool to parse the json file.

Use Case, 
  when working with Elastic search queries this helps to filter values.
  
  Sample Elastic Curl query
  
```  
  curl -XGET "http://<host-name>:9200/_search?scroll=10m&pretty=true" -H 'Content-Type: application/json' --output ERRORS.JSON -d'
{
  "query": {
    "bool": {
      "should": [
        { "match": { "env": "<ENV>"}},
        { "match": { "message": "<WORD1>"} },
        { "match": { "message": "<WORD2>" }}
      ],
       "filter": [ 
       {"range": {"@timestamp": {"from": "now-10d"}}}
      ]
    }
  }
}'
```

Above query will fetch the list of logs from the elastic-search over 10 count (using scroll 10m) and prettyfies.
filters for last 10 days for keywords in the messsage string indexed in elastic.

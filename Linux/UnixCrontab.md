##### Sample cron to execute every first tuesday of the month at 1 AM 

```
00 01 * * 2 [ `date +\%d` -le 7 ] && <path/to/the/script>
```

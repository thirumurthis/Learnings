

In this blog will demonstrate web scrapping using python and memcache to cache specific data for specific duration.

Here I have used Oracle Cloud to deploy the Python and access the instance from internet. This steps can be done in the local system as well.

Refer the documentation for creating the Oracle instance at the [Oracle Documentation](https://docs.oracle.com/en-us/iaas/Content/Compute/Tasks/launchinginstance.htm)
- Briefly once the instance is created from the Oracle Web UI interface
  - To access the key using Putty we need to setup the SSH key.
  - Enable the firewall to access the instance from the Internet.


### Install memcache in Oracle Linux 

Use below command to install the `memcached` server

```
sudo dnf install memcached
```

![image](https://user-images.githubusercontent.com/6425536/183714745-ff5aed09-6399-4537-b7dc-b16f70da806b.png)


- Now we need to start the memcache service, intially the service will be disabled, this can be checked with below command

```
sudo systemctl status memcached
```

![image](https://user-images.githubusercontent.com/6425536/183717131-ed2287c1-ae62-4b7c-afaa-fdcb104c86cc.png)

- Start the service using `sudo systemctl start memcached` and check the status once again using above command. Refer the snapshot

![image](https://user-images.githubusercontent.com/6425536/183717819-2b69acb7-8ec9-4db3-a47a-0eddce1ea34b.png)


### Install the python memcached client package using pip

- Install the python package `pymemcache` using below command. This will enables us to use the client to access the memcache

```
sudo pip3 install pymemecache
```

![image](https://user-images.githubusercontent.com/6425536/183715412-88995960-48df-489f-ad0d-85daff160cb6.png)


> NOTE:-
> 
>  - 1. Refer [Oracle documentation for installing memchaced on Oracle Linux](https://docs.oracle.com/cd/E17952_01/mysql-5.6-en/ha-memcached-install.html)
>  
>  - 2. In case if need to flush all the values that where cached from the memcache server, we can use below command
>  
>    ```
>      $ echo "flush_all" > nc localhost 11211
>    ```
>    ![image](https://user-images.githubusercontent.com/6425536/183742561-e63516db-5f1d-4871-b3ff-29f392b27b12.png)


### Python code that will scrap the web site

  - The code will use `request-html` module to scrap the  `https://travel.state.gov` to extract the Immigartion data for EB2 and EB3 for India category.
  - The code will dynamically create the url template `https://travel.state.gov/content/travel/en/legal/visa-law0/visa-bulletin/{YEAR}/visa-bulletin-for-{NAME-OF-MONTH}-{YEAR}.html`
 
> INFO
> 
> - To install the pyton request-html package use below command. For more details refer [PYPI documenation](https://pypi.org/project/requests-html/)
> 
>    ```
>    pip install request-html
>    ```

#### About Code
  - The python code uses HTTP Server to handle GET requests. This code is to demonstrate the use of cache and scarpping website.
  - The `memcached` server should be started and running during the execution of the below python app.
  - When GET request is recevied, the code checks the memcache for the key `YYYY-MM`, if value exits it is served as response.
  - The code scraps the website for specific data set, and stores it in cache with TTL (expiration seconds) for 1 day.

- Create a file app.y with the below content.
```py
import os
from requests_html import HTML, HTMLSession
from datetime import date
from datetime import datetime 
import calendar
import json
from http.server import BaseHTTPRequestHandler, HTTPServer
from pymemcache.client.base import Client
import ast

PORT_NUMBER = int(os.environ.get('PORT', 8084))

##########
#### POC to scrap the WEBsite and get info (visabulletin)
#### This is not a perfect code, requires improvement, possibly tunning.
#### challenges, calling a funtion within a function where i had to use @staticmethod
#### The web link http://localhost:8084 - renders the raw html non formatted string
#### this can be tuned to converted to json if needed - next step
#########

# HTTPRequestHandler class
class testHTTPServer_RequestHandler(BaseHTTPRequestHandler):
 
  # better to apply DRY prinicple
  @staticmethod
  def toFetchTitleA(response, debug = False):
        xpathStrA='/html/body/div[3]/div[7]/div[2]/div[1]/div[2]/div[3]'
        tbl = response.html.xpath(xpathStrA)
        tmptxt=''
        if debug :
           print (tbl)
        for tb in tbl:
           if debug:
             print (tb)
           ptags = tb.find("p")
           cntr = 0;
           for ptag in ptags:
              cntr = cntr +1
              if cntr ==14 :
                 tmptxt = str(ptag.text).replace('\n',' ')
                 #print(tmptxt)
        return tmptxt
      
  @staticmethod
  def toFetchTitleB (response, debug = False) :
        xpathStrB='/html/body/div[3]/div[7]/div[2]/div[1]/div[2]/div[5]'
        tbl = response.html.xpath(xpathStrB)
        tmptxt=''
        if debug :
          print (tbl)
        for tb in tbl:
           if debug :
              print (tb)
           ptags = tb.find("p")
           cntr = 0;
           for ptag in ptags:
              cntr = cntr +1
              if cntr ==3 :
                 tmptxt = str(ptag.text).replace('\n',' ')
                 #print(tmptxt)
        return tmptxt

  @staticmethod
  def parseToJson(resultList, debug = False) : 
       key = ''
       value=''
       iter=0
       temp = {}
       result = {}
       for item in resultList:
         temp = {}
         codedItem = item.split("##")
         if debug:
           print (codedItem)
         for item in codedItem :
           if len(item) > 0 :
            codedKey = item.split(":-")[0]
            value = item.split(":-")[1]
            if debug:
               print(f"value = {value}")
            if '@' in codedKey:
               iter= codedKey.split('@')[0]
               key= codedKey.split('@')[1].strip()
               if debug:
                  print(f"key = {key}")
               temp[key]=value.strip()
         result[iter] = temp
       return result

  @staticmethod
  def getWebContent():
    session = HTMLSession()
    toDate = date.today()
    currentMonth = toDate.month
    currentYear = toDate.year
    monthName = calendar.month_name
    currentMonthName = monthName[currentMonth].lower()
    #print(f"{monthName[currentMonth].lower()} and {currentYear}")
    url = f"https://travel.state.gov/content/travel/en/legal/visa-law0/visa-bulletin/{currentYear}/visa-bulletin-for-{currentMonthName}-{currentYear}.html"
    print(url)
    # form output to list 
    outputList = []
    response = session.get(url)

    tables= response.html.find("table")

    colCnt = 0
    IndCheckCol = 5
    CategoryCheckCol=1
    rowCnt = 0
    checkRow = 4
    requiredInfo= ''
    printTableInfo = False
    employTitleACnt = 1
    employTitleBCnt = 2
    tblCnt =0
    toDebug = False
    for table in tables:
      trs = table.find("tr") #,first=True)
      #tds = trs.find("td")
      firstTd = table.find("td", first=True)
      #print(td.text)
      output = ''
      colCnt =0
      rowCnt =0
      requiredInfo= ''
      if "Employment" in firstTd.text :
         tblCnt +=1
         if tblCnt == employTitleACnt:
            ret = testHTTPServer_RequestHandler.toFetchTitleA(response)
            requiredInfo = str(tblCnt)+'@title :- ' + requiredInfo + ret + ' ## '
            if toDebug :
              print (requiredInfo)
         if tblCnt == employTitleBCnt:
            ret = testHTTPServer_RequestHandler.toFetchTitleB(response)
            requiredInfo = str(tblCnt)+'@title :- ' + requiredInfo + ret + ' ## '
            if toDebug :
              print (requiredInfo)
            
         tmp = str(firstTd.text).replace('\n',' ')
         if toDebug :
           print (f">>>>> Found Employment - {tmp}")
         for tr in trs:
            # we use only till rowcount 3
            rowCnt = rowCnt+1 
            headtd = tr.find("td")
            colCnt = 0;
            for head in headtd :
              colCnt = colCnt+1;
              tmp = str(head.text).replace('\n',' ')
              output += tmp+ " | "
              if colCnt == CategoryCheckCol and rowCnt <= checkRow:
                 requiredInfo += str(tblCnt)+'@'+tmp + ' :- '
              if colCnt == IndCheckCol and rowCnt <= checkRow :
                 requiredInfo += tmp + ' ## '
      
            ## prints the info in tabular format
            if printTableInfo == True :
              print (output)
            output= ''
            #print(">>>>>>>>>>>>>>>> tr end <<<<<<<<<<<")
            #print(tr.text, end="|")

         #print(f">>> {requiredInfo}")
         outputList.append(requiredInfo)
    return outputList

  @staticmethod
  def parseToJson(resultList, debug = False) : 

       toDate = date.today()
       currentMonth = toDate.month
       currentYear = toDate.year
       monthName = calendar.month_name
       cacheClient = Client('localhost')

       isCacheResult = cacheClient.get(f'{currentYear}-{currentMonth}')

       if isCacheResult :
           # we need to decode since the returned value is byte type
           return isCacheResult

       key = ''
       value=''
       iter=0
       temp = {}
       result = {}
       if debug:
          print(f"resultList input := {resultList}")
       for item in resultList:
         if debug :
            print(f"processing ... {item}")
         temp = {}
         codedItem = item.split("##")
         if debug:
           print (codedItem)
         for item in codedItem :
           if len(item) > 0 and len(item.split(":-")) > 1 :
            if debug:
               print (f"item = {item} && length = {len(item)}")
            codedKey = item.split(":-")[0]
            value = item.split(":-")[1]
            if debug:
               print(f"value = {value} && codedKey = {codedKey}")
            if '@' in codedKey:
                iter= codedKey.split('@')[0]
                key= codedKey.split('@')[1].strip()
                if debug:
                   print(f"key = {key}")
                temp[key]=value.strip()
         result[iter] = temp
       result['timestamp']= str(datetime.now())
       # cache only for 1 day
       cacheClient.set(f'{currentYear}-{currentMonth}',result,86400)
       return result
 
  # GET
  def do_HEAD(self):
        # Send response status code
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        return

  # GET
  def do_GET(self):
        # Send response status code
        self.send_response(200)
 
        # Send headers
        self.send_header('Content-type','text/json')
        self.end_headers()
 
        # Send message back to client
        outMessage = testHTTPServer_RequestHandler.getWebContent()
        #print(type(outMessage))
        #print (outMessage)
        message = ""
        json_data = testHTTPServer_RequestHandler.parseToJson(outMessage, False)
        #print(json_data)
        if json_data and not isinstance(json_data,bytes):
            #Python pretty print JSON
            message = json.dumps(json_data, indent=4)
        
        # below condition to convert the bytes type since cache value is of this type
        if json_data and  isinstance(json_data,bytes):
           # convert the string type of dictionary value to dictonary type
           dict_str = json_data.decode("utf-8").replace("'",'"')
           # String converted to dict type, using ast 
           dict_type_val = ast.literal_eval(dict_str)
           # pretty the json value back
           message = json.dumps(dict_type_val, indent=4)

        message = str(message)
        #print(f"message :- {message}")
        #message = "Hello world!"
        # Write content as utf-8 data
        self.wfile.write(bytes(message, "utf8"))
        return
 
def run():
  print('starting server...')
 
  # Server settings
  # Choose port 8080, for port 80, which is normally used for a http server, you need root access
  server_address = ('0.0.0.0', PORT_NUMBER)
  httpd = HTTPServer(server_address, testHTTPServer_RequestHandler)
  print('running server...')
  httpd.serve_forever()
  
run()
```

> Note:
>  To setup the memcache as Windows refer to [the memcache documentation](https://docs.kony.com/konylibrary/konyfabric/kony_fabric_manual_install_guide/Content/Install_Memcached_Server.htm).
>  Reference to execute specific port [1](https://www.coretechnologies.com/products/AlwaysUp/Apps/RunMemcachedAsAService.html)
>  In Windows 10, edited the registry using `regedit` and pass the additional argument `-m` for memory and `-p` for port.


### Output 

#### Executing the python code

- The Oracle Linux instance installed with python, we can use below command to run the process in background 

```
$ nohup python app.py &
```

- Since the Oracle instance is accessible from the internet, with the IP address we can access the Instance to serve the response
- The url in this case is `http://192.9.244.94:8084/`, the response will be a JSON pay load looks like below
```
{
    " 1": {
        "title": "A. FINAL ACTION DATES FOR\u00a0EMPLOYMENT-BASED\u00a0PREFERENCE CASES",
        "Employment- based": "INDIA",
        "1st": "C",
        "2nd": "01DEC14",
        "3rd": "15FEB12"
    },
    " 2": {
        "title": "B. DATES FOR FILING OF EMPLOYMENT-BASED\u00a0VISA\u00a0APPLICATIONS",
        "Employment- based": "INDIA",
        "1st": "C",
        "2nd": "01JAN15",
        "3rd": "22FEB12"
    },
    "timestamp": "2022-08-09 19:17:47.274246"
}
```
#### Snapshot
![image](https://user-images.githubusercontent.com/6425536/183776111-968ed252-483d-4296-9664-70c04b9cb3d2.png)



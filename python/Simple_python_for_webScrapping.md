
Store below content in to app.py

exeucting the `python app.py`, connect to the localhost.

use for package
```
pip install requests-html
```

```py
import os
from requests_html import HTML, HTMLSession
from datetime import date 
import calendar
from http.server import BaseHTTPRequestHandler, HTTPServer
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
              if cntr ==13 :
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
            requiredInfo = requiredInfo + ret + ' :- '
            if toDebug :
              print (requiredInfo)
         if tblCnt == employTitleBCnt:
            ret = testHTTPServer_RequestHandler.toFetchTitleB(response)
            requiredInfo = requiredInfo + ret +' :- '
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
                 requiredInfo += tmp + ' - '
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
        self.send_header('Content-type','text/html')
        self.end_headers()
 
        # Send message back to client
        outMessage = testHTTPServer_RequestHandler.getWebContent()
        #print(type(outMessage))
        #print (outMessage)
        message = ''
        for item in outMessage:
          message += item + ';'

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

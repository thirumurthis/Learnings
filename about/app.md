Building a simple app for personal use and deploying in Heroku.

 - Heroku provides hosting Java, NodeJS application for free, with limitation and advantages.
   - The application will be down if not active for 30 minutes.
   - Postgres Database add-on's with almost 10,000 rows. 
   
 Coming to the application that I built is a simple Stock application using Yahoo Finance API. This a personal project always there is scope for improvements.
 
 Design consideration for the app:
 
  - The first point in my consideration while desinging the app is security and not to display any of the personal information if any one using this app.
  - This is where I used Spring Boot security and JWT token.

End points at high level:

 - The POST end-point `/stock-app/signup` can be used for creating a API token, it requires to pass user name and password in the request body.
 - Using the User Name and Api key, the POST end-point `/stock-app/token` provides the JwtToken when invoked with User Name and API key in the request body.
 - The POST end-point `stock-app/apiKey`, can be used to get the API key in case user forgets it, this requires the user name and password.
 - The POST end-point `/stock/v1/add` is used to add single stock,the format of the input in request body will be explained latter.
 - The application also has end-point to add list of Stocks, delete stock, update stocks.
 - The SWAGGER endpoint will provide available end-point details `http://<domain>/swagger-ui/index.html` and providing application name.

Access the endpoint:
  - The Stock App can be accessed using Postman or equivalent tool.
  - I also built an NodeJS and Express based app, just with login page.
      - Currently this is not a full fledge app, at this point the Stocks need to be added from Postman.

** Input format **
```json
[{
		"symbol": "MSFT",
		"stockCount": "5.00",
		"avgStockPrice": "12"
	},
	{
		"symbol": "INFY",
		"stockCount": "100.796152",
		"avgStockPrice": "12.55"
	},
	{
		"symbol": "INTC",
		"stockCount": "65.428345",
		"avgStockPrice": "52.43"
}]
```

The [Git code](https://github.com/thirumurthis/stock-api#readme) for the application.

**Swagger Snapshot of the application**

![image](https://user-images.githubusercontent.com/6425536/152632205-78700a1a-dd1f-4599-8240-9413214a3144.png)

**Sample Sign-Up form respone using Postman**
![image](https://user-images.githubusercontent.com/6425536/152632464-7f732bb1-9a46-444c-9182-9f4453e9a579.png)

**Postman to post a list of stock using API. Note the API key used as Bearer token in this case**
![image](https://user-images.githubusercontent.com/6425536/152632936-f735d592-45a4-4fb6-8473-2c030a10eb01.png)


**Sample NodeJS input login page **
![image](https://user-images.githubusercontent.com/6425536/152633066-88336748-442f-4837-8dab-c9229abc14a3.png)

**Sample UI Accessing the backend and displaying the data**
![image](https://user-images.githubusercontent.com/6425536/152632992-d388d183-ee29-4430-a055-e2c415b00378.png)



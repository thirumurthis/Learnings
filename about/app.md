Building a simple app for personal use and deploying in Heroku.

 - Heroku provides hosting Java, NodeJS application for free. Free tier has some limitation as well.

   - The application will be down if it is inactive for 30 minutes. 
       - For example, if I didn't access the backend or frontend API it will not be active. 
       - Eventually once I access the endpoint, it will startup automatically.
   - Postgres Database on free tier support only 10,000 rows, which is sufficient for my use case.
   
** About the app **
Now lets look at the application that I built. This application will take the stock symbol, number of stock and average price of the stock invested. 

The backend Spring Boot APP uses Java Yahoo Finance API, to compute lasted price of the Stock. Since I am using for personal purpose, I don't worry about real time update on stock price.
 
**Thinking of design **
 
  - Being a personal use application, I wanted the application to be secure, so the decision there is Spring Security and JWT tokens.
  - Since the application is hosted in public domain, I also don't want my personal details if input to be available to public. This is where simple Sign Up end-point with User Name and Password originated.
  - Next, I don't want the user or myself to use Password for every transaction, the next idea was to provide a Random generated API key as a response of `SignUp` process. This is the first end-point to the application, where the user name, encoded password and API Key are stored in database.
  - Now, what if user or myself forget the API key, for that I expose another end-point which will provide the API key. This is a POST end-point which require user name and password in request body.
     - This app right now doesn't have forget password. This is another idea, yet to be built where I can have send a link and request user to provide a new password.
  

** Few of the end-points at high level:**

 - The POST end-point `/stock-app/signup` can be used for creating a API token, it requires to pass user name and password in the request body.
 - Using the User Name and Api key, the POST end-point `/stock-app/token` provides the JwtToken when invoked with User Name and API key in the request body.
 - The POST end-point `stock-app/apiKey`, can be used to get the API key in case user forgets it, this requires the user name and password.
 - The POST end-point `/stock/v1/add` is used to add single stock,the format of the input in request body will be explained latter.
 - The application also has end-point to add list of Stocks, delete stock, update stocks.
 - The SWAGGER endpoint will provide available end-point which can be accessed  `http://<domain>/swagger-ui.html`, the snapshot below provides the list of end-points. 

**Tools to access the endpoint:**

  - The Stock App can be accessed using Postman or equivalent tool.
  - I also built an frontend app using NodeJS and Express  just to login page and view the data in presentable format.
   - This is not a full fledged usable app. Like in order to add the stock and sign up only way is to access the API via Postman.

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

**Code link**
  - [Spring Boot Application in Github](https://github.com/thirumurthis/stock-api#readme) and 
  - [NodeJS Application in Github](https://github.com/thirumurthis/stock-app#readme).

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



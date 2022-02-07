Building a simple app for personal use and deploying in Heroku.

 - Heroku provides hosting Java, NodeJS application for free. Free tier has some limitation as well.

   - The application will be down if it is inactive for 30 minutes. 
       - For example, if I didn't access the backend or frontend API it will not be active. 
       - Eventually once I access the endpoint, it will startup automatically.
   - Postgres Database on free tier support only 10,000 rows, which is sufficient for my use case.
   
** About the app **

Now lets look at the application, the backend will provide set of REST end-point API's which takes stock symbols, number of stocks invested and average price of the stock invested in JSON format and computes below against the latest stock price.
   - Profit or Loss for the Stock symbol
   - Total invested amount, today's gain, etc. (refer the snapshot at the bottom)

The backend Spring Boot app uses Java Yahoo Finance API to compute the above out put. This app is used for personal use, there is no concerns for me to worry about real time price update of each stock.
 
**Thinking of design **
 
 - Though this is application is for personal use, I wanted it to be secure, the choice her ewas to use `Spring Security` and `JWT`.
 - Next I wanted an free Cloud platform to host the application that is where `Heroku` came into picture with little search.
- Hosting and displaying the personal finance information publicly is a hard hitter, I had to think about this a lot. This is where `Sign Up` end-point came into play which requires User Name and Password to register to access the application.
- Next, to access the application I don't want myself or user to supply the password for every transaction, this gave the idea of generating API key a unique and randomly generated key which will be the response of Sign Up` process. The user name, password and API key are stored in the Postgres database.
- What if user or myself forget the API key, in order to fetch the API Key exposed another end-point which requires user name and password to fetch the API key. 
- The next part is to generate JWT token which expires every hour, for this another end-point is exposed which requires API key and user name.
- Finally in order to Add, Delete, Update stocks using the API end-point the JWT Bearer token should be passed in Authorization header.

-  This app right now doesn't have most of the features like forget password,etc.
  

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

** Development and deployment process **

 - First we need to create an account in Heroku platform.
 - Download and install Heroku CLI in local laptop.
 - The developed code needs to be pushed to GitHub main branch (or any development branch).
 
 - When we make changes to the code, we need to push the changes first to GitHub.
 - In laptop we can use git-bash, navigate to the local project folder, use below command
   ```
   ## Below command will open up the browser for user to login to Heroku platform
    heroku login

    ## Below command will create an app, this will create an app with random name
    ## The below command will also update the git remote for Heroku
    heroku create 

    ## To rename the app name created in Heroku platform, use below command
    heroku apps:rename --app <existing-app-random-generated-name> <new-name-for-app>

    ## List the Git remote to see if the Heroku remote is created using below command
    heroku remote -v

    ## Finally push the changes to heroku git, below command also deploy the app
    ## Below command will prompt for git.heroku user name and password. 
    ## Username is not required, password is the API Key (refer below notes for generating keys)
    git push heroku main

   ```
 ** Generating API Keys **
   To get the API key of git.heroku.com
  ```
   ## Below command will generate a API token 
   heroku auth:token
  ``` 

**Code link**
  - [Spring Boot Application in Github](https://github.com/thirumurthis/stock-api#readme) and 
  - [NodeJS Application in Github](https://github.com/thirumurthis/stock-app#readme).

**Swagger Snapshot of the application**

![image](https://user-images.githubusercontent.com/6425536/152632205-78700a1a-dd1f-4599-8240-9413214a3144.png)

**Sign-Up form response using Postman**
![image](https://user-images.githubusercontent.com/6425536/152632464-7f732bb1-9a46-444c-9182-9f4453e9a579.png)

**Postman used to store list of stocks using API end-point**
  - Note the generated Bearer token was used.
![image](https://user-images.githubusercontent.com/6425536/152632936-f735d592-45a4-4fb6-8473-2c030a10eb01.png)


**NodeJS application input login page **
![image](https://user-images.githubusercontent.com/6425536/152633066-88336748-442f-4837-8dab-c9229abc14a3.png)

**The stock info in presentable format with profit/loss and investment summary**
![image](https://user-images.githubusercontent.com/6425536/152632992-d388d183-ee29-4430-a055-e2c415b00378.png)

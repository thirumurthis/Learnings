Building a simple app for personal use and deploying in Heroku.

 - Heroku provides hosting Java, NodeJS applicaton for free, with limitation and advantages.
   - The application will be down if not active for 30 minutes.
   - Postgres Database add-on's with almost 10,000 rows. 
   
 Coming to the application that I built is a simple Stock application using Yahoo Finance API. This a personal project always there is scope for improvements.
 
 Design considernation for the app:
 
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
      
The [Git code](https://github.com/thirumurthis/stock-api#readme) for the application.

#### Swagger Snapshot of the application

![image](https://user-images.githubusercontent.com/6425536/152632205-78700a1a-dd1f-4599-8240-9413214a3144.png)

#### Sample Sign-Up form repsone using Postman
![image](https://user-images.githubusercontent.com/6425536/152632464-7f732bb1-9a46-444c-9182-9f4453e9a579.png)

#### Postman to post a list of stock using API. Note the API key was used as Bearer token in this case
![image](https://user-images.githubusercontent.com/6425536/152632636-cc15c316-9c56-4ca0-8710-ff018a646e7c.png)

#### Sample NodeJS input login page
![image](https://user-images.githubusercontent.com/6425536/152632657-a1d1d140-dca8-4bcf-941c-417c497581f4.png)

### Sample UI Accessing the backend and displaying the data
![image](https://user-images.githubusercontent.com/6425536/152632703-3195fd63-22e3-4c69-b8b5-f7f6b89dc2f7.png)



#### Using __`TypeORM`__ for Relational and Non-Relational db

 - Relational DB like PostgreSQL.
 - Non-relational DB like MangoDB.
 
 TypeORM - `is Typescrit based ORM.`
 
 #### How to install it? using `npm`
 
 Step:1
 ```
 $ npm install typeorm
 ```
 
 Step:2 - install reflect-metadata and imprt to main file.
 ```
 $ npm install reflect-metadata
 import "reflect-metadata"; // part of the NESTJS rest api
 ```
 
 Step:3 - install a database driver for database instanced used. For PostgreSql(pg)
 ```
 $ npm install pg
 ```
 
 ##### Creating connection using TypeORM:
 
 ```
 import { createConnection, Connection } from "typeorm";
 
 const connection = await createConnection({
   type: "postgres",
   host: "localhost",
   port: 5432,
   username: "db-username",
   password: "db-password",
   database: "db-name"
   });
 ```
 `createConnection` => comes from the TypeOrm module. We pass a function to this, which is the db configuration.
 
 #### How to map entities

 ```
 import { createConnection, Connection } from "typeorm";
 
 const connection = await createConnection({
   type: "postgres",
   host: "localhost",
   port: 5432,
   username: "db-username",
   password: "db-password",
   database: "db-name",
   entities: ["entity/*.js"]
   });
 ```
 `entities` key in our connection will be used to find our entity files.
 - To work, `create a folder entity` and place the js files which has the entity details.
 - since entities are array, ["entity/users.js","entity/car.js"] can be used for refering specific entities.
 
 #### Creating Entities:
  - Entities are often called as models in other ORM (or content of our table)
  - This is how we tell TypeORM what columns exists in a table.
  
 ```js
 
 @Entity() //entity decorator tells typeORM this is a entity definition
 export class User{
  @PrimaryGeneratorColumn()  //this is primary key and auto generated value
  id: number;
  
  @Column()  // defines a column of the table. additional options can be passed refer doc.
  name: string;
  
  @Column()
  email: string;
  
  @Column()
  age: number;
  }
 ```
  - The SQL datatypes are automatically translated from typescript datatypes.
 
 #### Repositories:
   - In TypeORM, `repositories` allow us to create an object that allow us to query an entity.
 
 ```js 
 const repository = getRepository(User);
 // getRepository() -> comes from the TypeORM
 // pass entity to this repository.
 ```

##### Inserting Data using typeORM:

```js
const user = new User();  //instantiate a new user 
user.name = "Thiru";
user.email= "email@domain.com"
user.age= 20;
await repository.save(user); // finally save the user to db
``` 

#### Selecting data using TypeORM: `find()`

```js
const results = await repository.find({where: {name:"Thiru"}});
// the find method is called on the user repository.
// the above will give a list of user with the name Thiru
```

```js
const results = await repository.findOne(where: {name:"Thiru"}});
// this will return only one record or one user with name Thiru
```

##### Update data using TypeOrm

```js
const userDeatil = await repository.findOne(where: {name:"Thiru"}});
userDetail.email = "newmail@domain.com";
await repostiory.save(userDetails);
```

##### Deleting data using TypeORM:

```js
const userDetail = await repository.findOne(where: {name:"Thiru"}});
await repository.delete(userDetail.id); // we pass a single id to delete the record

await repostory.delete({name: "Thiru"}); // to delete all the records with name as Thiru
```
------------------------
------------------------

### NESTJS 
  - NestJs is a type of web application server built with Nodejs
  - this uses `TypeScript` as its primary language.
  
##### Routes = destination urls on web sites are called Routes.
  - Root Route = mostly "/" the homepage is root route in an website (www.mysite.com/).
  - other routes can be (www.mysite.com/help), help - is different route.
  - (www.mysite.com/store/products) store/productes - is different route.

  - GET routes - Get data from server
  - POST routes - Send data to the server

##### installing NestJs check `https://docs.nestjs.com`
- after the NESTJS is installed, run the scaffolding command
```
$ nest new cars
// the command created a brand new web application server named cars.

//below command will start the webservice and run on port mostly localhost:3000
$ yarn start 
$ npm run start 
```
  - `Scaffolding` in software development is the process of running a command that builds a whole bunch of stuff for you very quickly that is ready to use right away.
  
  [Link for example project](https://github.com/SoloLearn-Courses/nest_init)
  
##### The `NESTJS` directory structure:
  - controller
  - module
  - service
  
- This is similar to angular strucutre, where below files gets created
```
app.controller.spec.ts
app.controller.ts   // handles web traffic
app.module.ts  // works behind to bind the controller and service.
app.service.ts  // handles the data 
main.ts
```
- code in controller
```js
import { Controller, Get}  from '@nestjs/common';
import {AppService} from './app.service';

@Controller()
export class AppController {

constructor (private readonly appService: AppService){}

@Get()
getHello(): string{
  return this.appService.getHello();  // To send response we can simple change as return "hello";
 }
 }
```

##### Creating routes in NESTjs
```
$ nest generate route cars
// this will create a directory cars
```

##### Understanding Routes
  - Root route is the app.controller where the @Get() represents "/"
  - if we create a `car` route and the file name is included part of controller.
      - the http://localost:3000/car => will invoke the car controller
      - the http://localhost:3000/car/details => will invoke any method in the controller tagged as `@Get('/details')....`
      
   - passing `wildcard` routes. For example, http://localhost:3000/cars/4 (where 4 is an id) then use `@Get(':id')....`
```js

@Get(':id')
findOne(@Req() request: Request): {} { // {} is the return value of the method
   return { id:25, make: 'toyota' };
}
```
 
    - `wildcard` with several levels
```
@Get('cars/:make/:model/:year')
```
 - sample controller code to return carsl
```js
import { Controller, Get } from '@nestjs/common';
import { AppService } from './app.service';

@Controller() /// usually if a route car is created the string 'car' displayed as arg
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get('/cars')
  getCars(): {} {
     return [{make: 'honda', model: 'accord'},
    {make: 'subaru', model: 'outback'},
    {make: 'fiat', model: '123 spider'}];
  }

  @Get('/car/:id')
  getCar(): {} {
    return {id:25, make: 'honda', model: 'accord'};
  }
}
```

##### Making Post requests
  - For post request it will be a different route, we need to accept `Body` of data.
  - The body will come in the form of javascript object that has been sent by the front-end
  - route to make recieve data 
```js
import { Controller, Get, Post, Body } from '@nestjs/common';
import { AppService } from './app.service';

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get('/cars')
  //... same as the above code

  @Post()
  async createCar (@Body() carInfo){
    return "request recieved to insert car ${carInfo.make}";
  }
}
```

  - `async` in the method means asynchronous. `Async/await` is a javascript construct that allows you to create "async" methods, __`these methods waits for a process to finish before returning results`__. If this is a database query, this method needs to wait for the query to fetch the results from the DB.
  
  
##### Post to delete records
  - delete route only needs an id so we don't need a @Body(), only @Param() decorator
```js
 @Pist(':id/delete')
 async delete(@Param() carId){
   returning 'fire query to delete the db, use the repository typeOrm';
   }
 }
```

##### How to connect to the database from Nestjs
 - in `app.module.ts` file add 
```
  @Module({
   imports: [
     TypeOrmModule.forRoot({
       type: 'postgres',
       host: 'localhost',
       port: 5432,
       username: 'db-username',
       password: 'db-password',
       database: 'transportation',
       entites: [Cars],
       synchronize: true,
       }),
       CarModule,
       ],
       })
       export class AppModule {}
```
- we need to tell TypeOrm we are connecting to Cars table.
- in [`app.module.ts`](https://github.com/SoloLearn-Courses/nest_typeorm-postgres/blob/master/src/app.module.ts) file import the entity class

```js
import {Cars} from './car/carInfo.entity';
//refer the above documentation on typeOrm entity creation
```
 - `Service` like import car service into the service file.
```js
@Injectable()
export class CarService {

constructor(
  @InjectRepository(Cars) private readonly carsRepository: Repository<Cars>,
     ){}
     
async findAll(): Promise<Cars[]> {
  return this.carRepository.find();
}
```

 - __`Promises`__ is returned by the method.

##### angular http client connection to server
```js
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs' ;
//...
  getCars(): Observable {
    return this.http.get('/cars/');
  }
//...
```
 
 Observable: 
  - is a stream of data, is similar to `promise`.
  - it's essentially asynchronous data. This means the request travels over the network and data doesn't arrives immediately. This just waits for the response to reach.
  
##### upgrade Angluar 8 to 9, is simple
```
$ ng upgrade
```
 - To check the angular version, use the package.json 
 - in the dependencies category, it looks like '"@angular/common":"~9.0.2"'
 
`scss` is a way of writing CSS that allows to write nested CSS styles, use variables called mixins, other features.


[sample code link](https://stackblitz.com/github/SoloLearn-Courses/TSFlights1/tree/step9c?file)

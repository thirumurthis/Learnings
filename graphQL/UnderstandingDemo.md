GraphQL was created by developers at Facebook in 2012.

GraphQL was developed to support the complicated data structures required to show the Facebook News Feed on the mobile application.

Using GraphQL, the client can request the necessary data using a single endpoint with a defined schema.

Considering you're building a blogging platform, you will need to fetch posts, author, published date, comments, and other associated data with a particular post.

With a standard REST API, you can easily design such an API.

But now consider that you want to display comments only on the web app, not the mobile app.

It's almost impossible to achieve this with a single REST API.

This is where GraphQL can help you.

The client can ask for the data they need, and the server will return with only that data.

Let's see how you can build a GraphQL API.

Install nodejs.

As now you know what GraphQL is, let's start creating a quick GraphQL server.


mkdir graphql
cd graphql
npm init -y

The next step is to create an empty JavaScript file (File name could be anything, in this case, app.js) and install all the necessary packages.

We will discuss the use of each package as we progress further into this thread.

npm install express express-graphql graphql

now lets create a basic local server

```js

const express = require("express");
const app = express();
const PORT = 3000;
app.listen(PORT, () => console.log(`app running at port ${PORT}`));

```

Our server is now running, it's time to build the schema that is nothing but the description of data that the client can request.

Let's create a new "schema" folder in our graphql directory.

Inside that schema folder, create a JavaScript file as schema.js.

graphql > schema > schema.js

Let's start with creating a simple `User` schema inside the schema.js file.

We need two things:

- Schema that defines the Query type.
- A “resolver” function for each API endpoint.

The resolver function is a collection of functions that generate responses for a GraphQL query.

```js

const { buildSchema } = require("graphql");
const schema = buildSchema(`
    type Query {
       user: User
    }
    type User{
        name: String,
        age: Int
    }
`);

const root = {
     user: () => { 
           return { name: "Thiru", age: 40 };
          },
};

```

The above code is pretty intuitive.

`User` type has two fields, name and age of type string and integer, respectively.

The `root` provides the resolver function. In this case, we are resolving `user` type which returns the value of name and age.

For schema defintion details check below link
https://rapidapi.com/learn/graphql-apis/introduction/what-is-sdl?utm_source=twitter.com%2FRapid_API&utm_medium=DevRel&utm_campaign=DevRel


Export `schema` and `root` so that we can use them in our app.js file. 

Add below code snippet at the end of schema.js file.

```
module.exports = {schema, root};
```
It's time to run this query on the GraphQL Server that we just built (app.js file).

We will use `graphql-express` libaray to run GraphQL server on "/graphql" HTTP endpoint.

```js
const { graphqlHTTP } = require("express-graphql");
const {schema, root } = require("./schema/schema");

app.use(
 "/graphql", graphqlHTTP({
      schema: schema,
      rootValue: root,
      graphiql : true,
})
);
```

`graphiql: true` provides the GraphQL playground.

Head over to http://localhost:3000/graphql and you see the playground like this.

To run the query from browser, use the play ground

use below query
```
{
  user{
    name
    age
   }
}
```

Sample screen shot:

![image](https://user-images.githubusercontent.com/6425536/170892748-5fdfd0f1-5fec-412d-83a0-ce9aae1f48f9.png)

![image](https://user-images.githubusercontent.com/6425536/170892766-2a9e1a69-114f-4784-9778-f4358cc7b87a.png)


---------------------------

File content `app.js`

```js
const express = require("express")
const app = express();
const PORT = 3000;

const { graphqlHTTP } = require("express-graphql");
const {schema, root } = require("./schema/schema");

app.use(
 "/graphql", graphqlHTTP({
      schema: schema,
      rootValue: root,
      graphiql : true,
})
);

app.listen(PORT, ()=> console.log(`App listening at port ${PORT}`) );
```

- Schema.js

```js
const { buildSchema } = require("graphql");
const schema = buildSchema(`
    type Query {
       user: User
    }
    type User{
        name: String,
        age: Int
    }
`);

const root = {
     user: () => { 
           return { name: "Thiru", age: 40 };
          },
};

# makes this schema.js accessible from another JS 
module.exports = {schema, root};
```

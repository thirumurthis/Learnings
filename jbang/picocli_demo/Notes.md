### The picocli demo with Jbang
- With picocli we can create java application which can take command line as arguments.
- Refer the [picocli documentation](https://picocli.info/#_introduction).

#### Summary
- The example will use Jbang to load the picocli dependency to build the simple java application.
- The simple application which takes `-r` or `--random-message` command line argumets.
- If the `-r` arguments is passed, then the logic will fetch some random message from API and displays.


#### Executing the code
 - With the jbang installed in local machine use below command to run the code

```
$ jbang demoappwithpckg.java  --random-message
```

- Output looks like below

```
[jbang] Building jar for demoappwithpckg.java...
{ "message": "What do you do when you see a space man?" }
```

#### Jbang structure
- In this Jbang example we have used a helper class under support folder
- In the main Jbang class we load this using `//SOURCES support/*` and import the package `import support.apihelper`.

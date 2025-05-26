### Plugin for maven example

- In this approach, we use IntelliJ IDEA IDE to create a java project with maven.
- Then create a module within that project, remove the src folder from outer project.
- Update the pom.xml with the four basic dependency maven-plugin-api, maven-plugin-annotation, maven-core, maven-project

- Add the plugin override with `AbstratMojo`, in `execute()` method
- Issue `mvn install`
- To run the plugin issue `mvn com.demo.mvn:<artifactId-of-the-module-note-outer-project>:demo`.
- Also, `mvn com.demo.mvn:demo-plugin:1.0-SNAPSHOT:demo` will output the data from `getLog()` line of code.
- Also, we can use the scope property in the cli like `mvn com.demo.mvn:demo-plugin:1-0-SNAPSHOT:demo -Dscope=test` will print 1 which is junit in pom.xml

- How to configure the plugin to use only the plugin and goal in the cli
  - In the `$HOME/.m2` folder, edit the `settings.xml`
  - Add a pluginGroups section
  ```shell
   <pluginGroups>
     <pluginGroup>com.demo.mvn</pluginGroup>
   </pluginGroups>
  ```
  - once added then issue `mvn demo-plugin:demo -Dscope=test` should print the same output

- The reporting, with the `mvn clean site`, the target folder will create an `index.html` but doesn't have any info.
- To add details, we need to add the `mvn-site-plugin` to pom.xml of module `packageManagement/plugin` property
- And add `reporting` property and the report only works when the `maven-plugin-plugin` version is set to `3.8.1` any new version is not work when using `mvn clean site`.

### Reporting functionality is not implemented in this project but there are diferent approach
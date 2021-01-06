#### Mavent surefire plugin:
  - The surefire plugin loads the `/src/main/java/*java` classes and `/src/main/resources` under `/target/classes`.
  - The `/src/test/java` test classes and `/src/test/resources` are loaded under `/target/test-classes`
  - For maven testing, the class path of the config is set to use the `/target/test-classes`. 
      - If any other folder path needs to be used under class-path for testing use 'additionalClassPath` element. Check maven documentation.


refer [link](https://www.codetab.org/tutorial/apache-maven/plugins/maven-resources-plugin/)

## Maven custom plugin


### Step:1

- First set of code after updating pom.xml file
  - Note, currently the java 24 is not supported and throwing exception

```java
// Below code is
@Mojo(name="echo",defaultPhase = LifecyclePhase.INITIALIZE)
public class SampleMvnPlugin extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("mvn custom plugin - starter");
    }
}
```

- Build the project

```shell
$ mvn clean install 
```

- Run the command `mvn {groupId}:{artifactId}:{goal}` and should see the output 
- The output from the `getLog.info()` method.

```shell
$ mvn org.sample.mvn:sample-mvn-plugin:echo
```

### Step: 2
- Add command and MojoProject variable

```java
@Mojo(name="echo",defaultPhase = LifecyclePhase.INITIALIZE)
public class SampleMvnPlugin extends AbstractMojo {

    @Parameter(property = "git.command",defaultValue = "git rev-parse --short HEAD")
    private String command;

    @Parameter(property = "project",readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String version = getVersion(command);
        project.getProperties().put("projVersion", version);
        getLog().info("Git hash version "+version);
    }

    public String getVersion(String command){
        StringBuffer sb = new StringBuffer();
        try{
            Process process = Runtime.getRuntime().exec(command);
              Executors.newSingleThreadExecutor()
                   .submit(() -> new BufferedReader(new InputStreamReader(process.getInputStream()))
                   .lines()
                   .forEach(sb::append));
            int exitCode = process.waitFor();

            if(exitCode != 0){
                throw new MojoExecutionException("Exception occurred executing command "+command + "with exit code: "+exitCode);
            }
        } catch (IOException | MojoExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
```
- Build the project using `mvn clean install`
- Use below command to execute the project first one uses the defaultValue from the command
- If using the `outputDirectory` in the configuration of `maven-plugin-plugin` plugin make sure to package it in jar

```shell
$ mvn org.sample.mvn:sample-mvn-plugin:echo
```

```shell
$ mvn org.sample.mvn:sample-mvn-plugin:echo -Dgit.command="git rev-parse --short HEAD"
```

### Step: 3
- To extract the`getVersion()` method to its own class and inject to the main Mojo class
  - The extracted class is injected using JSR-330

The latest code in the project can be executed similar to above.

Note, based on this documentation [maven documentation](https://maven.apache.org/maven-jsr330.html) 
- Add the dependency for `javax.inject`, and add the plugin `sisu-maven-plugin`.
- The `@Inject` annotation should be applied on the constructor so it works in conjunction with`@Named` and `@Singleton`

### Testing with the pom.xml

- Create a folder test-plugin/pom.xml with the contents, to run the test pom.xml use below command

```shell
$ mvn package -f test-plugin/pom.xml
```

- Output
```
[INFO] --- sample-plugin-prefix:1.0-SNAPSHOT:echo (default) @ sample-plugin-usage ---
[INFO] Git hash version 3337
```

- In the main pom.xml add the `reporting` section and organization tag and pre-requisites
- Add below content and issue `mvn site` this will be generate the html

```xml
  <prerequisites>
    <maven>3.9.0</maven>
  </prerequisites>

```

```xml
  <reporting>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-project-info-reports-plugin</artifactId>
      <version>3.9.0</version>
      <reportSets>
        <reportSet>
          <reports>
            <report>index</report>
            <report>dependencies</report>
            <report>team</report>
            <report>mailing-lists</report>
            <report>ci-management</report>
            <report>issue-management</report>
            <report>licenses</report>
            <report>scm</report>
          </reports>
        </reportSet>
      </reportSets>
    </plugin>
  </plugins>
</reporting>
```

- To see the details of the custom plugin goal, use the below command

```shell
$ mvn help:describe -Dplugin=org.sample.mvn:sample-mvn-plugin -Ddetail
```
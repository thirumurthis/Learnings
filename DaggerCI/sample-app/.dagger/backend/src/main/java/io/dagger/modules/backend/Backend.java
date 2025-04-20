package io.dagger.modules.backend;

import io.dagger.client.CacheVolume;
import io.dagger.client.Container;
import io.dagger.client.DaggerQueryException;
import io.dagger.client.Directory;
import io.dagger.client.File;
import io.dagger.client.Service;
import io.dagger.module.annotation.Function;
import io.dagger.module.annotation.Object;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.dagger.client.Dagger.dag;

/** Backend main object */
@Object
public class Backend {
  /** Returns a container that echoes whatever string argument is provided */
  @Function
  public Container containerEcho(String stringArg) {
    return dag().container().from("alpine:latest").withExec(List.of("echo", stringArg));
  }

  /** Returns lines that match a pattern in the files of the provided Directory */
  @Function
  public String grepDir(Directory directoryArg, String pattern)
      throws InterruptedException, ExecutionException, DaggerQueryException {
    return dag()
        .container()
        .from("alpine:latest")
        .withMountedDirectory("/mnt", directoryArg)
        .withWorkdir("/mnt")
        .withExec(List.of("grep", "-R", pattern, "."))
        .stdout();
  }

  /** Create environment for build **/
  @Function
  public Container buildEnv(Directory source)
          throws InterruptedException, ExecutionException, DaggerQueryException {
    CacheVolume mavenCache = dag().cacheVolume("maven-cache");
    return dag().container()
            .from("maven:3.9.9-amazoncorretto-21")
            .withMountedCache("root/.m2",mavenCache)
            .withMountedDirectory("/app/backend",source
                    .withoutDirectory(".idea")
                    .withoutDirectory(".dagger"))
            .withWorkdir("/app/backend");
  }

  /** Build jar artifacts*/
  @Function
  public File build(Directory source)
          throws InterruptedException, ExecutionException, DaggerQueryException {

    return buildEnv(source)
            .withExec(List.of("mvn","-DskipTests","clean","install"))
            .file("target/sample-app-0.0.1-SNAPSHOT.jar");

  }

  /** Run test*/
  @Function
  public String test(Directory source)
          throws InterruptedException, ExecutionException, DaggerQueryException {

    return buildEnv(source)
            .withExec(List.of("mvn","test"))
            .stdout();

  }

  /** Publish the artifact */
  @Function
  public String publish(Directory source)
          throws InterruptedException, ExecutionException, DaggerQueryException {

    String dockerhub_url = "docker.io/thirumurthi/sample-app:latest";
    String dockerhub_token = "your_dockerhub_personal_access_token";
    return dag().container()
            .from("openjdk:21-jdk-slim")
            .withFile("/app/sample-app.jar",build(source))
            .withExec(List.of("chmod","777","/app/sample-app.jar"))
            .withEntrypoint(List.of("java","-jar","/app/sample-app.jar"))
            .withExposedPort(8080)
            //.publish("localhost:5000/simple-app:1.0.0")
            //.publish("localhost:5000/simple-app-%d".formatted((int) (Math.random() * 10000000)))
            .publish("ttl.sh/simple-dagger-app-%d".formatted((int) (Math.random() * 10000000)))
            ;
  }

  /** Runs as service*/
  @Function
  public Service run(Directory source)
          throws InterruptedException, ExecutionException, DaggerQueryException {

    return dag().container()
            .from("openjdk:21-jdk-slim")
            .withFile("/app/sample-app.jar",build(source))
            .withExec(List.of("chmod","777","/app/sample-app.jar"))
            .withEntrypoint(List.of("java","-jar","/app/sample-app.jar"))
            .withExposedPort(8080)
            .asService()
            .withHostname("localhost")
            //.asService(new Container.AsServiceArguments().withArgs(List.of("")))
            //.start()
            //.
            ;
    //.withExec(List.of("java","-jar","/app/sample-app.jar")) -- is not working when use as service
  }

  /** simple test container */
  @Function
  public Service testRun()
          throws InterruptedException, ExecutionException, DaggerQueryException {
    return dag().container()
            .from("nginx:latest")
            .withExposedPort(80)
            .asService()
            .withHostname("localhost")
            .start()
            ;
  }

  /** Build helper returns container*/
  @Function
  public Container buildHelper(Directory directoryArg)
          throws InterruptedException, ExecutionException, DaggerQueryException {

    return dag().container()
            .from("maven:3.9.9-amazoncorretto-21")
            .withMountedDirectory("/src",directoryArg)
            .withWorkdir("/src")
            //.withExec(List.of("ls","-lrt"))
            //.withExec(List.of("mvn","--version"))
            //.withExec(List.of("mvn","install"))
            //.stdout();
            //.withExec(List.of("mvn","-DskipTests","clean","install"))
            //.file("/src/target/sample-app-0.0.1-SNAPSHOT.jar")
            ;
  }

  /** Build the jar file*/
  @Function
  public File buildJar(Directory directoryArg)
          throws InterruptedException, ExecutionException, DaggerQueryException {

    return dag().container()
            .from("maven:3.9.9-amazoncorretto-21")
            .withMountedDirectory("/src",directoryArg)
            .withWorkdir("/src")
            //.withExec(List.of("ls","-lrt"))
            //.withExec(List.of("mvn","--version"))
            .withExec(List.of("mvn","-DskipTests","clean","install"))
            //.stdout();
            //.withExec(List.of("mvn","-DskipTests","clean","install"))
            .file("target/sample-app-0.0.1-SNAPSHOT.jar")
            ;
  }
}

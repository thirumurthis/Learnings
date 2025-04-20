package io.dagger.modules.sampleapp;

import io.dagger.client.Container;
import io.dagger.client.DaggerQueryException;
import io.dagger.client.Directory;
import io.dagger.client.Service;
import io.dagger.module.annotation.Function;
import io.dagger.module.annotation.Object;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.dagger.client.Dagger.dag;

/** SampleApp main object */
@Object
public class SampleApp {
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

  /** publish image of the apps */
  @Function
  public String publish(Directory source)
          throws InterruptedException, ExecutionException, DaggerQueryException {
    return dag().backend().publish(source);
  }

  /** Run the application as service*/
  @Function
  public Service run(Directory source){
    return dag().backend().run(source);
  }
}

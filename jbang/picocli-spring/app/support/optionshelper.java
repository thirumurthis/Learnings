package app.support;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(name="appcli", mixinStandardHelpOptions= true, 
         version="app-cli v1.0", description="Takes input and fetch random message from static list of message" )
public class optionshelper implements Callable<Integer> {

    private final servicehelper svcHelper;

    @Parameters(index="0", description="input user name")
    private String userName;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display help message")
    private boolean helpflag = false;

    public optionshelper(servicehelper svcHelper){
        this.svcHelper = svcHelper;
    }

    @Override
    public Integer call() throws Exception{
        try{
            System.out.println(svcHelper.process(userName));
        } catch(Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.err.println(sw.toString());
            return -1;
        }

        return 0;
    }
}

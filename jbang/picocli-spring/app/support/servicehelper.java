package app.support;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class servicehelper {
    
    public servicehelper(){}

    Random random = new Random();

    List<String> msgs = List.of("If there is a cat but where will the rat be?",
                                "If peacock features has colors will it be a rainbow?",
                                "If tigers are in the zoo what it would think of the wild?",
                                "If monkey runs around why not it climb up the tree?"
                                );

    public String process(String input){
        String msg = msgs.get(random.nextInt(msgs.size()));
        String format = """
                { "input" : "%s", "msg" : "%s" }
                """;
        return String.format(format, input, msg);
    }
}

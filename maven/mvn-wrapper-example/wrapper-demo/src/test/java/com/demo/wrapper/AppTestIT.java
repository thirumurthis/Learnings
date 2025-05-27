package com.demo.wrapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AppTestIT {

    @Test
    void getMessageTest(){
        App app = new App();

        Assertions.assertEquals("app message",app.getMessage());
    }
}

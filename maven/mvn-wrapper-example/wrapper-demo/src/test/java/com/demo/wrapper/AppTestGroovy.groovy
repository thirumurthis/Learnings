package com.demo.wrapper

import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

import java.util.stream.Stream
import static org.junit.jupiter.api.Assertions.assertTrue;


class AppTestGroovy {
    @Test
    void streamSum() {
        assertTrue(Stream.of(1, 2, 3)
                .mapToInt(i -> i)
                .sum() > 5, () -> "Sum should be greater than 5")
    }

    @RepeatedTest(value=2, name = "{displayName} {currentRepetition}/{totalRepetitions}")
    void streamSumRepeated() {
        assert Stream.of(1, 2, 3).mapToInt(i -> i).sum() == 6
    }
}

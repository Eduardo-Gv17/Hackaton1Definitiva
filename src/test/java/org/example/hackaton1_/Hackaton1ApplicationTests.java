package org.example.hackaton1_;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class Hackaton1ApplicationTests {

    @Test
    void contextLoads() {
    }

}

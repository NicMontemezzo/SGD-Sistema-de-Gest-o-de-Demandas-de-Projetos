package br.com.uniamerica.sgd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "sgd.jwt.secret=segredo-de-teste-com-pelo-menos-32-bytes")
class SgdApplicationIT {

    @Test
    void contextLoads() {
    }
}

package com.oalbarber.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "JWT_SECRETO=clave-falsa-solo-pruebas-no-usar-produccion-123456")
class ApiApplicationTests {

	@Test
	void contextLoads() {
	}

}

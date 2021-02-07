package se.skltp.ei;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(classes = {EiBackendApplication.class})
class EiBackendApplicationTest {
  @Test
  public void contextLoads() {
  }
}

package se.simple.simplelog.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("integrationtest")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional(propagation = Propagation.NEVER)
@Rollback(value = false)
class LogTypeRepositoryIntegrationTest {

    @Autowired
    private LogTypeRepository logTypeRepository;

    @Test
    void givenLogTypes_WhenGettingLogTypes_ShouldReturnLogTypes() {
        assertThat(logTypeRepository.findByName("DEVICE_ASSIGNED")).isNotEmpty();
    }

}

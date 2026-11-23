package Therapie_Management.backend.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * Laedt data.sql in die persistente Datenbank, aber nur beim allerersten Start. Spring Boots
 * eigene data.sql-Ausfuehrung (spring.sql.init.mode) wuerde bei jedem Neustart erneut inserten
 * und an den Primaerschluesseln scheitern bzw. Daten duplizieren.
 */
@Component
public class DatabaseSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSeeder(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        if (userCount != null && userCount > 0) {
            assignDefaultModules();
            return;
        }
        log.info("Leere Datenbank erkannt - lade Startdaten aus data.sql");
        new ResourceDatabasePopulator(new ClassPathResource("data.sql")).execute(dataSource);
    }

    /**
     * Migration fuer Datenbanken aus der Zeit vor der Modul-Zuordnung: Inhalte ohne Modul waeren
     * fuer Patienten unsichtbar. Uebungen kommen in Modul 3, alle anderen Tipps in Modul 2.
     */
    private void assignDefaultModules() {
        int uebungen = jdbcTemplate.update("UPDATE contents SET modul_id = 3 WHERE modul_id IS NULL AND kategorie_id IN "
            + "(SELECT id FROM categories WHERE name = 'Uebungen') AND EXISTS (SELECT 1 FROM modules WHERE id = 3)");
        int tipps = jdbcTemplate.update("UPDATE contents SET modul_id = 2 WHERE modul_id IS NULL "
            + "AND EXISTS (SELECT 1 FROM modules WHERE id = 2)");
        if (uebungen + tipps > 0) {
            log.info("{} Inhalte ohne Modul wurden Standard-Modulen zugeordnet", uebungen + tipps);
        }
    }
}

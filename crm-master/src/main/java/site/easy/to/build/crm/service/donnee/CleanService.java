package site.easy.to.build.crm.service.donnee;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CleanService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void cleanTables(List<? extends JpaRepository<?, ?>> repositories) {
    
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        for (JpaRepository<?, ?> repository : repositories) {
            repository.deleteAll();
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    /**
     * Vide une table en désactivant temporairement les contraintes de clé étrangère.
     *
     * @param tableNames une liste de nom de table à nettoyer.
     */
    public void cleanTable(List<String> tableNames) {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        for(String tableName : tableNames){
            jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
}

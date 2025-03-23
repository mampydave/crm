package site.easy.to.build.crm.service.donnee;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportService {

    private List<Object> entities = new ArrayList<>();

    public <T> List<String> importCsv(MultipartFile file, Class<T> entityClass) {
        List<String> errorMessages = new ArrayList<>();
        entities.clear();

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] headers = csvReader.readNext(); // Lire la première ligne (en-têtes)

            String[] line;
            int lineNumber = 1; 
            while ((line = csvReader.readNext()) != null) {
                try {

                    T entity = entityClass.getDeclaredConstructor().newInstance();


                    for (int i = 0; i < headers.length; i++) {
                        String header = headers[i];
                        String value = line[i];


                        Field field = entityClass.getDeclaredField(header);
                        field.setAccessible(true); // Permet d'accéder aux champs privés


                        if (field.getType() == String.class) {
                            field.set(entity, value);
                        } else if (field.getType() == Integer.class || field.getType() == int.class) {
                            field.set(entity, Integer.parseInt(value));
                        } else if (field.getType() == Double.class || field.getType() == double.class) {
                            field.set(entity, Double.parseDouble(value));
                        } else if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                            field.set(entity, Boolean.parseBoolean(value));
                        }

                    }

                    entities.add(entity); 
                } catch (Exception e) {
                    errorMessages.add("Erreur lors de la lecture de la ligne " + lineNumber + ": " + e.getMessage());
                }
                lineNumber++;
            }
        } catch (IOException | CsvValidationException e) {
            errorMessages.add("Erreur lors de la lecture du fichier CSV: " + e.getMessage());
        }

        return errorMessages;
    }

    public List<Object> getEntities() {
        return entities;
    }
}
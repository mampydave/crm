package site.easy.to.build.crm.service.donnee;

import com.opencsv.CSVReader;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.entity.User;


import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
@Transactional
public class ImportService {
    private static final Logger logger = LoggerFactory.getLogger(ImportService.class);
    
    @PersistenceContext
    private EntityManager entityManager;

    public Map<String, List<String>> importAllFiles(MultipartFile customerFile, 
                                                  MultipartFile budgetFile, 
                                                  MultipartFile ticketLeadFile,
                                                  User currentUser) {
        
        Map<String, List<String>> allErrors = new LinkedHashMap<>();
        Map<String, Customer> customerCache = new HashMap<>();
        List<Object> entitiesToPersist = new ArrayList<>();

        // 1. Validation complète avant toute persistance
        if (customerFile != null && !customerFile.isEmpty()) {
            List<String> errors = validateCustomerFile(customerFile, customerCache, currentUser, entitiesToPersist);
            if (!errors.isEmpty()) {
                allErrors.put(customerFile.getOriginalFilename(), errors);
            }
        }

        if (budgetFile != null && !budgetFile.isEmpty()) {
            List<String> errors = validateBudgetFile(budgetFile, customerCache, entitiesToPersist);
            if (!errors.isEmpty()) {
                allErrors.put(budgetFile.getOriginalFilename(), errors);
            }
        }

        if (ticketLeadFile != null && !ticketLeadFile.isEmpty()) {
            List<String> errors = validateTicketLeadFile(ticketLeadFile, customerCache, currentUser, entitiesToPersist);
            if (!errors.isEmpty()) {
                allErrors.put(ticketLeadFile.getOriginalFilename(), errors);
            }
        }


        if (!allErrors.isEmpty()) {
            return allErrors;
        }


        try {
            for (Object entity : entitiesToPersist) {
                entityManager.persist(entity);
            }
            entityManager.flush();
        } catch (Exception e) {
            logger.error("Erreur lors de la persistance", e);
            allErrors.put("system", List.of("Erreur système lors de l'import: " + e.getMessage()));
            throw new TransactionSystemException("Rollback dû à une erreur de persistance");
        }

        return allErrors;
    }

    private List<String> validateCustomerFile(MultipartFile file, Map<String, Customer> customerCache, 
                                           User currentUser, List<Object> entitiesToPersist) {
        List<String> errors = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = reader.readNext();
            String[] line;
            int lineNum = 1;
            
            while ((line = reader.readNext()) != null) {
                lineNum++;
                try {
                    if (line.length < 2) {
                        errors.add(formatError(lineNum, "Nombre de colonnes insuffisant", ""));
                        continue;
                    }
                    
                    String email = line[0].trim();
                    if (email.isEmpty()) {
                        errors.add(formatError(lineNum, "Email vide", ""));
                        continue;
                    }
                    
                    if (customerCache.containsKey(email) || customerExists(email)) {
                        errors.add(formatError(lineNum, "Email déjà existant", email));
                        continue;
                    }
                    
                    Customer customer = new Customer();
                    customer.setEmail(email);
                    customer.setName(line[1].trim());
                    customer.setUser(currentUser);
                    customer.setCreatedAt(LocalDateTime.now());
                    customer.setCountry("FR");

                    if (customer.getCountry() == null || customer.getCountry().isEmpty()) {
                        errors.add(formatError(lineNum, "Le pays est obligatoire", ""));
                        continue;
                    }
                    
                    customerCache.put(email, customer);
                    entitiesToPersist.add(customer);
                    
                } catch (Exception e) {
                    errors.add(formatError(lineNum, e.getMessage(), Arrays.toString(line)));
                    logger.error("Erreur validation client ligne {}: {}", lineNum, e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Erreur lecture fichier: " + e.getMessage());
            logger.error("Erreur lecture fichier clients", e);
        }
        return errors;
    }

    private List<String> validateBudgetFile(MultipartFile file, Map<String, Customer> customerCache, 
                                         List<Object> entitiesToPersist) {
        List<String> errors = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = reader.readNext();
            String[] line;
            int lineNum = 1;
            
            while ((line = reader.readNext()) != null) {
                lineNum++;
                try {
                    if (line.length < 2) {
                        errors.add(formatError(lineNum, "Nombre de colonnes insuffisant", ""));
                        continue;
                    }
                    
                    String email = line[0].trim();
                    Customer customer = customerCache.get(email);
                    if (customer == null) {
                        errors.add(formatError(lineNum, "Client introuvable (email non importé)", email));
                        continue;
                    }
                    
                    BigDecimal amount;
                    try {
                        amount = new BigDecimal(line[1].trim().replace(",", "."));
                        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new IllegalArgumentException("Montant doit être positif");
                        }
                    } catch (Exception e) {
                        errors.add(formatError(lineNum, "Montant invalide", line[1]));
                        continue;
                    }
                    
                    Budget budget = new Budget();
                    budget.setAmount(amount);
                    budget.setCustomer(customer);
                    
                    
                    entitiesToPersist.add(budget);
                    
                } catch (Exception e) {
                    errors.add(formatError(lineNum, e.getMessage(), Arrays.toString(line)));
                    logger.error("Erreur validation budget ligne {}: {}", lineNum, e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Erreur lecture fichier: " + e.getMessage());
            logger.error("Erreur lecture fichier budgets", e);
        }
        return errors;
    }

    private List<String> validateTicketLeadFile(MultipartFile file, Map<String, Customer> customerCache, 
                                             User currentUser, List<Object> entitiesToPersist) {
        List<String> errors = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = reader.readNext();
            String[] line;
            int lineNum = 1;
            
            while ((line = reader.readNext()) != null) {
                lineNum++;
                try {
                    if (line.length < 5) {
                        errors.add(formatError(lineNum, "Nombre de colonnes insuffisant", ""));
                        continue;
                    }
                    
                    String email = line[0].trim();
                    Customer customer = customerCache.get(email);
                    if (customer == null) {
                        errors.add(formatError(lineNum, "Client introuvable (email non importé)", email));
                        continue;
                    }
                    
                    String type = line[2].trim().toLowerCase();
                    if (!"ticket".equals(type) && !"lead".equals(type)) {
                        errors.add(formatError(lineNum, "Type invalide (doit être 'ticket' ou 'lead')", type));
                        continue;
                    }
                    
                    if ("ticket".equals(type)) {
                        validateTicket(line, customer, currentUser, errors, lineNum, entitiesToPersist);
                    } else {
                        validateLead(line, customer, currentUser, errors, lineNum, entitiesToPersist);
                    }
                    
                } catch (Exception e) {
                    errors.add(formatError(lineNum, e.getMessage(), Arrays.toString(line)));
                    logger.error("Erreur validation ticket/lead ligne {}: {}", lineNum, e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Erreur lecture fichier: " + e.getMessage());
            logger.error("Erreur lecture fichier tickets/leads", e);
        }
        return errors;
    }

    private void validateTicket(String[] data, Customer customer, User currentUser, 
                              List<String> errors, int lineNum, List<Object> entitiesToPersist) {
        String status = data[3].trim();
        
        // Set<String> validStatuses = Set.of(
        //     "open", "assigned", "on-hold", "in-progress", 
        //     "resolved", "closed", "reopened", 
        //     "pending-customer-response", "escalated", "archived"
        // );
    

        // if (!validStatuses.contains(status)) {
        //     errors.add(formatError(lineNum, 
        //         "Statut de ticket invalide. Valide: " + String.join(", ", validStatuses), 
        //         data[3]));

        // }
        
        BigDecimal amount;
        try {
            amount = parseAmount(data[4]);
        } catch (Exception e) {
            errors.add(formatError(lineNum, "Montant dépense invalide", data[4]));
            return;
        }
        
        Ticket ticket = new Ticket();
        ticket.setCustomer(customer);
        ticket.setSubject(data[1].trim());
        ticket.setStatus("open");
        ticket.setPriority("medium");
        ticket.setManager(currentUser);
        ticket.setCreatedAt(LocalDateTime.now());
        
        entitiesToPersist.add(ticket);
        
        // On crée la dépense mais on ne la persiste pas encore
        Depense depense = new Depense();
        depense.setAmount(amount);
        depense.setCustomer(customer);
        depense.setTicket(ticket); // La relation sera établie après la persistance
        
        entitiesToPersist.add(depense);
    }

    private void validateLead(String[] data, Customer customer, User currentUser, 
                            List<String> errors, int lineNum, List<Object> entitiesToPersist) {
        String status = data[3].trim();
        // if (!status.matches("^(meeting-to-schedule|scheduled|archived|success|assign-to-sales)$")) {
        //     errors.add(formatError(lineNum, "Status de lead invalide", status));
        //     return;
        // }

        // Set<String> validStatuses = Set.of(
        //     "meeting-to-schedule", "scheduled", "archived", "success", 
        //     "assign-to-sales"
        // );
    

        // if (!validStatuses.contains(status)) {
        //     errors.add(formatError(lineNum, 
        //         "Status de lead invalide. Valide: " + String.join(", ", validStatuses), 
        //         data[3]));

        // }
        
        BigDecimal amount;
        try {
            amount = parseAmount(data[4]);
        } catch (Exception e) {
            errors.add(formatError(lineNum, "Montant dépense invalide", data[4]));
            return;
        }
        
        Lead lead = new Lead();
        lead.setCustomer(customer);
        lead.setName(data[1].trim());
        lead.setStatus("success");
        lead.setManager(currentUser);
        lead.setCreatedAt(LocalDateTime.now());
        
        entitiesToPersist.add(lead);
        
        // On crée la dépense mais on ne la persiste pas encore
        Depense depense = new Depense();
        depense.setAmount(amount);
        depense.setCustomer(customer);
        depense.setLead(lead);
        
        entitiesToPersist.add(depense);
    }

    public BigDecimal parseAmount(String amountStr) throws IllegalArgumentException {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Montant vide");
        }
        
        String cleanedAmount = amountStr.replace("\"", "").trim();
        

        cleanedAmount = cleanedAmount.replace(",", ".");
        
        try {
            BigDecimal amount = new BigDecimal(cleanedAmount);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Montant doit être positif");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format de montant invalide: " + amountStr);
        }
    }

    private boolean customerExists(String email) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(c) FROM Customer c WHERE c.email = :email", Long.class)
            .setParameter("email", email)
            .getSingleResult();
        return count > 0;
    }

    private String formatError(int lineNum, String message, String value) {
        return String.format("Ligne %d - %s: %s", lineNum, message, value);
    }
}
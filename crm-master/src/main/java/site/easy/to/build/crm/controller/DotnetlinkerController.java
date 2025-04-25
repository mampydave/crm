package site.easy.to.build.crm.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.donnee.ImportService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.money.AlerteService;
import site.easy.to.build.crm.service.money.BudgetService;
import site.easy.to.build.crm.service.money.DepenseService;
import site.easy.to.build.crm.service.ticket.TicketService;
import site.easy.to.build.crm.service.user.UserService;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class DotnetlinkerController {
    private static final Logger log = LoggerFactory.getLogger(DotnetlinkerController.class);
    private final AlerteService alerteService;
    private final BudgetService budgetService;
    private final DepenseService depenseService;
    private final CustomerService customerService;
    private final TicketService ticketService;
    private final LeadService leadService;
    private final ImportService importService;
    private final UserService userService;



    @Autowired
    public DotnetlinkerController(AlerteService alerteService, BudgetService budgetService, DepenseService depenseService, 
                                  CustomerService customerService, TicketService ticketService, LeadService leadService,ImportService importService,UserService userService) {
        this.alerteService = alerteService;
        this.budgetService = budgetService;
        this.depenseService = depenseService;
        this.customerService = customerService;
        this.ticketService = ticketService;
        this.leadService = leadService;
        this.importService = importService;
        this.userService = userService;
    }

    @GetMapping("/dashboard-data")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> client = new HashMap<>();
        client.put("totalclient", customerService.count());
        client.put("depenseclient", depenseService.getSumDepenseOfAllUsers());
        client.put("budgetclient", budgetService.getSumBudgetOfAllCustomer());

        Map<String, Object> ticket = new HashMap<>();
        ticket.put("totalticket", ticketService.count());
        ticket.put("depenseticket", depenseService.getGlobalSumDepensesOfTicket());

        Map<String, Object> lead = new HashMap<>();
        lead.put("totallead", leadService.count());
        lead.put("depenselead", depenseService.getGlobalSumDepensesOfLead());

        List<Map<String, Object>> detailcli = new ArrayList<>();
        
        for (Customer custome : customerService.findAll()) {
            Map<String, Object> clientel = new HashMap<>();
            clientel.put("cliId", custome.getCustomerId());
            clientel.put("cliName", custome.getName());
            clientel.put("detailbudgetcli", budgetService.getBudgetsByCustomerId(custome.getCustomerId()));
            clientel.put("detaildepensecli", depenseService.getSumDepenseByIdCustomer(custome.getCustomerId()));
            detailcli.add(clientel);
        }
        

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("client", client);
        responseData.put("ticket", ticket);
        responseData.put("lead", lead);
        responseData.put("detail", detailcli);
        responseData.put("taux", alerteService.getTauxAlerteGlobal());
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/client-info")
    public Map<String, Object> getClientInfo() {
        List<Map<String, Object>> infoClient = new ArrayList<>();
        
        for (Customer customer : customerService.findAll()) {
            Map<String, Object> client = new HashMap<>();
            client.put("name", customer.getName());
            client.put("phone", customer.getPhone());
            client.put("address", customer.getAddress());
            client.put("budget", budgetService.getBudgetsByCustomerId(customer.getCustomerId()));   
            infoClient.add(client);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("infoClient", infoClient);
        return response;
    }

    @GetMapping("/ticket-info")
    public Map<String, Object> allTicket() {
        List<Map<String, Object>> infoticket = new ArrayList<>();
        
        for (Ticket ticket : ticketService.findAll()) {
            Map<String, Object> ticketMap = new HashMap<>();
            ticketMap.put("ticketId", ticket.getTicketId());
            ticketMap.put("subject", ticket.getSubject());
            ticketMap.put("status", ticket.getStatus());
            ticketMap.put("depense", depenseService.findAmountTicketId(ticket.getTicketId()));
            ticketMap.put("client", ticket.getCustomer().getName());   
            infoticket.add(ticketMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("infoTicket", infoticket);
        return response;
    }

    @GetMapping("/lead-info")
    public Map<String, Object> allLead() {
        List<Map<String, Object>> infolead = new ArrayList<>();
        
        for (Lead lead : leadService.findAll()) {
            Map<String, Object> leadMap = new HashMap<>();
            leadMap.put("leadId", lead.getLeadId());
            leadMap.put("name", lead.getName());
            leadMap.put("status", lead.getStatus());
            leadMap.put("depense", depenseService.findAmountLeadId(lead.getLeadId())); 
            leadMap.put("client", lead.getCustomer().getName());  
            infolead.add(leadMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("infoLead", infolead);
        return response;
    }

    @PostMapping("/taux")
    public ResponseEntity<?> configurerTaux(@RequestBody double pourcentage) {
        try {
            alerteService.saveTauxAlerteGlobal(pourcentage);
            return ResponseEntity.ok().body("{\"message\": \"Taux d'alerte configuré avec succès !\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Erreur lors de la configuration du taux d'alerte.\"}");
        }
    }

    @DeleteMapping("/ticket/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable int id) {
        try {
            boolean exists = ticketService.existsById(id);
            if (!exists) {
                return ResponseEntity.status(404).body("{\"error\": \"Ticket non trouvé.\"}");
            }
            ticketService.deleteById(id);
            return ResponseEntity.ok().body("{\"message\": \"Ticket supprimé avec succès.\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Erreur lors de la suppression du ticket.\"}");
        }
    }


    @PutMapping("/ticket/update/{id}")
    @Transactional 
    public ResponseEntity<?> updateTicket(@PathVariable Integer id, @RequestBody double depense) {
        try {

            if (!depenseService.existsByTicket_TicketId(id)) {
                return ResponseEntity.status(404).body("{\"error\": \"Aucune dépense trouvée pour ce ticket.\"}");
            }
    

            Depense deps = depenseService.findDepenseByIdTicket(id);
            if (deps == null || deps.getCustomer() == null) {
                return ResponseEntity.status(404).body("{\"error\": \"Client associé introuvable.\"}");
            }
    
            Customer customer = customerService.findByCustomerId(deps.getCustomer().getCustomerId());
            if (customer == null) {
                return ResponseEntity.status(404).body("{\"error\": \"Client introuvable.\"}");
            }
    
            double sumBudget = budgetService.getSumBudgetByIdCustomer(customer.getCustomerId()).doubleValue();
            double sumDepense = depenseService.getSumDepenseByIdCustomer(customer.getCustomerId()).doubleValue();
    

            depenseService.updateAmountOfTicketId(id, depense);
    

            String message = "Ticket mis à jour avec succès.";
            boolean alerteAtteint = alerteService.alerteAtteint(sumBudget, sumDepense);    
            boolean budgetDepasser = alerteService.budgetDepasser(sumBudget, sumDepense + depense);
            
            if (budgetDepasser) {
                message = "Attention : Le budget du client " + customer.getName() + " est dépassé !";
            } else if (alerteAtteint) {
                message = "Attention : Le taux d'alerte est atteint sur le budget du client " + customer.getName() + " !";
            }
    
            return ResponseEntity.ok().body("{\"message\": \"" + message + "\"}");
        } catch (Exception e) {
            log.error("Erreur mise à jour ticket", e);
            return ResponseEntity.badRequest()
                .body("{\"error\": \"Erreur technique lors de la mise à jour.\""+e+"\"}");
        }
    }


    @DeleteMapping("/lead/{id}")
    public ResponseEntity<?> deleteLead(@PathVariable int id) {
        try {
            boolean exists = leadService.existsById(id);
            if (!exists) {
                return ResponseEntity.status(404).body("{\"error\": \"Lead non trouvé.\"}");
            }
            leadService.deleteById(id);
            return ResponseEntity.ok().body("{\"message\": \"Lead supprimé avec succès.\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Erreur lors de la suppression du Lead.\"}");
        }
    }


    @PutMapping("/lead/update/{id}")
    @Transactional
    public ResponseEntity<?> updateLead(@PathVariable Integer id, @RequestBody double depense) {
        try {

            boolean existingDepense = depenseService.existsByLead_LeadId(id);
            if (!existingDepense) {
                return ResponseEntity.status(404).body("{\"error\": \"Lead non trouvé.\"}");
            }

            Depense deps = depenseService.findDepenseByIdLead(id);
            String message = "Lead mis à jour avec succès.";

            double sumBudget = budgetService.getSumBudgetByIdCustomer(deps.getCustomer().getCustomerId()).doubleValue();

            double sumDepense = depenseService.getSumDepenseByIdCustomer(deps.getCustomer().getCustomerId()).doubleValue();

            depenseService.updateAmountOfLeadId(id, depense);

            boolean alerteAtteint = alerteService.alerteAtteint(sumBudget, sumDepense);    
            boolean budgetDepasser = alerteService.budgetDepasser(sumBudget, sumDepense);
            
            if (alerteAtteint) {
                Customer customer = customerService.findByCustomerId(deps.getCustomer().getCustomerId());
                message = "Attention : Le taux d'alerte est atteint sur le budget du client "+customer.getName()+" !";
            }
            if (budgetDepasser) {
                Customer customer = customerService.findByCustomerId(deps.getCustomer().getCustomerId());
                message = "Attention : Le budget du client "+customer.getName()+" est dépassé !";
            }

            return ResponseEntity.ok().body("{\"message\": \"" + message + "\"}");            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Erreur lors de la mise à jour du Lead.\""+e+"\"}");
        }
    }

    @PostMapping(value = "/import", consumes = "text/csv")
    public ResponseEntity<?> importCustomers(@RequestBody String csvData) {
        try {
            List<String[]> importedData = parseCsvData(csvData);
            

            processImportedData(importedData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Import réalisé avec succès");
            response.put("importedCount", importedData.size());
            
            return ResponseEntity.ok(response);
        } catch (CsvValidationException e) {
            return ResponseEntity.badRequest().body(
                "Erreur de validation CSV: " + e.getMessage()
            );
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                "Erreur de lecture du fichier: " + e.getMessage()
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                "Erreur lors de l'import: " + e.getMessage()
            );
        }
    }

    @PostMapping(value = "/import-file", consumes = "multipart/form-data")
    public ResponseEntity<?> importCustomersFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Le fichier est vide");
            }
            
            List<String[]> importedData = parseCsvFile(file);
            processImportedData(importedData);
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body("{\"success\": true, \"message\": \"Fichier importé avec succès\", \"importedCount\": " + importedData.size() + "}");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private List<String[]> parseCsvData(String csvData) throws IOException, CsvValidationException {
        List<String[]> result = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new StringReader(csvData))) {
            reader.skip(1); // Skip header row
            String[] line;
            while ((line = reader.readNext()) != null) {
                result.add(line);
            }
        }
        return result;
    }

    private List<String[]> parseCsvFile(MultipartFile file) throws IOException, CsvValidationException {
        List<String[]> result = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            reader.skip(1); // Skip header row
            String[] line;
            while ((line = reader.readNext()) != null) {
                result.add(line);
            }
        }
        return result;
    }


    @Transactional
    private void processImportedData(List<String[]> importedData) {
            importedData.forEach(row -> {
                try {

                    if (row.length < 7) {
                        throw new IllegalArgumentException("Ligne CSV incomplète");
                    }
        
                    String email = row[0];
                    String name = row[1];
                    String user_id = row[2];
                    String subject_or_name = row[3];
                    String type = row[4].toLowerCase();
                    String status = row[5];
                    String expense = row[6];
        

                    User user = userService.findById(Integer.parseInt(user_id));
                    if (user == null) {
                        throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + user_id);
                    }
        

                    Customer customer = customerService.findByEmail(email);
                    if (customer == null) {
                        customer = new Customer();
                        customer.setEmail(email);
                        customer.setName(name);
                        customer.setUser(user);
                        customer.setCreatedAt(LocalDateTime.now());
                        customer.setCountry("Mada");
                        customer = customerService.save(customer);
                    }
        

                    BigDecimal amount = importService.parseAmount(expense);
                    Depense depense = new Depense();
                    depense.setAmount(amount);
                    depense.setCustomer(customer);

        
                    if ("lead".equals(type)) {
                        Lead lead = new Lead();
                        lead.setCustomer(customer);
                        lead.setName(subject_or_name);
                        lead.setStatus(status);
                        lead.setManager(user);
                        lead.setCreatedAt(LocalDateTime.now());
                        Lead savedLead = leadService.save(lead);
                        
                        depense.setLead(savedLead);
                        depenseService.saveDepense(depense);
                    } 
                    else if ("ticket".equals(type)) {
                        Ticket ticket = new Ticket();
                        ticket.setCustomer(customer);
                        ticket.setSubject(subject_or_name);
                        ticket.setPriority("medium");
                        ticket.setStatus(status);
                        ticket.setManager(user);
                        ticket.setCreatedAt(LocalDateTime.now());
                        Ticket savedTicket = ticketService.save(ticket);
                        
                        depense.setTicket(savedTicket);
                        depenseService.saveDepense(depense);
                    } else {
                        throw new IllegalArgumentException("Type non reconnu: " + type);
                    }
        
                } catch (Exception e) {
                    log.error("Erreur lors du traitement de la ligne: " + String.join(",", row), e);
                    
                    throw e;
                }
            });
        }
}

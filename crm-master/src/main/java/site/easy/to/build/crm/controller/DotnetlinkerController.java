package site.easy.to.build.crm.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.money.AlerteService;
import site.easy.to.build.crm.service.money.BudgetService;
import site.easy.to.build.crm.service.money.DepenseService;
import site.easy.to.build.crm.service.ticket.TicketService;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class DotnetlinkerController {
    
    private final AlerteService alerteService;
    private final BudgetService budgetService;
    private final DepenseService depenseService;
    private final CustomerService customerService;
    private final TicketService ticketService;
    private final LeadService leadService;

    @Autowired
    public DotnetlinkerController(AlerteService alerteService, BudgetService budgetService, DepenseService depenseService, 
                                  CustomerService customerService, TicketService ticketService, LeadService leadService) {
        this.alerteService = alerteService;
        this.budgetService = budgetService;
        this.depenseService = depenseService;
        this.customerService = customerService;
        this.ticketService = ticketService;
        this.leadService = leadService;
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

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("client", client);
        responseData.put("ticket", ticket);
        responseData.put("lead", lead);

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
    public ResponseEntity<?> updateTicket(@PathVariable Integer id, @RequestBody double depense) {
        try {

            boolean existingDepense = depenseService.existsByTicket_TicketId(id);
            if (!existingDepense) {
                return ResponseEntity.status(404).body("{\"error\": \"Ticket non trouvé.\"}");
            }


            depenseService.updateAmountOfTicketId(id, depense);
            return ResponseEntity.ok().body("{\"message\": \"Ticket mis à jour avec succès.\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Erreur lors de la mise à jour du ticket.\"}");
        }
    }


    @DeleteMapping("/lead/{id}")
    public ResponseEntity<?> deleteLead(@PathVariable int id) {
        try {
            boolean exists = leadService.existsById(id);
            if (!exists) {
                return ResponseEntity.status(404).body("{\"error\": \"Ticket non trouvé.\"}");
            }
            leadService.deleteById(id);
            return ResponseEntity.ok().body("{\"message\": \"Ticket supprimé avec succès.\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Erreur lors de la suppression du ticket.\"}");
        }
    }


    @PutMapping("/lead/update/{id}")
    public ResponseEntity<?> updateLead(@PathVariable Integer id, @RequestBody double depense) {
        try {

            boolean existingDepense = depenseService.existsByLead_LeadId(id);
            if (!existingDepense) {
                return ResponseEntity.status(404).body("{\"error\": \"Ticket non trouvé.\"}");
            }


            depenseService.updateAmountOfLeadId(id, depense);
            return ResponseEntity.ok().body("{\"message\": \"Ticket mis à jour avec succès.\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Erreur lors de la mise à jour du ticket.\"}");
        }
    }
}

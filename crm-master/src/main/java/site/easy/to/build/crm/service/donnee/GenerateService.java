package site.easy.to.build.crm.service.donnee;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVWriter;

import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.repository.DepenseRepository;
import site.easy.to.build.crm.repository.LeadRepository;
import site.easy.to.build.crm.repository.TicketRepository;

@Service
public class GenerateService {
       
    private CustomerRepository customerRepository;
    
    private BudgetRepository budgetRepository;
    
    private TicketRepository ticketRepository;
    
    private LeadRepository leadRepository;
    
    private DepenseRepository depenseRepository;
    
    @Autowired
    public GenerateService(CustomerRepository customerRepository,BudgetRepository budgetRepository,TicketRepository ticketRepository,LeadRepository leadRepository,DepenseRepository depenseRepository){
        this.customerRepository = customerRepository;
        this.budgetRepository = budgetRepository;
        this.ticketRepository = ticketRepository;
        this.leadRepository = leadRepository;
        this.depenseRepository = depenseRepository;
    }

    @Transactional
    public void generateTestData() {
        // User manager = createUser("manager@test.com", "ROLE_MANAGER");
        // User employee = createUser("employee@test.com", "ROLE_EMPLOYEE");
        
        List<Customer> customers = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Customer c = new Customer();
            c.setName("Client " + i);
            c.setEmail("client"+i+"@test.com");
            c.setCountry("France");
            // c.setUser(i % 2 == 0 ? manager : employee);
            customers.add(customerRepository.save(c));
        }


        List<Budget> budgets = new ArrayList<>();
        for (Customer c : customers) {
            Budget b = new Budget();
            b.setCustomer(c);
            b.setAmount(BigDecimal.valueOf(10000 + Math.random() * 50000));
            budgets.add(budgetRepository.save(b));
        }

        for (Customer c : customers) {
            Ticket t = new Ticket();
            t.setSubject("Problème " + c.getName());
            t.setStatus(randomStatusTicket());
            t.setPriority(randomPriorityTicket());
            t.setCustomer(c);
            ticketRepository.save(t);

            Lead l = new Lead();
            l.setName("Opportunité " + c.getName());
            l.setStatus(randomStatusLead());
            l.setCustomer(c);
            leadRepository.save(l);
        }

        createDepensesForTicketsAndLeads();
    }

    private void createDepensesForTicketsAndLeads() {
        List<Ticket> tickets = ticketRepository.findAll();
        List<Lead> leads = leadRepository.findAll();

        tickets.forEach(t -> {
            Depense d = new Depense();
            d.setAmount(BigDecimal.valueOf(100 + Math.random() * 1000));
            d.setTicket(t);
            d.setCustomer(t.getCustomer());
            depenseRepository.save(d);
        });

        leads.forEach(l -> {
            Depense d = new Depense();
            d.setAmount(BigDecimal.valueOf(500 + Math.random() * 2000));
            d.setLead(l);
            d.setCustomer(l.getCustomer());
            depenseRepository.save(d);
        });
    }

    public String randomStatusLead() {
        String[] statuses = {
            "meeting-to-schedule",
            "scheduled",
            "archived",
            "success",
            "assign-to-sales"
        };
        return statuses[new Random().nextInt(statuses.length)];
    }

    public String randomStatusTicket() {
        String[] statuses = {
            "open",
            "assigned",
            "on-hold",
            "in-progress",
            "resolved",
            "closed",
            "reopened",
            "pending-customer-response",
            "escalated",
            "archived"
        };
        return statuses[new Random().nextInt(statuses.length)];
    }

    public String randomPriorityTicket() {
        String[] statuses = {
            "low",
            "medium",
            "high",
            "closed",
            "urgent",
            "critical"
        };
        return statuses[new Random().nextInt(statuses.length)];
    }

    // private User createUser(String email, String role) {
    //     User user = new User();
    //     user.setEmail(email);
    //     user.setPassword(passwordEncoder.encode("password"));
    //     user.setRole(role);
    //     return userRepository.save(user);
    // }

    public void writeCustomerDuplicateDateTOCSV(String filepath,List<String[]> data)throws IOException{
        String[] header ={
            "customer_email",
            "customer_name",
            "user_id",
            "subject_or_name",
            "type",
            "status",
            "expense"
        };
        int expectedColumn = header.length;

        try (CSVWriter writter = new CSVWriter(new FileWriter(filepath))){
            writter.writeNext(header);

            for (String[] row : data) {
                if (row.length != expectedColumn) {
                    throw new IllegalArgumentException("la ligne doit contenir : "+expectedColumn +" column");
                }
                writter.writeNext(row);
            }
            
        } catch (Exception e) {
            throw e;
        }
    }
}

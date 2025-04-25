package site.easy.to.build.crm.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.donnee.GenerateService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.money.DepenseService;
import site.easy.to.build.crm.service.ticket.TicketService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.AuthorizationUtil;

@Controller
@RequestMapping("/data/duplicate")
public class DupicateController {

    private final CustomerService customerService;
    private final LeadService leadService;
    private final TicketService ticketService;
    private final UserService userService;
    private final AuthenticationUtils authenticationUtils;
    private final GenerateService generateService;
    private final DepenseService depenseService;


    @Autowired
    public DupicateController(CustomerService customerService,LeadService leadService,TicketService ticketService,UserService userService,AuthenticationUtils authenticationUtils,GenerateService generateService,DepenseService depenseService){
        this.customerService = customerService;
        this.leadService = leadService;
        this.ticketService = ticketService;
        this.userService = userService;
        this.authenticationUtils = authenticationUtils;
        this.generateService = generateService;
        this.depenseService = depenseService;
    }

    @PostMapping("/duplicate-customer/{id}")
    @Transactional
    public String duplicateCustomer(@ModelAttribute("customer") Customer tempCustomer, BindingResult bindingResult ,@PathVariable("id") int id,Authentication authentication, RedirectAttributes redirectAttributes) {
        Customer customer;
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        if(user.isInactiveUser()) {
            return "error/account-inactive";
        }
        try {
            if(!AuthorizationUtil.hasRole(authentication,"ROLE_MANAGER")) {
                bindingResult.rejectValue("failedErrorMessage", "error.failedErrorMessage",
                        "Sorry, you are not authorized to delete this customer. Only administrators have permission to delete customers.");
                redirectAttributes.addFlashAttribute("bindingResult", bindingResult);
                return "redirect:/employee/customer/my-customers";
            }
            customer = customerService.findByCustomerId(id);
            List<Ticket> tickets = ticketService.findByCustomerCustomerId(id);
            List<Lead> leads = leadService.findByCustomerCustomerId(id);
            String filepath  ="E:\\data.csv";
            List<String[]> data = new ArrayList<>();

            for (Ticket ticket : tickets) {
                
                String[] put = {
                    "copy_"+customer.getEmail(),
                    "copy_"+customer.getName(),
                    String.valueOf(userId),
                    ticket.getSubject(),
                    "ticket",
                    ticket.getStatus(),
                    String.valueOf(depenseService.findAmountTicketId(ticket.getTicketId()))
                };
                data.add(put);
            }

            for (Lead lead : leads) {
                
                String[] put = {
                    "copy_"+customer.getEmail(),
                    "copy_"+customer.getName(),
                    String.valueOf(userId),
                    lead.getName(),
                    "lead",
                    lead.getStatus(),
                    String.valueOf(depenseService.findAmountLeadId(lead.getLeadId()))
                };
                data.add(put);
            }

            generateService.writeCustomerDuplicateDateTOCSV(filepath, data);
        }catch(Exception e){
            return e.getMessage();
        }
        return "redirect:/employee/customer/manager/all-customers";
    }
}

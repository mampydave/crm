package site.easy.to.build.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import site.easy.to.build.crm.entity.Contract;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.repository.ContractRepository;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.repository.TicketRepository;
import site.easy.to.build.crm.service.contract.ContractService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.donnee.ImportService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import site.easy.to.build.crm.entity.Contract;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.repository.ContractRepository;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.repository.TicketRepository;
import site.easy.to.build.crm.service.contract.ContractService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.donnee.ImportService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/data/import")
public class ImportController {
    private final ImportService importService;
    private final AuthenticationUtils authenticationUtils;
    private final UserService userService;
    @Autowired
    public ImportController(ImportService importService,AuthenticationUtils authenticationUtils,UserService userService){
        this.importService = importService;
        this.authenticationUtils = authenticationUtils;
        this.userService = userService;
    }
    @GetMapping("donnee/import")
    public String showImportPage() {
        return "donnee/import";
    }

    @PostMapping("/all")
    public String handleImport(
            @RequestParam("customerFile") MultipartFile customerFile,
            @RequestParam("budgetFile") MultipartFile budgetFile,
            @RequestParam("ticketLeadFile") MultipartFile ticketLeadFile,
            RedirectAttributes redirectAttributes,
            Authentication authentication) {
        
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User currentUser = userService.findById(userId);        
        Map<String, List<String>> allErrors = importService.importAllFiles(
            customerFile, budgetFile, ticketLeadFile, currentUser);
        
        if (!allErrors.isEmpty()) {
            redirectAttributes.addFlashAttribute("importErrors", allErrors);
        } else {
            redirectAttributes.addFlashAttribute("successMessage", 
                "Tous les fichiers ont été importés avec succès");
        }
        
        return "redirect:/data/import/donnee/import";
    }
}
package site.easy.to.build.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import site.easy.to.build.crm.entity.Contract;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.repository.ContractRepository;
import site.easy.to.build.crm.repository.TicketRepository;
import site.easy.to.build.crm.service.donnee.ImportService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/data/import")
public class ImportController {

    private final ImportService csvImportService;
    
    private final ContractRepository contractRepository;
    private final TicketRepository ticketRepository;

    public ImportController(ImportService csvImportService, ContractRepository contractRepository, TicketRepository ticketRepository) {
        this.csvImportService = csvImportService;
        this.contractRepository = contractRepository;
        this.ticketRepository = ticketRepository;
    }

    @PostMapping("/contract")
    public String importContract(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<String> errorMessages = csvImportService.importCsv(file, Contract.class);

            if (!errorMessages.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", String.join("\n", errorMessages));
                return "redirect:donnee/import";
            }

            contractRepository.saveAll((List<Contract>) csvImportService.getEntities().stream().filter(Contract.class::isInstance).map(Contract.class::cast).collect(Collectors.toList()));
            redirectAttributes.addFlashAttribute("successMessage", "Importation réussie de produits.");
            return "redirect:donnee/import";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:donnee/import"; 
        }
    }

    @PostMapping("/ticket")
    public String importTickets(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<String> errorMessages = csvImportService.importCsv(file, Ticket.class);

            if (!errorMessages.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", String.join("\n", errorMessages));
                return "redirect:donnee/import"; 
            }

            ticketRepository.saveAll((List<Ticket>) csvImportService.getEntities().stream().filter(Ticket.class::isInstance).map(Ticket.class::cast).collect(Collectors.toList()));
    
            // ticketRepository.saveAll(csvImportService.getEntities());
            redirectAttributes.addFlashAttribute("successMessage", "Importation réussie de commandes.");
            return "redirect:donnee/import"; 

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur: " + e.getMessage());
            return "redirect:donnee/import"; 
        }
    }

    @GetMapping("/donnee/import")
    public String showImportPage(Model model) {
        return "donnee/import"; 
    }
}

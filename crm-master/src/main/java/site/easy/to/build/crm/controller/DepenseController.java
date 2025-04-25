package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.money.AlerteService;
import site.easy.to.build.crm.service.money.BudgetService;
import site.easy.to.build.crm.service.money.DepenseService;
import site.easy.to.build.crm.service.ticket.TicketService;

@Controller
@RequestMapping("/depenses")
public class DepenseController {
    private final DepenseService depenseService;
    private final LeadService leadService;
    private final TicketService ticketService;
    private final AlerteService alerteService;
    private final BudgetService budgetService;

    @Autowired
    public DepenseController(DepenseService depenseService, LeadService leadService, TicketService ticketService,
                            AlerteService alerteService, BudgetService budgetService) {
        this.depenseService = depenseService;
        this.leadService = leadService;
        this.ticketService = ticketService;
        this.alerteService = alerteService;
        this.budgetService = budgetService;
    }


    @GetMapping("/add")
    public String showAddBudgetForm(Model model, @RequestParam(required = false) String success) {
        // Réinjecter les données du formulaire si elles sont présentes dans le modèle
        if (!model.containsAttribute("depense")) {
            model.addAttribute("depense", new Depense());
        }

        model.addAttribute("leads", leadService.findLeadsWithoutDepenses());
        model.addAttribute("tickets", ticketService.findTicketsWithoutDepenses());

        if (success != null && success.equals("true")) {
            model.addAttribute("successMessage", "La dépense a été ajoutée avec succès !");
        }

        if (model.containsAttribute("alerteAtteint")) {
            model.addAttribute("alerteAtteint", true);
        }

        if (model.containsAttribute("budgetDepasser")) {
            model.addAttribute("budgetDepasser", true);
        }

        return "money/add-depense";
    }



    @PostMapping("/save")
    public String saveDepense(@Valid @ModelAttribute("depense") Depense depense, BindingResult result, Model model, RedirectAttributes redirectAttributes,@RequestParam(name = "forceInsert", defaultValue = "false") boolean forceInsert) {
        if (depense.getLead() != null && depense.getLead().getLeadId() != 0) {
            Lead lead = leadService.findByLeadId(depense.getLead().getLeadId());
            depense.setLead(lead);
            depense.setCustomer(depense.getLead().getCustomer());
        } else {
            depense.setLead(null);
        }

        if (depense.getTicket() != null && depense.getTicket().getTicketId() != 0) {
            Ticket ticket = ticketService.findByTicketId(depense.getTicket().getTicketId());
            depense.setTicket(ticket);
            depense.setCustomer(depense.getTicket().getCustomer());
        } else {
            depense.setTicket(null);
        }


        if (depense.getLead() != null && depense.getTicket() != null) {
            result.rejectValue("lead", "error.depense", "Une dépense ne peut être associée qu'à un Lead ou un Ticket, pas aux deux.");
            result.rejectValue("ticket", "error.depense", "Une dépense ne peut être associée qu'à un Lead ou un Ticket, pas aux deux.");
        }


        if (depense.getLead() == null && depense.getTicket() == null) {
            result.rejectValue("lead", "error.depense", "Choisir au moins un Lead ou un Ticket.");
            result.rejectValue("ticket", "error.depense", "Choisir au moins un Lead ou un Ticket.");
        }


        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.depense", result);
            redirectAttributes.addFlashAttribute("depense", depense); 
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur : la dépense n'a pas pu être ajoutée. Veuillez vérifier les champs.");
            return "redirect:/depenses/add";
        }
        

        double sumBudget = budgetService.getSumBudgetByIdCustomer(depense.getCustomer().getCustomerId()).doubleValue();

        double sumDepense = depenseService.getSumDepenseByIdCustomer(depense.getCustomer().getCustomerId()).doubleValue();

        boolean budgetDepasser = alerteService.budgetDepasser(sumBudget, sumDepense + depense.getAmount().doubleValue());
        if (!forceInsert) {
            if (budgetDepasser) {
                redirectAttributes.addFlashAttribute("budgetDepasser", true);
                redirectAttributes.addFlashAttribute("depense", depense);
                redirectAttributes.addAttribute("success", "false");
                return "redirect:/depenses/add";
            }            
        }        

        boolean alerteAtteint = alerteService.alerteAtteint(sumBudget, sumDepense);

        if (alerteAtteint) {
            redirectAttributes.addFlashAttribute("alerteAtteint", true);
            redirectAttributes.addAttribute("success", "false");
        } else {
            redirectAttributes.addAttribute("success", "true");
        }
        
        depenseService.saveDepense(depense);

        return "redirect:/depenses/add";
    }
}
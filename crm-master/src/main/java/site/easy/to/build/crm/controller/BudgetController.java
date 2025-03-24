package site.easy.to.build.crm.controller;

import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.money.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    private final CustomerService customerService;
    
    @Autowired
    public BudgetController(BudgetService budgetService,CustomerService customerService){
        this.budgetService = budgetService;
        this.customerService = customerService;
    }


    @GetMapping("/add")
    public String showAddBudgetForm(Model model, @RequestParam(required = false) String success) {
        model.addAttribute("budget", new Budget());
        model.addAttribute("customers", customerService.findAll());

        if (success != null && success.equals("true")) {
            model.addAttribute("successMessage", "Le budget a été ajouté avec succès !");
        }

        return "money/add-budget"; 
    }


    @PostMapping("/save")
    public String saveBudget(@Valid @ModelAttribute("budget") Budget budget, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("customers", customerService.findAll());
            model.addAttribute("errorMessage", "Erreur : le budget n'a pas pu être ajouté. Veuillez vérifier les champs.");
            return "money/add-budget"; 
        }

        budgetService.saveBudget(budget);
        redirectAttributes.addAttribute("success", "true");
        return "redirect:/budgets/add"; 
    }
}

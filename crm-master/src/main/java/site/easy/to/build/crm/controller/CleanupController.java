package site.easy.to.build.crm.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;

import site.easy.to.build.crm.service.donnee.CleanService;

@Controller
@RequestMapping("/data/clean")
public class CleanupController {
    private CleanService cleanService;
    
    @Autowired
    public CleanupController(CleanService cleanService){
        this.cleanService = cleanService;
    }

    @GetMapping("/donnee/reinitialise")
    public String gotoHtmlviewclean(Model model) {
        return "donnee/reinitialise"; // Renvoie la vue 
    }

    @PostMapping("/all")
    public String cleanAllTables(RedirectAttributes redirectAttributes) {
        try {
            List<JpaRepository<?, ?>> repositories = Arrays.asList(
                // userRepository,
                // orderRepository,
                // productRepository
            );

            List<String> tableNames = Arrays.asList(
                // "customer",
                "trigger_contract"
            );
    
            // cleanService.cleanTables(repositories);
            cleanService.cleanTable(tableNames);

            redirectAttributes.addFlashAttribute("successMessage", "Les données ont été réinitialisées avec succès !");            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Une erreur s'est produite lors de la réinitialisation des données.");
        }
    


        return "redirect:donnee/reinitialise";
    }    
}

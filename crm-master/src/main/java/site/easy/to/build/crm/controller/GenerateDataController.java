package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import site.easy.to.build.crm.service.donnee.GenerateService;

@Controller
@RequestMapping("/data/generate")
public class GenerateDataController {
    private GenerateService generateService;
    @Autowired
    public GenerateDataController(GenerateService generateService){
        this.generateService = generateService;
    }

    @GetMapping("/donnee/generate")
    public String gotoHtmlviewclean(Model model) {
        return "donnee/generate";  
    }

    @PostMapping("/datagenerator")
    public String cleanAllTables(RedirectAttributes redirectAttributes) {
        try {
    
            generateService.generateTestData();

            redirectAttributes.addFlashAttribute("successMessage", "Les données ont été generer avec succès !");            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Une erreur s'est produite lors de la generation des données."+e);
        }
    


        return "redirect:donnee/generate";
    }    
}

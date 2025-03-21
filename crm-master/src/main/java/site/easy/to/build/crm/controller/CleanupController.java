package site.easy.to.build.crm.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;

import site.easy.to.build.crm.service.donnee.CleanService;

@Controller
public class CleanupController {
    private CleanService cleanService;
    
    @Autowired
    public CleanupController(CleanService cleanService){
        this.cleanService = cleanService;
    }

    @DeleteMapping("/all")
    public String cleanAllTables() {
    
        List<JpaRepository<?, ?>> repositories = Arrays.asList(
            // userRepository,
            // orderRepository,
            // productRepository
        );

        cleanService.cleanTables(repositories);

        return "All tables cleaned successfully!";
    }    
}

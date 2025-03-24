package site.easy.to.build.crm.service.money;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import site.easy.to.build.crm.entity.Alerte;
import site.easy.to.build.crm.repository.AlerteRepository;

@Service
public class AlerteService {
    AlerteRepository alerteRepository;

    @Autowired
    public AlerteService(AlerteRepository alerteRepository){
        this.alerteRepository = alerteRepository;
    }

    public double getTauxAlerteGlobal() {
        Alerte alerte = alerteRepository.findFirstByOrderByIdAlerteDesc();
        return alerte != null ? alerte.getPourcentage() : 0.0; 
    }

    public boolean alerteAtteint(double budget,double depense){
        double budgetPourcentage = (budget * getTauxAlerteGlobal()) / 100;
        // double passebas = budget - budgetPourcentage;
        if (depense >= budgetPourcentage && depense <= budget) {
            return true;
        }
        return false;
    }

    public boolean budgetDepasser(double budget,double depense){
        if (depense > budget ) {
            return true;
        }
        return false;
    }
    
    public void saveTauxAlerteGlobal(double pourcentage) {

        Alerte alerte = new Alerte();

        alerte.setPourcentage(pourcentage);

        alerteRepository.save(alerte);
    }
}

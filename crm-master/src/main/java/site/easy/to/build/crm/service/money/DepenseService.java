package site.easy.to.build.crm.service.money;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.repository.DepenseRepository;

@Service
public class DepenseService {
    DepenseRepository depenseRepository;
    
    @Autowired
    public DepenseService(DepenseRepository depenseRepository){
        this.depenseRepository = depenseRepository;
    }

    public Depense saveDepense(Depense depense) {
        if (depense.getLead() != null) {
            depense.setCustomer(depense.getLead().getCustomer());
        } else if (depense.getTicket() != null) {
            depense.setCustomer(depense.getTicket().getCustomer());
        } else {
            throw new IllegalArgumentException("Une dépense doit être associée à un Lead ou un Ticket.");
        }
        return depenseRepository.save(depense);
    }

    public BigDecimal getSumDepenseByIdCustomer(Integer customerId) {
        BigDecimal sum = depenseRepository.getSumDepenseByIdCustomer(customerId);
        return sum != null ? sum : BigDecimal.ZERO; 
    }

    public BigDecimal getSumDepenseOfAllUsers(){
        return depenseRepository.getSumDepenseOfAllUsers();
    }
    public BigDecimal getGlobalSumDepensesOfTicket(){
        BigDecimal somme = depenseRepository.getGlobalSumDepensesOfTicket();
        return somme != null ? somme : BigDecimal.ZERO;
    }
    public BigDecimal getGlobalSumDepensesOfLead(){
        BigDecimal somme = depenseRepository.getGlobalSumDepensesOfLead();
        return somme != null ? somme : BigDecimal.ZERO;
    }

    public BigDecimal findAmountLeadId(Integer leadId){
        BigDecimal reponse = depenseRepository.findAmountLeadId(leadId);
        return reponse != null ? reponse : BigDecimal.ZERO;
    }

    public BigDecimal findAmountTicketId(Integer ticketId){
        BigDecimal reponse = depenseRepository.findAmountTicketId(ticketId);
        return reponse != null ? reponse : BigDecimal.ZERO;
    }


    public void updateAmountOfTicketId(Integer ticketId,double amount){
        depenseRepository.updateAmountOfTicketId(ticketId, amount);
    }


    public void updateAmountOfLeadId(Integer leadId,double amount){
        depenseRepository.updateAmountOfLeadId(leadId, amount);
    }

    public boolean existsByTicket_TicketId(Integer ticketId){
        return depenseRepository.existsByTicket_TicketId(ticketId);
    }


    public boolean existsByLead_LeadId(Integer leadId){
        return depenseRepository.existsByLead_LeadId(leadId);
    }
}


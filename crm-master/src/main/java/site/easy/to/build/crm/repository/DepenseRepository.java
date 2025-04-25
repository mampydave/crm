package site.easy.to.build.crm.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import site.easy.to.build.crm.entity.Depense;

@Repository
public interface DepenseRepository extends JpaRepository<Depense, Integer>{
    @Query("SELECT SUM(d.amount) FROM Depense d WHERE d.customer.customerId = :customerId")
    BigDecimal getSumDepenseByIdCustomer(@Param("customerId") Integer customerId);

    @Query("SELECT SUM(d.amount) FROM Depense d")
    BigDecimal getSumDepenseOfAllUsers();

    @Query("SELECT SUM(d.amount) FROM Depense d WHERE d.ticket.ticketId is not null")
    BigDecimal getGlobalSumDepensesOfTicket();

    @Query("SELECT SUM(d.amount) FROM Depense d WHERE d.lead.leadId is not null")
    BigDecimal getGlobalSumDepensesOfLead();

    @Query("SELECT amount FROM Depense d WHERE d.lead.leadId = :leadId")
    BigDecimal findAmountLeadId(@Param("leadId") Integer leadId);
    
    @Query("SELECT amount FROM Depense d WHERE d.ticket.ticketId = :ticketId")
    BigDecimal findAmountTicketId(@Param("ticketId") Integer ticketId);

    @Transactional
    @Modifying
    @Query("UPDATE Depense d SET d.amount = :amount WHERE d.ticket.ticketId = :ticketId")
    void updateAmountOfTicketId(@Param("ticketId") Integer ticketId, @Param("amount") double amount);

    @Transactional
    @Modifying
    @Query("UPDATE Depense d SET d.amount = :amount WHERE d.lead.leadId = :leadId")
    void updateAmountOfLeadId(@Param("leadId") Integer leadId, @Param("amount") double amount);

    boolean existsByTicket_TicketId(Integer ticketId);

    boolean existsByLead_LeadId(Integer leadId);

    @Query("SELECT d FROM Depense d WHERE d.ticket.ticketId = :ticketId")
    Depense findDepenseByIdTicket(@Param("ticketId") Integer ticketId);

    @Query("SELECT d FROM Depense d WHERE d.lead.leadId = :leadId")
    Depense findDepenseByIdLead(@Param("leadId") Integer leadId);

}

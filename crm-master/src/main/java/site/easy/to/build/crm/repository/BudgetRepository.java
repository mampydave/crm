package site.easy.to.build.crm.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import site.easy.to.build.crm.entity.Budget;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer>{

    @Query("SELECT SUM(b.amount) FROM Budget b WHERE b.customer.customerId = :customerId")
    BigDecimal getSumBudgetByIdCustomer(@Param("customerId") Integer customerId);

    @Query("SELECT SUM(b.amount) FROM Budget b")
    BigDecimal getSumBudgetOfALlCustomer();

    List<Budget> findByCustomer_CustomerId(Integer customerId);

    @Query("SELECT SUM(b.amount) FROM Budget b WHERE b.customer.customerId = :customerId")
    BigDecimal findTotalAmountByCustomerId(@Param("customerId") Integer customerId);

}

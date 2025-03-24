package site.easy.to.build.crm.service.money;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.repository.BudgetRepository;

@Service
public class BudgetService {
    
    private BudgetRepository budgetRepository;

    @Autowired
    public BudgetService(BudgetRepository budgetRepository){
        this.budgetRepository = budgetRepository;
    }

    public Budget saveBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    public BigDecimal getSumBudgetByIdCustomer(Integer customerId) {
        BigDecimal sum = budgetRepository.getSumBudgetByIdCustomer(customerId);
        return sum != null ? sum : BigDecimal.ZERO; 
    }

    public BigDecimal getBudgetsByCustomerId(Integer customerId) {
        return budgetRepository.getSumBudgetByIdCustomer(customerId);
    }
    public BigDecimal getSumBudgetOfAllCustomer() {
        return budgetRepository.getSumBudgetOfALlCustomer();
    }

}

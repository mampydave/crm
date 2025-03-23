package site.easy.to.build.crm.service.lead;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.repository.DepenseRepository;
import site.easy.to.build.crm.repository.LeadRepository;
import site.easy.to.build.crm.entity.Lead;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final DepenseRepository depenseRepository;

    public LeadServiceImpl(LeadRepository leadRepository,DepenseRepository depenseRepository) {
        this.leadRepository = leadRepository;
        this.depenseRepository = depenseRepository;
    }

    @Override
    public Lead findByLeadId(int id) {
        return leadRepository.findByLeadId(id);
    }

    @Override
    public List<Lead> findAll() {
        return leadRepository.findAll();
    }

    @Override
    public List<Lead> findAssignedLeads(int userId) {
        return leadRepository.findByEmployeeId(userId);
    }

    @Override
    public List<Lead> findCreatedLeads(int userId) {
        return leadRepository.findByManagerId(userId);
    }

    @Override
    public Lead findByMeetingId(String meetingId){
        return leadRepository.findByMeetingId(meetingId);
    }
    @Override
    public Lead save(Lead lead) {
        return leadRepository.save(lead);
    }

    @Override
    public void delete(Lead lead) {
        leadRepository.delete(lead);
    }

    @Override
    public List<Lead> getRecentLeadsByEmployee(int employeeId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return leadRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageable);
    }

    @Override
    public List<Lead> getRecentCustomerLeads(int customerId, int limit) {
        Pageable pageable = PageRequest.of(0,limit);
        return leadRepository.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId, pageable);
    }

    @Override
    public void deleteAllByCustomer(Customer customer) {
        leadRepository.deleteAllByCustomer(customer);
    }

    @Override
    public List<Lead> getRecentLeads(int managerId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return leadRepository.findByManagerIdOrderByCreatedAtDesc(managerId, pageable);
    }

    @Override
    public List<Lead> getCustomerLeads(int customerId) {
        return leadRepository.findByCustomerCustomerId(customerId);
    }

    @Override
    public long countByEmployeeId(int employeeId) {
        return leadRepository.countByEmployeeId(employeeId);
    }

    @Override
    public long countByManagerId(int managerId) {
        return leadRepository.countByManagerId(managerId);
    }

    @Override
    public long countByCustomerId(int customerId) {
        return leadRepository.countByCustomerCustomerId(customerId);
    }
    
    @Override
    public List<Lead> getLeadsNotInDepense() {

        List<Lead> allLeads = leadRepository.findAll();

        List<Lead> leadsInDepense = depenseRepository.findAll().stream()
                .map(Depense::getLead)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return allLeads.stream()
                .filter(lead -> !leadsInDepense.contains(lead))
                .collect(Collectors.toList());
    }

    @Override
    public long count(){
        return leadRepository.count();
    }
    @Override
    public boolean existsById(Integer id){

        return leadRepository.existsById(id);
    }
    @Override
    public void deleteById(Integer id){
        leadRepository.deleteById(id);
    }
}

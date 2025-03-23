package site.easy.to.build.crm.service.ticket;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.repository.DepenseRepository;
import site.easy.to.build.crm.repository.TicketRepository;
import site.easy.to.build.crm.entity.Ticket;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService{

    private final TicketRepository ticketRepository;
    private final DepenseRepository depenseRepository;
    public TicketServiceImpl(TicketRepository ticketRepository,DepenseRepository depenseRepository) {
        this.ticketRepository = ticketRepository;
        this.depenseRepository = depenseRepository;
    }

    @Override
    public Ticket findByTicketId(int id) {
        return ticketRepository.findByTicketId(id);
    }

    @Override
    public Ticket save(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @Override
    public void delete(Ticket ticket) {
        ticketRepository.delete(ticket);
    }

    @Override
    public List<Ticket> findManagerTickets(int id) {
        return ticketRepository.findByManagerId(id);
    }

    @Override
    public List<Ticket> findEmployeeTickets(int id) {
        return ticketRepository.findByEmployeeId(id);
    }

    @Override
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    @Override
    public List<Ticket> findCustomerTickets(int id) {
        return ticketRepository.findByCustomerCustomerId(id);
    }

    @Override
    public List<Ticket> getRecentTickets(int managerId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return ticketRepository.findByManagerIdOrderByCreatedAtDesc(managerId, pageable);
    }

    @Override
    public List<Ticket> getRecentEmployeeTickets(int employeeId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return ticketRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageable);
    }

    @Override
    public List<Ticket> getRecentCustomerTickets(int customerId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return ticketRepository.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId, pageable);
    }

    @Override
    public long countByEmployeeId(int employeeId) {
        return ticketRepository.countByEmployeeId(employeeId);
    }

    @Override
    public long countByManagerId(int managerId) {
        return ticketRepository.countByManagerId(managerId);
    }

    @Override
    public long countByCustomerCustomerId(int customerId) {
        return ticketRepository.countByCustomerCustomerId(customerId);
    }

    @Override
    public void deleteAllByCustomer(Customer customer) {
        ticketRepository.deleteAllByCustomer(customer);
    }
    
    @Override
    public List<Ticket> getTicketsNotInDepense() {
        List<Ticket> allTickets = ticketRepository.findAll();

        List<Ticket> ticketsInDepense = depenseRepository.findAll().stream()
                .map(Depense::getTicket)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return allTickets.stream()
                .filter(ticket -> !ticketsInDepense.contains(ticket))
                .collect(Collectors.toList());
    }

    @Override
    public long count(){
        return ticketRepository.count();
    }   
    @Override
    public boolean existsById(Integer id){
        return ticketRepository.existsById(id);
    }
    @Override
    @Transactional
    public void deleteById(Integer id){
        ticketRepository.deleteById(id);
    }
}

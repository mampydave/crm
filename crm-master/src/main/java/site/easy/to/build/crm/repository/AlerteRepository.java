package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import site.easy.to.build.crm.entity.Alerte;


@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Integer>{
    public Alerte findFirstByOrderByIdAlerteDesc();
}

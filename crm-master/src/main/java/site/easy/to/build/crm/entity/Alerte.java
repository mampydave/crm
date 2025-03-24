package site.easy.to.build.crm.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "alerte")
public class Alerte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerte")
    private Integer idAlerte;

    @Column(name = "pourcentage", nullable = false)
    private double pourcentage;

    
    public Alerte() {
    }

    public Alerte(Integer idAlerte, double pourcentage) {
        this.idAlerte = idAlerte;
        this.pourcentage = pourcentage;
    }

    public Integer getIdAlerte() {
        return idAlerte;
    }

    public void setIdAlerte(Integer idAlerte) {
        this.idAlerte = idAlerte;
    }

    public double getPourcentage() {
        return pourcentage;
    }

    public void setPourcentage(double pourcentage) {
        this.pourcentage = pourcentage;
    }
    
}

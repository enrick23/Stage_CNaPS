package mg.cnaps.drh.prets.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Table principale enregistrant les demandes de prêts formulées par les agents CNaPS.
 * Application : Gestion des Prêts et Avances
 * Schéma Oracle : DRH
 */
@Entity
@Table(name = "DEMANDE_PRET", schema = "DRH")
@SequenceGenerator(name = "seq_pret", sequenceName = "DRH.SEQ_DEMANDE_PRET", allocationSize = 1)
public class DemandePret {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pret")
    @Column(name = "ID_DEMANDE", nullable = false)
    private Long idDemande;

    @Column(name = "MATRICULE_AGENT", length = 15, nullable = false)
    private String matriculeAgent;

    @Column(name = "CODE_TYPE_PRET", length = 10, nullable = false)
    private String codeTypePret;

    @Column(name = "MONTANT_DEMANDE", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantDemande;

    @Column(name = "MENSUALITE_RETENUE", precision = 12, scale = 2, nullable = false)
    private BigDecimal mensualiteRetenue;

    @Column(name = "DATE_DEMANDE", nullable = false)
    private LocalDate dateDemande;

    @Column(name = "STATUT_VALIDATION", length = 20, nullable = false)
    private String statutValidation;

    public DemandePret() {
    }

    public Long getIdDemande() {
        return idDemande;
    }

    public void setIdDemande(Long idDemande) {
        this.idDemande = idDemande;
    }

    public String getMatriculeAgent() {
        return matriculeAgent;
    }

    public void setMatriculeAgent(String matriculeAgent) {
        this.matriculeAgent = matriculeAgent;
    }

    public String getCodeTypePret() {
        return codeTypePret;
    }

    public void setCodeTypePret(String codeTypePret) {
        this.codeTypePret = codeTypePret;
    }

    public BigDecimal getMontantDemande() {
        return montantDemande;
    }

    public void setMontantDemande(BigDecimal montantDemande) {
        this.montantDemande = montantDemande;
    }

    public BigDecimal getMensualiteRetenue() {
        return mensualiteRetenue;
    }

    public void setMensualiteRetenue(BigDecimal mensualiteRetenue) {
        this.mensualiteRetenue = mensualiteRetenue;
    }

    public LocalDate getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDate dateDemande) {
        this.dateDemande = dateDemande;
    }

    public String getStatutValidation() {
        return statutValidation;
    }

    public void setStatutValidation(String statutValidation) {
        this.statutValidation = statutValidation;
    }
}

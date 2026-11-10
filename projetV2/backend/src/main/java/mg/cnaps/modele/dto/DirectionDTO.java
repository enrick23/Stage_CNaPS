package mg.cnaps.modele.dto;

public class DirectionDTO {
    private String codeDirection;
    private String libelleDirection;
    private String codeAbrev;
    private Character flagActif;

    public DirectionDTO() {
    }

    public DirectionDTO(String codeDirection, String libelleDirection,
                       String codeAbrev, Character flagActif) {
        this.codeDirection = codeDirection;
        this.libelleDirection = libelleDirection;
        this.codeAbrev = codeAbrev;
        this.flagActif = flagActif;
    }

    public String getCodeDirection() {
        return codeDirection;
    }

    public void setCodeDirection(String codeDirection) {
        this.codeDirection = codeDirection;
    }

    public String getLibelleDirection() {
        return libelleDirection;
    }

    public void setLibelleDirection(String libelleDirection) {
        this.libelleDirection = libelleDirection;
    }

    public String getCodeAbrev() {
        return codeAbrev;
    }

    public void setCodeAbrev(String codeAbrev) {
        this.codeAbrev = codeAbrev;
    }

    public Character getFlagActif() {
        return flagActif;
    }

    public void setFlagActif(Character flagActif) {
        this.flagActif = flagActif;
    }

}
package mg.cnaps.modele.dto;

public class ColonneDTO {

    private String nom;
    private String type;
    private Integer longueur;
    private boolean nullable;

    public ColonneDTO() { }

    public ColonneDTO(String nom, String type, Integer longueur, boolean nullable) {
        this.nom = nom;
        this.type = type;
        this.longueur = longueur;
        this.nullable = nullable;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getLongueur() { return longueur; }
    public void setLongueur(Integer longueur) { this.longueur = longueur; }

    public boolean isNullable() { return nullable; }
    public void setNullable(boolean nullable) { this.nullable = nullable; }
}
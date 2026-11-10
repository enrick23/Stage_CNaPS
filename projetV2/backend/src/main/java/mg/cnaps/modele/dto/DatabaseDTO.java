package mg.cnaps.modele.dto;

public class DatabaseDTO {

    private String nomTable;
    private int nombreColonnes;
    private int nombreLignes;

    public DatabaseDTO() { }

    public DatabaseDTO(String nomTable) {
        this.nomTable = nomTable;
    }

    public DatabaseDTO(String nomTable, int nombreColonnes, int nombreLignes) {
        this.nomTable = nomTable;
        this.nombreColonnes = nombreColonnes;
        this.nombreLignes = nombreLignes;
    }

    public String getNomTable() { return nomTable; }
    public void setNomTable(String nomTable) { this.nomTable = nomTable; }

    public int getNombreColonnes() { return nombreColonnes; }
    public void setNombreColonnes(int nombreColonnes) { this.nombreColonnes = nombreColonnes; }

    public int getNombreLignes() { return nombreLignes; }
    public void setNombreLignes(int nombreLignes) { this.nombreLignes = nombreLignes; }
}
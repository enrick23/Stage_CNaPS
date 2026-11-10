package mg.cnaps.modele.dictionnaire;

import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Application;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Colonne;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Objet;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Sequence;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Trigger;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Export Excel au format du fichier « DICO DE DONNES.xlsx ».
 * Une ligne par colonne ; les informations de niveau table (application, schéma,
 * description, séquences, triggers) sont fusionnées verticalement sur le bloc de la table.
 * Seules des métadonnées sont exportées.
 */
@Service
public class DictionnaireExcelService {

    static final String[] ENTETES = {
            "APPLICATION", "SCHEMAS", "TABLES/VUES DESCRIPTION", "COLONNES", "NIVEAU DE SENSIBILITE",
            "TYPES", "TAILLES", "CONTRAINTE", "VALEUR PAR DEFAUT", "DESCRIPTION DES COLONNES",
            "SEQUENCE", "DESCRIPTION DES TRIGGERS", "NOM INDEXE", "OBSERVATION"
    };
    private static final int[] LARGEURS = {22, 12, 45, 26, 14, 14, 9, 16, 20, 45, 26, 50, 28, 30};

    private final DictionnaireRepository repository;

    public DictionnaireExcelService(DictionnaireRepository repository) {
        this.repository = repository;
    }

    /** @param nom objet unique, ou null pour tout le schéma */
    public byte[] exporter(String schema, String nom, Application application) throws IOException {
        List<Objet> objets = repository.objets(schema, nom);
        Map<String, List<Colonne>> colonnes = repository.colonnes(schema, nom).stream()
                .collect(Collectors.groupingBy(c -> c.table));
        Map<String, List<Trigger>> triggers = repository.triggers(schema, nom).stream()
                .collect(Collectors.groupingBy(t -> t.table));
        Map<String, List<Sequence>> sequences = repository.sequences(schema, nom).stream()
                .collect(Collectors.groupingBy(s -> s.table));

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Dictionnaire " + schema);
            CellStyle entete = style(wb, true);
            CellStyle cellule = style(wb, false);

            Row r0 = sheet.createRow(0);
            for (int i = 0; i < ENTETES.length; i++) {
                ecrire(r0, i, ENTETES[i], entete);
                sheet.setColumnWidth(i, LARGEURS[i] * 256);
            }
            sheet.createFreezePane(0, 1);

            String libelleApp = application == null ? "" : application.libelle + " (" + application.code + ")";
            int ligne = 1;
            int debutApp = ligne;
            for (Objet o : objets) {
                List<Colonne> cols = colonnes.getOrDefault(o.nom, List.of());
                if (cols.isEmpty()) continue;
                String appCourante = !libelleApp.isEmpty()
                        ? libelleApp
                        : (o.appLibelle != null && o.appCode != null
                                ? o.appLibelle + " (" + o.appCode + ")"
                                : (o.appCode != null ? o.appCode : ""));
                int debut = ligne;
                String descTable = o.nom + (o.type.equals("VIEW") ? " (VUE)" : "")
                        + (o.description != null ? " : " + o.description : "");
                String seq = sequences.getOrDefault(o.nom, List.of()).stream()
                        .map(s -> s.nom).distinct().collect(Collectors.joining("\n"));
                String trg = triggers.getOrDefault(o.nom, List.of()).stream()
                        .map(t -> schema + "." + t.nom + (t.description != null ? " : " + t.description : ""))
                        .collect(Collectors.joining("\n"));

                for (Colonne c : cols) {
                    Row r = sheet.createRow(ligne++);
                    ecrire(r, 0, ligne - 1 == debutApp ? appCourante : "", cellule);
                    ecrire(r, 1, ligne - 1 == debut ? o.schema : "", cellule);
                    ecrire(r, 2, ligne - 1 == debut ? descTable : "", cellule);
                    ecrire(r, 3, c.nom, cellule);
                    ecrire(r, 4, c.niveauSensibilite == null ? "" : String.valueOf(c.niveauSensibilite), cellule);
                    ecrire(r, 5, c.typeBase, cellule);
                    ecrire(r, 6, c.taille, cellule);
                    ecrire(r, 7, c.getContrainte(), cellule);
                    ecrire(r, 8, c.defaut, cellule);
                    String desc = c.description == null ? "" : c.description;
                    if (c.reference != null) desc += (desc.isEmpty() ? "" : " ") + "→ " + c.reference;
                    ecrire(r, 9, desc, cellule);
                    ecrire(r, 10, ligne - 1 == debut ? seq : "", cellule);
                    ecrire(r, 11, ligne - 1 == debut ? trg : "", cellule);
                    ecrire(r, 12, String.join(", ", c.index), cellule);
                    ecrire(r, 13, c.observation, cellule);
                }
                if (ligne - 1 > debut) {
                    for (int col : new int[]{1, 2, 10, 11}) {
                        sheet.addMergedRegion(new CellRangeAddress(debut, ligne - 1, col, col));
                    }
                }
            }
            if (ligne - 1 > debutApp) {
                sheet.addMergedRegion(new CellRangeAddress(debutApp, ligne - 1, 0, 0));
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    private static void ecrire(Row r, int col, String valeur, CellStyle style) {
        Cell c = r.createCell(col);
        c.setCellValue(valeur == null ? "" : valeur);
        c.setCellStyle(style);
    }

    private static CellStyle style(Workbook wb, boolean entete) {
        CellStyle s = wb.createCellStyle();
        s.setWrapText(true);
        s.setVerticalAlignment(VerticalAlignment.TOP);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        if (entete) {
            Font f = wb.createFont();
            f.setBold(true);
            s.setFont(f);
            s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            s.setVerticalAlignment(VerticalAlignment.CENTER);
        }
        return s;
    }
}


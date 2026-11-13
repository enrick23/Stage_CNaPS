# Application : Gestion des Incidents (Helpdesk DSI)

> **Application** : `Gestion des Incidents`  
> **Code / APP_ID** : `INCIDENT` (ou UUID `550E8400-E29B-41D4-A716-446655440020`)  
> **Schéma Oracle** : `SIG`  
> **Domaine** : Support Utilisateur, Exploitation Informatique & Maintenance

## Objets Oracle Définis
1. **`SIG.REF_CATEGORIE_INCIDENT`** (Table de référence) : Nomenclature des catégories (Matériel, Réseau, Applicatif...).
2. **`SIG.INCIDENT_TICKET`** (Table métier) : Tickets d'incidents déclarés avec statut, priorité et séquence `SIG.SEQ_INCIDENT`.
3. **`SIG.INCIDENT_COMMENTAIRE`** (Table métier) : Échanges et notes de suivi d'intervention.
4. **`SIG.TRG_INCIDENT_BI`** (Trigger) : Affectation de la séquence et horodatage automatique.

## Comment tester dans l'Assistant IA
- Glissez l'archive `Gestion_Incidents.zip` ou collez `IncidentTicket.java` dans l'Assistant IA.
- L'IA reconnaîtra automatiquement :
  - `REF_CATEGORIE_INCIDENT` comme table référentielle (`FLAG_REF = Y`).
  - `MATRICULE_DECLARANT` comme donnée Confidentielle (Niveau 3).
  - La séquence `SIG.SEQ_INCIDENT`.

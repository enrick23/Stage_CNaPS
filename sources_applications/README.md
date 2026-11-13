# Sources des Applications CNaPS pour Tests & Rétro-Ingénierie IA

Ce répertoire regroupe les artefacts de code source (entités Java JPA, scripts DDL/DML SQL, triggers PL/SQL et archives `.zip`) pour **chacune des applications de l'écosystème CNaPS**, prêts à être importés dans l'**Assistant IA** du Dictionnaire de Données.

---

## 📁 Répertoire des Applications

| Répertoire | Application | Schéma Oracle | Objets Inclus | Archive Prête |
| :--- | :--- | :---: | :--- | :---: |
| [`01_RevuServices/`](file:///c:/Users/enric/OneDrive/Documents/Stage/dico_donnee/sources_applications/01_RevuServices) | **Revue des Habilitations (RevuServices)** | `IAM` | `APP_DMD_AGENT_CONCERNE`, `APP_DMD_PRIVILEGE_DETAIL`, `APP_DMD_PRIVILEGE_HISTO`, `TRG_PRIVILEGE_AUDIT` | `RevuServices.zip` |
| [`02_Gestion_Incidents/`](file:///c:/Users/enric/OneDrive/Documents/Stage/dico_donnee/sources_applications/02_Gestion_Incidents) | **Gestion des Incidents (Helpdesk)** | `SIG` | `REF_CATEGORIE_INCIDENT`, `INCIDENT_TICKET`, `INCIDENT_COMMENTAIRE`, `TRG_INCIDENT_BI` | `Gestion_Incidents.zip` |
| [`03_Gestion_Prets_Avances/`](file:///c:/Users/enric/OneDrive/Documents/Stage/dico_donnee/sources_applications/03_Gestion_Prets_Avances) | **Gestion des Prêts et Avances** | `DRH` | `REF_TYPE_PRET`, `DEMANDE_PRET`, `V_SUIVI_PRET_AGENT`, `TRG_BI_DEMANDE_PRET` | `Gestion_Prets_Avances.zip` |
| [`04_Cartographie_SIG/`](file:///c:/Users/enric/OneDrive/Documents/Stage/dico_donnee/sources_applications/04_Cartographie_SIG) | **Cartographie & Directions Régionales** | `SIG` | `SIG_DR` (modèle officiel CNaPS), `SIG_AGENCE`, `TRIG_SIG_DR` | `Cartographie_SIG.zip` |

---

## 🚀 Comment tester avec l'Assistant IA dans Lucy (`http://localhost:4200/dictionnaire`)

1. Allez sur le module **Dictionnaire de données** (`/dictionnaire`).
2. Cliquez sur **« Nouveau dictionnaire »** puis sur le bouton **« Assistant IA »** (ou « Générer par IA »).
3. **Tester un fichier ZIP complet** :
   - Glissez l'un des fichiers `.zip` (ex: `Gestion_Prets_Avances.zip` ou `RevuServices.zip`) directement dans la zone de dépôt.
   - Cliquez sur **« Analyser le(s) fichier(s) »**.
   - Constatez l'extraction automatique des tables, des types, des clés primaires, des séquences, la classification de sensibilité (1 à 4) et les descriptions rédigées en français.
4. **Tester unitaire par glisser-déposer** :
   - Déposez un ou plusieurs fichiers `.java` et `.sql` individuels.
5. **Tester un extrait par copier-coller** :
   - Ouvrez n'importe quelle classe `.java` ci-dessus, copiez son contenu dans l'onglet *« Coller un extrait de code »*, puis lancez l'analyse.

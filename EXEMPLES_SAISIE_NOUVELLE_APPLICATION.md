# Exemples de Remplissage — Création / Déclaration d'une Nouvelle Application

Ce document fournit des jeux de données réalistes (contexte **CNaPS**) prêts à être copiés-collés étape par étape dans le formulaire **« Nouveau dictionnaire »** (`/dictionnaire`).

---

## Exemple 1 : Nouvelle Application RH — « Gestion des Prêts et Avances »

Cet exemple complet permet de tester :
1. La déclaration d'une **nouvelle application** avec une **Table Référentielle** (`REF_TYPE_PRET`),
2. L'ajout d'une **Table Métier** (`DEMANDE_PRET`) dans cette même application,
3. L'ajout d'une **Vue SQL** (`V_SUIVI_PRET_AGENT`) dans cette même application.

---

### A. Première saisie : Déclaration de la nouvelle application + Table Référentielle (`REF_TYPE_PRET`)

#### Étape 01 — Contexte de l'application & Schéma Oracle
- **Choix du mode** : `➕ Créer / Déclarer une nouvelle application`
- **Nom / Libellé de l'application** :
  ```text
  Gestion des Prêts et Avances
  ```
- **Schéma Oracle dédié** *(à sélectionner dans la liste déroulante)* :
  ```text
  DRH
  ```
- **Description fonctionnelle de l'application** :
  ```text
  Application permettant la gestion, l'instruction, la validation et le suivi du remboursement des prêts et avances sur salaire accordés aux agents de la CNaPS.
  ```
*(Note : l'identifiant `APP_ID` est généré automatiquement à l'enregistrement).*

#### Étape 02 — Définition de l'objet (Table ou Vue)
- **Type de l'objet** : `TABLE`
- **Case « Table Référentielle (flag_ref) »** : ✅ **Cochée**
- **Nom de l'objet (Table ou Vue)** :
  ```text
  REF_TYPE_PRET
  ```
- **Description fonctionnelle globale de la table / vue** :
  ```text
  Table référentielle listant les catégories de prêts et avances disponibles pour les agents CNaPS (avance spéciale, prêt immobilier, prêt scolaire, prêt équipement) avec leurs plafonds et durées maximales.
  ```

#### Étape 03 — Colonnes & Attributs (Format Excel)
Cliquez sur **« + Ajouter une colonne »** pour saisir les 5 colonnes suivantes :

| # | COLONNE * | TYPE | TAILLE | CONTRAINTE | SENSIBILITÉ | VALEUR DÉFAUT | DESCRIPTION DE LA COLONNE | NOM INDEXÉ | OBSERVATION |
| :-: | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | `CODE_TYPE_PRET` | `VARCHAR2` | `10` | `PK` | `1 - Public` | *(vide)* | `Code unique identifiant le type de prêt (ex: PRET_IMMO, AV_SAL)` | `PK_REF_TYPE_PRET` | `Clé primaire` |
| **2** | `LIBELLE_TYPE_PRET` | `VARCHAR2` | `100` | `Not null` | `1 - Public` | *(vide)* | `Intitulé complet du type de prêt ou d'avance` | `IDX_TYPE_PRET_LIB` | *(vide)* |
| **3** | `DUREE_MAX_MOIS` | `NUMBER` | `3` | `Not null` | `2 - Interne` | `12` | `Durée maximale de remboursement autorisée en mois` | *(vide)* | `En mois` |
| **4** | `PLAFOND_MONTANT` | `NUMBER` | `15,2` | `Not null` | `2 - Interne` | `0` | `Montant plafond autorisé en Ariary pour cette catégorie de prêt` | *(vide)* | `Montant en MGA` |
| **5** | `ACTIF` | `CHAR` | `1` | `Not null` | `1 - Public` | `'O'` | `Indicateur de validité du type de prêt (O = Oui, N = Non)` | *(vide)* | `Valeurs : O / N` |

#### Étape 04 — Séquences, Déclencheurs (Triggers) & Code SQL
- **Séquence(s) associée(s)** :
  ```text
  DRH.SEQ_REF_TYPE_PRET
  ```
- **Déclencheur(s) / Triggers & Description** :
  ```text
  DRH.TRG_REF_TYPE_PRET_BI : Déclencheur avant insertion pour formater le code en majuscules et initialiser la date de création.
  ```

---

### B. Deuxième saisie : Ajout d'une Table Métier (`DEMANDE_PRET`) à cette application

*(Cette fois, à l'Étape 01, laissez coché **« Application existante »** et sélectionnez **« Gestion des Prêts et Avances »** dans la liste).*

#### Étape 01 — Contexte de l'application & Schéma Oracle
- **Choix du mode** : `Application existante`
- **Application cible** : `Gestion des Prêts et Avances`
- **Schéma Oracle propriétaire** : `DRH`

#### Étape 02 — Définition de l'objet (Table ou Vue)
- **Type de l'objet** : `TABLE`
- **Case « Table Référentielle (flag_ref) »** : ⬜ **Décochée** *(c'est une table transactionnelle)*
- **Nom de l'objet (Table ou Vue)** :
  ```text
  DEMANDE_PRET
  ```
- **Description fonctionnelle globale de la table / vue** :
  ```text
  Table principale enregistrant les demandes de prêts soumises par les agents, le montant sollicité, la retenue mensuelle calculée et le statut de validation hiérarchique.
  ```

#### Étape 03 — Colonnes & Attributs (Format Excel)

| # | COLONNE * | TYPE | TAILLE | CONTRAINTE | SENSIBILITÉ | VALEUR DÉFAUT | DESCRIPTION DE LA COLONNE | NOM INDEXÉ | OBSERVATION |
| :-: | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | `ID_DEMANDE` | `NUMBER` | `12` | `PK` | `2 - Interne` | *(vide)* | `Identifiant unique de la demande de prêt généré par séquence` | `PK_DEMANDE_PRET` | `Auto-incrémenté` |
| **2** | `MATRICULE_AGENT` | `VARCHAR2` | `15` | `FK` | `3 - Confidentiel` | *(vide)* | `Numéro matricule de l'agent demandeur (référence DRH.AGENT)` | `IDX_DEM_PRET_MAT` | `FK vers DRH.AGENT` |
| **3** | `CODE_TYPE_PRET` | `VARCHAR2` | `10` | `FK` | `2 - Interne` | *(vide)* | `Catégorie du prêt sollicité (référence DRH.REF_TYPE_PRET)` | `IDX_DEM_PRET_TYPE` | `FK vers REF_TYPE_PRET` |
| **4** | `MONTANT_DEMANDE` | `NUMBER` | `15,2` | `Not null` | `4 - Secret / Données sensibles` | *(vide)* | `Montant total du prêt demandé par l'agent en Ariary` | *(vide)* | `Donnée financière sensible` |
| **5** | `MENSUALITE_RETENUE` | `NUMBER` | `12,2` | `Not null` | `4 - Secret / Données sensibles` | `0` | `Montant de la retenue mensuelle prélevée sur salaire` | *(vide)* | `Calculé selon quotité` |
| **6** | `DATE_DEMANDE` | `DATE` | *(vide)* | `Not null` | `2 - Interne` | `SYSDATE` | `Date de dépôt de la demande de prêt` | `IDX_DEM_PRET_DATE` | *(vide)* |
| **7** | `STATUT_VALIDATION` | `VARCHAR2` | `20` | `Not null` | `2 - Interne` | `'EN_ATTENTE'` | `État d'avancement du dossier (EN_ATTENTE, VALIDE, REJETE, SOLDE)` | `IDX_DEM_PRET_STATUT` | *(vide)* |

#### Étape 04 — Séquences, Déclencheurs (Triggers) & Code SQL
- **Séquence(s) associée(s)** *(exemple avec 2 séquences séparées par une virgule)* :
  ```text
  DRH.SEQ_DEMANDE_PRET, DRH.SEQ_DOSSIER_PRET
  ```
- **Déclencheur(s) / Triggers & Description** *(exemple avec 2 triggers sur 2 lignes)* :
  ```text
  DRH.TRG_DEMANDE_PRET_BI : Renseigne automatiquement ID_DEMANDE via la séquence SEQ_DEMANDE_PRET avant insertion.
  DRH.TRG_DEMANDE_PRET_HISTO : Alimente la table d'audit lors de chaque changement du statut de validation.
  ```

---

### C. Troisième saisie : Ajout d'une Vue (`V_SUIVI_PRET_AGENT`) avec Script SQL

#### Étape 01 — Contexte de l'application & Schéma Oracle
- **Choix du mode** : `Application existante`
- **Application cible** : `Gestion des Prêts et Avances`
- **Schéma Oracle propriétaire** : `DRH`

#### Étape 02 — Définition de l'objet (Table ou Vue)
- **Type de l'objet** : `VUE`
- **Nom de l'objet (Table ou Vue)** :
  ```text
  V_SUIVI_PRET_AGENT
  ```
- **Description fonctionnelle globale de la table / vue** :
  ```text
  Vue de synthèse consolidant les demandes de prêts validées avec le libellé du type de prêt pour le suivi comptable et RH.
  ```

#### Étape 03 — Colonnes & Attributs (Format Excel)

| # | COLONNE * | TYPE | TAILLE | CONTRAINTE | SENSIBILITÉ | VALEUR DÉFAUT | DESCRIPTION DE LA COLONNE | NOM INDEXÉ | OBSERVATION |
| :-: | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | `ID_DEMANDE` | `NUMBER` | `12` | `Not null` | `2 - Interne` | *(vide)* | `Identifiant de la demande de prêt` | *(vide)* | `Issu de DEMANDE_PRET` |
| **2** | `MATRICULE_AGENT` | `VARCHAR2` | `15` | `Not null` | `3 - Confidentiel` | *(vide)* | `Matricule de l'agent bénéficiaire` | *(vide)* | `Issu de DEMANDE_PRET` |
| **3** | `LIBELLE_TYPE_PRET` | `VARCHAR2` | `100` | `Not null` | `1 - Public` | *(vide)* | `Libellé de la catégorie de prêt` | *(vide)* | `Issu de REF_TYPE_PRET` |
| **4** | `MONTANT_DEMANDE` | `NUMBER` | `15,2` | `Not null` | `4 - Secret / Données sensibles` | *(vide)* | `Montant accordé en Ariary` | *(vide)* | `Issu de DEMANDE_PRET` |
| **5** | `STATUT_VALIDATION` | `VARCHAR2` | `20` | `Not null` | `2 - Interne` | *(vide)* | `Statut actuel du prêt` | *(vide)* | `Issu de DEMANDE_PRET` |

#### Étape 04 — Séquences, Déclencheurs (Triggers) & Code SQL
- **Séquence(s) associée(s)** : *(laisser vide)*
- **Déclencheur(s) / Triggers & Description** : *(laisser vide)*
- **Script de création de la vue (SQL)** :
  ```sql
  CREATE OR REPLACE VIEW DRH.V_SUIVI_PRET_AGENT AS
  SELECT d.ID_DEMANDE,
         d.MATRICULE_AGENT,
         t.LIBELLE_TYPE_PRET,
         d.MONTANT_DEMANDE,
         d.STATUT_VALIDATION
    FROM DRH.DEMANDE_PRET d
    JOIN DRH.REF_TYPE_PRET t ON t.CODE_TYPE_PRET = d.CODE_TYPE_PRET
   WHERE d.STATUT_VALIDATION IN ('VALIDE', 'SOLDE');
  ```

---

## Exemple 2 : Cas Réel CNaPS — Application « Système d'Information Géographique » (`SIG.SIG_DR`)

Cet exemple reprend les données exactes du modèle Excel officiel `DICO DE DONNES.xlsx` de la CNaPS :

- **Étape 01** :
  - Mode : `➕ Créer / Déclarer une nouvelle application`
  - Nom / Libellé de l'application : `Système d'Information Géographique`
  - Schéma Oracle dédié : `SIG`
  - Description : `Gestion des représentations régionales (Directions Régionales, Antennes et Bureaux) et du découpage géographique de la CNaPS.`
- **Étape 02** :
  - Type : `TABLE` | Case `Table Référentielle (flag_ref)` : ✅ **Cochée**
  - Nom de l'objet : `SIG_DR`
  - Description : `TABLE REFERENCIELLE Contient les noms de chaque représentation avec codes respectifs et la répartition par zone (DR, ANTENNES et BUREAUX)`
- **Étape 03 (Colonnes)** :

| # | COLONNE * | TYPE | TAILLE | CONTRAINTE | SENSIBILITÉ | VALEUR DÉFAUT | DESCRIPTION DE LA COLONNE | NOM INDEXÉ | OBSERVATION |
| :-: | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | `DR_CODE` | `CHAR` | `2` | `PK` | `1 - Public` | *(vide)* | `Code de représentation. Regénéré automatiquement par la séquence` | `PK_SIG_DR` | *(vide)* |
| **2** | `DR_NOM` | `VARCHAR2` | `50` | `Not null` | `2 - Interne` | *(vide)* | `Nom de représentation` | *(vide)* | *(vide)* |
| **3** | `DR_ABREVIATION` | `VARCHAR2` | `5` | `— Aucune —` | `1 - Public` | *(vide)* | `Abréviation de la direction régionale` | *(vide)* | *(vide)* |
| **4** | `DR_LIEU` | `VARCHAR2` | `250` | `— Aucune —` | `1 - Public` | *(vide)* | `Lieu d'implantation` | *(vide)* | *(vide)* |
| **5** | `NUM_MOBILE` | `VARCHAR2` | `15` | `Not null` | `3 - Confidentiel` | *(vide)* | `Numéro flotte pour rejoindre le chef de représentation` | *(vide)* | *(vide)* |
| **6** | `CODE_ZONE` | `VARCHAR2` | `3` | `Not null` | `2 - Interne` | *(vide)* | `Zone de représentation` | *(vide)* | *(vide)* |

- **Étape 04** :
  - Séquence(s) associée(s) : `SIG.SEQ_SIG_DR`
  - Déclencheur(s) / Triggers & Description :
    ```text
    SIG.TRIG_SIG_DR : Déclencheur pour mise à jour automatique de la colonne Num_mobile dans la table SIG.SIG_ADRESSE.
    ```

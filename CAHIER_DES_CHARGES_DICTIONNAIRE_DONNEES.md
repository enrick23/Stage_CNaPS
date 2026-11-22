# CAHIER DES CHARGES DE RÉALISATION

## Conception et développement d'un outil intelligent de génération et de gestion du dictionnaire de données Oracle basé sur l'intelligence artificielle

*Document d'encadrement et de réalisation du projet de stage*

| Élément | Valeur |
| :--- | :--- |
| Propriétaire | CNaPS (Caisse Nationale de Prévoyance Sociale) |
| Rattachement | DSI — Service BI & DBA |
| Projet | Application intranet « Dictionnaire de données » |
| Période de référence | 08/10/2026 au 07/01/2027 |
| Charge planifiée | 66 jours ouvrés |
| Nature | Cahier des charges de réalisation, d'encadrement et des besoins fonctionnels |
| Version | V1.0 — 18/11/2026 |
| Statut | Projet de document — à valider |

## 1. Présentation du projet

### 1.1 Contexte
La CNaPS s’appuie sur un parc important d’applications qui lisent et écrivent dans une base de données Oracle. Jusqu’à présent, la connaissance de ces données, quelles tables une application utilise, ce que contient chaque colonne, quel est son niveau de sensibilité n’est pas encore consignée ni documentée.

Le projet livre un dictionnaire de données centralisé dans la base Oracle, exposé par une application intranet intégrée (API Spring Boot et interface Angular) et alimenté par un assistant d’intelligence artificielle qui analyse le code source des applications et le catalogue Oracle, tout en permettant la saisie manuelle et l’export au format Excel officiel.

### 1.2 Problématique
- Absence de lien applicatif natif dans Oracle : le dictionnaire système Oracle (ALL_TABLES, ALL_TAB_COLUMNS) ignore à quelle application métier est rattachée une table ou une vue donnée.
- Absence de métadonnées sémantiques et de gouvernance : les vues système Oracle ne renseignent ni la description fonctionnelle métier, ni le caractère référentiel d'une table, ni le niveau de sensibilité des colonnes, ni l'explication métier des règles PL/SQL contenues dans les déclencheurs (Triggers).
- Absence de documentation existante et lourdeur d'un recensement manuel : aucune information sur l'usage applicatif des tables et le rôle métier des colonnes n'étant actuellement consignée, documenter manuellement à partir de zéro l'ensemble du parc applicatif de la CNaPS (descriptions, sensibilité, contraintes, triggers, séquences, index) représenterait une charge de travail considérable et un risque élevé d'oubli sans rétro-ingénierie automatisée.
- Absence d'outil centralisé de gouvernance et de restitution : les équipes BI, DBA et études ne disposent d'aucun référentiel partagé dans l'intranet pour consulter la cartographie des données par application, qualifier la sensibilité des informations manipulées et générer à la demande une documentation structurée et à jour.
- Contraintes strictes de sécurité et de confidentialité : l'outil ne doit en aucun cas lire ni exposer les données métiers contenues dans les tables (SELECT * proscrit) ni divulguer les identifiants de connexion (DatabaseCredentials), et doit fonctionner dans le strict respect des privilèges du compte applicatif Oracle.

### 1.3 Finalité du stage
Le stagiaire est chargé de contribuer à l’analyse, la conception, la réalisation, les tests, la documentation et la préparation de la recette de l’outil de dictionnaire de données, sous validation de l’encadrant et du Service BI & DBA.

### 1.4 Périmètre fonctionnel de référence

| Code Module | Intitulé du Module | Finalité fonctionnelle |
| :--- | :--- | :--- |
| M1-CONSULT | Consultation & Exploration du Dictionnaire | Parcourir les applications documentées, afficher la fiche application, filtrer par schéma/application, inspecter la structure détaillée des tables et vues (colonnes, sensibilité, contraintes, index, triggers, séquences, relations FK, script SQL). |
| M2-CONCEPT | Conception & Gestion Manuelle du Dictionnaire | Saisir, compléter ou modifier manuellement le dictionnaire d'une application existante ou nouvelle via un formulaire normé en 4 étapes, avec sauvegarde transactionnelle. |
| M3-GENE_IA | Génération Automatique & Rétro-Ingénierie par IA | Analyser automatiquement du code source (.java JPA, .sql, archives .zip ou texte collé) ou une table Oracle existante, croiser avec ALL_*, générer par IA les descriptions, le FLAG_REF, la sensibilité et l'explication des triggers, puis valider avant persistance unitaire ou par lot. |
| M4-EXPORT & KPI | Export Réglementaire Excel & Indicateurs | Exporter la documentation au format excel et restituer les indicateurs de couverture et de sensibilité du référentiel. |

## 2. Objectifs du stage

### 2.1 Objectif général
Concevoir et réaliser une application web intranet fonctionnelle, sécurisée, traçable et documentée du dictionnaire de données Oracle assisté par IA, consulter et exporter le dictionnaire de données Oracle des applications de la CNaPS.

### 2.2 Objectifs spécifiques
1. Analyser l’existant (modèle Excel officiel, schémas Oracle, applications d’exemple) et formaliser les besoins.
2. Concevoir le modèle de données du dictionnaire et l’architecture fonctionnelle.
3. Réaliser les écrans de consultation, de saisie et de l’assistant IA.
4. Automatiser la génération du dictionnaire à partir du code source et du catalogue Oracle, avec validation humaine.
5. Garantir la qualité des descriptions et la fiabilité de la classification de sensibilité (niveaux 1 à 4).
6. Mettre en œuvre l’authentification, les rôles et l’historisation des opérations.
7. Fournir des indicateurs de couverture et des exports Excel/PDF.
8. Réaliser les tests, corriger les anomalies, préparer la recette et fournir la documentation.

## 3. Documents de référence et livrables de cadrage

| Document | Utilisation |
| :--- | :--- |
| Modèle Excel DICO DE DONNES.xlsx | Référence de structure des 14 colonnes attendues par la CNaPS. |
| Cahier des charges de réalisation | Cadre de travail du stagiaire. |
| MCD / MLD du dictionnaire | Référence de conception des tables IAM.DICO_DATA_*. |
| Matrice RBAC et sensibilité | Référence des profils utilisateurs et des niveaux de sensibilité (1 à 4). |
| Plan de tests | Référence de validation fonctionnelle et technique. |
| Dossier de recette | Preuve de conformité avant livraison. |

La période de référence retenue pour le stage est de 66 jours ouvrés, du 08/10/2026 au 07/01/2027.

## 4. Périmètre fonctionnel détaillé et besoins fonctionnels

### 4.1 M1-CONSULT — Consultation et exploration du dictionnaire
Ce module permet aux équipes de parcourir le référentiel centralisé des données :
- Filtrer la liste des tables et des vues par application métier ou consulter l'ensemble du catalogue tous schémas confondus.
- Afficher la fiche descriptive d'une application (code, libellé, description fonctionnelle et nombre d'objets rattachés).
- Rechercher rapidement une table ou une vue par son schéma et son nom (sous la forme SCHEMA.NOM_OBJET, par exemple DRH.AGENT_DSI ou SIG.SIG_DR).
- Consulter la fiche complète d'un objet répartie en cinq onglets : colonnes (type, taille, contrainte PK/FK/UK/Not null, niveau de sensibilité, valeur par défaut, description, observation), triggers et séquences, index, relations entre tables et script SQL pour les vues.

### 4.2 M2-CONCEPT — Conception et gestion manuelle du dictionnaire
Ce module offre un formulaire en 4 étapes aligné sur les 14 colonnes du modèle Excel de la CNaPS :
- Étape 1 : sélectionner une application existante ou déclarer une nouvelle application (code, libellé, description) et choisir le schéma Oracle concerné.
- Étape 2 : définir l'objet (TABLE ou VIEW), son nom, sa description générale et préciser s'il s'agit d'une table référentielle (flag_ref).
- Étape 3 : saisir ou modifier la grille des colonnes (nom, type, taille, contrainte, sensibilité de 1 à 4, valeur par défaut, description métier, index et observation).
- Étape 4 : renseigner les séquences associées, les déclencheurs (triggers) avec leur rôle métier, ainsi que la requête SQL dans le cas d'une vue.
- Permettre la reprise et la mise à jour d'un objet déjà enregistré via l'action « Compléter / Modifier ».

### 4.3 M3-GENE_IA — Génération automatique et rétro-ingénierie par IA
Ce module automatise la constitution du dictionnaire pour éviter la saisie manuelle complète :
- Importer les sources d'une application par dépôt de fichiers (.java, .sql, archive .zip), par collage direct d'un extrait de code (entité JPA ou script SQL), ou en sélectionnant directement une table déjà présente dans Oracle.
- Extraire automatiquement la structure technique des objets (tables, vues, colonnes, types, clés primaires et étrangères, séquences, triggers) et croiser ces informations avec le dictionnaire système Oracle (ALL_TAB_COLUMNS, ALL_CONSTRAINTS, ALL_IND_COLUMNS, ALL_TRIGGERS).
- Générer automatiquement en français les descriptions fonctionnelles des tables et des colonnes, détecter les tables référentielles, proposer le niveau de sensibilité (1 à 4) et traduire les triggers PL/SQL en règles de gestion compréhensibles.
- Fonctionner aussi bien avec un modèle de langage (LLM) qu'en mode 100 % hors-ligne grâce à un moteur de règles métier CNaPS intégré.
- Pré-remplir le formulaire pour permettre à l'utilisateur de vérifier et d'ajuster les résultats avant d'enregistrer un objet seul ou tout un lot d'objets en une fois.

### 4.4 M4-EXPORT & KPI — Export réglementaire Excel et indicateurs
Ce module assure la restitution et le suivi de l'avancement de la documentation :
- Exporter à la demande une table, une vue ou l'ensemble des objets d'une application au format Excel officiel (.xlsx à 14 colonnes avec fusion automatique des cellules par application et par table).
- Présenter les indicateurs de couverture du dictionnaire : nombre d'applications et de schémas documentés, répartition entre tables métiers, tables référentielles et vues, et répartition des colonnes par niveau de sensibilité.

### 4.5 Besoins non fonctionnels et règles de sécurité
- Lecture limitée aux métadonnées : aucune requête ne doit lire le contenu métier des tables (SELECT * interdit).
- Protection des accès : les identifiants techniques de connexion à la base ne sont jamais exposés dans les échanges API.
- Compatibilité avec les droits du compte applicatif Oracle (DEV_DEPL) : les sauvegardes et mises à jour s'effectuent uniquement par SELECT, INSERT et UPDATE, sans utiliser d'instruction DELETE.

## 5. Acteurs et responsabilités

| Acteur | Responsabilités |
| :--- | :--- |
| Stagiaire | Analyse, conception, développement, intégration de l'assistant IA, tests, documentation et préparation de la recette. |
| Responsable BI & DBA | Pilotage, validation fonctionnelle et technique, arbitrage et réception des livrables. |
| Encadrant technique | Suivi quotidien et hebdomadaire, revue du code et assistance technique. |
| DBA / Équipe BI | Validation du modèle de données dans le schéma IAM, vérification des scripts SQL et exploitation du dictionnaire. |
| Développeurs / Chefs de projet | Utilisation de l'outil et de l'assistant IA pour documenter les applications existantes et nouvelles. |
| Utilisateurs pilotes / Auditeurs | Consultation du dictionnaire, contrôle des niveaux de sensibilité, tests utilisateurs et retour d'expérience. |
| DSI | Validation des choix d'intégration, de sécurité et de mise en production. |

## 6. Architecture cible

L'architecture ci-dessous présente l'organisation des composants de l'application intranet et ses interactions avec la base Oracle.

![Architecture cible](C:\Users\enric\.gemini\antigravity\brain\7a18dfea-353f-4568-8f58-64b0a4114b75\architecture_cible_dico.png)

### 6.1 Principes
- Séparation entre l'interface Angular, l'API REST Spring Boot et l'accès aux données Oracle.
- Contrôle et validation des entrées côté serveur.
- Interrogation stricte des vues système (ALL_*) et des tables du dictionnaire (IAM.DICO_DATA_*) sans lecture des données métiers.
- Continuité de service de l'assistant IA grâce au repli automatique sur le moteur expert local en cas d'absence de réseau.
- Journalisation des erreurs et traçabilité des mises à jour.

## 7. Conception fonctionnelle

### 7.1 Fiche standard à produire pour chaque fonctionnalité

| Rubrique | Contenu attendu |
| :--- | :--- |
| Identifiant | Code unique de la fonctionnalité (ex. F01-CONSULT, F02-CONCEPT, F03-IA-CODE, F04-EXPORT). |
| Objectif | Finalité métier de la fonctionnalité. |
| Acteurs | Profils autorisés à utiliser la fonction. |
| Préconditions | Conditions nécessaires avant exécution (droits d'accès, connexion Oracle active). |
| Données d'entrée | Champs du formulaire, filtres ou fichiers sources (.java, .sql, .zip). |
| Traitement | Étapes successives réalisées par le frontend, le backend et la base Oracle. |
| Règles de gestion | Contraintes métier (structure à 14 colonnes, classification de sensibilité 1 à 4, mise à jour sans DELETE). |
| Contrôles | Contrôles de validité des schémas, des noms d'objets et de la taille des champs. |
| Sorties | Affichage à l'écran, pré-remplissage du formulaire ou fichier Excel généré. |
| Historisation | Éléments de suivi et de traçabilité à enregistrer. |
| Cas d'erreur | Comportement et message affiché en cas d'échec ou de donnée invalide. |
| Critère d'acceptation | Condition vérifiable permettant de déclarer la fonctionnalité conforme. |

### 7.2 Workflows à produire

![Cycle de réalisation du projet](C:\Users\enric\.gemini\antigravity\brain\7a18dfea-353f-4568-8f58-64b0a4114b75\cycle_realisation_dico.png)

- Workflow général de navigation dans l'application Dictionnaire de données
- Workflow de consultation et de recherche par application et par schéma (M1-CONSULT)
- Workflow de saisie et de mise à jour manuelle en 4 étapes (M2-CONCEPT)
- Workflow d'analyse de code source et de rétro-ingénierie par IA avec validation humaine (M3-GENE_IA)
- Workflow d'export Excel et de suivi des indicateurs (M4-EXPORT & KPI)
- Workflow de recette et de validation

## 8. Conception des données — MCD / MLD

### 8.1 Entités minimales

![MCD fonctionnel minimal](C:\Users\enric\.gemini\antigravity\brain\7a18dfea-353f-4568-8f58-64b0a4114b75\mcd_dico_data.png)

| Entité (Schéma IAM) | Attributs / contenu attendu |
| :--- | :--- |
| DICO_DATA_TABLE | Identifiant (TABLE_ID), nom de la table ou vue (NOM_TABLE), type d'objet (TABLE / VIEW), description fonctionnelle, indicateur référentiel (FLAG_REF Y/N), schéma Oracle (SCHEMAS), identifiant et libellé de l'application (APP_ID, APP_LIBELLE), séquence associée. |
| DICO_DATA_COLUMN | Identifiant (COLUMN_ID), nom de la colonne (COLUMN_NAME), ordre d'affichage, niveau de sensibilité (1 à 4), type Oracle (TYPES), taille/précision (TAILLES), contrainte (PK, FK, UK, Not null), valeur par défaut, description métier, observation, rattachement à la table (TABLE_ID). |
| DICO_DATA_INDEXES | Identifiant (INDEX_ID), nom de l'index (NOM_INDEX), description, rattachement à la colonne (COLUMN_ID). |
| DICO_DATA_TRIGGER | Identifiant (TRIGGER_ID), nom du trigger (NOM_TRIGGER), description de la règle de gestion, rattachement à la table (TABLE_ID). |
| DICO_DATA_VIEW | Identifiant (VIEW_ID), nom de la vue (NOM_VIEW), description, script SQL de création (SCRIPT), rattachement à la table (TABLE_ID). |
| DICO_DATA_FUNCTION | Identifiant (FUNCTION_ID), nom de la fonction (NOM_FONCTION), description, rattachement à la table (TABLE_ID). |

Ces six tables stockées dans le schéma IAM couvrent l'intégralité des 14 colonnes du modèle Excel officiel de la CNaPS (Application, Schéma, Table/Vue et description, Colonne, Niveau de sensibilité, Type, Taille, Contrainte, Valeur par défaut, Description de la colonne, Séquence, Triggers, Index, Observation).

### 8.2 Livrables de conception des données
- MCD validé
- MLD relationnel dans le schéma IAM
- Dictionnaire de données des tables IAM.DICO_DATA_*
- Contraintes PK / FK / UNIQUE / NOT NULL
- Séquences et triggers d'auto-génération des identifiants (SEQ_DICO_DATA_*, TRG_DICO_DATA_*_BI)
- Script SQL de création des tables (schema_dico_data.sql)
- Jeux de données d'exemple et corpus d'applications de test

## 9. Matrice RBAC et classification de sensibilité

### 9.1 Matrice des droits d'accès (base de travail à valider)

| Fonctionnalité | Admin / DBA | Développeur / Études | Chef de Service | Auditeur |
| :--- | :--- | :--- | :--- | :--- |
| Consulter le catalogue et les fiches applications | CRUD | R | R (selon périmètre) | R |
| Consulter le détail d'une table ou d'une vue | Oui | Oui | Oui | Oui |
| Créer ou déclarer une nouvelle application / table | Oui | Oui | Non | Non |
| Compléter ou modifier un dictionnaire existant | Oui | Selon habilitation | Non | Non |
| Utiliser l'assistant IA (analyse code et base) | Oui | Oui | Non | Non |
| Enregistrer un lot d'objets validés | Oui | Oui | Non | Non |
| Exporter le dictionnaire en Excel (.xlsx) | Oui | Oui | Oui | Oui |

### 9.2 Niveaux de sensibilité des colonnes

| Niveau | Libellé | Définition et exemples |
| :--- | :--- | :--- |
| 1 | Public | Nomenclatures générales, codes et libellés de tables référentielles non nominatives (ex. CODE_TYPE, LIBELLE, ACTIF). |
| 2 | Interne | Données de gestion interne, identifiants techniques, dates de suivi, statuts de traitement (ex. ID_DEMANDE, DATE_CREATION, STATUT). |
| 3 | Confidentiel | Données personnelles identifiant un agent, un assuré ou un employeur (ex. MATRICULE, NOM, PRENOMS, CIN, TELEPHONE, EMAIL). |
| 4 | Secret | Données financières sensibles ou secrets de sécurité (ex. SALAIRE, MONTANT_PRET, RIB, MOT_DE_PASSE). |

## 10. Maquettes à réaliser

| ID | Écran | Éléments minimum |
| :--- | :--- | :--- |
| M01 | En-tête et navigation du module | Bascule Consultation / Nouveau dictionnaire, bouton d'accès à l'Assistant IA, notifications de succès ou d'erreur. |
| M02 | Consultation — Filtres et fiche application | Liste déroulante des applications avec recherche, résumé de l'application sélectionnée et nombre d'objets liés. |
| M03 | Consultation — Catalogue des tables et vues | Recherche par schéma ou nom d'objet, liste latérale des tables et vues avec nombre de colonnes. |
| M04 | Consultation — Détail d'un objet | Fil d'Ariane, badge Référentiel, onglets Colonnes, Triggers & séquences, Index, Relations et Script de la vue, boutons Modifier et Exporter. |
| M05 | Conception — Étapes 1 et 2 | Choix ou création d'application, sélection du schéma Oracle, choix Table/Vue, case Table référentielle et description générale. |
| M06 | Conception — Étape 3 (Grille des colonnes) | Tableau de saisie des colonnes (nom, type, taille, contrainte, sensibilité 1 à 4, valeur par défaut, description, index, observation). |
| M07 | Conception — Étape 4 (Séquences, triggers et vue) | Saisie des séquences, des triggers et du script SQL pour les vues, boutons d'enregistrement. |
| M08 | Assistant IA — Fenêtre d'importation | Choix du mode d'analyse (dépôt de fichiers .java/.sql/.zip, collage de code ou sélection d'une table Oracle existante). |
| M09 | Assistant IA — Résultats et validation | Bandeau des objets détectés avec bascule d'un objet à l'autre, pré-remplissage du formulaire et bouton « Tout enregistrer ». |
| M10 | Export Excel | Téléchargement du fichier Excel formaté selon les 14 colonnes officielles. |

### 10.1 Règles d'ergonomie
- Libellés clairs et conformes au vocabulaire métier de la CNaPS.
- Champs obligatoires facilement identifiables dans le formulaire.
- Messages d'erreur et de confirmation explicites.
- Recherche et filtrage rapides sur les listes d'applications, de schémas et de tables.
- Respect de la charte graphique de la CNaPS (couleurs sobres, typographie homogène et badges lisibles pour les niveaux de sensibilité).

## 11. Développement

### 11.1 Principes
- Développer uniquement les fonctionnalités validées dans le périmètre.
- Respecter l'architecture et les conventions de nommage du projet (Spring Boot et Angular).
- Ne jamais stocker de mots de passe ou de clés d'API en clair dans le code source.
- Valider systématiquement les paramètres côté serveur avant exécution des requêtes SQL.
- Séparer la logique d'analyse (code et IA), les contrôleurs REST et l'accès aux données JDBC.
- Respecter les privilèges du compte Oracle applicatif (opérations SELECT, INSERT et UPDATE uniquement).
- Commenter et documenter les traitements principaux.

### 11.2 Gestion des anomalies

| Niveau | Définition | Traitement |
| :--- | :--- | :--- |
| Bloquante | Empêche l'utilisation d'une fonction majeure (enregistrement en base, analyse IA ou export). | Correction prioritaire avant recette. |
| Majeure | Fonction disponible mais résultat incomplet ou erreur sur certains cas de données. | Correction avant validation du module. |
| Mineure | Défaut d'affichage ou d'ergonomie sans impact sur les données enregistrées. | Correction planifiée. |
| Évolution | Nouvelle demande hors périmètre initial. | Validation séparée avec l'encadrant avant développement. |

## 12. Plan de travail — 66 jours ouvrés

La période de référence du projet représente 66 jours ouvrés répartis sur 14 semaines, avec une dernière journée consacrée à la formation et à la clôture.

| Semaine | Dates | Jours | Phase | Activités principales | Livrable |
| :--- | :--- | :--- | :--- | :--- | :--- |
| S1 | 08/10–14/10 | 5 | Cadrage et prise en main | Planification du projet, étude de l'existant, installation des environnements (Java, Angular, Oracle) et prise en main. | Note de cadrage + feuille de route |
| S2 | 15/10–21/10 | 5 | Analyse et spécifications | Analyse détaillée des besoins, rédaction du document technique, début de conception du modèle de données. | Dossier d'analyse |
| S3 | 22/10–28/10 | 5 | MCD / MLD et maquettes | Finalisation du MCD/MLD, maquettes des écrans, création des tables et triggers IAM.DICO_DATA_* dans Oracle. | MCD + MLD + maquettes + script SQL |
| S4 | 29/10–04/11 | 5 | Développement M1-CONSULT (1/2) | Filtre par application, fiche détaillée d'application, catalogue des tables et vues par schéma. | Consultation catalogue V1 |
| S5 | 05/11–11/11 | 5 | Développement M1-CONSULT (2/2) | Consultation détaillée des colonnes, triggers, séquences, index, relations FK et script SQL des vues. | Module M1-CONSULT complet |
| S6 | 12/11–18/11 | 5 | Export Excel et début M2-CONCEPT | Export Excel au format CNaPS (14 colonnes), formulaire de déclaration d'application et sélection des schémas. | Export Excel + Étape 1 du formulaire |
| S7 | 19/11–25/11 | 5 | Développement M2-CONCEPT (suite) | Définition table/vue et table référentielle, grille dynamique des colonnes, sensibilité, séquences et triggers. | Formulaire de saisie en 4 étapes |
| S8 | 26/11–02/12 | 5 | Mise à jour et validation M2 | Modification d'un objet existant du dictionnaire, contrôles de cohérence et validation des métadonnées saisies. | Module M2-CONCEPT complet |
| S9 | 03/12–09/12 | 5 | Charte graphique et M3-GENE_IA (1/2) | Harmonisation visuelle CNaPS, module de lecture du code source (.java, .sql, .zip) et détection automatique des objets. | Analyseur de code source V1 |
| S10 | 10/12–16/12 | 5 | Développement M3-GENE_IA (2/2) | Croisement avec le catalogue Oracle, génération IA des descriptions et de la sensibilité, pré-remplissage et sauvegarde par lot. | Module M3-GENE_IA complet |
| S11 | 17/12–23/12 | 5 | Indicateurs M4 et début des tests | Compteurs d'applications, schémas, tables/vues/référentiels, répartition par sensibilité et synthèse de couverture. | Indicateurs + premiers tests |
| S12 | 24/12–30/12 | 5 | Tests et corrections | Tests fonctionnels, tests d'intégration Oracle, corrections des anomalies et améliorations. | Rapport de tests |
| S13 | 31/12–06/01 | 5 | Validation SI, déploiement et guides | Présentation au niveau du SI, mise en production, rédaction du guide technique et du manuel d'utilisation. | Version déployée + guides |
| S14 | 07/01/2027 | 1 | Formation et clôture | Formation des utilisateurs, livraison finale et bilan de stage. | Version finale + bilan |

## 13. Jalons de validation

| Jalon | Échéance indicative | Condition de passage |
| :--- | :--- | :--- |
| J1 — Cadrage | Fin S1 | Planning et périmètre validés avec l'encadrant. |
| J2 — Analyse | Fin S2 | Besoins fonctionnels et spécifications techniques validés. |
| J3 — Conception et BDD | Fin S3 | MCD/MLD, maquettes et tables Oracle IAM.DICO_DATA_* validés. |
| J4 — Consultation et Export | Fin S6 | Module de consultation et export Excel 14 colonnes démontrables. |
| J5 — Conception manuelle | Fin S8 | Formulaire de création et de modification d'un dictionnaire opérationnel. |
| J6 — Génération par IA | Fin S10 | Analyse automatique du code et des tables Oracle avec enrichissement IA fonctionnelle. |
| J7 — Indicateurs et intégration | Fin S11 | Tous les modules intégrés et indicateurs de couverture disponibles. |
| J8 — Tests | Fin S12 | Cas de tests exécutés et anomalies bloquantes corrigées. |
| J9 — Recette et livraison | 07/01/2027 | Application validée, documentation remise et présentation effectuée. |

## 14. Plan de tests

| Type | Objectif | Exemples |
| :--- | :--- | :--- |
| Tests unitaires | Vérifier les composants isolés. | Analyse d'une classe JPA ou d'un script SQL, attribution du niveau de sensibilité, détection de table référentielle. |
| Tests fonctionnels | Vérifier les règles métier sur chaque module. | Consultation par application, création manuelle d'une table et d'une vue, modification d'une colonne. |
| Tests d'intégration | Vérifier les échanges avec la base Oracle. | Croisement avec les vues ALL_*, enregistrement unitaire et par lot dans les 6 tables IAM.DICO_DATA_*. |
| Tests de sécurité et droits | Vérifier le respect des contraintes Oracle. | Aucune lecture des données métiers (pas de SELECT *), sauvegarde fonctionnelle avec le compte DEV_DEPL sans DELETE. |
| Tests de continuité IA | Vérifier le fonctionnement hors-ligne. | Bascule automatique sur le moteur expert local lorsque aucun service LLM externe n'est configuré. |
| Tests d'export | Vérifier la conformité des livrables générés. | Ouverture du fichier Excel .xlsx généré et contrôle des 14 colonnes et des cellules fusionnées. |
| Tests de non-régression | Vérifier la stabilité après correction. | Rejouer les scénarios de consultation, de saisie et d'analyse IA après chaque évolution. |

## 15. Cas de tests minimum à préparer

| ID | Cas | Résultat attendu |
| :--- | :--- | :--- |
| CT-01 | Filtre par application | Affichage de la fiche application et de la liste des tables/vues rattachées. |
| CT-02 | Recherche d'une table par schéma et nom | Filtrage immédiat du catalogue et affichage des 5 onglets de détail au clic. |
| CT-03 | Déclaration d'une nouvelle application et d'une table référentielle | Enregistrement dans IAM.DICO_DATA_TABLE avec FLAG_REF = 'Y' et affichage du badge Référentiel. |
| CT-04 | Modification d'une table existante avec retrait d'une colonne | Mise à jour réussie sans instruction DELETE (désactivation logique de la colonne retirée). |
| CT-05 | Enregistrement d'une vue avec son script SQL | Sauvegarde du script dans IAM.DICO_DATA_VIEW et affichage dans l'onglet Script de la vue. |
| CT-06 | Analyse IA d'une archive .zip (entités Java et scripts SQL) | Détection automatique de l'ensemble des tables, colonnes, contraintes, séquences et triggers. |
| CT-07 | Attribution automatique du niveau de sensibilité (1 à 4) | Niveaux cohérents selon la nature des colonnes (code public = 1, date suivi = 2, matricule/téléphone = 3, salaire/montant = 4). |
| CT-08 | Explication automatique d'un trigger PL/SQL | Génération d'une phrase claire en français résumant l'action du déclencheur. |
| CT-09 | Rétro-ingénierie IA directe d'une table Oracle existante | Récupération de la structure physique dans ALL_* et génération des descriptions sans code source. |
| CT-10 | Fonctionnement de l'assistant IA hors-ligne | Enrichissement complet assuré par le moteur expert local sans erreur réseau. |
| CT-11 | Enregistrement par lot (« Tout enregistrer ») | Sauvegarde transactionnelle de tous les objets détectés et mise à jour du catalogue. |
| CT-12 | Export Excel d'une table ou d'une application | Fichier .xlsx téléchargé conforme au modèle officiel à 14 colonnes de la CNaPS. |

## 16. Critères de recette
- Les quatre modules (M1-CONSULT, M2-CONCEPT, M3-GENE_IA, M4-EXPORT & KPI) sont accessibles et fonctionnels.
- Les workflows de consultation, de saisie manuelle et de génération par IA sont respectés.
- Le modèle de données IAM.DICO_DATA_* (6 tables, séquences et triggers) est respecté.
- Aucune donnée métier n'est lue dans les tables applicatives et les opérations fonctionnent avec les droits du compte DEV_DEPL.
- Les descriptions générées par l'IA et les niveaux de sensibilité (1 à 4) sont pertinents et modifiables par l'utilisateur avant sauvegarde.
- Les anomalies bloquantes sont corrigées.
- Les exports Excel et les indicateurs de couverture sont cohérents.
- Les guides utilisateur et technique sont fournis.
- Le code source, les scripts SQL et les éléments nécessaires au déploiement sont livrés.
- Une démonstration finale est réalisée devant l'encadrant et le Service BI & DBA.

## 17. Livrables obligatoires du stagiaire

| Réf. | Livrable | Moment |
| :--- | :--- | :--- |
| L01 | Note de cadrage et planning de stage | S1 |
| L02 | Analyse de l'existant et cahier des charges | S2 |
| L03 | Spécifications fonctionnelles et techniques du module IA | S2–S3 |
| L04 | MCD / MLD du dictionnaire de données | S3 |
| L05 | Scripts SQL de création (schema_dico_data.sql) | S3 |
| L06 | Architecture cible et maquettes des écrans | S3 |
| L07 | Matrice RBAC et grille de sensibilité des données | S3 |
| L08 | Code source Backend Spring Boot et Frontend Angular versionné | À chaque phase |
| L09 | Corpus d'applications exemples pour les tests IA | S9–S10 |
| L10 | Plan et fiches de tests | S11–S12 |
| L11 | Registre et suivi des corrections d'anomalies | S12 |
| L12 | Dossier de recette | S13 |
| L13 | Guide utilisateur | S13 |
| L14 | Guide technique et d'installation | S13 |
| L15 | Version finale déployée et présentation de clôture | S14 |

## 18. Documentation technique minimale
- Pré-requis techniques (Java, Maven, Node.js / Angular, Oracle).
- Installation et configuration de l'environnement local et serveur.
- Structure du projet Backend et Frontend.
- Paramètres de configuration (connexion Oracle, schémas autorisés, configuration du moteur IA).
- Modèle de base de données IAM.DICO_DATA_* et scripts SQL associés.
- Fonctionnement de la sauvegarde sans instruction DELETE pour le compte DEV_DEPL.
- Architecture du pipeline d'analyse de code et du moteur IA (LLM et moteur expert local).
- Gestion des erreurs et journalisation.
- Procédure de déploiement.

## 19. Documentation utilisateur minimale
- Accès au module Dictionnaire de données.
- Consultation du catalogue par application et recherche d'une table ou d'une vue.
- Lecture de la fiche détaillée d'un objet (colonnes, sensibilité, triggers, séquences, index, relations, script de vue).
- Création ou déclaration d'une nouvelle application et saisie manuelle d'une table ou d'une vue en 4 étapes.
- Modification et complétion d'un dictionnaire existant.
- Utilisation de l'Assistant IA (dépôt de fichiers .java/.sql/.zip, collage de code, rétro-ingénierie d'une table Oracle).
- Vérification des propositions de l'IA et enregistrement unitaire ou par lot.
- Export du dictionnaire au format Excel (.xlsx).
- Questions fréquentes et résolution des erreurs courantes.

## 20. Points nécessitant validation avant développement

Les éléments suivants doivent faire l'objet d'une validation avec l'encadrant technique et le Service BI & DBA :
- Modalités d'authentification et d'intégration dans l'application intranet existante.
- Politique d'exécution du moteur IA en production (utilisation du moteur expert local hors-ligne, serveur LLM interne sur le réseau CNaPS ou API externe).
- Liste des schémas Oracle ouverts à la consultation et à la rétro-ingénierie.
- Validation définitive de la grille des 4 niveaux de sensibilité des colonnes.
- Matrice définitive des rôles et habilitations selon les profils utilisateurs.
- Exécution des scripts DDL et attribution des droits sur le schéma IAM par l'équipe DBA.
- Environnements de développement, de recette et de production.

## 21. Gouvernance du stage

| Rituel | Fréquence | Contenu |
| :--- | :--- | :--- |
| Point avec encadrant | Quotidien / selon besoin | Blocages techniques, priorités, tâches en cours. |
| Revue hebdomadaire | Chaque semaine | Avancement par rapport au planning de 66 jours, livrables, risques. |
| Démonstration | Fin de phase | Présentation des fonctionnalités développées sur chaque module. |
| Revue de code | À chaque module | Qualité du code, requêtes SQL, sécurité et respect des conventions. |
| Revue fonctionnelle | À chaque jalon | Conformité des écrans et des exports au besoin du Service BI & DBA. |
| Recette finale | Fin du projet | Validation globale avant clôture du stage. |

## 22. Gestion des risques

| Risque | Impact | Mesure préventive |
| :--- | :--- | :--- |
| Périmètre ou structure des 14 colonnes mal compris | Élevé | Validation du modèle Excel de référence et des tables IAM.DICO_DATA_* dès le début du stage. |
| Privilèges restreints du compte Oracle DEV_DEPL | Élevé | Concevoir toutes les mises à jour uniquement par SELECT, INSERT et UPDATE sans instruction DELETE. |
| Indisponibilité d'un service IA externe ou réseau isolé | Élevé | Prévoir un moteur expert local fonctionnant hors-ligne en secours automatique. |
| Imprécision de l'IA sur certaines descriptions ou types | Moyen | Croiser systématiquement avec le catalogue physique Oracle (ALL_*) et imposer une étape de validation humaine avant sauvegarde. |
| Retard sur le planning de développement | Moyen / élevé | Suivi hebdomadaire par jalons et livraison progressive module par module. |
| Données de test insuffisantes | Moyen | Préparer un corpus varié d'applications exemples (Java JPA et scripts SQL) dès la phase de conception. |
| Documentation incomplète en fin de stage | Moyen | Rédiger la documentation technique et le guide utilisateur au fil de l'avancement. |

## 23. Définition de « terminé »

Une fonctionnalité est considérée comme terminée uniquement si elle est développée, conforme aux règles de sécurité Oracle, testée avec des données réelles ou d'exemple, documentée et démontrée à l'encadrant. Une fonctionnalité seulement codée mais non testée ni validée n'est pas considérée comme livrée.

## 24. Validation du cahier des charges

| Fonction | Nom | Date | Signature |
| :--- | :--- | :--- | :--- |
| Responsable BI & DBA | Heritiana ANDRIAMANAMPISON | | |
| Encadrant technique | Heritiana ANDRIAMANAMPISON | | |
| Représentant DSI | | | |
| Stagiaire | | | |

## ANNEXE — CHECK-LIST HEBDOMADAIRE DU STAGIAIRE

| Semaine | Questions de contrôle |
| :--- | :--- |
| Chaque semaine | Qu'ai-je réalisé ? Qu'ai-je livré ? Quelles anomalies ? Quels blocages ? Quelle validation est attendue ? |
| Avant développement | Le besoin est-il validé ? La structure des tables IAM.DICO_DATA_* et des 14 colonnes Excel est-elle respectée ? |
| Avant livraison module | Tests passés ? Compatibilité compte DEV_DEPL (sans DELETE ni SELECT *) ? Mode hors-ligne IA fonctionnel ? Documentation à jour ? |
| Avant recette | Tous les cas de test sont-ils exécutés ? Les exports Excel sont-ils conformes ? Les anomalies bloquantes sont-elles clôturées ? |
| Avant clôture | Code + scripts SQL + corpus de test + documentation technique + guide utilisateur + dossier de recette + présentation sont-ils disponibles ? |

# Installation locale Oracle

Ces scripts reproduisent les tables externes consultées par les repositories et ajoutent quelques données d'essai.

## Prérequis

Un administrateur Oracle doit créer les utilisateurs/schémas `IAM`, `DRH`, `TRESO` et `DEV_DEPL`. Accorder `CREATE SESSION` à chacun, `CREATE TABLE` aux propriétaires `IAM`, `DRH` et `TRESO`, et un quota de tablespace à ces propriétaires. Sans `CREATE TABLE`, les scripts d'initialisation échouent avec `ORA-01031`.

Exemple à exécuter par l'administrateur (adapter le tablespace si nécessaire) :

```sql
GRANT CREATE SESSION TO IAM, DRH, TRESO, DEV_DEPL;
GRANT CREATE TABLE TO IAM, DRH, TRESO;
ALTER USER IAM QUOTA UNLIMITED ON USERS;
ALTER USER DRH QUOTA UNLIMITED ON USERS;
ALTER USER TRESO QUOTA UNLIMITED ON USERS;
```

Les scripts ci-dessous ne créent pas les utilisateurs et ne contiennent aucun mot de passe.

## Ordre d'exécution

1. Connecté au propriétaire `IAM`, exécuter `conception.sql`.
2. Toujours connecté au propriétaire `IAM`, exécuter `revue_matrice.sql` pour créer `IAM.REVUE_MATRICE`, sa séquence et ses triggers.
3. Connecté au propriétaire `DRH`, exécuter `local_drh.sql`.
4. Connecté au propriétaire `TRESO`, exécuter `local_treso.sql`.
5. Connecté au propriétaire `IAM`, exécuter `local_iam_grants.sql`.

Les scripts `local_drh.sql` et `local_treso.sql` accordent les droits correspondants à `DEV_DEPL`. Si le nom de l'utilisateur applicatif change, adapter ces commandes `GRANT`.

## Connexion de l'application

Modifier localement `src/main/resources/application.properties` pour viser le service Oracle local (hôte, port et service/PDB), puis renseigner l'utilisateur et le mot de passe locaux de `DEV_DEPL`. Ne pas remplacer les identifiants du serveur partagé par des identifiants locaux dans une configuration destinée au dépôt.

Les données d'essai permettent de tester une direction, un service, une fonction, un agent, une section et une application. Elles peuvent être supprimées ou remplacées après validation.


-- 1. Créer l'utilisateur (définir le mot de passe et l'espace de table par défaut)
CREATE USER TRESO IDENTIFIED BY "tafita"
DEFAULT TABLESPACE USERS
TEMPORARY TABLESPACE TEMP;

-- 2. Accorder le privilège de connexion (CREATE SESSION)
GRANT CREATE SESSION TO TRESO;

-- 3. Accorder le quota d'espace de table (permettre la création d'objets dans l'espace de table spécifié)
ALTER USER TRESO QUOTA UNLIMITED ON USERS;
# Application : RevuServices (Revue des Habilitations CNaPS)

> **Application** : `RevuServices`  
> **Code / APP_ID** : `REVUSERVICES` (ou UUID `550E8400-E29B-41D4-A716-446655440010`)  
> **Schéma Oracle** : `IAM`  
> **Domaine** : Sécurité, Contrôle Interne & Habilitations

## Objets Oracle Définis
1. **`IAM.APP_DMD_AGENT_CONCERNE`** (Table) : Agents ciblés par la campagne de revue des habilitations.
2. **`IAM.APP_DMD_PRIVILEGE_DETAIL`** (Table) : Détail des privilèges attribués par profil, service et application.
3. **`IAM.APP_DMD_PRIVILEGE_HISTO`** (Table) : Historique d'audit des modifications de privilèges.
4. **`IAM.TRG_PRIVILEGE_AUDIT`** (Trigger) : Déclencheur après modification/suppression consignant les actions dans l'audit.

## Comment tester dans l'Assistant IA
- **Option 1 (Drag & Drop)** : Glissez l'ensemble des fichiers `.java` et `.sql` ou l'archive `RevuServices.zip` dans la zone de dépôt.
- **Option 2 (Copier-Coller)** : Copiez le contenu de `PrivilegeDetail.java` dans l'onglet *« Coller un extrait de code »* et cliquez sur *Analyser*.

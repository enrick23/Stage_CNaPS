# Application : Gestion des Prêts et Avances (DRH CNaPS)

> **Application** : `Gestion des Prêts et Avances`  
> **Code / APP_ID** : `GEST_PRETS` (ou UUID `550E8400-E29B-41D4-A716-446655440001`)  
> **Schéma Oracle** : `DRH`  
> **Domaine** : Ressources Humaines, Paie & Prêts aux Agents

## Objets Oracle Définis
1. **`DRH.REF_TYPE_PRET`** (Table de référence) : Barèmes et types de prêts (`FLAG_REF = Y`).
2. **`DRH.DEMANDE_PRET`** (Table métier) : Demandes soumises par les agents (montant, mensualité, statut).
3. **`DRH.V_SUIVI_PRET_AGENT`** (Vue) : Vue consolidée pour le suivi financier.
4. **`DRH.TRG_BI_DEMANDE_PRET`** (Trigger) : Alimentation de la séquence `DRH.SEQ_DEMANDE_PRET` et horodatage.

## Comment tester dans l'Assistant IA
- Glissez `Gestion_Prets_Avances.zip` dans l'Assistant IA pour tester la détection simultanée d'une table de référence, d'une table transactionnelle et d'une vue avec son script SQL.
- Vérifiez la classification de sensibilité : `MONTANT_DEMANDE` sera classifié en Niveau 4 (Secret).

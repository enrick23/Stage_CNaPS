# Application : Système d'Information Géographique (Cartographie SIG)

> **Application** : `Système d'Information Géographique`  
> **Code / APP_ID** : `SIG` (ou UUID `550E8400-E29B-41D4-A716-446655440002`)  
> **Schéma Oracle** : `SIG`  
> **Domaine** : Organisation Territoriale, Directions Régionales & Antennes

## Objets Oracle Définis
1. **`SIG.SIG_DR`** (Table référentielle) : Directions régionales (correspond exactement à la table d'exemple du classeur officiel `DICO DE DONNES (1).xlsx`).
2. **`SIG.SIG_AGENCE`** (Table métier) : Agences locales et coordonnées de contact.
3. **`SIG.TRIG_SIG_DR`** (Trigger) : Mise à jour automatique en cascade de la flotte mobile.

## Comment tester dans l'Assistant IA
- Glissez `Cartographie_SIG.zip` dans l'Assistant IA.
- L'IA générera automatiquement les descriptions fonctionnelles et le caractère référentiel (`FLAG_REF = Y` sur `SIG_DR`).

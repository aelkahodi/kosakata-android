# Guide Play Console — Kosakata (KataLab)

> Réponses prêtes à copier pour chaque formulaire. Réutilisable pour Wordstock et Frasa
> en changeant le nom, la langue et les compteurs.

---

## 1. Créer l'application

**Toutes les applications → Créer une application**

| Champ | Valeur |
|---|---|
| Nom de l'application | `Kosakata : indonésien facile` |
| Langue par défaut | Français (France) – fr-FR |
| Application ou jeu | **Application** |
| Gratuite ou payante | **Gratuite** (le premium est un achat intégré) |

⚠️ « Gratuite » est **irréversible** — mais c'est bien le bon choix pour un freemium.

Cocher les deux déclarations (règles du programme + lois américaines sur l'exportation).

---

## 2. Fiche du Play Store (Présence sur le Store → Fiche principale)

**Textes** : voir `store/fiche-store.md` (versions FR et EN prêtes).

| Élément | Spécification |
|---|---|
| Icône | 512 × 512 PNG (fourni) |
| Image de présentation | 1024 × 500 PNG (fourni) |
| Captures téléphone | Min. 2, idéalement 4-6 — PNG/JPEG, 16:9 ou 9:16, côté 320-3840 px |

**Ordre conseillé des captures** : carte des quêtes → question de quiz avec emoji → feedback avec synonymes → leçon de grammaire → dictionnaire → tableau de bord.

**Traduction anglaise** : ajouter en-US et coller la partie « English listing » de `fiche-store.md`.

---

## 3. Contenu de l'application (Règles → Contenu de l'application)

### Politique de confidentialité
```
https://katalab.eu/privacy/kosakata.html
```

### Accès à l'application
> Tout le contenu est accessible sans identifiants particuliers.
**« Aucun identifiant requis »** — l'app n'a ni compte ni connexion.

### Annonces
**Non**, l'application ne contient pas d'annonces.

### Sécurité des données (Data safety) — le formulaire le plus long

| Question | Réponse |
|---|---|
| Collectez-vous ou partagez-vous des types de données utilisateur requis ? | **Non** |
| Les données sont-elles chiffrées en transit ? | Sans objet (aucune collecte) |
| Proposez-vous une procédure de suppression des données ? | Sans objet |

> Justification : progression, statistiques et préférences restent en localStorage sur
> l'appareil ; le micro n'est utilisé que ponctuellement par le service de reconnaissance
> du système, sans que l'app conserve ni transmette d'enregistrement.

### Classification du contenu (questionnaire IARC)
| Question | Réponse |
|---|---|
| Catégorie | **Application → Référence, actualités ou éducation** |
| Violence, sexualité, langage grossier, drogues, jeux d'argent | **Non** à tout |
| Contenu généré par les utilisateurs | **Non** |
| Partage de position / infos personnelles | **Non** |
| Achats intégrés | **Oui** (le déblocage premium) |

→ Classification attendue : PEGI 3 / Tout public.

### Public cible
- Tranches d'âge : **13-15, 16-17, 18 et plus** (éviter « moins de 13 ans » qui déclenche
  les règles « Familles » et des obligations supplémentaires)
- L'application attire-t-elle les enfants ? **Non**

### Autres déclarations
| Question | Réponse |
|---|---|
| App gouvernementale | Non |
| Fonctionnalités financières | Non |
| Santé | Non |
| COVID-19 / traçage | Non |
| Application d'IA générative | Non |

---

## 4. Produit intégré (Monétiser → Produits → Produits intégrés)

**Créer un produit** :

| Champ | Valeur |
|---|---|
| ID du produit | `premium_unlock` ⚠️ **exactement** cet identifiant, non modifiable ensuite |
| Nom | Premium — Tout débloquer |
| Description | Accès à vie à tous les thèmes, quêtes, leçons et exercices. |
| Prix | 5,99 € (Google convertit automatiquement dans les autres devises) |
| État | **Actif** |

L'app lit le prix affiché depuis Google Play : tant que ce produit n'existe pas, le bouton
d'achat s'affiche sans montant.

---

## 5. Version de production

**Production → Créer une version**

1. **Signature d'application Play** : accepter (Google gère la clé de diffusion,
   `kosakata.keystore` devient la clé d'importation)
2. Importer le fichier **app-release.aab**
   (Actions GitHub → dernier build → artefact `Kosakata-release-aab`)
3. Nom de la version : la date du build (ex. `2026.07.28`)
4. Notes de version :
   ```
   <fr-FR>
   Première version : 2 000 mots, mode Quest, leçons de grammaire, audio hors ligne.
   </fr-FR>
   <en-US>
   First release: 2,000 words, Quest mode, grammar lessons, offline audio.
   </en-US>
   ```
5. **Pays de diffusion** : tous (ou au minimum France, Belgique, Suisse, Canada, Indonésie)

---

## 6. Ordre de traitement recommandé

1. Créer l'app
2. Contenu de l'application (toutes les sections en vert)
3. Fiche du Store + visuels
4. Produit `premium_unlock`
5. Importer l'AAB en production
6. **Envoyer pour examen** → 1 à 7 jours

---

## Erreurs fréquentes à éviter

- Oublier une section de « Contenu de l'application » — le bouton d'envoi reste grisé
- Créer l'app en « Payante » au lieu de « Gratuite »
- Un ID de produit différent de `premium_unlock` → l'achat échouera
- Importer l'APK au lieu de l'AAB
- Cocher « moins de 13 ans » dans le public cible

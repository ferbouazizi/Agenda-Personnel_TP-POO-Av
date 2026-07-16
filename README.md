# Agenda Personnel : Application de Gestion d'un Agenda

## Aperçu du projet

**Agenda Personnel** est une application desktop développée en **Java 17 / JavaFX** et connectée à une base de données **Oracle Database XE**. Réalisée dans le cadre du module **POO Avancée**, elle permet à un utilisateur de gérer efficacement son emploi du temps grâce à la création d'événements, au suivi des tâches personnelles, à la recherche multicritères ainsi qu'à une visualisation de type **Kanban** pour le suivi de l'avancement des tâches.

L'application met en œuvre une architecture en couches (**Modèle – DAO – Contrôleur – Vue**) afin de séparer clairement la logique métier, l'accès aux données et l'interface utilisateur.

---

# Fonctionnalités clés

## 1. Authentification
- Inscription et connexion des utilisateurs
- Authentification sécurisée
- Mots de passe hachés en **SHA-256** avant stockage

## 2. Gestion des événements
- CRUD complet des événements
- Gestion du titre, de la date, de l'heure et de la description
- Association à une catégorie personnalisée
- Mise en évidence des événements importants

## 3. Gestion des tâches
- CRUD complet des tâches personnelles
- Priorisation des tâches
- Organisation sous forme de tableau **Kanban**
- Changement de statut :
  - À faire
  - En cours
  - Terminé

## 4. Recherche multicritères
- Recherche par titre
- Recherche par date
- Recherche par catégorie
- Filtrage des événements à venir

## 5. Tableau de bord
- Vue d'ensemble des événements
- Vue d'ensemble des tâches
- Navigation rapide entre les différentes fonctionnalités

---

# Structure du projet

```text
agenda-personnel/
├── config/
│   ├── db.properties.example      # Modèle de configuration
│   └── db.properties              # Configuration locale (ignorée par Git)
│
├── src/
│   ├── modele/                    # Classes métier
│   │   ├── Evenement
│   │   ├── Tache
│   │   ├── Utilisateur
│   │   └── Categorie
│   │
│   ├── dao/                       # Accès aux données Oracle (JDBC)
│   ├── controleur/                # Contrôleurs JavaFX
│   ├── vue/                       # Interfaces JavaFX
│   └── agendatp/                  # Classe Main
│
├── agenda_personnel.sql           # Création de la base et données de démonstration
├── run.sh
├── run.bat
├── .gitignore
└── README.md
```

---

# Technologies utilisées

| Composant | Technologie |
|-----------|-------------|
| **Langage** | Java 17 |
| **Interface** | JavaFX |
| **Base de données** | Oracle Database XE |
| **Accès aux données** | JDBC (`ojdbc8`) |
| **Sécurité** | SHA-256 |
| **IDE recommandé** | VS Code / NetBeans |

---

# Schéma de la base de données

## Tables principales

### Utilisateur
Contient les comptes des utilisateurs de l'application.

### Catégorie
Stocke les catégories d'événements avec :
- Libellé
- Couleur
- Icône

### Événement
Représente les événements créés par les utilisateurs.

Chaque événement contient :
- titre
- description
- date
- heure
- catégorie
- importance

### Tâche
Contient les tâches personnelles avec :
- titre
- description
- priorité
- statut
- événement associé (optionnel)

---

# Installation et lancement

## Prérequis

- JDK **17** ou supérieur
- JavaFX SDK **17+**
- Oracle Database XE
- Docker
- VS Code avec **Extension Pack for Java**

---

## 1. Cloner le projet

```bash
git clone https://github.com/<votre-utilisateur>/agenda-personnel.git

cd agenda-personnel
```

---

## 2. Créer la base de données

Copier le script SQL dans le conteneur Oracle :

```bash
docker cp agenda_personnel.sql <nom_du_conteneur>:/tmp/
```

Puis exécuter :

```bash
docker exec -it <nom_du_conteneur> \
sqlplus system/VotreMotDePasse@localhost:1521/XE \
@/tmp/agenda_personnel.sql
```

Le script crée automatiquement :

- les tables
- les clés étrangères
- les séquences
- les triggers
- les catégories de démonstration

---

## 3. Configurer la connexion Oracle

Copiez le fichier :

```bash
cp config/db.properties.example config/db.properties
```

Puis renseignez vos informations :

```properties
db.url=jdbc:oracle:thin:@localhost:1521:XE
db.user=system
db.password=VotreMotDePasse
```

Le fichier **db.properties** est ignoré par Git afin de protéger les identifiants de connexion.

---

## 4. Configurer JavaFX

Créer la variable d'environnement :

### Windows

```powershell
[System.Environment]::SetEnvironmentVariable(
"PATH_TO_FX",
"C:\chemin\vers\javafx-sdk-17\lib",
"User")
```

### Linux / macOS

```bash
export PATH_TO_FX=/chemin/vers/javafx-sdk-17/lib
```

Redémarrer complètement VS Code après cette configuration.

---

## 5. Compiler et lancer

Depuis VS Code :

- Ouvrir le projet
- Attendre l'indexation Java
- Appuyer sur **F5**

Ou utiliser :

Linux / macOS

```bash
./run.sh
```

Windows

```bat
run.bat
```

---

# Décisions d'architecture

## Architecture MVC

L'application est organisée selon quatre couches :

- **Modèle** : représentation des données
- **DAO** : accès à la base Oracle via JDBC
- **Contrôleur** : logique métier
- **Vue** : interfaces JavaFX

Cette séparation améliore :

- la maintenance
- la lisibilité
- la réutilisabilité du code

---

## Accès sécurisé aux données

Toutes les requêtes SQL utilisent des **PreparedStatement** afin de prévenir les injections SQL.

---

## Connexion centralisée

La classe **ConnexionDB** gère une connexion unique vers Oracle et centralise son ouverture et sa fermeture.

---

## Sécurité

- Aucun mot de passe Oracle n'est codé en dur.
- Les identifiants sont externalisés dans **db.properties**.
- Les mots de passe utilisateurs sont hachés avec **SHA-256**.

---

# Correctifs apportés

Ce dépôt a été audité et amélioré après le développement initial.

### Externalisation des identifiants Oracle

Les identifiants de connexion présents dans **ConnexionDB.java** ont été déplacés vers :

```
config/db.properties
```

Un modèle :

```
config/db.properties.example
```

est fourni pour faciliter la configuration.

---

### Ajout du fichier .gitignore

Les fichiers suivants sont désormais exclus du dépôt :

- out/
- .vscode/
- fichiers temporaires
- desktop.ini

---

### Correction des caractères accentués

Les catégories de démonstration provoquaient un mauvais affichage sous Windows.

Les libellés ont été adaptés afin d'éviter toute corruption des caractères.

---

### Correction du script de lancement

Le chemin JavaFX présent dans **run.bat** était codé en dur.

Il est désormais configurable via la variable d'environnement :

```
PATH_TO_FX
```

---

# Exigences du TP couvertes

- [x] Interface graphique JavaFX
- [x] Authentification utilisateur
- [x] Gestion des événements
- [x] Gestion des catégories
- [x] Gestion des tâches
- [x] Tableau Kanban
- [x] Recherche multicritères
- [x] Mise en évidence des rendez-vous importants
- [x] Base de données Oracle
- [x] Architecture orientée objet
- [x] JDBC
- [x] Sécurité par hachage SHA-256

---

# Notes

- Les mots de passe ne sont jamais stockés en clair.
- Des catégories de démonstration sont automatiquement créées lors de l'exécution du script SQL.
- Oracle Database XE est exécutée via Docker et n'est pas incluse dans le dépôt.
- Le projet est compatible avec **Java 17+**.

---

# Contexte académique

Projet réalisé dans le cadre du module **POO Avancée** à l'**Institut Supérieur de Gestion de Tunis (ISG Tunis)**.

## Membres du groupe

- Feriel Bouazizi
- Yosr Ferjani

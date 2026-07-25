# Agenda Personnel : Application de Gestion d'un Agenda

## Aperçu du projet

Agenda Personnel est une application desktop développée en **Java 17 / JavaFX** et connectée à une base de données **Oracle Database XE**. Réalisée dans le cadre du module **POO Avancée**, elle permet à un utilisateur de gérer efficacement son emploi du temps grâce à la création d'événements, au suivi des tâches personnelles, à la recherche multicritères ainsi qu'à une visualisation de type Kanban pour le suivi de l'avancement des tâches.

L'application adopte une architecture en couches (**Modèle – DAO – Contrôleur – Vue**) afin de séparer clairement la logique métier, l'accès aux données et l'interface utilisateur.

---

# Fonctionnalités clés

## 1. Authentification

- Inscription et connexion des utilisateurs
- Authentification sécurisée
- Mots de passe hachés avec **SHA-256** avant stockage

---

## 2. Gestion des événements

- CRUD complet des événements
- Gestion du titre, de la date, de l'heure et de la description
- Association à une catégorie personnalisée
- Mise en évidence des événements importants

---

## 3. Gestion des tâches

- CRUD complet des tâches personnelles
- Gestion des priorités
- Organisation sous forme de tableau Kanban
- Gestion des différents statuts :
  - À faire
  - En cours
  - Terminé

---

## 4. Recherche multicritères

- Recherche par titre
- Recherche par date
- Recherche par catégorie
- Filtrage des événements à venir

---

## 5. Tableau de bord

- Vue d'ensemble des événements
- Vue d'ensemble des tâches
- Navigation rapide entre les différentes fonctionnalités

---

# Structure du projet

```
agenda-personnel/
│
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
|---|---|
| Langage | Java 17 |
| Interface graphique | JavaFX |
| Base de données | Oracle Database XE |
| Accès aux données | JDBC (ojdbc8) |
| Sécurité | SHA-256 |
| IDE recommandé | VS Code / NetBeans |

---

# Schéma de la base de données

## Tables principales

### Utilisateur

Contient les comptes des utilisateurs de l'application.

---

### Catégorie

Stocke les catégories d'événements avec :

- Libellé
- Couleur
- Icône

---

### Événement

Représente les événements créés par les utilisateurs.

Chaque événement contient :

- titre
- description
- date
- heure
- catégorie
- importance

---

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

- JDK 17 ou supérieur
- JavaFX SDK 17+
- Oracle Database XE
- Docker
- VS Code avec Extension Pack for Java

---

# 1. Cloner le projet

```bash
git clone https://github.com/<votre-utilisateur>/agenda-personnel.git

cd agenda-personnel
```

---

# 2. Créer la base de données

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

# 3. Configurer la connexion Oracle

Copier le fichier de configuration :

```bash
cp config/db.properties.example config/db.properties
```

Puis renseigner les informations :

```properties
db.url=jdbc:oracle:thin:@localhost:1521:XE
db.user=system
db.password=VotreMotDePasse
```

Le fichier `db.properties` contient les informations sensibles de connexion et n'est pas versionné dans Git.

---

# 4. Configurer JavaFX

Créer la variable d'environnement :

## Windows

```powershell
[System.Environment]::SetEnvironmentVariable(
"PATH_TO_FX",
"C:\chemin\vers\javafx-sdk-17\lib",
"User")
```

## Linux / macOS

```bash
export PATH_TO_FX=/chemin/vers/javafx-sdk-17/lib
```

Redémarrer VS Code après cette configuration.

---

# 5. Compiler et lancer

Depuis VS Code :

1. Ouvrir le projet
2. Attendre l'indexation Java
3. Appuyer sur **F5**

Ou utiliser :

## Linux / macOS

```bash
./run.sh
```

## Windows

```bash
run.bat
```

---

# Décisions d'architecture

## Architecture MVC

L'application est organisée selon quatre couches :

### Modèle
Représentation des données métier.

### DAO
Gestion de l'accès à la base Oracle via JDBC.

### Contrôleur
Gestion de la logique applicative.

### Vue
Interfaces graphiques développées avec JavaFX.

Cette séparation améliore :

- la maintenance
- la lisibilité
- la réutilisation du code

---

## Accès sécurisé aux données

Toutes les requêtes SQL utilisent des **PreparedStatement** afin de prévenir les injections SQL.

---

## Gestion centralisée des connexions

La classe `ConnexionDB` assure la gestion centralisée des connexions avec Oracle Database.

---

# Sécurité

- Aucun mot de passe Oracle n'est codé en dur dans le projet.
- Les informations de connexion sont externalisées dans `db.properties`.
- Les mots de passe utilisateurs sont hachés avec SHA-256.
- Aucun mot de passe utilisateur n'est stocké en clair.

---

# Gestion du dépôt Git

Le fichier `.gitignore` permet d'exclure les fichiers locaux et temporaires :

```
out/
.vscode/
fichiers temporaires
desktop.ini
config/db.properties
```

---

# Configuration JavaFX

L'application utilise la variable d'environnement :

```
PATH_TO_FX
```

afin de définir l'emplacement du SDK JavaFX lors de l'exécution.

---

# Exigences du TP couvertes

✅ Interface graphique JavaFX  
✅ Authentification utilisateur  
✅ Gestion des événements  
✅ Gestion des catégories  
✅ Gestion des tâches  
✅ Tableau Kanban  
✅ Recherche multicritères  
✅ Mise en évidence des rendez-vous importants  
✅ Base de données Oracle  
✅ Architecture orientée objet  
✅ JDBC  
✅ Sécurité par hachage SHA-256  

---

# Notes

- Les mots de passe utilisateurs ne sont jamais stockés en clair.
- Des catégories de démonstration sont créées automatiquement lors de l'exécution du script SQL.
- Oracle Database XE est exécutée via Docker et n'est pas incluse dans le dépôt.
- Le projet est compatible avec Java 17 et versions supérieures.

---

# Contexte académique

Projet réalisé dans le cadre du module **POO Avancée** à l'**Institut Supérieur de Gestion de Tunis (ISG Tunis)**.

---

# Membres du groupe

- Feriel Bouazizi
- Yosr Ferjani

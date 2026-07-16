# 📅 Agenda Personnel

Application de bureau développée en **Java / JavaFX**, permettant de gérer un agenda personnel : création, recherche et suivi d'événements, tâches et catégories, avec authentification utilisateur et stockage dans une base **Oracle**.

Projet réalisé dans le cadre du module **POO Avancée** — Institut Supérieur de Gestion de Tunis (ISG Tunis), 2ème année LN-BIS.

## 👥 Auteurs
- **Feriel**
- **Yosr**

## ✨ Fonctionnalités
- Authentification (inscription / connexion) avec mots de passe hashés (SHA-256)
- Gestion des événements (titre, date, heure, description, catégorie)
- Gestion des tâches personnelles
- Recherche par date, titre ou catégorie
- Mise en évidence des rendez-vous importants
- Interface graphique JavaFX (Dashboard, formulaires, vues dédiées)
- Persistance des données dans une base Oracle relationnelle

## 🛠️ Stack technique
- **Java 17+** (JavaFX 17+ pour l'interface graphique)
- **Oracle Database** (via JDBC, driver `ojdbc8`)
- Architecture en couches : `modele` / `dao` / `controleur` / `vue`

## 📋 Prérequis
- JDK 17 ou supérieur
- JavaFX SDK 17+ ([télécharger ici](https://gluonhq.com/products/javafx/))
- Oracle Database (XE ou via Docker) accessible sur `localhost:1521`
- VS Code avec l'**Extension Pack for Java** (Microsoft)

## 🚀 Installation

### 1. Cloner le projet
```bash
git clone https://github.com/<votre-utilisateur>/agenda-personnel.git
cd agenda-personnel
```

### 2. Configurer la base de données
Exécutez le script `agenda_personnel.sql` dans votre base Oracle (SQL Developer, SQL*Plus, ou tout client SQL connecté à votre conteneur Docker).

Puis créez votre fichier de configuration local à partir du modèle fourni :
```bash
cp config/db.properties.example config/db.properties
```
Ouvrez `config/db.properties` et renseignez vos identifiants :
```properties
db.url=jdbc:oracle:thin:@localhost:1521:XE
db.user=system
db.password=VotreMotDePasse
```
> ⚠️ `config/db.properties` est ignoré par git (voir `.gitignore`) : vos identifiants ne seront **jamais** publiés sur GitHub. Ne modifiez que `db.properties`, jamais le `.example`.

### 3. Configurer JavaFX
Définir la variable d'environnement `PATH_TO_FX` :

**Windows (PowerShell)**
```powershell
[System.Environment]::SetEnvironmentVariable("PATH_TO_FX", "C:\chemin\vers\javafx-sdk-17\lib", "User")
```

**macOS / Linux** (`~/.bashrc` ou `~/.zshrc`)
```bash
export PATH_TO_FX=/chemin/vers/javafx-sdk-17/lib
```

### 4. Lancer l'application

**Depuis VS Code**
1. Ouvrir le dossier du projet
2. Attendre l'indexation par l'extension Java
3. Redémarrer VS Code après avoir défini `PATH_TO_FX`
4. Appuyer sur **F5** (ou Run → Run Agenda Personnel)

**Depuis le terminal**
```bash
./run.sh      # Linux / macOS
run.bat       # Windows
```

## 📁 Structure du projet
```
agenda-personnel/
├── config/
│   ├── db.properties.example   # modèle de configuration (versionné)
│   └── db.properties           # vos identifiants réels (ignoré par git)
├── src/
│   ├── modele/                 # classes métier (Evenement, Tache, Utilisateur, Categorie)
│   ├── dao/                    # accès aux données (JDBC / Oracle)
│   ├── controleur/             # logique de contrôle
│   ├── vue/                    # interfaces JavaFX
│   └── agendatp/               # point d'entrée (main)
├── agenda_personnel.sql        # script de création de la base
├── run.sh / run.bat            # scripts de lancement
└── README.md
```

## 🔒 Sécurité
Les identifiants de connexion à la base de données ne sont pas codés en dur dans le code source : ils sont chargés dynamiquement depuis `config/db.properties`, un fichier exclu du dépôt Git. Les mots de passe utilisateurs de l'application sont stockés sous forme de hash (SHA-256), jamais en clair.

## 📄 Licence
Projet académique — usage éducatif dans le cadre du module POO Avancée, ISG Tunis.

Agenda Personnel : Application de Gestion d'un Agenda

Apercu du projet
Agenda Personnel est une application desktop en Java / JavaFX, connectee a une base de donnees Oracle, developpee dans le cadre d'un projet academique du module POO Avancee. Elle permet a un utilisateur de gerer ses evenements et ses taches au quotidien : creation, recherche multicriteres, categorisation, mise en evidence des rendez-vous importants et suivi de l'avancement des taches sous forme de tableau Kanban.

Fonctionnalites cles
1. Authentification
Inscription et connexion des utilisateurs
Mots de passe haches en SHA-256 avant stockage

2. Gestion des evenements
CRUD complet sur les evenements (titre, date, heure, description, categorie)
Association a une categorie (couleur, icone)
Mise en evidence des evenements marques comme importants

3. Gestion des taches
CRUD complet sur les taches personnelles
Vue Kanban par statut (a faire / en cours / termine)
Priorisation des taches

4. Recherche
Recherche multicriteres : par date, par titre, par categorie
Filtrage des rendez-vous a venir

5. Tableau de bord
Vue d'ensemble des evenements et taches
Acces rapide aux differentes sections de l'application

Structure du projet
agenda-personnel/
├── config/
│   ├── db.properties.example   # modele de configuration (verse au depot)
│   └── db.properties           # identifiants reels (ignore par git)
├── src/
│   ├── modele/                 # classes metier (Evenement, Tache, Utilisateur, Categorie)
│   ├── dao/                    # acces aux donnees (JDBC / Oracle)
│   ├── controleur/             # logique de controle
│   ├── vue/                    # interfaces JavaFX
│   └── agendatp/               # point d'entree (main)
├── agenda_personnel.sql        # script de creation de la base + donnees de demo
├── run.sh / run.bat            # scripts de lancement (hors IDE)
├── .gitignore
└── README.md

Technologies utilisees
Composant	Technologie
Langage	Java (17+)
Interface	JavaFX
Base de donnees	Oracle Database (XE), via Docker
Acces aux donnees	JDBC (ojdbc8), SQL parametre (PreparedStatement)
Securite	Hachage des mots de passe en SHA-256, identifiants externalises

Schema de la base de donnees
Tables principales :

Utilisateur : comptes utilisateurs de l'application
Categorie : categories d'evenements (libelle, couleur, icone)
Evenement : evenements planifies, rattaches a une categorie et un utilisateur
Tache : taches personnelles, rattachees a un statut, une priorite et eventuellement un evenement

Installation et lancement
Prerequis
JDK 17 ou superieur
JavaFX SDK 17+ (https://gluonhq.com/products/javafx/)
Docker, avec une image Oracle Database Express (XE) en cours d'execution sur le port 1521
VS Code avec l'Extension Pack for Java

Etapes
Cloner le depot :
git clone https://github.com/<votre-utilisateur>/agenda-personnel.git
cd agenda-personnel

Creer la base de donnees :
Copier le script dans le conteneur Docker puis l'executer avec sqlplus :
docker cp agenda_personnel.sql <nom_conteneur>:/tmp/agenda_personnel.sql
docker exec -it <nom_conteneur> sqlplus system/VotreMotDePasse@localhost:1521/XE "@/tmp/agenda_personnel.sql"
Cela cree les tables, les sequences, les triggers et insere les categories de demonstration.

Configurer les identifiants :
cp config/db.properties.example config/db.properties
Renseigner dans config/db.properties l'URL, l'utilisateur et le mot de passe de votre instance Oracle :
db.url=jdbc:oracle:thin:@localhost:1521:XE
db.user=system
db.password=VotreMotDePasse
Ce fichier est ignore par git : vos identifiants ne sont jamais publies sur GitHub. Ne modifier que db.properties, jamais le .example.

Configurer JavaFX :
Definir la variable d'environnement PATH_TO_FX vers le dossier lib du SDK JavaFX telecharge.

Windows (PowerShell) :
[System.Environment]::SetEnvironmentVariable("PATH_TO_FX", "C:\chemin\vers\javafx-sdk-17\lib", "User")

macOS / Linux (~/.bashrc ou ~/.zshrc) :
export PATH_TO_FX=/chemin/vers/javafx-sdk-17/lib

Redemarrer completement VS Code apres avoir defini cette variable (un simple rechargement de fenetre ne suffit pas).

Compiler et lancer :
Depuis VS Code : ouvrir le dossier du projet, attendre l'indexation par l'extension Java, puis lancer avec F5.
Depuis un terminal :
./run.sh      (Linux / macOS)
run.bat       (Windows)

Decisions d'architecture
Separation en couches : modele, dao, controleur et vue sont clairement separes, chaque DAO encapsulant l'acces JDBC pour une seule entite.
Requetes parametrees : toutes les requetes SQL utilisent des PreparedStatement pour eviter les injections SQL.
Connexion en singleton : ConnexionDB centralise l'ouverture et la fermeture de la connexion Oracle.
Identifiants externalises : aucun mot de passe n'est code en dur dans le code source ; ils sont lus depuis un fichier de configuration ignore par git.

Correctifs apportes
Ce depot a ete audite et corrige apres le developpement initial :

Mot de passe Oracle code en dur : ConnexionDB.java contenait les identifiants de connexion directement dans le code source. Ils ont ete externalises vers config/db.properties (ignore par git), avec un modele config/db.properties.example verse au depot.

Fichiers non lies au projet presents dans l'archive : desktop.ini (metadonnee Windows Explorer) a ete supprime, et un .gitignore a ete ajoute pour exclure les artefacts de compilation (out/) et les fichiers d'environnement (.vscode/).

Corruption des caracteres accentues : les categories de demonstration Reunion et RDV Medical etaient inserees avec des accents dans agenda_personnel.sql, ce qui provoquait leur corruption (affichage sous forme de points d'interrogation) lors de l'insertion via JDBC sous Windows. Les libelles ont ete reecrits sans accents dans le script pour eviter le probleme a la source.

Chemin JavaFX code en dur dans run.bat : le script pointait vers un chemin fixe (C:\Program Files\javafx-sdk-21\lib) qui ne correspond pas necessairement a l'emplacement reel du SDK sur chaque machine. A ajuster manuellement selon l'installation locale.

Exigences du TP couvertes
Interface graphique Java (JavaFX)
Gestion des evenements (titre, date, heure, description, type/categorie)
Recherche selon plusieurs criteres (date, titre, categorie)
Mise en evidence des rendez-vous importants
Stockage dans une base de donnees relationnelle (Oracle)
Fonctionnalite supplementaire : gestion des taches avec vue Kanban

Notes
Les mots de passe utilisateurs sont haches en SHA-256 avant stockage ; aucun mot de passe n'est jamais stocke en clair.
Des categories de demonstration sont inserees automatiquement par agenda_personnel.sql.
La base de donnees est executee via Docker (image Oracle Database Express) et n'est pas incluse dans le depot.

Contexte academique
Projet realise dans le cadre du module POO Avancee - Institut Superieur de Gestion de Tunis (ISG Tunis), 2eme annee LN-BIS.

Membres du groupe :

Feriel Bouazizi
Yosr Ferjani
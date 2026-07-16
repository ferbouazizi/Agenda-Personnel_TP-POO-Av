package vue;

import dao.EvenementDAO;
import dao.TacheDAO;
import modele.Evenement;
import modele.Tache;
import modele.Utilisateur;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Écran principal (Dashboard) — construit en Java pur.
 * Affiche : barre de navigation, événements du jour, tâches urgentes.
 */
public class DashboardVue {

    private EvenementDAO evenementDAO = new EvenementDAO();
    private TacheDAO     tacheDAO     = new TacheDAO();
    private Utilisateur  utilisateur;
    private Stage        stage;

    public void afficher(Stage stage, Utilisateur utilisateur) {
        this.stage       = stage;
        this.utilisateur = utilisateur;

        // ── Barre de navigation ──
        HBox navbar = creerNavbar();

        // ── Contenu principal ──
        HBox contenu = new HBox(16);
        contenu.setPadding(new Insets(20));
        contenu.setStyle("-fx-background-color: #f8fafc;");

        VBox colGauche = new VBox(16);
        HBox.setHgrow(colGauche, Priority.ALWAYS);

        VBox colDroite = new VBox(16);
        colDroite.setPrefWidth(280);

        // Carte bienvenue
        colGauche.getChildren().add(carteBienvenue());

        // Événements du jour
        colGauche.getChildren().add(carteEvenementsDuJour());

        // Événements importants à venir
        colGauche.getChildren().add(carteEvenementsImportants());

        // Tâches urgentes (en retard)
        colDroite.getChildren().add(carteTachesEnRetard());

        // Actions rapides
        colDroite.getChildren().add(carteActionsRapides());

        contenu.getChildren().addAll(colGauche, colDroite);

        // ── Layout principal ──
        VBox root = new VBox(navbar, contenu);
        VBox.setVgrow(contenu, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc;");

        Scene scene = new Scene(scroll, 960, 650);
        stage.setTitle("Agenda Personnel — Dashboard");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    // ── Barre de navigation ───────────────────────────────────

    private HBox creerNavbar() {
        HBox nav = new HBox();
        nav.setPadding(new Insets(0, 20, 0, 20));
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPrefHeight(56);
        nav.setStyle("-fx-background-color: #1e40af;");

        Label logo = new Label("📅 Agenda Personnel");
        logo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Boutons de navigation
        Button btnEvenements = navBtn("📋 Événements");
        Button btnTaches     = navBtn("✅ Tâches");
        Button btnDeconnexion = new Button("Déconnexion");
        btnDeconnexion.setStyle("-fx-background-color: transparent; -fx-text-fill: #fca5a5; "
                + "-fx-font-size: 13px; -fx-cursor: hand; -fx-border-color: #fca5a5; "
                + "-fx-border-radius: 4; -fx-padding: 4 10;");

        btnEvenements.setOnAction(e -> new EvenementsVue().afficher(stage, utilisateur));
        btnTaches.setOnAction(e -> new TachesVue().afficher(stage, utilisateur));
        btnDeconnexion.setOnAction(e -> new LoginVue().afficher(stage));

        nav.getChildren().addAll(logo, spacer, btnEvenements, btnTaches, btnDeconnexion);
        HBox.setMargin(btnEvenements, new Insets(0, 8, 0, 0));
        HBox.setMargin(btnTaches, new Insets(0, 16, 0, 0));

        return nav;
    }

    private Button navBtn(String texte) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; "
                + "-fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 4 10;");
        return btn;
    }

    // ── Carte bienvenue ───────────────────────────────────────

    private VBox carteBienvenue() {
        VBox carte = carte();
        carte.setStyle(carte.getStyle() + "-fx-background-color: #1e40af;");

        String date = new SimpleDateFormat("EEEE dd MMMM yyyy",
                new java.util.Locale("fr")).format(new java.util.Date());

        Label bonjour = new Label("Bonjour, " + utilisateur.getPrenom() + " 👋");
        bonjour.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #93c5fd;");

        carte.getChildren().addAll(bonjour, dateLabel);
        return carte;
    }

    // ── Carte événements du jour ──────────────────────────────

    private VBox carteEvenementsDuJour() {
        VBox carte = carte();
        List<Evenement> liste = evenementDAO.evenementsDuJour(utilisateur.getIdUtil());

        Label titre = titreSection("📅 Événements aujourd'hui (" + liste.size() + ")");
        carte.getChildren().add(titre);

        if (liste.isEmpty()) {
            carte.getChildren().add(labelVide("Aucun événement aujourd'hui."));
        } else {
            for (Evenement e : liste) {
                carte.getChildren().add(ligneEvenement(e));
            }
        }
        return carte;
    }

    // ── Carte événements importants ───────────────────────────

    private VBox carteEvenementsImportants() {
        VBox carte = carte();
        List<Evenement> liste = evenementDAO.evenementsImportantsAVenir(utilisateur.getIdUtil());

        Label titre = titreSection("⭐ Événements importants (7 prochains jours)");
        carte.getChildren().add(titre);

        if (liste.isEmpty()) {
            carte.getChildren().add(labelVide("Aucun événement important à venir."));
        } else {
            for (Evenement e : liste) {
                carte.getChildren().add(ligneEvenement(e));
            }
        }
        return carte;
    }

    // ── Carte tâches en retard ────────────────────────────────

    private VBox carteTachesEnRetard() {
        VBox carte = carte();
        List<Tache> liste = tacheDAO.tachesEnRetard(utilisateur.getIdUtil());

        Label titre = titreSection("🔴 Tâches en retard (" + liste.size() + ")");
        carte.getChildren().add(titre);

        if (liste.isEmpty()) {
            carte.getChildren().add(labelVide("Aucune tâche en retard."));
        } else {
            for (Tache t : liste) {
                carte.getChildren().add(ligneTache(t));
            }
        }
        return carte;
    }

    // ── Carte actions rapides ─────────────────────────────────

    private VBox carteActionsRapides() {
        VBox carte = carte();
        carte.getChildren().add(titreSection("⚡ Actions rapides"));

        Button btnAjouterEv = actionBtn("➕ Nouvel événement", "#1e40af");
        Button btnAjouterTache = actionBtn("➕ Nouvelle tâche", "#065f46");

        btnAjouterEv.setOnAction(e -> {
            new FormulaireEvenementVue().afficher(null, utilisateur, () ->
                new DashboardVue().afficher(stage, utilisateur));
        });
        btnAjouterTache.setOnAction(e ->
            new TachesVue().afficher(stage, utilisateur));

        carte.getChildren().addAll(btnAjouterEv, btnAjouterTache);
        return carte;
    }

    // ── Helpers UI ────────────────────────────────────────────

    /** Crée un conteneur carte blanc avec ombre simulée */
    private VBox carte() {
        VBox v = new VBox(10);
        v.setPadding(new Insets(16));
        v.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-border-color: #e5e7eb; -fx-border-radius: 8;");
        return v;
    }

    private Label titreSection(String texte) {
        Label l = new Label(texte);
        l.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        return l;
    }

    private Label labelVide(String texte) {
        Label l = new Label(texte);
        l.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px; -fx-font-style: italic;");
        return l;
    }

    private HBox ligneEvenement(Evenement e) {
        HBox ligne = new HBox(10);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setPadding(new Insets(6, 10, 6, 10));
        ligne.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 6;");

        String couleur = (e.getCategorie() != null) ? e.getCategorie().getCouleur() : "#6b7280";
        Label point = new Label("●");
        point.setStyle("-fx-text-fill: " + couleur + "; -fx-font-size: 14px;");

        Label info = new Label((e.isImportant() ? "⭐ " : "") + e.getTitre()
                + (e.getHeure() != null ? "  " + e.getHeure() : ""));
        info.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        ligne.getChildren().addAll(point, info);
        return ligne;
    }

    private HBox ligneTache(Tache t) {
        HBox ligne = new HBox(10);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setPadding(new Insets(6, 10, 6, 10));
        ligne.setStyle("-fx-background-color: #fef2f2; -fx-background-radius: 6;");

        String couleurPrio = t.getPriorite().equals("HAUTE") ? "#ef4444"
                : t.getPriorite().equals("MOYENNE") ? "#f59e0b" : "#10b981";
        Label prio = new Label(t.getPriorite());
        prio.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-background-color: "
                + couleurPrio + "; -fx-padding: 2 6; -fx-background-radius: 4;");

        Label info = new Label(t.getTitre());
        info.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

        ligne.getChildren().addAll(prio, info);
        return ligne;
    }

    private Button actionBtn(String texte, String couleur) {
        Button btn = new Button(texte);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setPrefHeight(36);
        btn.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; "
                + "-fx-font-size: 13px; -fx-background-radius: 6; -fx-cursor: hand;");
        return btn;
    }
}
package vue;

import dao.UtilisateurDAO;
import modele.Utilisateur;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginVue {

    private UtilisateurDAO utilisateurDAO;

    private TextField     fieldEmail  = new TextField();
    private PasswordField fieldMdp    = new PasswordField();
    private Label         labelErreur = new Label();
    private Stage         stage;

    public void afficher(Stage stage) {
        this.stage = stage;

        try {
            this.utilisateurDAO = new UtilisateurDAO();
        } catch (Exception e) {
            afficherErreur("Impossible de se connecter à la base de données : " + e.getMessage());
        }

        // ── En-tête ──
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(40, 0, 30, 0));
        header.setStyle("-fx-background-color: #1e40af;");

        Label titre = new Label("📅 Agenda Personnel");
        titre.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label sous = new Label("Connectez-vous pour accéder à votre agenda");
        sous.setStyle("-fx-font-size: 12px; -fx-text-fill: #93c5fd;");
        header.getChildren().addAll(titre, sous);

        // ── Formulaire ──
        VBox form = new VBox(14);
        form.setPadding(new Insets(30, 40, 30, 40));
        form.setStyle("-fx-background-color: white;");

        Label lblEmail = new Label("Adresse e-mail");
        lblEmail.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #374151;");
        fieldEmail.setPromptText("exemple@mail.com");
        fieldEmail.setPrefHeight(38);
        styleField(fieldEmail);

        Label lblMdp = new Label("Mot de passe");
        lblMdp.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #374151;");
        fieldMdp.setPromptText("••••••••");
        fieldMdp.setPrefHeight(38);
        styleField(fieldMdp);

        labelErreur.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; "
                + "-fx-background-color: #fef2f2; -fx-padding: 8; "
                + "-fx-background-radius: 6;");
        labelErreur.setWrapText(true);
        labelErreur.setVisible(false);
        labelErreur.setManaged(false);

        Button btnConnecter = new Button("Se connecter");
        btnConnecter.setPrefWidth(Double.MAX_VALUE);
        btnConnecter.setPrefHeight(40);
        btnConnecter.setStyle("-fx-background-color: #1e40af; -fx-text-fill: white; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-background-radius: 6; -fx-cursor: hand;");
        btnConnecter.setOnAction(e -> seConnecter());
        fieldMdp.setOnAction(e -> seConnecter());

        Hyperlink lienInscription = new Hyperlink("Pas encore de compte ? S'inscrire");
        lienInscription.setStyle("-fx-font-size: 12px; -fx-text-fill: #1e40af;");
        lienInscription.setOnAction(e -> ouvrirInscription());

        form.getChildren().addAll(
            lblEmail, fieldEmail,
            lblMdp, fieldMdp,
            labelErreur,
            btnConnecter,
            lienInscription
        );

        VBox root = new VBox(header, form);
        root.setStyle("-fx-background-color: #f8fafc;");

        Scene scene = new Scene(root, 420, 480);
        stage.setTitle("Agenda Personnel — Connexion");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void seConnecter() {
        // ── Validation des champs ──
        String email = fieldEmail.getText() == null ? "" : fieldEmail.getText().trim();
        String mdp   = fieldMdp.getText()   == null ? "" : fieldMdp.getText();

        if (email.isEmpty() && mdp.isEmpty()) {
            afficherErreur("Veuillez saisir votre email et votre mot de passe.");
            return;
        }
        if (email.isEmpty()) {
            afficherErreur("Veuillez saisir votre adresse e-mail.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            afficherErreur("L'adresse e-mail saisie n'est pas valide.");
            return;
        }
        if (mdp.isEmpty()) {
            afficherErreur("Veuillez saisir votre mot de passe.");
            return;
        }
        if (mdp.length() < 6) {
            afficherErreur("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        // ── Authentification ──
        try {
            if (utilisateurDAO == null) {
                afficherErreur("Connexion à la base de données indisponible.");
                return;
            }
            Utilisateur u = utilisateurDAO.authentifier(email, mdp);
            if (u != null) {
                new DashboardVue().afficher(stage, u);
            } else {
                afficherErreur("Email ou mot de passe incorrect.");
                fieldMdp.clear();
            }
        } catch (Exception e) {
            afficherErreur("Erreur lors de la connexion : " + e.getMessage());
        }
    }

    private void ouvrirInscription() {
        try {
            new InscriptionVue().afficher(stage);
        } catch (Exception e) {
            afficherErreur("Impossible d'ouvrir l'écran d'inscription : " + e.getMessage());
        }
    }

    private void afficherErreur(String msg) {
        labelErreur.setText("⚠ " + msg);
        labelErreur.setVisible(true);
        labelErreur.setManaged(true);
    }

    private void styleField(Control field) {
        field.setStyle("-fx-border-color: #d1d5db; -fx-border-radius: 6; "
                + "-fx-background-radius: 6; -fx-padding: 6 10; -fx-font-size: 13px;");
    }
}

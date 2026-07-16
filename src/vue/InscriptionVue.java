package vue;

import dao.UtilisateurDAO;
import modele.Utilisateur;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class InscriptionVue {

    private UtilisateurDAO dao;

    private TextField     fieldNom     = new TextField();
    private TextField     fieldPrenom  = new TextField();
    private TextField     fieldEmail   = new TextField();
    private PasswordField fieldMdp     = new PasswordField();
    private PasswordField fieldConfirm = new PasswordField();
    private Label         labelErreur  = new Label();
    private Label         labelSucces  = new Label();
    private Stage         stage;

    public void afficher(Stage stage) {
        this.stage = stage;

        try {
            this.dao = new UtilisateurDAO();
        } catch (Exception e) {
            afficherErreur("Impossible de se connecter à la base de données : " + e.getMessage());
        }

        // ── En-tête ──
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 0, 20, 0));
        header.setStyle("-fx-background-color: #1e40af;");
        Label titre = new Label("Créer un compte");
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label sous = new Label("Rejoignez Agenda Personnel");
        sous.setStyle("-fx-font-size: 12px; -fx-text-fill: #93c5fd;");
        header.getChildren().addAll(titre, sous);

        // ── Formulaire ──
        VBox form = new VBox(12);
        form.setPadding(new Insets(24, 40, 24, 40));
        form.setStyle("-fx-background-color: white;");

        HBox nomPrenom = new HBox(12);
        VBox boxNom    = champAvecLabel("Nom *",    fieldNom,    "Dupont");
        VBox boxPrenom = champAvecLabel("Prénom *", fieldPrenom, "Jean");
        HBox.setHgrow(boxNom,    Priority.ALWAYS);
        HBox.setHgrow(boxPrenom, Priority.ALWAYS);
        nomPrenom.getChildren().addAll(boxNom, boxPrenom);

        VBox boxEmail   = champAvecLabel("Email *",                   fieldEmail,   "exemple@mail.com");
        VBox boxMdp     = champAvecLabel("Mot de passe * (6 car. min)", fieldMdp,   "••••••••");
        VBox boxConfirm = champAvecLabel("Confirmer le mot de passe *", fieldConfirm,"••••••••");

        labelErreur.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; "
                + "-fx-background-color: #fef2f2; -fx-padding: 8; -fx-background-radius: 6;");
        labelErreur.setWrapText(true);
        labelErreur.setVisible(false);
        labelErreur.setManaged(false);

        labelSucces.setStyle("-fx-text-fill: #166534; -fx-font-size: 12px; "
                + "-fx-background-color: #f0fdf4; -fx-padding: 8; -fx-background-radius: 6;");
        labelSucces.setWrapText(true);
        labelSucces.setVisible(false);
        labelSucces.setManaged(false);

        Button btnInscrire = new Button("Créer mon compte");
        btnInscrire.setPrefWidth(Double.MAX_VALUE);
        btnInscrire.setPrefHeight(40);
        btnInscrire.setStyle("-fx-background-color: #1e40af; -fx-text-fill: white; "
                + "-fx-font-size: 14px; -fx-font-weight: bold; "
                + "-fx-background-radius: 6; -fx-cursor: hand;");
        btnInscrire.setOnAction(e -> inscrire());

        Hyperlink lienRetour = new Hyperlink("← Retour à la connexion");
        lienRetour.setStyle("-fx-font-size: 12px; -fx-text-fill: #1e40af;");
        lienRetour.setOnAction(e -> new LoginVue().afficher(stage));

        form.getChildren().addAll(
            nomPrenom, boxEmail, boxMdp, boxConfirm,
            labelErreur, labelSucces,
            btnInscrire, lienRetour
        );

        VBox root = new VBox(header, form);
        root.setStyle("-fx-background-color: #f8fafc;");
        Scene scene = new Scene(root, 460, 540);
        stage.setTitle("Agenda Personnel — Inscription");
        stage.setScene(scene);
        stage.show();
    }

    private void inscrire() {
        // ── Lecture des champs ──
        String nom     = fieldNom.getText()     == null ? "" : fieldNom.getText().trim();
        String prenom  = fieldPrenom.getText()  == null ? "" : fieldPrenom.getText().trim();
        String email   = fieldEmail.getText()   == null ? "" : fieldEmail.getText().trim();
        String mdp     = fieldMdp.getText()     == null ? "" : fieldMdp.getText();
        String confirm = fieldConfirm.getText() == null ? "" : fieldConfirm.getText();

        // ── Validations ──
        if (nom.isEmpty()) {
            afficherErreur("Le nom est obligatoire.");
            fieldNom.requestFocus();
            return;
        }
        if (nom.length() < 2 || nom.length() > 50) {
            afficherErreur("Le nom doit contenir entre 2 et 50 caractères.");
            fieldNom.requestFocus();
            return;
        }
        if (!nom.matches("[a-zA-ZÀ-ÿ\\s\\-']+")) {
            afficherErreur("Le nom ne doit contenir que des lettres, espaces ou tirets.");
            fieldNom.requestFocus();
            return;
        }

        if (prenom.isEmpty()) {
            afficherErreur("Le prénom est obligatoire.");
            fieldPrenom.requestFocus();
            return;
        }
        if (prenom.length() < 2 || prenom.length() > 50) {
            afficherErreur("Le prénom doit contenir entre 2 et 50 caractères.");
            fieldPrenom.requestFocus();
            return;
        }
        if (!prenom.matches("[a-zA-ZÀ-ÿ\\s\\-']+")) {
            afficherErreur("Le prénom ne doit contenir que des lettres, espaces ou tirets.");
            fieldPrenom.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            afficherErreur("L'adresse e-mail est obligatoire.");
            fieldEmail.requestFocus();
            return;
        }
        if (!email.matches("^[\\w.+\\-]+@[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,}$")) {
            afficherErreur("L'adresse e-mail saisie n'est pas valide (ex: jean@mail.com).");
            fieldEmail.requestFocus();
            return;
        }
        if (email.length() > 100) {
            afficherErreur("L'adresse e-mail ne peut pas dépasser 100 caractères.");
            fieldEmail.requestFocus();
            return;
        }

        if (mdp.isEmpty()) {
            afficherErreur("Le mot de passe est obligatoire.");
            fieldMdp.requestFocus();
            return;
        }
        if (mdp.length() < 6) {
            afficherErreur("Le mot de passe doit contenir au moins 6 caractères.");
            fieldMdp.requestFocus();
            return;
        }
        if (mdp.length() > 255) {
            afficherErreur("Le mot de passe est trop long (255 caractères maximum).");
            fieldMdp.requestFocus();
            return;
        }

        if (confirm.isEmpty()) {
            afficherErreur("Veuillez confirmer votre mot de passe.");
            fieldConfirm.requestFocus();
            return;
        }
        if (!mdp.equals(confirm)) {
            afficherErreur("Les mots de passe ne correspondent pas.");
            fieldMdp.clear();
            fieldConfirm.clear();
            fieldMdp.requestFocus();
            return;
        }

        // ── Vérification BDD ──
        try {
            if (dao == null) {
                afficherErreur("Connexion à la base de données indisponible.");
                return;
            }
            if (dao.trouverParEmail(email) != null) {
                afficherErreur("Cette adresse e-mail est déjà utilisée par un autre compte.");
                fieldEmail.requestFocus();
                return;
            }

            Utilisateur u = new Utilisateur(nom, prenom, email, mdp);
            boolean succes = dao.inserer(u);

            if (succes) {
                afficherSucces("✓ Compte créé avec succès ! Redirection vers la connexion...");
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    javafx.application.Platform.runLater(() -> new LoginVue().afficher(stage));
                }).start();
            } else {
                afficherErreur("Erreur lors de la création du compte. Veuillez réessayer.");
            }
        } catch (Exception e) {
            afficherErreur("Erreur inattendue : " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private VBox champAvecLabel(String libelle, Control champ, String prompt) {
        Label lbl = new Label(libelle);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #374151;");
        if (champ instanceof TextField) ((TextField) champ).setPromptText(prompt);
        champ.setStyle("-fx-border-color: #d1d5db; -fx-border-radius: 6; "
                + "-fx-background-radius: 6; -fx-padding: 6 10; -fx-font-size: 13px;");
        champ.setPrefHeight(36);
        return new VBox(4, lbl, champ);
    }

    private void afficherErreur(String msg) {
        labelErreur.setText("⚠ " + msg);
        labelErreur.setVisible(true);
        labelErreur.setManaged(true);
        labelSucces.setVisible(false);
        labelSucces.setManaged(false);
    }

    private void afficherSucces(String msg) {
        labelSucces.setText(msg);
        labelSucces.setVisible(true);
        labelSucces.setManaged(true);
        labelErreur.setVisible(false);
        labelErreur.setManaged(false);
    }
}

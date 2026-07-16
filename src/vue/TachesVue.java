package vue;

import dao.TacheDAO;
import dao.EvenementDAO;
import modele.Evenement;
import modele.Tache;
import modele.Utilisateur;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TachesVue {

    private TacheDAO     tacheDAO;
    private EvenementDAO evenementDAO;

    private Utilisateur utilisateur;
    private Stage       stage;

    private VBox colAFaire   = new VBox(8);
    private VBox colEnCours  = new VBox(8);
    private VBox colTermine  = new VBox(8);

    private Label lblCountAFaire  = new Label();
    private Label lblCountEnCours = new Label();
    private Label lblCountTermine = new Label();

    public void afficher(Stage stage, Utilisateur utilisateur) {
        this.stage       = stage;
        this.utilisateur = utilisateur;

        try {
            this.tacheDAO     = new TacheDAO();
            this.evenementDAO = new EvenementDAO();
        } catch (Exception e) {
            alerteErreur("Impossible de se connecter à la base de données : " + e.getMessage());
        }

        VBox root = new VBox();
        root.setStyle("-fx-background-color: #f8fafc;");
        root.getChildren().addAll(creerNavbar(), creerBarreActions(), creerKanban());

        Scene scene = new Scene(root, 960, 650);
        stage.setTitle("Agenda Personnel — Tâches");
        stage.setScene(scene);
        stage.show();

        chargerTaches();
    }

    private HBox creerNavbar() {
        HBox nav = new HBox(8);
        nav.setPadding(new Insets(0, 20, 0, 20));
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPrefHeight(56);
        nav.setStyle("-fx-background-color: #1e40af;");

        Label logo = new Label("📅 Agenda Personnel");
        logo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDashboard  = navBtn("🏠 Dashboard");
        Button btnEvenements = navBtn("📋 Événements");
        Button btnDeconnexion = new Button("Déconnexion");
        btnDeconnexion.setStyle("-fx-background-color: transparent; -fx-text-fill: #fca5a5; -fx-font-size: 13px; -fx-cursor: hand; -fx-border-color: #fca5a5; -fx-border-radius: 4; -fx-padding: 4 10;");

        btnDashboard.setOnAction(e  -> new DashboardVue().afficher(stage, utilisateur));
        btnEvenements.setOnAction(e -> new EvenementsVue().afficher(stage, utilisateur));
        btnDeconnexion.setOnAction(e -> new LoginVue().afficher(stage));

        HBox.setMargin(btnDashboard,  new Insets(0, 4, 0, 0));
        HBox.setMargin(btnEvenements, new Insets(0, 12, 0, 0));
        nav.getChildren().addAll(logo, spacer, btnDashboard, btnEvenements, btnDeconnexion);
        return nav;
    }

    private Button navBtn(String texte) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 4 10;");
        return btn;
    }

    private HBox creerBarreActions() {
        HBox barre = new HBox(8);
        barre.setPadding(new Insets(12, 20, 12, 20));
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        Label lblTitre = new Label("Gestion des tâches");
        lblTitre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNouvelle  = boutonPrimaire("➕ Nouvelle tâche");
        Button btnModifier  = boutonSecondaire("✏ Modifier");
        Button btnSupprimer = new Button("🗑 Supprimer");
        btnSupprimer.setPrefHeight(34);
        btnSupprimer.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 6; -fx-cursor: hand;");

        btnNouvelle.setOnAction(e  -> ouvrirFormulaireTache(null));
        btnModifier.setOnAction(e  -> alerteInfo("Double-cliquez sur une carte de tâche pour la modifier."));
        btnSupprimer.setOnAction(e -> alerteInfo("Double-cliquez sur une carte pour modifier ou supprimer une tâche."));

        barre.getChildren().addAll(lblTitre, spacer, btnNouvelle, btnModifier, btnSupprimer);
        return barre;
    }

    private ScrollPane creerKanban() {
        HBox kanban = new HBox(12);
        kanban.setPadding(new Insets(16, 20, 20, 20));
        kanban.setFillHeight(true);

        VBox wrapAFaire  = creerColonne("À faire",  "🔵", "#1e40af", colAFaire,  lblCountAFaire);
        VBox wrapEnCours = creerColonne("En cours", "🟡", "#d97706", colEnCours, lblCountEnCours);
        VBox wrapTermine = creerColonne("Terminé",  "🟢", "#16a34a", colTermine, lblCountTermine);

        HBox.setHgrow(wrapAFaire,  Priority.ALWAYS);
        HBox.setHgrow(wrapEnCours, Priority.ALWAYS);
        HBox.setHgrow(wrapTermine, Priority.ALWAYS);
        kanban.getChildren().addAll(wrapAFaire, wrapEnCours, wrapTermine);

        ScrollPane scroll = new ScrollPane(kanban);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    private VBox creerColonne(String titre, String emoji, String couleur, VBox colonneCards, Label lblCount) {
        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));

        Label lblTitre = new Label(emoji + " " + titre);
        lblTitre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + couleur + ";");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        lblCount.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-background-color: " + couleur + "; -fx-padding: 1 6; -fx-background-radius: 10;");
        header.getChildren().addAll(lblTitre, spacer, lblCount);

        colonneCards.setFillWidth(true);
        ScrollPane scroll = new ScrollPane(colonneCards);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefHeight(400);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox col = new VBox(6, header, scroll);
        col.setPadding(new Insets(14));
        col.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8;");
        return col;
    }

    private void chargerTaches() {
        colAFaire.getChildren().clear();
        colEnCours.getChildren().clear();
        colTermine.getChildren().clear();

        try {
            if (tacheDAO == null) {
                colAFaire.getChildren().add(labelVide("Connexion BDD indisponible."));
                return;
            }
            List<Tache> aFaire  = tacheDAO.parStatut(utilisateur.getIdUtil(), Tache.STATUT_A_FAIRE);
            List<Tache> enCours = tacheDAO.parStatut(utilisateur.getIdUtil(), Tache.STATUT_EN_COURS);
            List<Tache> termine = tacheDAO.parStatut(utilisateur.getIdUtil(), Tache.STATUT_TERMINE);

            aFaire.forEach(t  -> colAFaire.getChildren().add(creerCarteTache(t)));
            enCours.forEach(t -> colEnCours.getChildren().add(creerCarteTache(t)));
            termine.forEach(t -> colTermine.getChildren().add(creerCarteTache(t)));

            lblCountAFaire.setText(String.valueOf(aFaire.size()));
            lblCountEnCours.setText(String.valueOf(enCours.size()));
            lblCountTermine.setText(String.valueOf(termine.size()));

            if (aFaire.isEmpty())  colAFaire.getChildren().add(labelVide("Aucune tâche à faire."));
            if (enCours.isEmpty()) colEnCours.getChildren().add(labelVide("Aucune tâche en cours."));
            if (termine.isEmpty()) colTermine.getChildren().add(labelVide("Aucune tâche terminée."));

        } catch (Exception e) {
            colAFaire.getChildren().add(labelVide("Erreur de chargement : " + e.getMessage()));
        }
    }

    private VBox creerCarteTache(Tache tache) {
        VBox carte = new VBox(6);
        carte.setPadding(new Insets(10, 12, 10, 12));

        String couleurBord = switch (tache.getPriorite()) {
            case Tache.PRIORITE_HAUTE   -> "#ef4444";
            case Tache.PRIORITE_MOYENNE -> "#f59e0b";
            default                     -> "#10b981";
        };

        carte.setStyle("-fx-background-color: white; -fx-border-color: " + couleurBord
                + " #e2e8f0 #e2e8f0 " + couleurBord
                + "; -fx-border-width: 0 0 0 4; -fx-background-radius: 6; -fx-border-radius: 0 6 6 0; -fx-cursor: hand;");

        HBox ligneTitre = new HBox(6);
        ligneTitre.setAlignment(Pos.CENTER_LEFT);
        Label lblTitre = new Label(tache.getTitre());
        lblTitre.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        lblTitre.setWrapText(true);
        HBox.setHgrow(lblTitre, Priority.ALWAYS);
        Label badgePrio = new Label(tache.getPriorite());
        badgePrio.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-background-color: " + couleurBord + "; -fx-padding: 2 6; -fx-background-radius: 4;");
        ligneTitre.getChildren().addAll(lblTitre, badgePrio);
        carte.getChildren().add(ligneTitre);

        if (tache.getDescription() != null && !tache.getDescription().isBlank()) {
            String desc = tache.getDescription();
            if (desc.length() > 60) desc = desc.substring(0, 60) + "…";
            Label lblDesc = new Label(desc);
            lblDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
            lblDesc.setWrapText(true);
            carte.getChildren().add(lblDesc);
        }

        if (tache.getDeadline() != null) {
            String dateStr = new SimpleDateFormat("dd/MM/yyyy").format(tache.getDeadline());
            boolean enRetard = tache.isEnRetard();
            Label lblDate = new Label((enRetard ? "⚠ En retard · " : "📅 ") + dateStr);
            lblDate.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (enRetard ? "#ef4444" : "#6b7280") + ";" + (enRetard ? "-fx-font-weight: bold;" : ""));
            carte.getChildren().add(lblDate);
        }

        if (tache.getEvenement() != null) {
            Label lblEv = new Label("🔗 " + tache.getEvenement().getTitre());
            lblEv.setStyle("-fx-font-size: 11px; -fx-text-fill: #1e40af;");
            carte.getChildren().add(lblEv);
        }

        HBox btnStatut = new HBox(4);
        btnStatut.setAlignment(Pos.CENTER_RIGHT);
        if (!tache.getStatut().equals(Tache.STATUT_A_FAIRE)) {
            Button btnLeft = miniBtn("◀");
            btnLeft.setOnAction(e -> changerStatut(tache, statutPrecedent(tache.getStatut())));
            btnStatut.getChildren().add(btnLeft);
        }
        if (!tache.getStatut().equals(Tache.STATUT_TERMINE)) {
            Button btnRight = miniBtn("▶");
            btnRight.setOnAction(e -> changerStatut(tache, statutSuivant(tache.getStatut())));
            btnStatut.getChildren().add(btnRight);
        }
        carte.getChildren().add(btnStatut);

        carte.setOnMouseClicked(e -> { if (e.getClickCount() == 2) ouvrirFormulaireTache(tache); });
        return carte;
    }

    private void changerStatut(Tache tache, String nouveauStatut) {
        try {
            if (tacheDAO == null) { alerteErreur("Connexion BDD indisponible."); return; }
            tacheDAO.changerStatut(tache.getIdTache(), nouveauStatut);
            chargerTaches();
        } catch (Exception e) {
            alerteErreur("Erreur lors du changement de statut : " + e.getMessage());
        }
    }

    private String statutSuivant(String statut) {
        return switch (statut) {
            case Tache.STATUT_A_FAIRE  -> Tache.STATUT_EN_COURS;
            case Tache.STATUT_EN_COURS -> Tache.STATUT_TERMINE;
            default -> statut;
        };
    }

    private String statutPrecedent(String statut) {
        return switch (statut) {
            case Tache.STATUT_TERMINE  -> Tache.STATUT_EN_COURS;
            case Tache.STATUT_EN_COURS -> Tache.STATUT_A_FAIRE;
            default -> statut;
        };
    }

    private void ouvrirFormulaireTache(Tache existante) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(existante == null ? "Nouvelle tâche" : "Modifier la tâche");
        modal.setResizable(false);

        // ── En-tête ──
        VBox header = new VBox(4);
        header.setPadding(new Insets(18, 22, 14, 22));
        header.setStyle("-fx-background-color: #1e40af;");
        Label lblH = new Label(existante == null ? "Nouvelle tâche" : "Modifier la tâche");
        lblH.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lblS = new Label("Remplissez les informations ci-dessous");
        lblS.setStyle("-fx-font-size: 12px; -fx-text-fill: #93c5fd;");
        header.getChildren().addAll(lblH, lblS);

        // ── Champs ──
        TextField fieldTitre = new TextField();
        fieldTitre.setPromptText("Titre de la tâche (3 à 100 caractères)");
        styleChamp(fieldTitre);
        Label errTitre = errLabel();

        TextArea areaDesc = new TextArea();
        areaDesc.setPromptText("Description — optionnel (500 caractères max)");
        areaDesc.setPrefRowCount(2);
        areaDesc.setWrapText(true);
        areaDesc.setStyle("-fx-border-color: #d1d5db; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 13px;");
        Label errDesc = errLabel();

        DatePicker pickerDeadline = new DatePicker();
        pickerDeadline.setPromptText("Deadline — optionnel");
        pickerDeadline.setMaxWidth(Double.MAX_VALUE);
        pickerDeadline.getEditor().setEditable(false);
        styleChamp(pickerDeadline);
        Label errDeadline = errLabel();

        ComboBox<String> comboPriorite = new ComboBox<>(FXCollections.observableArrayList(
            Tache.PRIORITE_HAUTE, Tache.PRIORITE_MOYENNE, Tache.PRIORITE_BASSE));
        comboPriorite.setValue(Tache.PRIORITE_MOYENNE);
        comboPriorite.setMaxWidth(Double.MAX_VALUE);
        styleChamp(comboPriorite);

        ComboBox<String> comboStatut = new ComboBox<>(FXCollections.observableArrayList(
            Tache.STATUT_A_FAIRE, Tache.STATUT_EN_COURS, Tache.STATUT_TERMINE));
        comboStatut.setValue(Tache.STATUT_A_FAIRE);
        comboStatut.setMaxWidth(Double.MAX_VALUE);
        styleChamp(comboStatut);

        ComboBox<Object> comboEvenement = new ComboBox<>();
        comboEvenement.setMaxWidth(Double.MAX_VALUE);
        styleChamp(comboEvenement);
        Label errEven = errLabel();

        // Charger événements
        try {
            comboEvenement.getItems().add("Aucun événement lié");
            if (evenementDAO != null) {
                List<Evenement> evts = evenementDAO.tousLesEvenements(utilisateur.getIdUtil());
                comboEvenement.getItems().addAll(evts);
            }
            comboEvenement.setValue("Aucun événement lié");
        } catch (Exception e) {
            comboEvenement.getItems().add("Aucun événement lié");
            comboEvenement.setValue("Aucun événement lié");
            errEven.setText("⚠ Impossible de charger les événements.");
            errEven.setVisible(true); errEven.setManaged(true);
        }

        // Pré-remplissage
        if (existante != null) {
            try {
                fieldTitre.setText(existante.getTitre() != null ? existante.getTitre() : "");
                if (existante.getDescription() != null) areaDesc.setText(existante.getDescription());
                comboPriorite.setValue(existante.getPriorite());
                comboStatut.setValue(existante.getStatut());
                if (existante.getDeadline() != null)
                    pickerDeadline.setValue(existante.getDeadline().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate());
                if (existante.getEvenement() != null) {
                    comboEvenement.getItems().stream()
                        .filter(o -> o instanceof Evenement
                            && ((Evenement) o).getIdEven() == existante.getEvenement().getIdEven())
                        .findFirst().ifPresent(comboEvenement::setValue);
                }
            } catch (Exception e) {
                // Pré-remplissage échoué — formulaire vide acceptable
            }
        }

        HBox lignePrioStatut = new HBox(12);
        VBox boxPrio   = new VBox(4, lbl("Priorité *"), comboPriorite);
        VBox boxStatut = new VBox(4, lbl("Statut *"),   comboStatut);
        HBox.setHgrow(boxPrio,   Priority.ALWAYS);
        HBox.setHgrow(boxStatut, Priority.ALWAYS);
        lignePrioStatut.getChildren().addAll(boxPrio, boxStatut);

        Label labelMsg = new Label();
        labelMsg.setWrapText(true);
        labelMsg.setVisible(false); labelMsg.setManaged(false);

        VBox corps = new VBox(10);
        corps.setPadding(new Insets(18, 22, 10, 22));
        corps.setStyle("-fx-background-color: white;");
        corps.getChildren().addAll(
            lbl("Titre *"),     fieldTitre,    errTitre,
            lbl("Description"), areaDesc,      errDesc,
            lbl("Deadline"),    pickerDeadline, errDeadline,
            lignePrioStatut,
            lbl("Événement lié"), comboEvenement, errEven,
            labelMsg
        );

        // ── Boutons ──
        Button btnAnnuler  = boutonSecondaire("Annuler");
        btnAnnuler.setOnAction(e -> modal.close());

        Button btnSupprimer = new Button("🗑 Supprimer");
        btnSupprimer.setPrefHeight(34);
        btnSupprimer.setVisible(existante != null);
        btnSupprimer.setManaged(existante != null);
        btnSupprimer.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-border-color: #fca5a5; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 13px; -fx-cursor: hand;");

        if (existante != null) {
            btnSupprimer.setOnAction(e -> {
                if (confirmerSuppression(existante.getTitre())) {
                    try {
                        if (tacheDAO == null) { alerteErreur("Connexion BDD indisponible."); return; }
                        tacheDAO.supprimer(existante.getIdTache());
                        modal.close();
                        chargerTaches();
                    } catch (Exception ex) {
                        alerteErreur("Erreur lors de la suppression : " + ex.getMessage());
                    }
                }
            });
        }

        Button btnSave = boutonPrimaire("✓ Enregistrer");
        btnSave.setOnAction(e -> enregistrerTache(
            existante, modal, fieldTitre, areaDesc, pickerDeadline,
            comboPriorite, comboStatut, comboEvenement,
            errTitre, errDesc, errDeadline, labelMsg
        ));

        HBox boutons = new HBox(8);
        boutons.setAlignment(Pos.CENTER_RIGHT);
        boutons.setPadding(new Insets(12, 22, 12, 22));
        boutons.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1 0 0 0;");
        boutons.getChildren().addAll(btnAnnuler, btnSupprimer, btnSave);

        ScrollPane scroll = new ScrollPane(corps);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: white; -fx-background-color: white;");

        modal.setScene(new Scene(new VBox(header, scroll, boutons), 460, 530));
        modal.showAndWait();
    }

    private void enregistrerTache(
            Tache existante, Stage modal,
            TextField fieldTitre, TextArea areaDesc, DatePicker pickerDeadline,
            ComboBox<String> comboPriorite, ComboBox<String> comboStatut,
            ComboBox<Object> comboEvenement,
            Label errTitre, Label errDesc, Label errDeadline, Label labelMsg) {

        // Reset erreurs
        hide(errTitre); hide(errDesc); hide(errDeadline);
        boolean ok = true;

        // ── Titre ──
        String titre = fieldTitre.getText() == null ? "" : fieldTitre.getText().trim();
        if (titre.isEmpty()) {
            errTitre.setText("Le titre est obligatoire.");
            show(errTitre); ok = false;
        } else if (titre.length() < 3) {
            errTitre.setText("Le titre doit contenir au moins 3 caractères.");
            show(errTitre); ok = false;
        } else if (titre.length() > 100) {
            errTitre.setText("Le titre ne peut pas dépasser 100 caractères (" + titre.length() + "/100).");
            show(errTitre); ok = false;
        }

        // ── Description ──
        String desc = areaDesc.getText() == null ? "" : areaDesc.getText().trim();
        if (desc.length() > 500) {
            errDesc.setText("La description ne peut pas dépasser 500 caractères (" + desc.length() + "/500).");
            show(errDesc); ok = false;
        }

        // ── Deadline cohérente (optionnelle) ──
        LocalDate deadlineVal = pickerDeadline.getValue();
        if (deadlineVal != null && deadlineVal.isBefore(LocalDate.now().minusYears(1))) {
            errDeadline.setText("La deadline semble invalide (plus d'un an dans le passé).");
            show(errDeadline); ok = false;
        }

        // ── Priorité / Statut ──
        if (comboPriorite.getValue() == null) {
            ok = false; // Ne devrait pas arriver
        }
        if (comboStatut.getValue() == null) {
            ok = false;
        }

        if (!ok) return;

        try {
            if (tacheDAO == null) {
                afficherMsg(labelMsg, "⚠ Connexion BDD indisponible.", false);
                return;
            }

            Date deadline = null;
            if (deadlineVal != null)
                deadline = Date.from(deadlineVal.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Evenement evenementLie = null;
            Object evVal = comboEvenement.getValue();
            if (evVal instanceof Evenement) evenementLie = (Evenement) evVal;

            boolean succes;
            if (existante == null) {
                Tache t = new Tache(
                    titre,
                    desc.isEmpty() ? null : desc,
                    deadline,
                    comboPriorite.getValue(),
                    comboStatut.getValue(),
                    evenementLie,
                    utilisateur.getIdUtil()
                );
                succes = tacheDAO.inserer(t);
            } else {
                existante.setTitre(titre);
                existante.setDescription(desc.isEmpty() ? null : desc);
                existante.setDeadline(deadline);
                existante.setPriorite(comboPriorite.getValue());
                existante.setStatut(comboStatut.getValue());
                existante.setEvenement(evenementLie);
                succes = tacheDAO.modifier(existante);
            }

            if (succes) {
                afficherMsg(labelMsg, "✓ Tâche enregistrée avec succès !", true);
                new Thread(() -> {
                    try { Thread.sleep(900); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                    javafx.application.Platform.runLater(() -> { modal.close(); chargerTaches(); });
                }).start();
            } else {
                afficherMsg(labelMsg, "⚠ Erreur lors de l'enregistrement. Veuillez réessayer.", false);
            }
        } catch (Exception e) {
            afficherMsg(labelMsg, "⚠ Erreur inattendue : " + e.getMessage(), false);
        }
    }

    // ── Helpers UI ────────────────────────────────────────────

    private Label lbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #374151;");
        return l;
    }

    private Label errLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");
        l.setVisible(false); l.setManaged(false);
        return l;
    }

    private void show(Label l) { l.setVisible(true);  l.setManaged(true);  }
    private void hide(Label l) { l.setVisible(false); l.setManaged(false); }

    private void afficherMsg(Label label, String msg, boolean succes) {
        label.setText(msg);
        label.setStyle(succes
            ? "-fx-font-size: 12px; -fx-padding: 8; -fx-background-radius: 6; -fx-text-fill: #166534; -fx-background-color: #f0fdf4;"
            : "-fx-font-size: 12px; -fx-padding: 8; -fx-background-radius: 6; -fx-text-fill: #ef4444; -fx-background-color: #fef2f2;");
        label.setVisible(true); label.setManaged(true);
    }

    private Label labelVide(String texte) {
        Label l = new Label(texte);
        l.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px; -fx-font-style: italic;");
        l.setPadding(new Insets(10, 0, 0, 0));
        return l;
    }

    private Button miniBtn(String texte) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #374151; -fx-font-size: 11px; -fx-padding: 2 8; -fx-cursor: hand; -fx-background-radius: 4; -fx-border-color: #d1d5db; -fx-border-radius: 4;");
        return btn;
    }

    private Button boutonPrimaire(String texte) {
        Button btn = new Button(texte);
        btn.setPrefHeight(34);
        btn.setStyle("-fx-background-color: #1e40af; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        return btn;
    }

    private Button boutonSecondaire(String texte) {
        Button btn = new Button(texte);
        btn.setPrefHeight(34);
        btn.setStyle("-fx-background-color: white; -fx-text-fill: #374151; -fx-font-size: 13px; -fx-border-color: #d1d5db; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        return btn;
    }

    private void styleChamp(Control c) {
        c.setStyle("-fx-border-color: #d1d5db; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 10; -fx-font-size: 13px;");
        c.setPrefHeight(36);
    }

    private void alerteInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null); a.showAndWait();
    }

    private void alerteErreur(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Erreur"); a.showAndWait();
    }

    private boolean confirmerSuppression(String titre) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmer la suppression");
        a.setHeaderText("Supprimer la tâche ?");
        a.setContentText("Êtes-vous sûr de vouloir supprimer « " + titre + " » ?");
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.OK;
    }
}

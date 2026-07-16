package vue;

import dao.CategorieDAO;
import dao.EvenementDAO;
import modele.Categorie;
import modele.Evenement;
import modele.Utilisateur;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class FormulaireEvenementVue {

    private final EvenementDAO evenementDAO = new EvenementDAO();
    private final CategorieDAO categorieDAO = new CategorieDAO();

    public void afficher(Evenement evenement, Utilisateur utilisateur, Runnable onSucces) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle(evenement == null ? "Nouvel événement" : "Modifier événement");
        modal.setResizable(false);

        // ── En-tête ──
        VBox header = new VBox(4);
        header.setPadding(new Insets(18, 22, 14, 22));
        header.setStyle("-fx-background-color: #1e40af;");
        Label lblTitre = new Label(evenement == null ? "Nouvel événement" : "Modifier l'événement");
        lblTitre.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lblSous = new Label("Remplissez les informations ci-dessous");
        lblSous.setStyle("-fx-font-size: 12px; -fx-text-fill: #93c5fd;");
        header.getChildren().addAll(lblTitre, lblSous);

        // ── Champs ──
        TextField fieldTitre = new TextField();
        fieldTitre.setPromptText("Titre de l'événement (3 à 100 caractères)");
        styleChamp(fieldTitre);
        Label errTitre = errLabel();

        DatePicker pickerDate = new DatePicker();
        pickerDate.setPromptText("Sélectionner une date");
        pickerDate.setMaxWidth(Double.MAX_VALUE);
        styleChamp(pickerDate);
        // Empêcher la saisie libre dans le DatePicker
        pickerDate.getEditor().setEditable(false);
        Label errDate = errLabel();

        TextField fieldHeure = new TextField();
        fieldHeure.setPromptText("HH:MM — optionnel");
        styleChamp(fieldHeure);
        Label errHeure = errLabel();

        TextArea areaDesc = new TextArea();
        areaDesc.setPromptText("Description — optionnel (500 caractères max)");
        areaDesc.setPrefRowCount(2);
        areaDesc.setWrapText(true);
        areaDesc.setStyle("-fx-border-color: #d1d5db; -fx-border-radius: 6; "
                + "-fx-background-radius: 6; -fx-font-size: 13px;");
        Label errDesc = errLabel();

        ComboBox<Object> comboCat = new ComboBox<>();
        comboCat.setMaxWidth(Double.MAX_VALUE);
        styleChamp(comboCat);
        Label errCat = errLabel();

        // Charger les catégories
        try {
            comboCat.getItems().add("Aucune catégorie");
            List<Categorie> cats = categorieDAO.toutesLesCategories();
            comboCat.getItems().addAll(cats);
            comboCat.setValue("Aucune catégorie");
        } catch (Exception e) {
            comboCat.getItems().add("Aucune catégorie");
            comboCat.setValue("Aucune catégorie");
            errCat.setText("⚠ Impossible de charger les catégories.");
            errCat.setVisible(true); errCat.setManaged(true);
        }

        CheckBox chkImportant = new CheckBox("Marquer comme important ⭐");
        chkImportant.setStyle("-fx-font-size: 13px;");

        // ── Pré-remplissage si modification ──
        if (evenement != null) {
            try {
                fieldTitre.setText(evenement.getTitre() != null ? evenement.getTitre() : "");
                fieldHeure.setText(evenement.getHeure() != null ? evenement.getHeure() : "");
                areaDesc.setText(evenement.getDescription() != null ? evenement.getDescription() : "");
                chkImportant.setSelected(evenement.isImportant());
                if (evenement.getDateEven() != null) {
                    pickerDate.setValue(evenement.getDateEven().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate());
                }
                if (evenement.getCategorie() != null) {
                    comboCat.getItems().stream()
                        .filter(o -> o instanceof Categorie
                            && ((Categorie) o).getIdCat() == evenement.getCategorie().getIdCat())
                        .findFirst().ifPresent(comboCat::setValue);
                }
            } catch (Exception e) {
                // Pré-remplissage échoué — on continue avec le formulaire vide
            }
        }

        Label labelMsg = new Label();
        labelMsg.setWrapText(true);
        labelMsg.setVisible(false);
        labelMsg.setManaged(false);

        // ── Corps ──
        VBox corps = new VBox(10);
        corps.setPadding(new Insets(18, 22, 10, 22));
        corps.setStyle("-fx-background-color: white;");
        corps.getChildren().addAll(
            lbl("Titre *"),        fieldTitre,  errTitre,
            lbl("Date *"),         pickerDate,  errDate,
            lbl("Heure"),          fieldHeure,  errHeure,
            lbl("Description"),    areaDesc,    errDesc,
            lbl("Catégorie"),      comboCat,    errCat,
            chkImportant,
            labelMsg
        );

        // ── Boutons ──
        Button btnAnnuler = boutonSecondaire("Annuler");
        btnAnnuler.setOnAction(e -> modal.close());

        Button btnSave = boutonPrimaire("✓ Enregistrer");
        btnSave.setOnAction(e -> enregistrer(
            evenement, utilisateur, modal, onSucces,
            fieldTitre, pickerDate, fieldHeure, areaDesc, comboCat, chkImportant,
            errTitre, errDate, errHeure, errDesc, labelMsg
        ));

        HBox boutons = new HBox(8);
        boutons.setAlignment(Pos.CENTER_RIGHT);
        boutons.setPadding(new Insets(12, 22, 12, 22));
        boutons.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1 0 0 0;");
        boutons.getChildren().addAll(btnAnnuler, btnSave);

        ScrollPane scroll = new ScrollPane(corps);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: white; -fx-background-color: white;");

        VBox root = new VBox(header, scroll, boutons);
        modal.setScene(new Scene(root, 460, 530));
        modal.showAndWait();
    }

    private void enregistrer(
            Evenement evenement, Utilisateur utilisateur, Stage modal, Runnable onSucces,
            TextField fieldTitre, DatePicker pickerDate, TextField fieldHeure,
            TextArea areaDesc, ComboBox<Object> comboCat, CheckBox chkImportant,
            Label errTitre, Label errDate, Label errHeure, Label errDesc, Label labelMsg) {

        // Reset erreurs
        hideAll(errTitre, errDate, errHeure, errDesc);
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

        // ── Date ──
        LocalDate dateVal = pickerDate.getValue();
        if (dateVal == null) {
            errDate.setText("La date est obligatoire.");
            show(errDate); ok = false;
        }

        // ── Heure (optionnelle mais validée si saisie) ──
        String heure = fieldHeure.getText() == null ? "" : fieldHeure.getText().trim();
        if (!heure.isEmpty() && !heure.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            errHeure.setText("Format invalide. Utilisez HH:MM (ex: 09:30, 14:00).");
            show(errHeure); ok = false;
        }

        // ── Description (optionnelle mais limitée) ──
        String desc = areaDesc.getText() == null ? "" : areaDesc.getText().trim();
        if (desc.length() > 500) {
            errDesc.setText("La description ne peut pas dépasser 500 caractères (" + desc.length() + "/500).");
            show(errDesc); ok = false;
        }

        if (!ok) return;

        // ── Enregistrement ──
        try {
            Date date = Date.from(dateVal.atStartOfDay(ZoneId.systemDefault()).toInstant());
            String heureVal = heure.isEmpty() ? null : heure;
            String descVal  = desc.isEmpty()  ? null : desc;
            boolean important = chkImportant.isSelected();

            Categorie cat = null;
            Object catVal = comboCat.getValue();
            if (catVal instanceof Categorie) cat = (Categorie) catVal;

            boolean succes;
            if (evenement == null) {
                Evenement ev = new Evenement(titre, date, heureVal, descVal, important, cat, utilisateur.getIdUtil());
                succes = evenementDAO.inserer(ev);
            } else {
                evenement.setTitre(titre);
                evenement.setDateEven(date);
                evenement.setHeure(heureVal);
                evenement.setDescription(descVal);
                evenement.setImportant(important);
                evenement.setCategorie(cat);
                succes = evenementDAO.modifier(evenement);
            }

            if (succes) {
                afficherMsg(labelMsg, "✓ Événement enregistré avec succès !", true);
                new Thread(() -> {
                    try { Thread.sleep(900); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                    javafx.application.Platform.runLater(() -> {
                        modal.close();
                        if (onSucces != null) onSucces.run();
                    });
                }).start();
            } else {
                afficherMsg(labelMsg, "⚠ Erreur lors de l'enregistrement. Veuillez réessayer.", false);
            }
        } catch (Exception e) {
            afficherMsg(labelMsg, "⚠ Erreur inattendue : " + e.getMessage(), false);
        }
    }

    // ── Helpers UI ────────────────────────────────────────────

    private Label lbl(String texte) {
        Label l = new Label(texte);
        l.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #374151;");
        return l;
    }

    private Label errLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");
        l.setVisible(false); l.setManaged(false);
        return l;
    }

    private void show(Label l) { l.setVisible(true); l.setManaged(true); }

    private void hideAll(Label... labels) {
        for (Label l : labels) { l.setVisible(false); l.setManaged(false); }
    }

    private void afficherMsg(Label label, String msg, boolean succes) {
        label.setText(msg);
        if (succes)
            label.setStyle("-fx-font-size: 12px; -fx-padding: 8; -fx-background-radius: 6; -fx-text-fill: #166534; -fx-background-color: #f0fdf4;");
        else
            label.setStyle("-fx-font-size: 12px; -fx-padding: 8; -fx-background-radius: 6; -fx-text-fill: #ef4444; -fx-background-color: #fef2f2;");
        label.setVisible(true); label.setManaged(true);
    }

    private void styleChamp(Control c) {
        c.setStyle("-fx-border-color: #d1d5db; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 10; -fx-font-size: 13px;");
        c.setPrefHeight(36);
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
}

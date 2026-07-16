package vue;

import dao.CategorieDAO;
import dao.EvenementDAO;
import modele.Categorie;
import modele.Evenement;
import modele.Utilisateur;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EvenementsVue {

    private EvenementDAO evenementDAO;
    private CategorieDAO categorieDAO;

    private Utilisateur utilisateur;
    private Stage stage;

    private final TextField    fieldRecherche = new TextField();
    private final ComboBox<Object> comboCat   = new ComboBox<>();
    private final TableView<Evenement> table  = new TableView<>();
    private final Label        labelEtat      = new Label();

    public void afficher(Stage stage, Utilisateur utilisateur) {
        this.stage       = stage;
        this.utilisateur = utilisateur;

        try {
            this.evenementDAO = new EvenementDAO();
            this.categorieDAO = new CategorieDAO();
        } catch (Exception e) {
            afficherEtat("Impossible de se connecter à la base de données : " + e.getMessage(), false);
        }

        VBox root = new VBox();
        root.setStyle("-fx-background-color: #f8fafc;");

        VBox tableWrapper = new VBox(labelEtat, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(tableWrapper, Priority.ALWAYS);

        labelEtat.setWrapText(true);
        labelEtat.setPadding(new Insets(6, 20, 0, 20));
        labelEtat.setVisible(false);
        labelEtat.setManaged(false);

        root.getChildren().addAll(creerNavbar(), creerBarreFiltres(), creerBarreActions(), tableWrapper);

        Scene scene = new Scene(root, 960, 650);
        stage.setTitle("Agenda Personnel — Événements");
        stage.setScene(scene);
        stage.show();

        construireColonnes();
        chargerEvenements();
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

        Button btnDashboard = navBtn("🏠 Dashboard");
        Button btnTaches    = navBtn("✅ Tâches");
        Button btnDeconn    = new Button("Déconnexion");
        btnDeconn.setStyle("-fx-background-color: transparent; -fx-text-fill: #fca5a5; -fx-font-size: 13px; -fx-cursor: hand; -fx-border-color: #fca5a5; -fx-border-radius: 4; -fx-padding: 4 10;");

        btnDashboard.setOnAction(e -> new DashboardVue().afficher(stage, utilisateur));
        btnTaches.setOnAction(e    -> new TachesVue().afficher(stage, utilisateur));
        btnDeconn.setOnAction(e    -> new LoginVue().afficher(stage));

        nav.getChildren().addAll(logo, spacer, btnDashboard, btnTaches, btnDeconn);
        return nav;
    }

    private Button navBtn(String t) {
        Button btn = new Button(t);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 4 10;");
        return btn;
    }

    private VBox creerBarreFiltres() {
        VBox wrapper = new VBox(10);
        wrapper.setPadding(new Insets(16, 20, 8, 20));
        wrapper.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        Label titre = new Label("🔍 Recherche et filtres");
        titre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        fieldRecherche.setPromptText("Rechercher par titre (150 caractères max)...");
        fieldRecherche.setPrefWidth(280);
        styleChamp(fieldRecherche);
        // Limiter la saisie à 150 caractères
        fieldRecherche.textProperty().addListener((obs, old, nw) -> {
            if (nw != null && nw.length() > 150) fieldRecherche.setText(old);
        });

        // Charger catégories
        comboCat.getItems().add("Toutes les catégories");
        try {
            if (categorieDAO != null) {
                categorieDAO.toutesLesCategories().forEach(c -> comboCat.getItems().add(c));
            }
        } catch (Exception e) {
            // ComboBox reste avec "Toutes les catégories" seulement
        }
        comboCat.setValue("Toutes les catégories");

        Button btnChercher = new Button("🔍 Rechercher");
        btnChercher.setStyle("-fx-background-color: #1e40af; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 12;");
        Button btnReset = new Button("Réinitialiser");
        btnReset.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 6; -fx-cursor: hand; -fx-padding: 6 12;");

        btnChercher.setOnAction(e -> chargerEvenements());
        btnReset.setOnAction(e    -> reinitialiserFiltres());

        HBox ligne = new HBox(10, new Label("Titre:"), fieldRecherche, new Label("Catégorie:"), comboCat, btnChercher, btnReset);
        ligne.setAlignment(Pos.CENTER_LEFT);

        wrapper.getChildren().addAll(titre, ligne);
        return wrapper;
    }

    private HBox creerBarreActions() {
        HBox barre = new HBox(8);
        barre.setPadding(new Insets(10, 20, 10, 20));
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        Button btnNouveau  = new Button("➕ Nouvel événement");
        btnNouveau.setStyle("-fx-background-color: #1e40af; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 14;");
        Button btnModifier = new Button("✏ Modifier");
        btnModifier.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 6; -fx-cursor: hand; -fx-padding: 6 14;");
        Button btnSupprimer = new Button("🗑 Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 14;");

        btnNouveau.setOnAction(e ->
            new FormulaireEvenementVue().afficher(null, utilisateur, () -> chargerEvenements()));

        btnModifier.setOnAction(e -> {
            Evenement sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { alerte("Sélectionnez un événement dans la liste pour le modifier."); return; }
            new FormulaireEvenementVue().afficher(sel, utilisateur, () -> chargerEvenements());
        });

        btnSupprimer.setOnAction(e -> {
            Evenement sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { alerte("Sélectionnez un événement dans la liste pour le supprimer."); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'événement « " + sel.getTitre() + " » ?\nLes tâches liées seront conservées.", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            Optional<ButtonType> r = confirm.showAndWait();
            if (r.isPresent() && r.get() == ButtonType.YES) {
                try {
                    if (evenementDAO == null) { alerte("Connexion BDD indisponible."); return; }
                    evenementDAO.supprimer(sel.getIdEven());
                    chargerEvenements();
                } catch (Exception ex) {
                    afficherEtat("Erreur lors de la suppression : " + ex.getMessage(), false);
                }
            }
        });

        barre.getChildren().addAll(btnNouveau, btnModifier, btnSupprimer);
        return barre;
    }

    @SuppressWarnings("unchecked")
    private void construireColonnes() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        TableColumn<Evenement, String> colTitre = new TableColumn<>("Titre");
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colTitre.setPrefWidth(200);

        TableColumn<Evenement, Void> colDate = new TableColumn<>("Date");
        colDate.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                try {
                    Evenement ev = empty || getTableRow() == null ? null : (Evenement) getTableRow().getItem();
                    setText(ev != null && ev.getDateEven() != null ? sdf.format(ev.getDateEven()) : null);
                } catch (Exception e) { setText(null); }
            }
        });
        colDate.setPrefWidth(100);

        TableColumn<Evenement, String> colHeure = new TableColumn<>("Heure");
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        colHeure.setPrefWidth(80);

        TableColumn<Evenement, Void> colCat = new TableColumn<>("Catégorie");
        colCat.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                try {
                    Evenement ev = empty || getTableRow() == null ? null : (Evenement) getTableRow().getItem();
                    setText(ev != null ? (ev.getCategorie() != null ? ev.getCategorie().getLibelle() : "—") : null);
                } catch (Exception e) { setText(null); }
            }
        });
        colCat.setPrefWidth(130);

        TableColumn<Evenement, Boolean> colImportant = new TableColumn<>("Important");
        colImportant.setCellValueFactory(new PropertyValueFactory<>("important"));
        colImportant.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "⭐ Oui" : "Non"));
            }
        });
        colImportant.setPrefWidth(90);

        TableColumn<Evenement, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDesc.setPrefWidth(260);

        table.getColumns().addAll(colTitre, colDate, colHeure, colCat, colImportant, colDesc);
        table.setPlaceholder(new Label("Aucun événement trouvé."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void chargerEvenements() {
        labelEtat.setVisible(false); labelEtat.setManaged(false);
        try {
            if (evenementDAO == null) {
                afficherEtat("Connexion à la base de données indisponible.", false);
                table.setItems(FXCollections.observableArrayList());
                return;
            }
            String titre = fieldRecherche.getText() == null ? "" : fieldRecherche.getText().trim();
            int idCat = 0;
            Object catVal = comboCat.getValue();
            if (catVal instanceof Categorie) idCat = ((Categorie) catVal).getIdCat();

            List<Evenement> liste = evenementDAO.rechercherMultiCriteres(
                utilisateur.getIdUtil(), titre.isEmpty() ? null : titre, idCat, null, null);
            table.setItems(FXCollections.observableArrayList(liste != null ? liste : new ArrayList<>()));
        } catch (Exception e) {
            afficherEtat("Erreur lors du chargement : " + e.getMessage(), false);
            table.setItems(FXCollections.observableArrayList());
        }
    }

    private void reinitialiserFiltres() {
        fieldRecherche.clear();
        comboCat.setValue("Toutes les catégories");
        chargerEvenements();
    }

    private void afficherEtat(String msg, boolean succes) {
        labelEtat.setText(msg);
        labelEtat.setStyle(succes
            ? "-fx-text-fill: #166534; -fx-font-size: 12px;"
            : "-fx-text-fill: #ef4444; -fx-font-size: 12px;");
        labelEtat.setVisible(true); labelEtat.setManaged(true);
    }

    private void styleChamp(Control c) {
        c.setStyle("-fx-border-color: #d1d5db; -fx-border-radius: 6; -fx-padding: 6 10;");
    }

    private void alerte(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null); a.showAndWait();
    }
}

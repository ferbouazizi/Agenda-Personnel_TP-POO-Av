package controleur;

import javafx.scene.control.Control;
import javafx.scene.control.Label;

/**
 * Classe utilitaire de styles partagés entre les vues.
 * Évite la duplication de code CSS inline dans chaque Vue.
 */
public class FormulaireHelper {

    // ── Styles champs de saisie ───────────────────────────────

    public static final String STYLE_CHAMP =
        "-fx-border-color: #d1d5db; -fx-border-radius: 6;" +
        "-fx-background-radius: 6; -fx-padding: 6 10; -fx-font-size: 13px;";

    public static final String STYLE_LABEL =
        "-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #374151;";

    public static final String STYLE_ERR =
        "-fx-text-fill: #ef4444; -fx-font-size: 11px;";

    // ── Styles boutons ────────────────────────────────────────

    public static final String STYLE_BTN_PRIMARY =
        "-fx-background-color: #1e40af; -fx-text-fill: white;" +
        "-fx-font-size: 13px; -fx-font-weight: bold;" +
        "-fx-background-radius: 6; -fx-cursor: hand;";

    public static final String STYLE_BTN_SECONDARY =
        "-fx-background-color: white; -fx-text-fill: #374151;" +
        "-fx-font-size: 13px; -fx-border-color: #d1d5db;" +
        "-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;";

    public static final String STYLE_BTN_DANGER =
        "-fx-background-color: #ef4444; -fx-text-fill: white;" +
        "-fx-font-size: 13px; -fx-background-radius: 6; -fx-cursor: hand;";

    // ── Styles navbar ─────────────────────────────────────────

    public static final String STYLE_NAVBAR =
        "-fx-background-color: #1e40af;";

    public static final String STYLE_NAV_BTN =
        "-fx-background-color: transparent; -fx-text-fill: white;" +
        "-fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 4 10;";

    public static final String STYLE_NAV_DECONNEXION =
        "-fx-background-color: transparent; -fx-text-fill: #fca5a5;" +
        "-fx-font-size: 13px; -fx-cursor: hand;" +
        "-fx-border-color: #fca5a5; -fx-border-radius: 4; -fx-padding: 4 10;";

    // ── Styles cartes ─────────────────────────────────────────

    public static final String STYLE_CARTE =
        "-fx-background-color: white; -fx-background-radius: 8;" +
        "-fx-border-color: #e5e7eb; -fx-border-radius: 8;";

    // ── Méthodes utilitaires ──────────────────────────────────

    /**
     * Applique le style de champ standard à un Control.
     */
    public static void stylerChamp(Control champ) {
        champ.setStyle(STYLE_CHAMP);
        champ.setPrefHeight(36);
    }

    /**
     * Crée et retourne un Label d'erreur (caché par défaut).
     */
    public static Label creerLabelErreur() {
        Label l = new Label();
        l.setStyle(STYLE_ERR);
        l.setVisible(false);
        l.setManaged(false);
        return l;
    }

    /**
     * Affiche un message d'erreur dans un Label.
     */
    public static void afficherErreur(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    /**
     * Cache un Label d'erreur.
     */
    public static void cacherErreur(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}

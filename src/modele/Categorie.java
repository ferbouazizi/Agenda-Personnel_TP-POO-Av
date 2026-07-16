package modele;

/**
 * Classe modèle représentant une catégorie d'événement.
 * Correspond à la table CATEGORIE dans Oracle.
 */
public class Categorie {

    // ── Attributs ────────────────────────────────────────────
    private int    idCat;
    private String libelle;
    private String couleur;   // code couleur CSS ex: "#3B82F6"
    private String icone;     // nom d'icône ex: "briefcase", "heart"

    // ── Constructeurs ─────────────────────────────────────────

    public Categorie() {}

    public Categorie(int idCat, String libelle, String couleur, String icone) {
        this.idCat   = idCat;
        this.libelle = libelle;
        this.couleur = couleur;
        this.icone   = icone;
    }

    public Categorie(String libelle, String couleur, String icone) {
        this.libelle = libelle;
        this.couleur = couleur;
        this.icone   = icone;
    }

    // ── Getters / Setters ─────────────────────────────────────

    public int    getIdCat()          { return idCat; }
    public void   setIdCat(int idCat) { this.idCat = idCat; }

    public String getLibelle()               { return libelle; }
    public void   setLibelle(String libelle) { this.libelle = libelle; }

    public String getCouleur()               { return couleur; }
    public void   setCouleur(String couleur) { this.couleur = couleur; }

    public String getIcone()             { return icone; }
    public void   setIcone(String icone) { this.icone = icone; }

    // ── toString (affiché dans les ComboBox JavaFX) ───────────

    @Override
    public String toString() {
        return libelle;
    }
}

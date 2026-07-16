package modele;

import java.util.Date;

/**
 * Classe modèle représentant un événement de l'agenda.
 * Correspond à la table EVENEMENT dans Oracle.
 */
public class Evenement {

    // ── Attributs ────────────────────────────────────────────
    private int       idEven;
    private String    titre;
    private Date      dateEven;
    private String    heure;          // format "HH:MM"
    private String    description;
    private boolean   important;      // true = important
    private Date      dateCreation;

    // Clés étrangères (objets complets pour faciliter l'affichage)
    private Categorie categorie;
    private int       idUtil;

    // ── Constructeurs ─────────────────────────────────────────

    public Evenement() {}

    /** Constructeur complet (après lecture BDD) */
    public Evenement(int idEven, String titre, Date dateEven, String heure,
                     String description, boolean important, Date dateCreation,
                     Categorie categorie, int idUtil) {
        this.idEven       = idEven;
        this.titre        = titre;
        this.dateEven     = dateEven;
        this.heure        = heure;
        this.description  = description;
        this.important    = important;
        this.dateCreation = dateCreation;
        this.categorie    = categorie;
        this.idUtil       = idUtil;
    }

    /** Constructeur pour INSERT */
    public Evenement(String titre, Date dateEven, String heure,
                     String description, boolean important,
                     Categorie categorie, int idUtil) {
        this.titre       = titre;
        this.dateEven    = dateEven;
        this.heure       = heure;
        this.description = description;
        this.important   = important;
        this.categorie   = categorie;
        this.idUtil      = idUtil;
    }

    // ── Getters / Setters ─────────────────────────────────────

    public int     getIdEven()           { return idEven; }
    public void    setIdEven(int idEven) { this.idEven = idEven; }

    public String  getTitre()              { return titre; }
    public void    setTitre(String titre)  { this.titre = titre; }

    public Date    getDateEven()               { return dateEven; }
    public void    setDateEven(Date dateEven)  { this.dateEven = dateEven; }

    public String  getHeure()              { return heure; }
    public void    setHeure(String heure)  { this.heure = heure; }

    public String  getDescription()                    { return description; }
    public void    setDescription(String description)  { this.description = description; }

    public boolean isImportant()                  { return important; }
    public void    setImportant(boolean important) { this.important = important; }

    public Date    getDateCreation()                   { return dateCreation; }
    public void    setDateCreation(Date dateCreation)  { this.dateCreation = dateCreation; }

    public Categorie getCategorie()                    { return categorie; }
    public void      setCategorie(Categorie categorie) { this.categorie = categorie; }

    public int  getIdUtil()           { return idUtil; }
    public void setIdUtil(int idUtil) { this.idUtil = idUtil; }

    // ── toString ──────────────────────────────────────────────

    @Override
    public String toString() {
        return "[" + (important ? "⭐ " : "") + titre + "] " + dateEven + " " + heure;
    }
}

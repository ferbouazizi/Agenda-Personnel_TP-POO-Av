package modele;

import java.util.Date;

/**
 * Classe modèle représentant un utilisateur de l'application.
 * Correspond à la table UTILISATEUR dans Oracle.
 */
public class Utilisateur {

    // ── Attributs ────────────────────────────────────────────
    private int    idUtil;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;   // toujours stocker un hash (SHA-256)
    private Date   dateCreation;

    // ── Constructeurs ─────────────────────────────────────────

    /** Constructeur vide (utile pour JavaFX bindings) */
    public Utilisateur() {}

    /** Constructeur complet (utilisé après lecture en BDD) */
    public Utilisateur(int idUtil, String nom, String prenom,
                       String email, String motDePasse, Date dateCreation) {
        this.idUtil       = idUtil;
        this.nom          = nom;
        this.prenom       = prenom;
        this.email        = email;
        this.motDePasse   = motDePasse;
        this.dateCreation = dateCreation;
    }

    /** Constructeur sans id (utilisé pour un INSERT) */
    public Utilisateur(String nom, String prenom, String email, String motDePasse) {
        this.nom        = nom;
        this.prenom     = prenom;
        this.email      = email;
        this.motDePasse = motDePasse;
    }

    // ── Getters / Setters ─────────────────────────────────────

    public int    getIdUtil()       { return idUtil; }
    public void   setIdUtil(int id) { this.idUtil = id; }

    public String getNom()             { return nom; }
    public void   setNom(String nom)   { this.nom = nom; }

    public String getPrenom()               { return prenom; }
    public void   setPrenom(String prenom)  { this.prenom = prenom; }

    public String getEmail()              { return email; }
    public void   setEmail(String email)  { this.email = email; }

    public String getMotDePasse()                   { return motDePasse; }
    public void   setMotDePasse(String motDePasse)  { this.motDePasse = motDePasse; }

    public Date getDateCreation()                { return dateCreation; }
    public void setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }

    // ── toString ──────────────────────────────────────────────

    @Override
    public String toString() {
        return prenom + " " + nom + " <" + email + ">";
    }
}


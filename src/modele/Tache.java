package modele;

import java.util.Date;

/**
 * Classe modèle représentant une tâche de l'agenda.
 * Correspond à la table TACHE dans Oracle.
 * Une tâche peut être liée à un événement (optionnel).
 */
public class Tache {

    // ── Constantes statut (Kanban) ────────────────────────────
    public static final String STATUT_A_FAIRE  = "A_FAIRE";
    public static final String STATUT_EN_COURS = "EN_COURS";
    public static final String STATUT_TERMINE  = "TERMINE";

    // ── Constantes priorité ───────────────────────────────────
    public static final String PRIORITE_HAUTE   = "HAUTE";
    public static final String PRIORITE_MOYENNE = "MOYENNE";
    public static final String PRIORITE_BASSE   = "BASSE";

    // ── Attributs ────────────────────────────────────────────
    private int      idTache;
    private String   titre;
    private String   description;
    private Date     deadline;
    private String   priorite;      // HAUTE | MOYENNE | BASSE
    private String   statut;        // A_FAIRE | EN_COURS | TERMINE
    private Date     dateCreation;

    // Clés étrangères
    private Evenement evenement;    // peut être null
    private int       idUtil;

    // ── Constructeurs ─────────────────────────────────────────

    public Tache() {
        this.priorite = PRIORITE_MOYENNE;
        this.statut   = STATUT_A_FAIRE;
    }

    /** Constructeur complet (après lecture BDD) */
    public Tache(int idTache, String titre, String description,
                 Date deadline, String priorite, String statut,
                 Date dateCreation, Evenement evenement, int idUtil) {
        this.idTache      = idTache;
        this.titre        = titre;
        this.description  = description;
        this.deadline     = deadline;
        this.priorite     = priorite;
        this.statut       = statut;
        this.dateCreation = dateCreation;
        this.evenement    = evenement;
        this.idUtil       = idUtil;
    }

    /** Constructeur pour INSERT */
    public Tache(String titre, String description, Date deadline,
                 String priorite, String statut, Evenement evenement, int idUtil) {
        this.titre       = titre;
        this.description = description;
        this.deadline    = deadline;
        this.priorite    = priorite;
        this.statut      = statut;
        this.evenement   = evenement;
        this.idUtil      = idUtil;
    }

    // ── Getters / Setters ─────────────────────────────────────

    public int    getIdTache()             { return idTache; }
    public void   setIdTache(int idTache)  { this.idTache = idTache; }

    public String getTitre()               { return titre; }
    public void   setTitre(String titre)   { this.titre = titre; }

    public String getDescription()                   { return description; }
    public void   setDescription(String description) { this.description = description; }

    public Date   getDeadline()                { return deadline; }
    public void   setDeadline(Date deadline)   { this.deadline = deadline; }

    public String getPriorite()                { return priorite; }
    public void   setPriorite(String priorite) { this.priorite = priorite; }

    public String getStatut()              { return statut; }
    public void   setStatut(String statut) { this.statut = statut; }

    public Date   getDateCreation()                  { return dateCreation; }
    public void   setDateCreation(Date dateCreation) { this.dateCreation = dateCreation; }

    public Evenement getEvenement()                    { return evenement; }
    public void      setEvenement(Evenement evenement) { this.evenement = evenement; }

    public int  getIdUtil()           { return idUtil; }
    public void setIdUtil(int idUtil) { this.idUtil = idUtil; }

    // ── Méthodes utilitaires ──────────────────────────────────

    /** Retourne true si la tâche est en retard */
    public boolean isEnRetard() {
        return deadline != null
            && !statut.equals(STATUT_TERMINE)
            && deadline.before(new Date());
    }

    @Override
    public String toString() {
        return "[" + priorite + "] " + titre + " (" + statut + ")";
    }
}


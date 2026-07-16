package dao;

import modele.Evenement;
import modele.Categorie;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table EVENEMENT.
 * Contient le CRUD complet + recherches multi-critères.
 */
public class EvenementDAO {

    private Connection    conn;
    private CategorieDAO  categorieDAO;

    public EvenementDAO() {
        this.conn         = ConnexionDB.getInstance().getConnection();
        this.categorieDAO = new CategorieDAO();
    }

    // ── CREATE ────────────────────────────────────────────────

    /**
     * Insère un nouvel événement en BDD.
     * L'id est généré automatiquement par le trigger Oracle (SEQ_EVEN).
     * @return true si succès
     */
    public boolean inserer(Evenement e) {
        String sql = "INSERT INTO EVENEMENT "
                   + "(TITRE, DATE_EVEN, HEURE, DESCRIPTION, IMPORTANT, ID_CAT, ID_UTIL) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setDate(2, new java.sql.Date(e.getDateEven().getTime()));
            ps.setString(3, e.getHeure());
            ps.setString(4, e.getDescription());
            ps.setInt(5, e.isImportant() ? 1 : 0);
            // Catégorie optionnelle
            if (e.getCategorie() != null) {
                ps.setInt(6, e.getCategorie().getIdCat());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setInt(7, e.getIdUtil());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Erreur INSERT EVENEMENT : " + ex.getMessage());
            return false;
        }
    }

    // ── READ : tous les événements d'un utilisateur ───────────

    public List<Evenement> tousLesEvenements(int idUtil) {
        String sql = "SELECT e.*, c.ID_CAT, c.LIBELLE, c.COULEUR, c.ICONE "
                   + "FROM EVENEMENT e "
                   + "LEFT JOIN CATEGORIE c ON e.ID_CAT = c.ID_CAT "
                   + "WHERE e.ID_UTIL = ? "
                   + "ORDER BY e.DATE_EVEN, e.HEURE";
        return executerRequete(sql, idUtil);
    }

    // ── READ : événements importants à venir (7 jours) ───────

    public List<Evenement> evenementsImportantsAVenir(int idUtil) {
        String sql = "SELECT e.*, c.ID_CAT, c.LIBELLE, c.COULEUR, c.ICONE "
                   + "FROM EVENEMENT e "
                   + "LEFT JOIN CATEGORIE c ON e.ID_CAT = c.ID_CAT "
                   + "WHERE e.ID_UTIL = ? "
                   + "AND e.IMPORTANT = 1 "
                   + "AND e.DATE_EVEN BETWEEN SYSDATE AND SYSDATE + 7 "
                   + "ORDER BY e.DATE_EVEN";
        return executerRequete(sql, idUtil);
    }

    // ── READ : événements du jour ─────────────────────────────

    public List<Evenement> evenementsDuJour(int idUtil) {
        String sql = "SELECT e.*, c.ID_CAT, c.LIBELLE, c.COULEUR, c.ICONE "
                   + "FROM EVENEMENT e "
                   + "LEFT JOIN CATEGORIE c ON e.ID_CAT = c.ID_CAT "
                   + "WHERE e.ID_UTIL = ? "
                   + "AND TRUNC(e.DATE_EVEN) = TRUNC(SYSDATE) "
                   + "ORDER BY e.HEURE";
        return executerRequete(sql, idUtil);
    }

    // ── READ : recherche multi-critères ───────────────────────

    /**
     * Recherche flexible par titre, catégorie et/ou plage de dates.
     * Les paramètres null sont ignorés dans la requête.
     *
     * @param idUtil    identifiant de l'utilisateur connecté (obligatoire)
     * @param titre     texte recherché dans le titre (partiel, insensible à la casse)
     * @param idCat     filtrer par catégorie (0 = toutes)
     * @param dateDebut borne inférieure de date (null = pas de borne)
     * @param dateFin   borne supérieure de date (null = pas de borne)
     */
    public List<Evenement> rechercherMultiCriteres(int idUtil, String titre,
                                                    int idCat,
                                                    java.util.Date dateDebut,
                                                    java.util.Date dateFin) {
        StringBuilder sql = new StringBuilder(
            "SELECT e.*, c.ID_CAT AS CID, c.LIBELLE, c.COULEUR, c.ICONE "
          + "FROM EVENEMENT e "
          + "LEFT JOIN CATEGORIE c ON e.ID_CAT = c.ID_CAT "
          + "WHERE e.ID_UTIL = ? "
        );

        List<Object> params = new ArrayList<>();
        params.add(idUtil);

        if (titre != null && !titre.isEmpty()) {
            sql.append("AND UPPER(e.TITRE) LIKE UPPER(?) ");
            params.add("%" + titre + "%");
        }
        if (idCat > 0) {
            sql.append("AND e.ID_CAT = ? ");
            params.add(idCat);
        }
        if (dateDebut != null) {
            sql.append("AND e.DATE_EVEN >= ? ");
            params.add(new java.sql.Date(dateDebut.getTime()));
        }
        if (dateFin != null) {
            sql.append("AND e.DATE_EVEN <= ? ");
            params.add(new java.sql.Date(dateFin.getTime()));
        }
        sql.append("ORDER BY e.DATE_EVEN, e.HEURE");

        List<Evenement> liste = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) ps.setInt(i + 1, (Integer) p);
                else if (p instanceof java.sql.Date) ps.setDate(i + 1, (java.sql.Date) p);
                else ps.setString(i + 1, p.toString());
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(extraireEvenement(rs));
        } catch (SQLException e) {
            System.err.println("Erreur recherche événements : " + e.getMessage());
        }
        return liste;
    }

    // ── READ : par id ─────────────────────────────────────────

    public Evenement trouverParId(int idEven) {
        String sql = "SELECT e.*, c.ID_CAT, c.LIBELLE, c.COULEUR, c.ICONE "
                   + "FROM EVENEMENT e "
                   + "LEFT JOIN CATEGORIE c ON e.ID_CAT = c.ID_CAT "
                   + "WHERE e.ID_EVEN = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEven);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extraireEvenement(rs);
        } catch (SQLException e) {
            System.err.println("Erreur SELECT EVENEMENT par id : " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────

    public boolean modifier(Evenement e) {
        String sql = "UPDATE EVENEMENT SET TITRE=?, DATE_EVEN=?, HEURE=?, "
                   + "DESCRIPTION=?, IMPORTANT=?, ID_CAT=? WHERE ID_EVEN=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setDate(2, new java.sql.Date(e.getDateEven().getTime()));
            ps.setString(3, e.getHeure());
            ps.setString(4, e.getDescription());
            ps.setInt(5, e.isImportant() ? 1 : 0);
            if (e.getCategorie() != null) ps.setInt(6, e.getCategorie().getIdCat());
            else ps.setNull(6, Types.INTEGER);
            ps.setInt(7, e.getIdEven());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Erreur UPDATE EVENEMENT : " + ex.getMessage());
            return false;
        }
    }

    // ── DELETE ────────────────────────────────────────────────

    public boolean supprimer(int idEven) {
        // Les tâches liées passeront à ID_EVEN = NULL (ON DELETE SET NULL)
        String sql = "DELETE FROM EVENEMENT WHERE ID_EVEN = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEven);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur DELETE EVENEMENT : " + e.getMessage());
            return false;
        }
    }

    // ── Helpers privés ────────────────────────────────────────

    /** Exécute une requête avec un seul paramètre entier (idUtil). */
    private List<Evenement> executerRequete(String sql, int idUtil) {
        List<Evenement> liste = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtil);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(extraireEvenement(rs));
        } catch (SQLException e) {
            System.err.println("Erreur SELECT EVENEMENT : " + e.getMessage());
        }
        return liste;
    }

    /** Mappe une ligne ResultSet en objet Evenement (avec sa Categorie). */
    private Evenement extraireEvenement(ResultSet rs) throws SQLException {
        // Reconstruction de la catégorie (peut être null si ID_CAT est NULL)
        Categorie cat = null;
        int idCat = rs.getInt("ID_CAT");
        if (!rs.wasNull()) {
            cat = new Categorie(
                idCat,
                rs.getString("LIBELLE"),
                rs.getString("COULEUR"),
                rs.getString("ICONE")
            );
        }

        return new Evenement(
            rs.getInt("ID_EVEN"),
            rs.getString("TITRE"),
            rs.getDate("DATE_EVEN"),
            rs.getString("HEURE"),
            rs.getString("DESCRIPTION"),
            rs.getInt("IMPORTANT") == 1,
            rs.getDate("DATE_CREATION"),
            cat,
            rs.getInt("ID_UTIL")
        );
    }
}

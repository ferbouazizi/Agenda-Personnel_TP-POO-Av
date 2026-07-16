package dao;

import modele.Tache;
import modele.Evenement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table TACHE.
 * CRUD complet + filtrage Kanban par statut et priorité.
 */
public class TacheDAO {

    private Connection   conn;
    private EvenementDAO evenementDAO;

    public TacheDAO() {
        this.conn         = ConnexionDB.getInstance().getConnection();
        this.evenementDAO = new EvenementDAO();
    }

    // ── CREATE ────────────────────────────────────────────────

    /**
     * Insère une nouvelle tâche.
     * @return true si succès
     */
    public boolean inserer(Tache t) {
        String sql = "INSERT INTO TACHE "
                   + "(TITRE, DESCRIPTION, DEADLINE, PRIORITE, STATUT, ID_EVEN, ID_UTIL) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTitre());
            ps.setString(2, t.getDescription());
            if (t.getDeadline() != null)
                ps.setDate(3, new java.sql.Date(t.getDeadline().getTime()));
            else
                ps.setNull(3, Types.DATE);
            ps.setString(4, t.getPriorite());
            ps.setString(5, t.getStatut());
            if (t.getEvenement() != null)
                ps.setInt(6, t.getEvenement().getIdEven());
            else
                ps.setNull(6, Types.INTEGER);
            ps.setInt(7, t.getIdUtil());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur INSERT TACHE : " + e.getMessage());
            return false;
        }
    }

    // ── READ : toutes les tâches d'un utilisateur ─────────────

    public List<Tache> toutesLesTaches(int idUtil) {
        String sql = "SELECT * FROM TACHE WHERE ID_UTIL = ? "
                   + "ORDER BY CASE PRIORITE "
                   + "  WHEN 'HAUTE' THEN 1 WHEN 'MOYENNE' THEN 2 ELSE 3 END, "
                   + "DEADLINE NULLS LAST";
        return executerRequete(sql, idUtil);
    }

    // ── READ : tâches par statut (vue Kanban) ─────────────────

    /**
     * Retourne les tâches filtrées par statut — utilisé pour chaque colonne Kanban.
     * @param statut  Tache.STATUT_A_FAIRE | STATUT_EN_COURS | STATUT_TERMINE
     */
    public List<Tache> parStatut(int idUtil, String statut) {
        String sql = "SELECT * FROM TACHE WHERE ID_UTIL = ? AND STATUT = ? "
                   + "ORDER BY CASE PRIORITE "
                   + "  WHEN 'HAUTE' THEN 1 WHEN 'MOYENNE' THEN 2 ELSE 3 END, "
                   + "DEADLINE NULLS LAST";
        List<Tache> liste = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtil);
            ps.setString(2, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(extraireTache(rs));
        } catch (SQLException e) {
            System.err.println("Erreur SELECT TACHE par statut : " + e.getMessage());
        }
        return liste;
    }

    // ── READ : tâches en retard ───────────────────────────────

    public List<Tache> tachesEnRetard(int idUtil) {
        String sql = "SELECT * FROM TACHE "
                   + "WHERE ID_UTIL = ? "
                   + "AND STATUT != 'TERMINE' "
                   + "AND DEADLINE < SYSDATE "
                   + "ORDER BY DEADLINE";
        return executerRequete(sql, idUtil);
    }

    // ── UPDATE : modifier une tâche ───────────────────────────

    public boolean modifier(Tache t) {
        String sql = "UPDATE TACHE SET TITRE=?, DESCRIPTION=?, DEADLINE=?, "
                   + "PRIORITE=?, STATUT=?, ID_EVEN=? WHERE ID_TACHE=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTitre());
            ps.setString(2, t.getDescription());
            if (t.getDeadline() != null)
                ps.setDate(3, new java.sql.Date(t.getDeadline().getTime()));
            else
                ps.setNull(3, Types.DATE);
            ps.setString(4, t.getPriorite());
            ps.setString(5, t.getStatut());
            if (t.getEvenement() != null)
                ps.setInt(6, t.getEvenement().getIdEven());
            else
                ps.setNull(6, Types.INTEGER);
            ps.setInt(7, t.getIdTache());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur UPDATE TACHE : " + e.getMessage());
            return false;
        }
    }

    // ── UPDATE : changer le statut uniquement (Kanban drag) ───

    /**
     * Met à jour uniquement le statut d'une tâche.
     * Utilisé lorsque l'utilisateur déplace une carte dans la vue Kanban.
     */
    public boolean changerStatut(int idTache, String nouveauStatut) {
        String sql = "UPDATE TACHE SET STATUT = ? WHERE ID_TACHE = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nouveauStatut);
            ps.setInt(2, idTache);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur UPDATE STATUT : " + e.getMessage());
            return false;
        }
    }

    // ── DELETE ────────────────────────────────────────────────

    public boolean supprimer(int idTache) {
        String sql = "DELETE FROM TACHE WHERE ID_TACHE = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTache);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur DELETE TACHE : " + e.getMessage());
            return false;
        }
    }

    // ── Helpers privés ────────────────────────────────────────

    private List<Tache> executerRequete(String sql, int idUtil) {
        List<Tache> liste = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtil);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(extraireTache(rs));
        } catch (SQLException e) {
            System.err.println("Erreur SELECT TACHE : " + e.getMessage());
        }
        return liste;
    }

    /** Mappe une ligne ResultSet en objet Tache. */
    private Tache extraireTache(ResultSet rs) throws SQLException {
        // Récupération de l'événement lié (optionnel)
        Evenement evenement = null;
        int idEven = rs.getInt("ID_EVEN");
        if (!rs.wasNull()) {
            evenement = evenementDAO.trouverParId(idEven);
        }

        return new Tache(
            rs.getInt("ID_TACHE"),
            rs.getString("TITRE"),
            rs.getString("DESCRIPTION"),
            rs.getDate("DEADLINE"),
            rs.getString("PRIORITE"),
            rs.getString("STATUT"),
            rs.getDate("DATE_CREATION"),
            evenement,
            rs.getInt("ID_UTIL")
        );
    }
}

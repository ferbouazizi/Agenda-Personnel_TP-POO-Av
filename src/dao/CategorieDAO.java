package dao;

import modele.Categorie;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table CATEGORIE.
 * Opérations CRUD complètes.
 */
public class CategorieDAO {

    private Connection conn;

    public CategorieDAO() {
        this.conn = ConnexionDB.getInstance().getConnection();
    }

    // ── CREATE ────────────────────────────────────────────────

    public boolean inserer(Categorie c) {
        String sql = "INSERT INTO CATEGORIE (LIBELLE, COULEUR, ICONE) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getLibelle());
            ps.setString(2, c.getCouleur());
            ps.setString(3, c.getIcone());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur INSERT CATEGORIE : " + e.getMessage());
            return false;
        }
    }

    // ── READ : toutes les catégories ─────────────────────────

    /**
     * Retourne toutes les catégories — utilisé pour remplir les ComboBox JavaFX.
     */
    public List<Categorie> toutesLesCategories() {
        List<Categorie> liste = new ArrayList<>();
        String sql = "SELECT * FROM CATEGORIE ORDER BY LIBELLE";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(extraireCategorie(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur SELECT CATEGORIE : " + e.getMessage());
        }
        return liste;
    }

    // ── READ : par id ─────────────────────────────────────────

    public Categorie trouverParId(int idCat) {
        String sql = "SELECT * FROM CATEGORIE WHERE ID_CAT = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCat);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extraireCategorie(rs);
        } catch (SQLException e) {
            System.err.println("Erreur SELECT CATEGORIE par id : " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────

    public boolean modifier(Categorie c) {
        String sql = "UPDATE CATEGORIE SET LIBELLE=?, COULEUR=?, ICONE=? WHERE ID_CAT=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getLibelle());
            ps.setString(2, c.getCouleur());
            ps.setString(3, c.getIcone());
            ps.setInt(4, c.getIdCat());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur UPDATE CATEGORIE : " + e.getMessage());
            return false;
        }
    }

    // ── DELETE ────────────────────────────────────────────────

    public boolean supprimer(int idCat) {
        // Les événements liés passeront à ID_CAT = NULL (ON DELETE SET NULL)
        String sql = "DELETE FROM CATEGORIE WHERE ID_CAT = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCat);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur DELETE CATEGORIE : " + e.getMessage());
            return false;
        }
    }

    // ── Mapper ResultSet → Categorie ─────────────────────────

    private Categorie extraireCategorie(ResultSet rs) throws SQLException {
        return new Categorie(
            rs.getInt("ID_CAT"),
            rs.getString("LIBELLE"),
            rs.getString("COULEUR"),
            rs.getString("ICONE")
        );
    }
}


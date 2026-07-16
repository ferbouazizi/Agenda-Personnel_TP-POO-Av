package dao;

import modele.Utilisateur;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * DAO (Data Access Object) pour la table UTILISATEUR.
 * Contient toutes les opérations JDBC : connexion, inscription, lecture.
 */
public class UtilisateurDAO {

    private Connection conn;

    public UtilisateurDAO() {
        this.conn = ConnexionDB.getInstance().getConnection();
    }

    // ── Hachage SHA-256 ───────────────────────────────────────

    /**
     * Hache un mot de passe en SHA-256.
     * NE JAMAIS stocker un mot de passe en clair dans la BDD.
     */
    public static String hacherMDP(String motDePasse) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(motDePasse.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 non disponible", e);
        }
    }

    // ── CREATE ────────────────────────────────────────────────

    /**
     * Insère un nouvel utilisateur en BDD.
     * Le mot de passe est haché automatiquement avant insertion.
     * @return true si succès
     */
    public boolean inserer(Utilisateur u) {
        String sql = "INSERT INTO UTILISATEUR (NOM, PRENOM, EMAIL, MOT_DE_PASSE) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, hacherMDP(u.getMotDePasse())); // hash avant insertion
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur INSERT UTILISATEUR : " + e.getMessage());
            return false;
        }
    }

    // ── READ : Authentification ───────────────────────────────

    /**
     * Vérifie les identifiants de connexion.
     * @return l'Utilisateur si authentifié, null sinon
     */
    public Utilisateur authentifier(String email, String motDePasse) {
        String sql = "SELECT * FROM UTILISATEUR WHERE EMAIL = ? AND MOT_DE_PASSE = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, hacherMDP(motDePasse));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return extraireUtilisateur(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur authentification : " + e.getMessage());
        }
        return null; // identifiants incorrects
    }

    // ── READ : Recherche par email ────────────────────────────

    /**
     * Cherche un utilisateur par son email.
     * @return Utilisateur ou null si inexistant
     */
    public Utilisateur trouverParEmail(String email) {
        String sql = "SELECT * FROM UTILISATEUR WHERE EMAIL = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extraireUtilisateur(rs);
        } catch (SQLException e) {
            System.err.println("Erreur SELECT UTILISATEUR : " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────

    /**
     * Met à jour les informations d'un utilisateur (sans changer le mdp).
     */
    public boolean modifier(Utilisateur u) {
        String sql = "UPDATE UTILISATEUR SET NOM=?, PRENOM=?, EMAIL=? WHERE ID_UTIL=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setInt(4, u.getIdUtil());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur UPDATE UTILISATEUR : " + e.getMessage());
            return false;
        }
    }

    /**
     * Change le mot de passe d'un utilisateur (haché automatiquement).
     */
    public boolean changerMotDePasse(int idUtil, String nouveauMdp) {
        String sql = "UPDATE UTILISATEUR SET MOT_DE_PASSE=? WHERE ID_UTIL=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hacherMDP(nouveauMdp));
            ps.setInt(2, idUtil);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur changement MDP : " + e.getMessage());
            return false;
        }
    }

    // ── DELETE ────────────────────────────────────────────────

    /**
     * Supprime un utilisateur (CASCADE supprimera ses événements et tâches).
     */
    public boolean supprimer(int idUtil) {
        String sql = "DELETE FROM UTILISATEUR WHERE ID_UTIL = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUtil);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur DELETE UTILISATEUR : " + e.getMessage());
            return false;
        }
    }

    // ── Méthode privée : mapper ResultSet → Utilisateur ──────

    private Utilisateur extraireUtilisateur(ResultSet rs) throws SQLException {
        return new Utilisateur(
            rs.getInt("ID_UTIL"),
            rs.getString("NOM"),
            rs.getString("PRENOM"),
            rs.getString("EMAIL"),
            rs.getString("MOT_DE_PASSE"),
            rs.getDate("DATE_CREATION")
        );
    }
}

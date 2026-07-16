package dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Classe Singleton gerant la connexion JDBC vers Oracle.
 *
 * CONFIGURATION :
 *   - Les identifiants ne sont PLUS codes en dur ici (bonne pratique de securite).
 *   - Ils sont lus depuis le fichier "config/db.properties" a la racine du projet.
 *   - Un modele "config/db.properties.example" est fourni : copiez-le en
 *     "config/db.properties" et renseignez vos propres identifiants.
 *   - "config/db.properties" est ignore par git (voir .gitignore) donc vos
 *     identifiants ne seront jamais poussés sur GitHub.
 *
 * UTILISATION dans un DAO :
 *   Connection conn = ConnexionDB.getInstance().getConnection();
 */
public class ConnexionDB {

    private static final String CONFIG_PATH = "config/db.properties";

    private static ConnexionDB instance;
    private Connection connection;

    private ConnexionDB() {
        Properties props = new Properties();

        try (InputStream input = new FileInputStream(CONFIG_PATH)) {
            props.load(input);
        } catch (IOException e) {
            System.err.println("Fichier de configuration introuvable : " + CONFIG_PATH);
            System.err.println("-> Copiez config/db.properties.example vers config/db.properties");
            System.err.println("   puis renseignez vos identifiants Oracle.");
            return;
        }

        String url      = props.getProperty("db.url");
        String user     = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            this.connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion Oracle etablie avec succes.");

        } catch (ClassNotFoundException e) {
            System.err.println("Driver Oracle introuvable. Verifiez ojdbc8.jar dans les Libraries.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Impossible de se connecter a Oracle. Verifiez URL / USER / PASSWORD dans " + CONFIG_PATH);
            e.printStackTrace();
        }
    }

    /**
     * Retourne l'instance unique de ConnexionDB (pattern Singleton).
     * Reconnecte automatiquement si la connexion est fermee.
     */
    public static ConnexionDB getInstance() {
        try {
            if (instance == null || instance.getConnection() == null
                    || instance.getConnection().isClosed()) {
                instance = new ConnexionDB();
            }
        } catch (SQLException e) {
            instance = new ConnexionDB();
        }
        return instance;
    }

    /**
     * Retourne l'objet Connection JDBC actif.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Ferme proprement la connexion (a appeler a la fermeture de l'application).
     */
    public void fermer() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connexion Oracle fermee.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

package agendatp;

import dao.ConnexionDB;
import javafx.application.Application;
import javafx.stage.Stage;
import vue.LoginVue;

/**
 * Point d'entrée de l'application Agenda Personnel.
 * Démarre l'écran de connexion (LoginVue) au lancement.
 *
 * Prérequis NetBeans :
 *   - ojdbc11.jar dans Libraries du projet
 *   - Oracle XE démarré sur localhost:1521
 *   - Modifier ConnexionDB.java si USER/PASSWORD différents
 */
public class main extends Application {

    @Override
    public void start(Stage primaryStage) {
        new LoginVue().afficher(primaryStage);
    }

    @Override
    public void stop() throws Exception {
        ConnexionDB.getInstance().fermer();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

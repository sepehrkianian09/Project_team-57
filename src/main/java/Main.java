import model.db.ProductTable;
import view.menu.Menu;
import view.process.Processor;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        Processor.initProcessHashMaps();
        Menu menu = Menu.makeMenu("Main Menu");

        while(menu != null) {
            menu.show();
            menu = menu.execute();
        }

    }
}

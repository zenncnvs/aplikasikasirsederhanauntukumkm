
package kasir_umkm;

import java.sql.Connection;
import java.sql.DriverManager;
import javax.swing.JOptionPane;


public class KONEKSI {

  
    String url = "jdbc:mysql://localhost:3306/kasir_umkm?serverTimezone=UTC";
    String username = "root";
    String password = "";
    Connection con;
   
    public void connect(){
        try {
          con = DriverManager.getConnection(url, username, password);
            System.out.println("Koneksi Berhasil");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        }

    public Connection getCon() {
        return con;
    }  
}


package gui;

import cnx.Connex;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class Fenetre extends JFrame {
    Connection connection;
    Contentpane contentpane;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Contentpane getContentpane() {
        return contentpane;
    }

    public void setContentpane(Contentpane contentpane) {
        this.contentpane = contentpane;
    }

    public Fenetre(){
        Font font = new Font("Roboto",Font.PLAIN, 14);
        this.setFont(font);
        this.setTitle("Class Generator - Sanda");
        this.setSize(640,480);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e){
            e.printStackTrace();
        }
        UIManager.put("OptionPane.messageFont", font);
        UIManager.put("OptionPane.buttonFont", font);

        this.setVisible(true);

        try{
            /* --- Ouvrir une Connection pour la fenetre --- */
            this.setConnection(Connex.getConnection());
        }catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JFrame(),"Veuillez verifier les informations de DB.xml","Erreur de connexion",JOptionPane.ERROR_MESSAGE);
        }
        /* --------------------------------------------- */
        this.setContentpane(new Contentpane(this));
        this.setContentPane(this.getContentpane());

        this.revalidate();
    }
    public static void main(String[] args) {
        new Fenetre();
    }
}

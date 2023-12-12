package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

public class Table extends JLabel implements MouseListener {
    Contentpane contentpane;
    String nomTable;
    Color defaultColor;

    public Contentpane getContentpane() {
        return contentpane;
    }
    public void setContentpane(Contentpane contentpane) {
        this.contentpane = contentpane;
    }

    public String getNomTable() {
        return nomTable;
    }
    public void setNomTable(String nomTable) {
        this.nomTable = nomTable;
    }

    public Color getDefaultColor() {
        return defaultColor;
    }
    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public Table(){

    }

    public Table(String text, Color defaultColor,Contentpane contentpane){
        super(text.replaceFirst(text.substring(0,1), text.substring(0,1).toUpperCase()));
        this.setDefaultColor(defaultColor);
        this.setContentpane(contentpane);
        this.setNomTable(text);

        this.setBackground(this.getDefaultColor());

        this.addMouseListener(this);
    }

    public Table(String text,String nomTable, Color defaultColor, Contentpane contentpane){
        super(text.replaceFirst(text.substring(0,1), text.substring(0,1).toUpperCase()));
        this.setDefaultColor(defaultColor);
        this.setContentpane(contentpane);
        this.setNomTable(nomTable);

        this.setBackground(this.getDefaultColor());

        this.addMouseListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.setForeground(Color.BLUE);
        if(e.getSource().equals(this)){
            try {
                String utilsPath = this.getContentpane().utilsPath;
                String language = (String) this.getContentpane().getLanguages().getSelectedItem();
                File extensionFile = new File(utilsPath + "/" + language.toLowerCase() + "/" + language.toLowerCase() + "Extension.txt");
                String extension = Files.readAllLines(Paths.get(extensionFile.getCanonicalPath())).get(0);

                this.getContentpane().setListeColonne(utilsPath, this.getNomTable(), language);
                String classname = this.getContentpane().getTableName();
                classname = classname.replaceFirst(classname.substring(0,1), classname.substring(0,1).toUpperCase());
                this.getContentpane().getBottomLayout().getClassNameField().setTextColor(Color.BLACK);
                this.getContentpane().getBottomLayout().getClassNameField().setText(classname + "." + extension);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(new JFrame(),ex.getMessage(),"Erreur Liste de colonne",JOptionPane.ERROR_MESSAGE);

            }
        }
        this.getContentpane().revalidate();
        this.getContentpane().repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.setForeground(new Color(38,38,38));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if(e.getSource().equals(this)){
            this.setBackground(Color.LIGHT_GRAY);
            this.setForeground(new Color(38,38,38));

            this.getContentpane().revalidate();
            this.getContentpane().repaint();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if(e.getSource().equals(this)){
            this.setBackground(this.getDefaultColor());

            this.getContentpane().revalidate();
            this.getContentpane().repaint();
        }
    }
}

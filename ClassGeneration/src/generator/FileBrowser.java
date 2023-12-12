package generator;

import gui.Contentpane;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FileBrowser extends JFrame{
    Contentpane contentpane;
    JFileChooser fileChooser;

    public Contentpane getContentpane() {
        return contentpane;
    }
    public void setContentpane(Contentpane contentpane) {
        this.contentpane = contentpane;
    }

    public JFileChooser getFileChooser() {
        return fileChooser;
    }

    public void setFileChooser(JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    public FileBrowser(int width, int height, Contentpane contentpane){
        Font font = new Font("Roboto",Font.PLAIN, 14);
        this.setFont(font);
        this.setTitle("Choisir le chemin de la nouvelle classe");
        this.setSize(width,height);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e){
            e.printStackTrace();
        }
        this.setContentpane(contentpane);
        this.setFileChooser(new JFileChooser());
        customizeFileChooser(this.getFileChooser());

        this.setContentPane(this.getFileChooser());
        this.setVisible(true);
    }

    private void customizeFileChooser(JFileChooser jfc){
        jfc.setDialogTitle("Choisir le chemin du dossier");
        jfc.setCurrentDirectory(new File(System.getProperty("user.home")));

        // Show the file chooser dialog
        int result = jfc.showOpenDialog(null);

        // Handle the result
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
        } else {
            System.out.println("File selection canceled.");
        }
    }
}

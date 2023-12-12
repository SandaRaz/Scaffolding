package gui.listener;

import generator.ClassGenerator;
import generator.TypeAndName;
import gui.Board;
import gui.Contentpane;
import gui.surdef.Bouton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class EcouteSouris implements MouseListener {
    Component listened;

    public Component getListened() {
        return listened;
    }
    public void setListened(Component listened) {
        this.listened = listened;
    }

    public EcouteSouris(Component c){
        this.setListened(c);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(this.getListened() instanceof Board board){
            if(e.getSource() == board.getChoosePath()){
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setDialogTitle("Choisir le chemin du dossier");

                File lastPath = new File("./src/gui/folder.dir");
                String lastChoosenFolder = "";
                try {
                    if(!lastPath.exists()){
                        boolean create = lastPath.createNewFile();
                    }
                    List<String> lines = Files.readAllLines(Paths.get(lastPath.getCanonicalPath()));
                    if(lines.size() > 0){
                        lastChoosenFolder = lines.get(0);
                    }
                    System.out.println("LastChoosenFolder: "+lastChoosenFolder);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                String currentPath = board.getChoosedPath().getText();
                if(!currentPath.equals("...") && !currentPath.equals("")){
                    fileChooser.setCurrentDirectory(new File(currentPath));
                }else{
                    if(!lastChoosenFolder.equals("")){
                        fileChooser.setCurrentDirectory(new File(lastChoosenFolder));
                    }else{
                        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                    }
                }

                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                    board.getChoosedPath().setForeground(Color.BLACK);
                    board.getChoosedPath().setText(selectedFile.getAbsolutePath());

                    try {
                        Files.write(Path.of(lastPath.getCanonicalPath()), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                        Files.write(Path.of(lastPath.getCanonicalPath()), selectedFile.getCanonicalPath().getBytes(), StandardOpenOption.APPEND);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    System.out.println("File selection canceled.");
                }
            }else if(e.getSource() == board.getGenerateButton()){
                Contentpane c = board.getContentpane();

                String utilsPath = c.utilsPath;
                String generatedPath = board.getChoosedPath().getText();
                String tableName = c.getTableName();
                String className = board.getClassNameField().getText();
                String language = (String) board.getContentpane().getLanguages().getSelectedItem();
                String packageName = board.getPackageField().getText();
                List<TypeAndName> colonneChoisit = c.getListeColonneChoisit();

                int err = 0;
                if(generatedPath.isEmpty() || generatedPath.isBlank()){
                    err++;
                }
                if(board.getClassNameField().getForeground().equals(Color.LIGHT_GRAY)){
                    err++;
                }
                if(board.getPackageField().getForeground().equals(Color.LIGHT_GRAY)){
                    err++;
                }

                if(err != 0){
                    JOptionPane.showMessageDialog(new JFrame(),"Remplissez les cases vides","Case vide",JOptionPane.WARNING_MESSAGE);
                    throw new RuntimeException("Remplissez les cases vides");
                }

                try {
                    ClassGenerator.generateClass(c.getFenetre().getConnection(), utilsPath,generatedPath,tableName,className,language,packageName,colonneChoisit);
                    JOptionPane.showMessageDialog(new JFrame(),"Class générée dans "+generatedPath,"Génération réussie",JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(new JFrame(),ex.getMessage(),"Erreur de generation de code",JOptionPane.ERROR_MESSAGE);
                }
            }

        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}

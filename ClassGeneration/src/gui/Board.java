package gui;

import generator.FileBrowser;
import gui.listener.EcouteSouris;
import gui.surdef.Bouton;
import gui.surdef.InputText;
import gui.surdef.RoundBorder;
import gui.surdef.RoundedCornerBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

public class Board extends JPanel {
    Contentpane contentpane;
    InputText packageField;
    InputText classNameField;
    JLabel choosedPath;
    Bouton choosePath;
    Bouton generateButton;
    int radius;

    public Contentpane getContentpane() {
        return contentpane;
    }
    public void setContentpane(Contentpane contentpane) {
        this.contentpane = contentpane;
    }

    public InputText getPackageField() {
        return packageField;
    }
    public void setPackageField(InputText packageField) {
        this.packageField = packageField;
    }

    public InputText getClassNameField() {
        return classNameField;
    }
    public void setClassNameField(InputText classNameField) {
        this.classNameField = classNameField;
    }

    public JLabel getChoosedPath() {
        return choosedPath;
    }
    public void setChoosedPath(JLabel choosedPath) {
        this.choosedPath = choosedPath;
    }

    public Bouton getChoosePath() {
        return choosePath;
    }
    public void setChoosePath(Bouton choosePath) {
        this.choosePath = choosePath;
    }

    public Bouton getGenerateButton() {
        return generateButton;
    }
    public void setGenerateButton(Bouton generateButton) {
        this.generateButton = generateButton;
    }

    public int getRadius() {
        return radius;
    }
    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Board(int radius, Contentpane contentpane){
        Font font = new Font("Roboto", Font.PLAIN, 14);

        this.setContentpane(contentpane);
        this.setRadius(radius);
        Color champ = new Color(240,240,240);
        this.setPackageField(new InputText("com.example.mypackage","com.example.mypackage", Color.LIGHT_GRAY));
        this.setClassNameField(new InputText("Enter class name", "Enter class name", Color.LIGHT_GRAY));
        this.setChoosedPath(new JLabel());
        this.setChoosePath(new Bouton());
        this.setGenerateButton(new Bouton("Generate"));

        this.setPreferredSize(new Dimension(400,100));
        this.setBorder(new RoundBorder(radius, Color.LIGHT_GRAY));

        JLabel gauche = new JLabel();
        gauche.setPreferredSize(new Dimension(265,100));
        gauche.setLayout(new GridLayout(0,1,5,10));
        gauche.add(this.getPackageField());
        gauche.add(this.getClassNameField());

        this.getChoosedPath().setPreferredSize(new Dimension(265,40));
        this.getChoosedPath().setFont(font);
        this.getChoosedPath().setText("...");
        this.getChoosedPath().setHorizontalAlignment(SwingConstants.LEFT);
        this.getChoosedPath().setForeground(Color.LIGHT_GRAY);
        this.getChoosedPath().setBorder(new RoundedCornerBorder());
        //this.getChoosePath().setPreferredSize(new Dimension(50,40));

        this.getGenerateButton().setFont(font);

            JLabel chooseFolder = new JLabel();
            chooseFolder.setLayout(new BorderLayout(3,0));
            chooseFolder.add(this.getChoosedPath(), BorderLayout.WEST);
            chooseFolder.add(this.getChoosePath(), BorderLayout.EAST);

                JLabel confirm = new JLabel();
                confirm.setLayout(new GridLayout(0,2));
                confirm.add(new JLabel());
                confirm.add(this.getGenerateButton());

        JLabel droite = new JLabel();
        droite.setPreferredSize(new Dimension(305,100));
        droite.setLayout(new GridLayout(0,1,5,10));
        droite.add(chooseFolder);
        droite.add(confirm);

        this.setLayout(new BorderLayout());
        this.add(gauche, BorderLayout.WEST);
        this.add(droite, BorderLayout.EAST);

        this.getChoosePath().addMouseListener(new EcouteSouris(this));
        this.getGenerateButton().addMouseListener(new EcouteSouris(this));
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(Color.WHITE);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.fillRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);
        g2d.dispose();
    }

    public void addFieldListener(){
        this.getPackageField().addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
    }
}

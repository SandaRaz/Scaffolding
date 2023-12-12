package gui;

import gui.surdef.RoundBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

public class Column extends JLabel implements MouseListener {
    public static File assets = new File("./assets");
    Contentpane contentpane;
    String colName;
    String colType;
    JLabel columnName;
    JLabel columnType;
    JCheckBox checkBox;
    Color defaultColor;
    int radius;
    boolean checkAll;

    public Contentpane getContentpane() {
        return contentpane;
    }
    public void setContentpane(Contentpane contentpane) {
        this.contentpane = contentpane;
    }

    public String getColName() {
        return colName;
    }
    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getColType() {
        return colType;
    }
    public void setColType(String colType) {
        this.colType = colType;
    }

    public JLabel getColumnName() {
        return columnName;
    }
    public void setColumnName(JLabel columnName) {
        this.columnName = columnName;
    }

    public JLabel getColumnType() {
        return columnType;
    }
    public void setColumnType(JLabel columnType) {
        this.columnType = columnType;
    }

    public JCheckBox getCheckBox() {
        return checkBox;
    }
    public void setCheckBox(JCheckBox checkBox) {
        this.checkBox = checkBox;
    }

    public boolean isCheckAll() {
        return checkAll;
    }
    public void setCheckAll(boolean checkAll) {
        this.checkAll = checkAll;
    }

    public Color getDefaultColor() {
        return defaultColor;
    }
    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public Column(String name,String type, Color defaultColor,Contentpane contentpane, int width,int height,int radius, boolean checkAll){
        Font font = new Font("Roboto", Font.PLAIN, 12);

        this.setDefaultColor(defaultColor);
        this.setContentpane(contentpane);
        this.setColName(name);
        this.setColType(type);
        this.setColumnName(new JLabel(name.replaceFirst(name.substring(0,1), name.substring(0,1).toUpperCase())));
        this.setColumnType(new JLabel(type));
        this.setCheckBox(new JCheckBox());
        this.setCheckAll(checkAll);

        this.radius = radius;
        customizeCheckBox(this.getCheckBox());

        this.setFont(font);
        this.setOpaque(false);
        this.setBorder(new RoundBorder(this.radius, getDefaultColor()));
        this.setPreferredSize(new Dimension(width,height));

        this.getCheckBox().setBackground(getDefaultColor());
        this.getColumnName().setPreferredSize(new Dimension(100, height));
        this.getColumnName().setFont(font);

        this.getColumnType().setPreferredSize(new Dimension(100, height));
        this.getColumnType().setFont(font);
        this.getColumnType().setForeground(Color.LIGHT_GRAY);

        /* --- Marging des texte --- */
        this.getColumnName().setBorder(BorderFactory.createCompoundBorder(this.getColumnName().getBorder(), new EmptyBorder(0, 10, 0, 0)));
        this.getColumnType().setBorder(BorderFactory.createCompoundBorder(this.getColumnName().getBorder(), new EmptyBorder(0, 0, 0, 10)));
        this.getColumnType().setHorizontalAlignment(SwingConstants.RIGHT);

        this.setLayout(new BorderLayout());
        this.add(this.getColumnName(), BorderLayout.WEST);
        this.add(this.getColumnType(),BorderLayout.CENTER);
        this.add(this.getCheckBox(), BorderLayout.EAST);

        this.getCheckBox().addMouseListener(this);
        this.addMouseListener(this);
    }

    private static void customizeCheckBox(JCheckBox checkBox){
        ImageIcon customUncheckedIcon = null;
        ImageIcon customCheckedIcon = null;
        try {
            customUncheckedIcon = new ImageIcon(assets.getCanonicalPath()+"/img/unchecked.png");
            customCheckedIcon = new ImageIcon(assets.getCanonicalPath()+"/img/checked.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        customUncheckedIcon = new ImageIcon(customUncheckedIcon.getImage().getScaledInstance(16,16,Image.SCALE_SMOOTH));
        customCheckedIcon = new ImageIcon(customCheckedIcon.getImage().getScaledInstance(16,16,Image.SCALE_SMOOTH));

        checkBox.setIcon(customUncheckedIcon);
        checkBox.setSelectedIcon(customCheckedIcon);

        checkBox.setFont(new Font("Roboto", Font.PLAIN, 14));
        checkBox.setForeground(new Color(50,50,50));

        MouseListener[] listeners = checkBox.getMouseListeners();
        for(MouseListener listener : listeners){
            checkBox.removeMouseListener(listener);
        }
    }

    /* --------------- SURDEFINITION --------------- */

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(this.getDefaultColor());
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.fillRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);
        g2d.dispose();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Color hoverColor = Color.LIGHT_GRAY;
        if(e.getSource() instanceof JCheckBox newCheckBox){
            Column parent = (Column) newCheckBox.getParent();

            parent.setDefaultColor(hoverColor);
            parent.getCheckBox().setBackground(hoverColor);

            /* --- check if not checked and inverse --- */
            parent.getCheckBox().setSelected(!this.getCheckBox().isSelected());
            /* --- si c'est pour tout selectionner --- */
            if(parent.checkAll){
                if(parent.getCheckBox().isSelected()){
                    for(Column col : parent.getContentpane().getColumns()){
                        col.getCheckBox().setSelected(true);
                    }
                }else{
                    for(Column col : parent.getContentpane().getColumns()){
                        col.getCheckBox().setSelected(false);
                    }
                }
            }else{
                /* --- Si tous les colonnes sont checkés --- */
                boolean allChecked = true;
                for(Column col : parent.getContentpane().getColumns()){
                    if(!col.getCheckBox().isSelected()){
                        allChecked = false;
                        break;
                    }
                }

                if(allChecked){
                    if(!parent.getContentpane().getCheckAll().getCheckBox().isSelected()){
                        parent.getContentpane().getCheckAll().getCheckBox().setSelected(true);
                    }
                    System.out.println("ALL CHECKED");
                }else{
                    if(parent.getContentpane().getCheckAll().getCheckBox().isSelected()){
                        parent.getContentpane().getCheckAll().getCheckBox().setSelected(false);
                    }
                }
            }
        }
        if(e.getSource().equals(this) && e.getSource() instanceof Column){
            this.setDefaultColor(hoverColor);
            this.getCheckBox().setBackground(hoverColor);

            /* --- check if not checked and inverse --- */
            this.getCheckBox().setSelected(!this.getCheckBox().isSelected());
            /* --- si c'est pour tout selectionner --- */
            if(this.checkAll){
                if(this.getCheckBox().isSelected()){
                    for(Column col : this.getContentpane().getColumns()){
                        col.getCheckBox().setSelected(true);
                    }
                }else{
                    for(Column col : this.getContentpane().getColumns()){
                        col.getCheckBox().setSelected(false);
                    }
                }
            }else{
                /* --- Si tous les colonnes sont checkés --- */
                boolean allChecked = true;
                for(Column col : this.getContentpane().getColumns()){
                    if(!col.getCheckBox().isSelected()){
                        allChecked = false;
                        break;
                    }
                }

                if(allChecked){
                    if(!this.getContentpane().getCheckAll().getCheckBox().isSelected()){
                        this.getContentpane().getCheckAll().getCheckBox().setSelected(true);
                    }
                    System.out.println("ALL CHECKED");
                }else{
                    if(this.getContentpane().getCheckAll().getCheckBox().isSelected()){
                        this.getContentpane().getCheckAll().getCheckBox().setSelected(false);
                    }
                }
            }
        }
        this.getContentpane().revalidate();
        this.getContentpane().repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(e.getSource().equals(this) || e.getSource().equals(this.getCheckBox())){
            Color defaultColor = new Color(240,240,240);
            Color checkAllColor = Color.WHITE;
            if(this.checkAll){
                this.setDefaultColor(checkAllColor);
                this.getCheckBox().setBackground(checkAllColor);
            }else{
                this.setDefaultColor(defaultColor);
                this.getCheckBox().setBackground(defaultColor);
            }

            this.getContentpane().revalidate();
            this.getContentpane().repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    /* --------------------------------------------- */
}

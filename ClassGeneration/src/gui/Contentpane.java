package gui;

import cnx.Connex;
import generator.ClassGenerator;
import generator.TypeAndName;
import gui.surdef.CustomScrollBarUI;
import gui.surdef.RoundBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Contentpane extends JPanel {
    public String utilsPath = "./src/template";
    Fenetre fenetre;
    JComboBox<String> languages;
    JPanel westLayout;
    JPanel eastLayout;
    Board bottomLayout;
    List<Table> tables = new ArrayList<>();
    Column checkAll;
    List<Column> columns = new ArrayList<>();
    String tableName;

    public Fenetre getFenetre() {
        return fenetre;
    }
    public void setFenetre(Fenetre fenetre) {
        this.fenetre = fenetre;
    }

    public JComboBox<String> getLanguages() {
        return languages;
    }
    public void setLanguages(JComboBox<String> languages) {
        this.languages = languages;
    }

    public JPanel getWestLayout() {
        return westLayout;
    }
    public void setWestLayout(JPanel westLayout) {
        this.westLayout = westLayout;
    }

    public JPanel getEastLayout() {
        return eastLayout;
    }
    public void setEastLayout(JPanel eastLayout) {
        this.eastLayout = eastLayout;
    }

    public Board getBottomLayout() {
        return bottomLayout;
    }
    public void setBottomLayout(Board bottomLayout) {
        this.bottomLayout = bottomLayout;
    }

    public List<Table> getTables() {
        return tables;
    }
    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public Column getCheckAll() {
        return checkAll;
    }
    public void setCheckAll(Column checkAll) {
        this.checkAll = checkAll;
    }

    public List<Column> getColumns() {
        return columns;
    }
    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Contentpane(){
        this.setBackground(Color.BLACK);
    }
    public Contentpane(Fenetre fenetre){
        this.setFenetre(fenetre);
        this.setPreferredSize(new Dimension(this.getFenetre().getWidth(), this.getFenetre().getHeight()));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        int radius = 15;

        /* ------- WEST ------- */
        this.setWestLayout(new JPanel());
        try {
            createWestLayoutTableListe();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        /* -------------------- */

        /* ------- EAST ------- */
        this.setEastLayout(new JPanel());
        try {
            createEastLayoutColumn();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        /* -------------------- */

        /* ------- BOTTOM ------- */
        this.setBottomLayout(new Board(15, this));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(this.getBottomLayout(), BorderLayout.CENTER);
        bottom.setBorder(BorderFactory.createEmptyBorder(15,10,10,10));
        /* ---------------------- */

        /* -------- NORTH -------- */
        File template = new File(this.utilsPath);
        File[] files = template.listFiles();
        Font roboto = new Font("Roboto", Font.PLAIN, 12);

        JLabel north = new JLabel();
        north.setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
        north.setBackground(Color.RED);
        north.setPreferredSize(new Dimension((int) this.getPreferredSize().getWidth(), 30));
        north.setLayout(new GridLayout(0,5,2,0));

        JLabel choixLang = new JLabel("Choix de language: ");
        choixLang.setFont(roboto);
        choixLang.setForeground(Color.gray);

        this.setLanguages(new JComboBox<String>());
        this.getLanguages().setFont(roboto);
        assert files != null;
        for(File file : files){
            if(file.isDirectory()){
                this.getLanguages().addItem(file.getName());
            }
        }

        north.add(choixLang);
        north.add(this.getLanguages());
        /* ----------------------- */

        /* --- Add every Layout panel ---- */
        this.setLayout(new BorderLayout());
        this.add(north, BorderLayout.NORTH);
        this.add(this.getWestLayout(), BorderLayout.WEST);
        this.add(this.getEastLayout(), BorderLayout.EAST);
        this.add(bottom, BorderLayout.SOUTH);
    }

    public void createWestLayoutTableListe() throws SQLException {
        Font font = new Font("Roboto", Font.PLAIN, 14);
        this.setFont(font);

        List<String> tables = Connex.getAllTable(this.getFenetre().getConnection());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        String[] couleurs = {"Blanc", "Gris"};
        for(int i=0 ; i< tables.size() ; i++){
            Color dc = couleurs[i%2].equals("Blanc") ? new Color(250,250,250) : new Color(235,235,235);

            Table tableLabel = new Table(tables.get(i), dc,this);
            tableLabel.setOpaque(true);
            tableLabel.setFont(font);
            tableLabel.setBorder(BorderFactory.createCompoundBorder(tableLabel.getBorder(), new EmptyBorder(0, 10,0,0)));
            tableLabel.setPreferredSize(new Dimension(100,35));
            this.getTables().add(tableLabel);
        }
        for(Table table : this.getTables()){
            panel.add(table);
        }

        int radius = 15;
        JScrollPane scrollPane = new JScrollPane(panel){
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(Color.WHITE);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.fillRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);
                g2d.dispose();
            }
        };
        scrollPane.setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8,0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.setPreferredSize(new Dimension(250,300));
        scrollPane.setBorder(new RoundBorder(radius, Color.LIGHT_GRAY));

        this.getWestLayout().setLayout(new BorderLayout());
        this.getWestLayout().add(scrollPane);
        this.getWestLayout().setBorder(BorderFactory.createEmptyBorder(10,10,10,5));
    }

    public void createEastLayoutColumn() throws SQLException {
        Font font = new Font("Roboto", Font.PLAIN, 14);
        this.setFont(font);
        int radius = 15;

        JPanel tempPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(Color.WHITE);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.fillRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);
                g2d.dispose();
            }
        };
        tempPanel.setOpaque(false);
        tempPanel.setPreferredSize(new Dimension((int)(this.getPreferredSize().getWidth() - this.getWestLayout().getPreferredSize().getWidth() - 45),300));
        tempPanel.setBorder(new RoundBorder(radius, Color.LIGHT_GRAY));

        this.getEastLayout().setLayout(new BorderLayout(0,5));
        this.getEastLayout().add(tempPanel);
        this.getEastLayout().setBorder(BorderFactory.createEmptyBorder(10,5,10,10));
    }

    public void setListeColonne(String utilsPath, String nomTable, String language) throws Exception {
        this.getEastLayout().removeAll();
        this.getColumns().clear();

        List<TypeAndName> columns = ClassGenerator.getFields(this.getFenetre().getConnection(), utilsPath, nomTable, language);

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 5));
        panel.setBackground(Color.WHITE);
        int radius = 15;

        Color colsColor = new Color(240,240,240);
        Color checkAllColor = Color.WHITE;

        this.checkAll = new Column("CHECK ALL", "", checkAllColor,this,100,40,10,true);
        panel.add(this.getCheckAll());
        for (TypeAndName column : columns) {
            Column col = new Column(column.getColumnName(), column.getColumnType(), colsColor,this,100,40, 10, false);

            this.getColumns().add(col);
        }
        for(Column col : this.getColumns()){
            panel.add(col);
        }

        JScrollPane scrollPane = new JScrollPane(panel){
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(Color.WHITE);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.fillRoundRect(0,0,getWidth()-1,getHeight()-1,radius,radius);
                g2d.dispose();
            }
        };
        scrollPane.setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8,0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.setPreferredSize(new Dimension((int)(this.getPreferredSize().getWidth() - this.getWestLayout().getPreferredSize().getWidth() - 45),270));
        scrollPane.setBorder(new RoundBorder(radius, Color.LIGHT_GRAY));

        this.setTableName(nomTable);
        JLabel tableLabel = new JLabel(this.getTableName().toUpperCase());
        tableLabel.setFont(new Font("Roboto",Font.BOLD,16));
        tableLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tableLabel.setVerticalAlignment(SwingConstants.TOP);
        tableLabel.setPreferredSize(new Dimension((int) scrollPane.getPreferredSize().getWidth(), 30));

        this.getEastLayout().add(tableLabel, BorderLayout.NORTH);
        this.getEastLayout().add(scrollPane, BorderLayout.SOUTH);
    }

    public List<TypeAndName> getListeColonneChoisit(){
        List<TypeAndName> colonnes = new ArrayList<>();

        for(Column col : this.getColumns()){
            if(col.getCheckBox().isSelected()){
                colonnes.add(new TypeAndName(col.getColName(), col.getColType()));
            }
        }
        return colonnes;
    }
}

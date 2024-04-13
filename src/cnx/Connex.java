package cnx;

import generator.Methods;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import java.io.InputStream;
import java.sql.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Connex {
    static String dbName;
    static String userName;
    static Methods methods = new Methods();

    public static String getDbName() {
        return dbName;
    }

    public static void setDbName(String dbName) {
        Connex.dbName = dbName;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        Connex.userName = userName;
    }

    public static Connection PsqlConnect(){
        Connection c = null;
        try
        {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/entreprise","postgres","mdpprom15");
        }
        catch(Exception e){
            System.out.println("Erreur de connexion");
            e.printStackTrace();
        }
        return c;
    }
    public static Connection getConnection(){
        Connection cnx = null;
        String database,url,dbname,user,password;
        try {
            Map<String,String> infos = getDBInformations("DB.xml");
            database = infos.get("sgbd");
            url = infos.get("url");
            dbname = infos.get("dbname");
            user = infos.get("user");
            password = infos.get("password");

            setDbName(dbname);
            setUserName(user);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        String driverClassName = "";
        if(database.toLowerCase().contains("postgres")){
            driverClassName = "org.postgresql.Driver";
            url += "/" + dbname;
        }else if(database.toLowerCase().contains("oracle")){
            driverClassName = "oracle.jdbc.driver.OracleDriver";
            url += ":" + dbname;
        }

        try {
            Class.forName(driverClassName);
            cnx = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
        return cnx;
    }

    private static String getElementValue(Element parent, String elementName) {
        NodeList nodeList = parent.getElementsByTagName(elementName);
        if (nodeList != null && nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return node.getTextContent();
        }
        return null;
    }

    public static List<String> getAllTable(Connection cnx) throws SQLException {
        List<String> tables = new ArrayList<>();
        boolean closed = false;

        if(cnx == null){
            return tables;
        }
        if(cnx.isClosed()){
            cnx = getConnection();
            closed = true;
        }

        DatabaseMetaData metaData = cnx.getMetaData();
        ResultSet res = metaData.getTables(null,null,"%",new String[]{"TABLE"});
        while(res.next()){
            tables.add(res.getString("TABLE_NAME"));
        }

        if(closed){
            cnx.close();
        }
        return tables;
    }

    public static Map<String,String> getDBInformations(String xmlFileName) throws ParserConfigurationException, IOException, SAXException {
        Map<String,String> infos = new HashMap<String,String>();

        InputStream xmlStream = ClassLoader.getSystemResourceAsStream(xmlFileName);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlStream);

        Element racine = document.getDocumentElement();

        infos.put("sgbd", getElementValue(racine, "sgbd"));
        infos.put("url", getElementValue(racine, "url"));
        infos.put("dbname", getElementValue(racine, "dbname"));
        infos.put("user", getElementValue(racine, "user"));
        infos.put("password", getElementValue(racine,"password"));

        return infos;
    }
}

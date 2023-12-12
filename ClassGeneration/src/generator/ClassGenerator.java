package generator;

import cnx.Connex;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.*;

public class ClassGenerator {

    public static Map<String,String> getTypeMatching(String path) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(path));
        Map<String,String> map = new HashMap<String,String>();
        for(String line : lines){
            String[] imports = line.split(":");
            if(imports.length != 2){
                throw new Exception("Une ligne est incomplet dans "+path);
            }else{
                map.put(imports[0],imports[1]);
            }
        }
        return map;
    }

    public static Map<String, String> getImports(String path) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(path));
        Map<String,String> map = new HashMap<String,String>();
        for(String line : lines){
            String[] imports = line.split(":");
            if(imports.length != 2){
                throw new Exception("Une ligne est incomplet dans "+path);
            }else{
                map.put(imports[0],imports[1]);
            }
        }

        return map;
    }

    public static void generateClass(Connection cnx, String utilsPath, String generatePath, String tableName, String className, String language, String packageName, List<TypeAndName> columns) throws Exception {
        boolean closed = false;
        if(cnx.isClosed()){
            cnx = Connex.getConnection();
            closed = true;
        }
        File extensionFile = new File(utilsPath + "/" + language.toLowerCase() + "/" + language.toLowerCase() + "Extension.txt");
        String extension = Files.readAllLines(Paths.get(extensionFile.getCanonicalPath())).get(0);

        className = className.split("."+extension.toLowerCase())[0];

        /* ----------- fichier requis ------------- */
        className = className.replaceFirst(className.substring(0, 1), className.substring(0, 1).toUpperCase());
        String genPath = generatePath + "/" + className.split("."+extension.toLowerCase())[0] + "." + extension;
        File genFile = new File(genPath);
        if(!genFile.exists()){
            boolean create = genFile.createNewFile();
        }else{
            throw new Exception("Un fichier de ce nom existe deja !");
        }

        /* ----- required template file ----- */
        String mapping = "mapping.txt";
        String standardLine = language.toLowerCase() + "StandardLine.txt";
        String importTxt = language.toLowerCase() + "Imports.txt";
        String getterSetter = language.toLowerCase() + "GetSet.txt";
        /* ---------------------------------- */

        //String utilsPath = "./src/template";
        File mappingFile = new File(utilsPath + "/" + mapping);
        File standardLineFile = new File(utilsPath + "/" + language.toLowerCase() + "/" + standardLine);
        File importFile = new File(utilsPath + "/" + language.toLowerCase() + "/" + importTxt);
        File getterSetterFile = new File(utilsPath + "/" + language.toLowerCase() + "/" + getterSetter);
        /* ---------------------------------------- */

        /* --- Ligne Standard dans un code Java --- */
        List<String> standardLines = Files.readAllLines(Paths.get(standardLineFile.getCanonicalPath()));
        /* --- Les imports necessaires pour cet table --- */
        Map<String,String> requiredImports = new HashMap<>();
        /* --- Les imports possible pour un colonne venant de la base --- */
        Map<String,String> importsMap = getImports(importFile.getCanonicalPath());
        /* --- Sequence StringBuilder des fields --- */
        StringBuilder fields = new StringBuilder();
        /* --- Sequence StringBuilder des getters et setters --- */
        StringBuilder getset = new StringBuilder();

        int row = 0;
        /* --- Parcourir tous les colonnes --- */
        for(TypeAndName column : columns){
            /* --- verifier si le type de ce colonne necessite une import sur Java --- */
            if(importsMap.get(column.getColumnType()) != null){
                /* --- si une import est requis, on l'ajoute dans l'import necessaire --- */
                requiredImports.put(column.getColumnType(), importsMap.get(column.getColumnType()));
            }
            /* --- La premiere ligne du field a besoin d'un retour a la ligne --- */
            if(row == 0){
                fields.append("\n");
            }
            row++;
            /* --- concatener le type et le nom de la colonne a la sequence de field --- */
            fields.append("\t").append(column.getColumnType()).append(" ").append(column.getColumnName()).append("; \n");

            /* --- Mcree Map akana anle equivalence anle cle --- */
            Map<String, String> getsetMap = new HashMap<>();
            getsetMap.put("$type", column.getColumnType());
            getsetMap.put("$fieldName", column.getColumnName());
            getsetMap.put("$FieldName", column.getColumnName().replaceFirst(column.getColumnName().substring(0,1), column.getColumnName().substring(0,1).toUpperCase()));
            List<String> gettersSetters = replaceTemplateLine(getsetMap, getterSetterFile);

            for(String gs : gettersSetters){
                getset.append(gs).append(" \n");
            }
            getset.append(" \n");
        }

        List<String> lines = new ArrayList<>();
        Map<String,String> valuesMap = new HashMap<String,String>();
        valuesMap.put("$package", packageName);
        valuesMap.put("$class", className);
        valuesMap.put("$fields", fields.toString());
        valuesMap.put("$gettersSetters", getset.toString());

        for(String line : standardLines){   // exemple de line >> #package#:packages
            if(!line.isBlank()){
                String[] parts = line.split(":");
                String key = parts[0];  // equivalent a #package#
                String value = parts[1]; // equivalent a packages
                valuesMap.put(key,value);
            }
        }
        /* --- Creer une sequence des imports requis --- */
        StringBuilder imports = new StringBuilder();
        for(String key : requiredImports.keySet()){
            imports.append(valuesMap.get("#import#")).append(" ").append(requiredImports.get(key)).append("; \n");
        }
        valuesMap.put("$packages", packageName + "; \n \n");
        valuesMap.put("$import", imports + " \n");


        /* ------------ Ecriture dans le fichier Java ------------ */
        /* --- Vider d'abord le fichier --- */
        Files.write(Path.of(genFile.getCanonicalPath()), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        /* --- recuper une liste de String deja remplacer par de vrai valeur depuis le mapping.txt --- */
        List<String> resultLines = replaceTemplateLine(valuesMap, mappingFile);
        for(String line : resultLines){
            //System.out.println(line);
            Files.write(Path.of(genFile.getCanonicalPath()), line.getBytes(), StandardOpenOption.APPEND);
        }
        /* ------------------------------------------------------- */
        if(closed){
            cnx.close();
        }
    }

    public static List<TypeAndName> getFields(Connection cnx, String utilsPath, String table, String language) throws Exception {
        List<TypeAndName> fields = new ArrayList<>();

        boolean closed = false;
        if(cnx.isClosed()){
            cnx = Connex.getConnection();
            closed = true;
        }
        String typeMatching = language.toLowerCase() + "TypeMatching.txt";
        File typeMatchingFile = new File(utilsPath + "/" + language.toLowerCase() + "/" + typeMatching);

        String sql = "SELECT * FROM " + table + " WHERE 1=0";
        Map<String,String> fieldRealType = getTypeMatching(typeMatchingFile.getCanonicalPath());

        try(Statement statement = cnx.createStatement()){
            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int count = metaData.getColumnCount();
            for(int i=1 ; i<=count ; i++){
                String columnName = metaData.getColumnName(i);
                String columnType = fieldRealType.get(metaData.getColumnTypeName(i));
                if(columnType == null){
                    throw new Exception("Une type de colonne de la base n'est pas present dans "+typeMatchingFile.getCanonicalPath());
                }
                fields.add(new TypeAndName(columnName,columnType));
            }
        }
        if(closed){
            cnx.close();
        }
        return fields;
    }

    /* --- Remplacer les lignes ## ou $ d'un fichier de template par des vrais valeur --- */
    public static List<String> replaceTemplateLine(Map<String,String> realValue, File file) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(file.getCanonicalPath()));
        List<String> results = new ArrayList<>();

        Set<String> keySet = realValue.keySet();
        for(String line : lines){
            String newline = "";
            if(!line.isBlank()){
                newline = line;
                for(String key : keySet){
                    newline = newline.replace(key,realValue.get(key));
                }
            }
            results.add(newline);
        }
        return results;
    }
}

package test;

import cnx.Connex;
import generator.Generator;
import generator.TypeAndName;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FunctionTest {
    static Generator generator = new Generator();
    public static void main(String[] args) throws SQLException {
        String context = "    context: [                ";
        System.out.println("Space removed: \"" + generator.RemoveSpace(context) + "\"");

        String beginning = "api-proxy-beginning:context:[";
        String[] splitted = generator.SplitInTwo(beginning, ":");
        for(String word : splitted){
            System.out.println("Word: '"+word+"'");
        }

        String word = "[[#import# $import$]]#enclosure#]]";
        System.out.println("WORD: "+generator.replaceFirstAndLast(word,"[[","]]",""));

        Connection cnx = Connex.getConnection();

//        ListPrimaryKey(cnx, "groupe");

        cnx.close();
    }

    public static void ListPrimaryKey(Connection cnx, String table) throws SQLException {
        boolean isClosed = false;
        if(cnx.isClosed()){
            cnx = Connex.getConnection();
            isClosed = true;
        }

        DatabaseMetaData databaseMetaData = cnx.getMetaData();
        ResultSet columns = databaseMetaData.getColumns(null,null,table,null);
        while(columns.next()){
            String columnName = columns.getString("COLUMN_NAME");
            String databaseType = columns.getString("TYPE_NAME");

            ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(null,null,table);
            boolean isPrimaryKey = false;
            while(primaryKeys.next()){
                String pkColumnName = primaryKeys.getString("COLUMN_NAME");
                System.out.println("PK Column Name: "+pkColumnName);
                if(pkColumnName.equals(columnName)){
                    isPrimaryKey = true;
                    break;
                }
            }
            System.out.println("Colonne "+columnName+" isPrimaryKey: "+isPrimaryKey);
        }

        if(isClosed){
            cnx.close();
        }
    }
}

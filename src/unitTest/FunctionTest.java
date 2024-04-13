package unitTest;

import cnx.Connex;
import generator.Core;
import generator.Generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;

public class FunctionTest {
    static Generator generator = new Generator();
    static Core core = new Core();
    public static void main(String[] args) throws SQLException, IOException {

        Connection cnx = Connex.getConnection();

        cnx.close();

        System.out.println("Directory: "+System.getProperty("user.dir"));
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

package test;

import cnx.Connex;
import generator.Generator;
import generator.DaoGenerator;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    static Generator generator = new Generator();
    static DaoGenerator daoGenerator = new DaoGenerator(generator);
    static Connection cnx = Connex.getConnection();

    public static void main(String[] args) throws Exception {
        String utilsPath = "./src/template";
        String generatePath = "E:\\Sanda\\ITU\\Frameworks\\ProjetTest\\Angular3\\Angular3";
        String tableName = "etudiant";
        String language = "csharp";
        String viewFramework = "angular";
        String packageName = "Models";

        String templatePath = "./src/template";

        String test = "#extends#::";
        System.out.println(test.split("#extends#:")[1]);
        System.out.println("String");

        String filePath = "./src/template/csharp/csharpCaracteristique.cfg";
        String daoPath = "./src/template/csharp/csharpDAO.cfg";

        generator.GenerateClass(cnx, templatePath, generatePath,tableName, language, packageName);
        System.out.println("-------------------------------------------");
        generator.GenerateController(cnx,templatePath, generatePath,tableName,language,"Angular3.Controllers");
        System.out.println("-------------------------------------------");
        generator.GenerateView(cnx, templatePath,generatePath,tableName,language,viewFramework);

        System.out.println("======================================================================");
        String fileName = "Connex.cs";
        String fileToFindPath = "";
        //fileToFindPath = generator.findFile(fileToFindPath, fileName, templatePath);
        //System.out.println("File To Find: "+fileToFindPath);


        cnx.close();
    }
}

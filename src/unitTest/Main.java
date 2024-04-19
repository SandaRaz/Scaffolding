package unitTest;

import cnx.Connex;
import generator.Generator;
import generator.DaoGenerator;
import generator.LoginInfo;

import java.net.URL;
import java.sql.Connection;

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
        String authTable = "account";

        String templateFolder = "template";

        String test = "#extends#::";
        System.out.println(test.split("#extends#:")[1]);
        System.out.println("String");

        LoginInfo loginInfo = new LoginInfo(true, "email", "password",false);
        generator.GenerateClass(cnx, templateFolder, generatePath,tableName, language, packageName, loginInfo);
        System.out.println("-------------------------------------------");
        generator.GenerateController(cnx,templateFolder, generatePath,tableName,language,"Angular3.Controllers", loginInfo);
        System.out.println("-------------------------------------------");
        generator.GenerateView(cnx, templateFolder,generatePath,tableName,language,viewFramework, loginInfo, authTable);

        System.out.println("======================================================================");
        String fileName = "Connex.cs";
        String fileToFindPath = "";
        //fileToFindPath = generator.findFile(fileToFindPath, fileName, templatePath);
        //System.out.println("File To Find: "+fileToFindPath);


        cnx.close();
    }
}

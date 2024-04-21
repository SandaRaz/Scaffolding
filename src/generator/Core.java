package generator;

import cnx.Connex;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class Core {
    String defaultPackage = "";
    String defaultConrollerPackage = "";
    String authTable = "";
    private final Generator generator;

    public String getDefaultPackage(){
        return this.defaultPackage;
    }
    public void setDefaultPackage(String defaultPackage){
        this.defaultPackage = defaultPackage;
    }

    public String getDefaultConrollerPackage() {
        return defaultConrollerPackage;
    }

    public void setDefaultConrollerPackage(String defaultConrollerPackage) {
        this.defaultConrollerPackage = defaultConrollerPackage;
    }

    public String getAuthTable() {
        return authTable;
    }

    public void setAuthTable(String authTable) {
        this.authTable = authTable;
    }

    public Core(){
        this.generator = new Generator();
    }

    public void Process() throws SQLException {
        Connection cnx = Connex.getConnection();

        Scanner scanner = new Scanner(System.in);
        String command = "";
        while(true){
            System.out.print("> ");
            command = scanner.nextLine();
            if(EndTask(command)){
                break;
            }
            try {
                ExecuteTasks(cnx, command);
            } catch (Exception e) {
                System.out.println("Internal error: "+e.getMessage());
                System.out.print("Stack Trace: ");
                e.printStackTrace();
//                throw new RuntimeException(e);
            }
        }
        scanner.close();
        cnx.close();
    }

    public boolean EndTask(String command){
        String trimmedCommand = command.toLowerCase().trim();
        return trimmedCommand.equals("done") || trimmedCommand.equals("quit") || trimmedCommand.equals("exit");
    }

    public static double compareToDefaultCommande(String defaultCommande, String commande){
        List<String> syntaxes = Arrays.stream(defaultCommande.toLowerCase().split("\\s+"))
                .filter(cmd -> !cmd.startsWith("$") && !cmd.endsWith("$"))
                .toList();
        String[] commandeSplitted = commande.toLowerCase().split("\\s+");

        Map<String, Integer> occurences = new HashMap<>();
        for(String snt : syntaxes){
            occurences.merge(snt, 1, Integer::sum);
        }
        Map<String,Integer> similaires = new HashMap<>();
        for(String cmd : commandeSplitted){
            if(syntaxes.contains(cmd)){
                similaires.merge(cmd, 1, Integer::sum);
            }
        }

        return (double) similaires.size() / (occurences.size() + commandeSplitted.length);
    }

    public static String matchedCommande(String commande, String[] dcs){
        double[] similarities = Arrays.stream(dcs)
                .mapToDouble(dc -> compareToDefaultCommande(dc, commande))
                .toArray();
        int indiceMax = 0;
        for(int i = 1; i<similarities.length; i++){
            if(similarities[i] > similarities[indiceMax]){
                indiceMax = i;
            }
        }
        return dcs[indiceMax];
    }

    // set default package Models : set default package $yourPackage$
    // generate etudiant crud for csharp with angular : generate $tableName$ crud for $backend$ with $frontend$
    public void ExecuteTasks(Connection cnx, String command) throws Exception {
        boolean isClosed = false;
        if(cnx.isClosed()){
            cnx = Connex.getConnection();
            isClosed = true;
        }

        String unspacedCommand = generator.getMethods().RemoveSpaceBetween(command.toLowerCase());

        Map<String,String> mapCommand = new HashMap<>();
        if(unspacedCommand.startsWith("setdefaultpackage")){
            String defaultCommand = "set default package $yourPackage$";
            String[] tabDefaultCommand = defaultCommand.split("\\s+");
            String[] tabCommand = command.split("\\s+");

            if(CommandValid(tabDefaultCommand, tabCommand)) {
                for (int i = 0; i < tabDefaultCommand.length; i++) {
                    if (isVariable(tabDefaultCommand[i])) {
                        mapCommand.put(tabDefaultCommand[i].replace("$", ""), tabCommand[i]);
                    }
                }
                this.defaultPackage = mapCommand.get("yourPackage");
            }
        } else if(unspacedCommand.startsWith("setdefaultcontrollerpackage")){
            String defaultCommand = "set default controller package $yourControllerPackage$";
            String[] tabDefaultCommand = defaultCommand.split("\\s+");
            String[] tabCommand = command.split("\\s+");

            if(CommandValid(tabDefaultCommand, tabCommand)) {
                for (int i = 0; i < tabDefaultCommand.length; i++) {
                    if (isVariable(tabDefaultCommand[i])) {
                        mapCommand.put(tabDefaultCommand[i].replace("$", ""), tabCommand[i]);
                    }
                }
                this.defaultConrollerPackage = mapCommand.get("yourControllerPackage");
            }
        } else if(unspacedCommand.startsWith("setauthtable")){
            String defaultCommand = "set auth table $yourAuthTable$";
            String[] tabDefaultCommand = defaultCommand.split("\\s+");
            String[] tabCommand = command.split("\\s+");

            if(CommandValid(tabDefaultCommand, tabCommand)) {
                for (int i = 0; i < tabDefaultCommand.length; i++) {
                    if (isVariable(tabDefaultCommand[i])) {
                        mapCommand.put(tabDefaultCommand[i].replace("$", ""), tabCommand[i]);
                    }
                }
                this.authTable = mapCommand.get("yourAuthTable");
            }
        } else if(unspacedCommand.startsWith("generate")){
            String[] defaultCommands = {
                "generate $tableName$ crud for $backend$ with $frontend$",
                "generate $tableName$ crud for $backend$ with $frontend$ securized",
                "generate $tableName$ crud for $backend$ with $frontend$ auth column $usermail$ and $key$",
                "generate $tableName$ crud for $backend$ with $frontend$ securized auth column $usermail$ and $key$"
            };
            String defaultCommand = matchedCommande(command, defaultCommands);

            LoginInfo loginInfo = new LoginInfo(false,"","",false);

            String[] tabDefaultCommand = defaultCommand.split("\\s+");
            String[] tabUserCommand = command.split("\\s+");

            if(CommandValid(tabDefaultCommand, tabUserCommand)){
                for(int i=0; i<tabDefaultCommand.length; i++){
                    if(isVariable(tabDefaultCommand[i])){
                        mapCommand.put(tabDefaultCommand[i].replace("$",""), tabUserCommand[i]);
                    }
                }

                String tableName = mapCommand.get("tableName");
                String language = mapCommand.get("backend");
                String view = mapCommand.get("frontend");

                if(defaultCommand.toLowerCase().contains("securized")){
                    loginInfo.setNeedAuth(true);
                }
                if(defaultCommand.toLowerCase().contains("auth column")){
                    String usermail = mapCommand.get("usermail");
                    String key = mapCommand.get("key");

                    loginInfo.setLogin(true);
                    loginInfo.setUsermail(usermail);
                    loginInfo.setKey(key);
                }

                String templateFolder = "template";
                String generatePath = System.getProperty("user.dir");
//                System.out.println("User Dir: "+generatePath);
//                String generatePath = "E:\\Sanda\\ITU\\Frameworks\\ProjetTest\\Angular3\\Angular3";

                if(this.defaultPackage.isBlank()){
                    System.out.println("Default package is empty. Run: set default package $yourPackage$");
                }else if (this.defaultConrollerPackage.isBlank()){
                    System.out.println("Default controller package is empty. Run: set default controller package $yourPackage$");
                }else if (this.authTable.isBlank()){
                    System.out.println("Authentification table is empty. Run: set auth table $yourTable$");
                }else{
                    System.out.println("    > Default package: "+this.defaultPackage);
                    System.out.println("    > Default controller package: "+this.defaultConrollerPackage);
                    System.out.println("    > Default authentification table: "+this.authTable);

                    // Generating Model
                    this.generator.GenerateClass(cnx,templateFolder,generatePath,tableName,language,this.defaultPackage,loginInfo);
                    System.out.println("  -> Model generated");
                    // Generating Controller
                    this.generator.GenerateController(cnx,templateFolder,generatePath,tableName,language,this.defaultConrollerPackage,loginInfo);
                    System.out.println("  -> Controller generated");
                    // Generating View
                    this.generator.GenerateView(cnx, templateFolder, generatePath, tableName, language, view, loginInfo, this.authTable);

                }
            }
        } else {
            System.out.println("'"+command+"' is not a valid command");
        }

        if(isClosed){
            cnx.close();
        }
    }

    public boolean isVariable(String word){
        String trimmedWord = word.trim();
        return trimmedWord.startsWith("$") && trimmedWord.endsWith("$");
    }

    public boolean CommandValid(String[] defaultCommands, String[] yourCommands){
        boolean commandValid = true;

        StringBuilder command = new StringBuilder();
        if(defaultCommands.length != yourCommands.length){
            commandValid = false;
            if(yourCommands.length > defaultCommands.length){
                for(int i=0; i< yourCommands.length; i++){
                    if(i >= defaultCommands.length){
                        command.append(">").append(yourCommands[i]).append("<").append(" ");
                    }else{
                        command.append(yourCommands[i]).append(" ");
                    }
                }
            }else{
                for(int i = 0; i< yourCommands.length; i++){
                    if(isVariable(defaultCommands[i])){
                        command.append(yourCommands[i]).append(" ");
                    }else{
                        if(!defaultCommands[i].toLowerCase().trim().equals(yourCommands[i].toLowerCase().trim())){
                            command.append(">").append(yourCommands[i]).append("<").append(" ");
                            break;
                        }else{
                            command.append(yourCommands[i]).append(" ");
                        }
                    }
                }
            }
        }else{
            for(int i=0; i< defaultCommands.length; i++){
                if(isVariable(defaultCommands[i])){
                    command.append(yourCommands[i]).append(" ");
                }else{
                    if(!defaultCommands[i].toLowerCase().trim().equals(yourCommands[i].toLowerCase().trim())){
                        command.append(">").append(yourCommands[i]).append("<").append(" ");
                        commandValid = false;
                        break;
                    }else{
                        command.append(yourCommands[i]).append(" ");
                    }
                }
            }
        }

        if(!commandValid){
            System.out.println("Unexpected command: " + command);
        }

        return commandValid;
    }

    public void CommandExecute(String commande) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(commande);

        System.out.println(commande);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if(exitCode != 0){
            System.out.println("    "+commande+" execute avec error code "+exitCode);
        }
    }

    public void CommandExecute(List<String> commande) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(commande);

        System.out.println(commande);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if(exitCode != 0){
            System.out.println("    "+commande+" execute avec error code "+exitCode);
        }
    }

    public static void main(String[] args) {
        Core mainCore = new Core();
        try {
            mainCore.Process();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

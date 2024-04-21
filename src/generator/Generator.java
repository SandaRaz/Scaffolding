package generator;

import cnx.Connex;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Generator {
    Methods methods;
    DaoGenerator daoGenerator;

    public Methods getMethods() {
        return methods;
    }
    public void setMethods(Methods methods) {
        this.methods = methods;
    }

    public Generator(){
        this.daoGenerator = new DaoGenerator(this);
        this.methods = new Methods();
    }

    // --------------- FUNCTION -----------------
    public String GetVariableFromWord(String word, char variableIdentificator){
        String variable = "";
        AtomicBoolean isVariable = new AtomicBoolean(false);

        StringBuilder currentVariable = new StringBuilder();
        for(char character : word.toCharArray()){
            if(isVariable.get()){
                if(character == variableIdentificator){
                    variable = currentVariable.toString();

                    isVariable.set(false);
                    currentVariable.setLength(0);
                    break;
                }else{
                    currentVariable.append(character);
                }
            }else{
                if(character == variableIdentificator){
                    isVariable.set(true);
                }
            }
        }

        return variable;
    }
    public List<String> GetVariablesFromWord(String word, char variableIdentificator){
        List<String> variables = new ArrayList<>();
        AtomicBoolean isVariable = new AtomicBoolean(false);

        StringBuilder currentVariable = new StringBuilder();
        for(char character : word.toCharArray()){
            if(isVariable.get()){
                if(character == variableIdentificator){
                    if(!variables.contains(currentVariable.toString())){
                        variables.add(currentVariable.toString());
                    }

                    isVariable.set(false);
                    currentVariable.setLength(0);
                }else{
                    currentVariable.append(character);
                }
            }else{
                if(character == variableIdentificator){
                    isVariable.set(true);
                }
            }
        }

        return variables;
    }
    public List<String> ReadCaracteristique(String opening, String caracteristiques, String closing, String caracteristiquePath) throws IOException, URISyntaxException {
        List<String> lines = new ArrayList<>();

        List<String> fileLines = methods.readLines(caracteristiquePath);
        boolean add = false;
        for(String fileLine : fileLines){
            boolean foundCaracteristique = fileLine.equalsIgnoreCase(opening + caracteristiques + closing);
            boolean isNewCaracteristique = fileLine.startsWith(opening) && fileLine.trim().endsWith(closing);
            if(add){
                if(isNewCaracteristique){
                    break;
                }else{
                    if(!fileLine.isBlank() && !fileLine.trim().startsWith(";")){
                        lines.add(fileLine);
                    }
                }
            }else{
                if(foundCaracteristique){
                    add = true;
                }
            }

        }

        return lines;
    }
    public List<String> ReadCaracteristiqueIncludeBlank(String opening, String caracteristiques, String closing, String filePath) throws IOException, URISyntaxException {
        List<String> lines = new ArrayList<>();

        List<String> fileLines = methods.readLines(filePath);
        boolean add = false;
        for(String fileLine : fileLines){
            boolean foundCaracteristique = fileLine.equalsIgnoreCase(opening + caracteristiques + closing);
            boolean isNewCaracteristique = fileLine.startsWith(opening) && fileLine.trim().endsWith(closing);
            if(add){
                if(isNewCaracteristique){
                    break;
                }else{
                    if(!fileLine.trim().startsWith(";")){
                        lines.add(fileLine);
                    }
                }
            }else{
                if(foundCaracteristique){
                    add = true;
                }
            }

        }

        return lines;
    }

    public String ReadCaracAttribut(String attribut, List<String> caracteristiques){
        String value = "";
        for(String attrib : caracteristiques){
            if(attrib.toLowerCase().trim().startsWith(attribut.toLowerCase())){
                value = methods.SplitInTwo(attrib, ":")[1];
                break;
            }
        }
        return value;
    }

    public String GetSyntaxe(String syntaxe, String caracteristiquePath) throws IOException, URISyntaxException {
        String languageSyntaxe = "";

        List<String> fileLines = this.ReadCaracteristique("[","Syntaxe","]", caracteristiquePath);
        for(String fileLine : fileLines){
            if(fileLine.contains("#" + syntaxe + "#:")){
                String[] splittedSyntaxe = fileLine.split("#" + syntaxe + "#:");
                if(splittedSyntaxe.length == 2){
                    languageSyntaxe = fileLine.split("#" + syntaxe + "#:")[1];
                }else{
                    languageSyntaxe = "";
                }
                break;
            }
        }

        return languageSyntaxe;
    }

    public Map<String,String> GetTypeDatabaseMatching(String templateFolder, String language) throws Exception {
        String caracteristiquePath = templateFolder + "/" + language.toLowerCase() + "/" + language.toLowerCase()+"Caracteristique.cfg";
        List<String> typesMatching = ReadCaracteristique("[","TypeDatabaseMatching","]", caracteristiquePath);

        Map<String,String> map = new HashMap<String,String>();
        for(String line : typesMatching){
            String[] imports = line.split(":");
            if(imports.length == 2){
                map.put(imports[0],imports[1]);
            }
        }
        return map;
    }

    public Map<String,String> GetViewFormType(String caracteristiquePath, String viewFramework) throws Exception {
        List<String> typesMatching = ReadCaracteristique("[","ViewFormType","]", caracteristiquePath);

        Map<String,String> map = new HashMap<String,String>();
        for(String line : typesMatching){
            String[] imports = line.split(":");
            if(imports.length == 2){
                map.put(imports[0],imports[1]);
            }
        }
        return map;
    }

    public List<TypeAndName> GetTableFields(Connection cnx, String templateFolder, String language,String table) throws Exception {
        table = table.toLowerCase();

        List<TypeAndName> fields = new ArrayList<>();

        boolean closed = false;
        if(cnx.isClosed()){
            cnx = Connex.getConnection();
            closed = true;
        }

        Map<String,String> fieldRealType = GetTypeDatabaseMatching(templateFolder, language);

        DatabaseMetaData databaseMetaData = cnx.getMetaData();
        ResultSet columns = databaseMetaData.getColumns(null,null,table,null);
        while(columns.next()){
            String columnName = columns.getString("COLUMN_NAME");
            String databaseType = columns.getString("TYPE_NAME");
            String columnType = fieldRealType.get(databaseType);

            ResultSet foreignKeys = databaseMetaData.getImportedKeys(cnx.getCatalog(),null,table);
            boolean isForeignKey = false;
            String referencedTable = "";
            while(foreignKeys.next()){
                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                if(fkColumnName.equals(columnName)){
                    isForeignKey = true;
                    referencedTable = methods.UpperFirstChar(foreignKeys.getString("PKTABLE_NAME"));
                    break;
                }
            }

            ResultSet primaryKeys = databaseMetaData.getPrimaryKeys(null,null,table);
            boolean isPrimaryKey = false;
            while(primaryKeys.next()){
                String pkColumnName = primaryKeys.getString("COLUMN_NAME");
                if(pkColumnName.equals(columnName)){
                    isPrimaryKey = true;
                    break;
                }
            }

            fields.add(new TypeAndName(columnName,columnType,isPrimaryKey,isForeignKey,referencedTable,databaseType));
        }
        if(closed){
            cnx.close();
        }

        return fields;
    }

    public TypeAndName GetPrimaryKey(List<TypeAndName> fields){
        for(TypeAndName field : fields){
            if(field.isPrimaryKey()){
                return field;
            }
        }
        return null;
    }
    public String GetLibelle(List<TypeAndName> fields){
        String libelle = "";
        for(TypeAndName field : fields){
            if(!field.isPrimaryKey() && !field.isForeignKey() && field.getDatabaseType().contains("char")){
                libelle = field.getColumnName().toLowerCase();
            }
        }
        return libelle;
    }

    public Map<String, String> GetFieldRequiredImports(String caracteristiquePath) throws IOException, URISyntaxException {
        List<String> lines = this.ReadCaracteristique("[","ClassFieldImport","]", caracteristiquePath);
        Map<String,String> map = new HashMap<String,String>();
        for(String line : lines){
            String[] imports = line.split(":");
            if(imports.length == 2){
                map.put(imports[0],imports[1]);
            }
        }
        return map;
    }

    public String GettersSetters(String templateFolder, String language, TypeAndName field) throws IOException, URISyntaxException {
        StringBuilder validGetterSetter = new StringBuilder();

        String caracteristiquePath = templateFolder + "/" + language.toLowerCase() + "/" + language.toLowerCase() + "Caracteristique.cfg";
        List<String> getsetLines = this.ReadCaracteristique("[","GetSet","]", caracteristiquePath);

        Map<String, String> variableMapping = new HashMap<>();

        String type = field.getColumnType();
        String FieldName = methods.UpperFirstChar(field.getColumnName());
        String fieldname = field.getColumnName().toLowerCase();

        variableMapping.put("type",type);
        variableMapping.put("FieldName",FieldName);
        variableMapping.put("fieldName",fieldname);

        for(String getsetLine : getsetLines){
            AtomicBoolean isVariable = new AtomicBoolean(false);
            StringBuilder currentVariable = new StringBuilder();
            for(char character : getsetLine.toCharArray()){
                getsetLine = this.IdentifyVariable(getsetLine, variableMapping, currentVariable, isVariable, character, '$');
            }

            if(getsetLines.indexOf(getsetLine) == getsetLines.size()-1){
                validGetterSetter.append(getsetLine);
            }else{
                validGetterSetter.append(getsetLine).append("\n    ");
            }
        }

        return validGetterSetter.toString();
    }

    public void MakeConstructor(String caracteristiquePath, Map<String,String> mapppingVariables) throws IOException, URISyntaxException {
        List<String> constructorLines = this.ReadCaracteristique("[","VoidConstructor","]", caracteristiquePath);

        StringBuilder validLine = new StringBuilder();
        for(String line : constructorLines) {
            StringBuilder currentVariable = new StringBuilder();
            AtomicBoolean isVariable = new AtomicBoolean(false);

            for (char character : line.toCharArray()) {
                line = this.IdentifyVariable(line, mapppingVariables, currentVariable, isVariable, character, '$');
            }

            if(constructorLines.indexOf(line) == constructorLines.size()-1){
                validLine.append(line);
            }else{
                validLine.append(line).append("\n");
            }
        }

        mapppingVariables.put("VoidConstructor", validLine.toString());
    }

    public String IdentifySyntaxe(String caracteristiquePath, String ligne, StringBuilder currentSyntaxe, AtomicBoolean isSyntaxe, char character, char equivalence) throws IOException, URISyntaxException {
        String retour = ligne;
        if(isSyntaxe.get()){
            if(character == equivalence){
                String validSyntaxe = this.GetSyntaxe(currentSyntaxe.toString(), caracteristiquePath);
                retour = ligne.replace(equivalence + currentSyntaxe.toString() + equivalence, validSyntaxe);

                currentSyntaxe.setLength(0);

                isSyntaxe.set(false);
            }else{
                currentSyntaxe.append(character);
            }
        }else{
            if(character == equivalence){
                isSyntaxe.set(true);
            }
        }

        return retour;
    }

    public String IdentifyVariable(String ligne, Map<String, String> mappingVariables, StringBuilder currentVariable, AtomicBoolean isVariable, char character, char equivalence){
        String retour = ligne;
        if(isVariable.get()){
            if(character == '$'){
                String validVariable = mappingVariables.get(currentVariable.toString());
                retour = ligne.replace("$"+currentVariable+"$", validVariable);

                currentVariable.setLength(0);
                isVariable.set(false);
            }else{
                currentVariable.append(character);
            }
        }else{
            if(character == '$'){
                isVariable.set(true);
            }
        }

        return retour;
    }

    public String IdentifyListVariable(StringBuilder validListVariable, String ligne, Map<String, List<String>> mappingListes, StringBuilder currentListVariable, AtomicBoolean isListVariable, char character, char equivalence){
        String retour = ligne;
        if(isListVariable.get()){
            if(character == equivalence){
                List<String> listsFromMap = mappingListes.get(currentListVariable.toString());
                if(listsFromMap != null){
                    for(String listFromMap : listsFromMap){
                        String tempValidVariable = ligne.replace(equivalence + currentListVariable.toString() + equivalence, listFromMap);
                        tempValidVariable = tempValidVariable.replace("[","");
                        tempValidVariable = tempValidVariable.replace("]","");

                        if(listsFromMap.indexOf(listFromMap) == listsFromMap.size()-1){
                            validListVariable.append(tempValidVariable);
                        }else{
                            validListVariable.append(tempValidVariable).append("\n");
                        }
                        retour = ligne.replace(ligne, validListVariable);
//                        System.out.println("ValidVariableList: "+validListVariable);
                    }
                }
                isListVariable.set(false);
            }else{
                currentListVariable.append(character);
            }
        }else{
            if(character == '$'){
                isListVariable.set(true);
            }
        }

        return retour;
    }

    public String ReplaceSimpleVariable(String ligne, Map<String, String> mapping){
        String replaced = ligne;
        String trimmedLine = ligne.trim();
//        if(!(trimmedLine.startsWith("[") && trimmedLine.endsWith("]"))){
            List<String> variables = GetVariablesFromWord(replaced,'$');
            for(String variable : variables){
                //System.out.println("Variable de replacement: "+variable);
                replaced = replaced.replace("$"+variable+"$", mapping.get(variable));
            }
//        }

        return replaced;
    }

    public String ReplaceSyntaxe(String ligne, Map<String, String> mapping){
        String replaced = ligne;
        String trimmedLine = ligne.trim();
        if(!(trimmedLine.startsWith("[") && trimmedLine.endsWith("]"))){
            List<String> variables = GetVariablesFromWord(replaced,'#');
            for(String variable : variables){
                //System.out.println("Variable de replacement: "+variable);
                replaced = replaced.replace("#"+variable+"#", mapping.get(variable));
            }
        }

        return replaced;
    }

    public String replaceFirstAndLast(String word, String first, String last, String replacement){
        for(int i=0; i<word.length(); i++){
            if(word.startsWith(first, i)){
                word = word.substring(0, i) + replacement + word.substring(i+first.length());
                break;
            }
        }
        for(int i=word.length(); i>=0; i--){
            if(word.substring(0, i).endsWith(last)){
                word = word.substring(0,(i - last.length())) + replacement + word.substring(i);
                break;
            }
        }

        return word;
    }

    public String replaceFirstAndLast(String string, char first, char last, String replacement){
        char[] charArray = string.toCharArray();
        int indiceFirst = -1;
        int indiceLast = -1;
        for(int i=0; i<charArray.length; i++){
            if(indiceFirst == -1 && charArray[i] == first){
                indiceFirst = i;
            }
            if(charArray[i] == last){
                indiceLast = i;
            }
        }

        StringBuilder tempSb = new StringBuilder(string);
        if(indiceFirst != -1 && indiceLast != -1){
            tempSb.replace(indiceFirst,indiceFirst+1, replacement);
            tempSb.replace(indiceLast-1,indiceLast, replacement);
        }

        return tempSb.toString();
    }

    public String ReplaceVerticaleVariable(String ligne, List<TypeAndName> fields, Map<String, List<String>> mappingVariables, String opening, String closing){
        String horizontalLigne = ligne;
        StringBuilder buildResultat = new StringBuilder();

        if(ligne.trim().startsWith(opening) && ligne.trim().endsWith(closing)){
            List<String> lignesToReplaces = new ArrayList<>();
            for(int i=0; i<fields.size(); i++){
                lignesToReplaces.add(ligne);
            }

            List<String> allVariables = GetVariablesFromWord(ligne, '$');
            for(int i = 0; i<lignesToReplaces.size(); i++){
                String toReplace = lignesToReplaces.get(i);
                for(String variable : allVariables){
                    List<String> replacements = mappingVariables.get(variable);
                    if(i < replacements.size()){
                        toReplace = toReplace.replace("$"+variable+"$", replacements.get(i));
                    }else{
                        toReplace = "";
                    }
                }

                toReplace = replaceFirstAndLast(toReplace, opening,closing,"");
//                toReplace = toReplace.replace("[","").replace("]","");

                if(i == lignesToReplaces.size()-1){
                    if(!toReplace.isBlank()){
                        buildResultat.append(toReplace);
                    }
                }else{
                    if(!toReplace.isBlank()){
                        buildResultat.append(toReplace).append("\n");
                    }
                }
            }

            horizontalLigne = buildResultat.toString();
        }
        return horizontalLigne;
    }

    public String ReplaceVerticaleVariableWithoutField(String ligne, Map<String, List<String>> mappingVariables, String opening, String closing){
        String horizontalLigne = ligne;
        StringBuilder buildResultat = new StringBuilder();

        if(ligne.trim().startsWith(opening) && ligne.trim().endsWith(closing)){
            List<String> lignesToReplaces = new ArrayList<>();

            List<String> allSyntaxes = GetVariablesFromWord(ligne, '#');
            if(allSyntaxes.size() > 0){
                String firstSyntaxe = allSyntaxes.get(0);
                for(int i=0; i<mappingVariables.get(firstSyntaxe).size(); i++){
                    lignesToReplaces.add(ligne);
                }

                List<String> allVariables = GetVariablesFromWord(ligne, '$');
                for(int i = 0; i<lignesToReplaces.size(); i++){
                    String toReplace = lignesToReplaces.get(i);
                    for(String variable : allVariables){
                        List<String> replacements = mappingVariables.get(variable);
                        if(i < replacements.size()){
                            toReplace = toReplace.replace("$"+variable+"$", replacements.get(i));
                        }else{
                            toReplace = "";
                        }
                    }

                    toReplace = replaceFirstAndLast(toReplace, opening,closing,"");
//                toReplace = toReplace.replace("[","").replace("]","");

                    if(i == lignesToReplaces.size()-1){
                        if(!toReplace.isBlank()){
                            buildResultat.append(toReplace);
                        }
                    }else{
                        if(!toReplace.isBlank()){
                            buildResultat.append(toReplace).append("\n");
                        }
                    }
                }

                horizontalLigne = buildResultat.toString();
            }
        }
        return horizontalLigne;
    }

    public boolean TableExist(Connection cnx, String tableName) throws SQLException {
        boolean isClosed = false;
        if(cnx.isClosed()){
            cnx = Connex.getConnection();
            isClosed = true;
        }

        DatabaseMetaData metaData = cnx.getMetaData();
        ResultSet resultSet = metaData.getTables(null, null, tableName, null);
        boolean exist = resultSet.next();

        if(isClosed){
            cnx.close();
        }

        return exist;
    }

    public void GenerateClass(Connection cnx, String templateFolder, String generatePath, String tableName, String language, String packageName, LoginInfo loginInfo) throws Exception {
        List<String> lignes = new ArrayList<>();

        boolean closed = false;
        if(cnx.isClosed()){
            cnx = Connex.getConnection();
            closed = true;
        }

        System.out.println("    generating model for "+methods.UpperFirstChar(tableName)+" ...");

        if(!TableExist(cnx, tableName)){
            throw new Exception("Table "+tableName+" does not exist in "+Connex.getDbName());
        }

        String caracteristiquePath = templateFolder + "/" + language.toLowerCase() + "/" + language.toLowerCase() + "Caracteristique.cfg";
        String requiredFolderPath = templateFolder + "/" + language.toLowerCase() + "/" + "required";
        String mappingFilePath = templateFolder + "/" + "mapping.cfg";

        Map<String, String> importsMap = this.GetFieldRequiredImports(caracteristiquePath);

        List<String> listImports = new ArrayList<>();
        List<String> listFields = new ArrayList<>();
        List<String> listGetSet = new ArrayList<>();

        List<TypeAndName> fields = this.GetTableFields(cnx, templateFolder, language, tableName);
        for(TypeAndName field : fields){
            String type = field.getColumnType();
            String name = field.getColumnName();
            if(importsMap.get(type) != null){
                String packageToImport = importsMap.get(type);
                if(!listImports.contains(packageToImport.trim())){
                    listImports.add(packageToImport.trim());
                }
            }
            listFields.add(type + " " + name);
            listGetSet.add(this.GettersSetters(templateFolder,language,field));
        }
        // Collection.addAll(Collection)
        listImports.addAll(this.ReadCaracteristique("[","DaoImport","]", caracteristiquePath));

        Map<String, List<String>> mappingListes = new HashMap<>();
        mappingListes.put("import", listImports);
        mappingListes.put("fields", listFields);
        mappingListes.put("GetSet", listGetSet);
        Map<String, String> mappingVariables = new HashMap<>();
        mappingVariables.put("package", packageName);
        mappingVariables.put("class", methods.UpperFirstChar(tableName));
        daoGenerator.crudDAOToMap(cnx,tableName,templateFolder, language, mappingVariables, loginInfo);
        this.MakeConstructor(caracteristiquePath, mappingVariables);

        List<String> mappingLignes = methods.readLines(mappingFilePath);
        for(String ligne : mappingLignes){
            AtomicBoolean isSyntaxe = new AtomicBoolean(false);
            AtomicBoolean isVariable = new AtomicBoolean(false);
            AtomicBoolean isList = new AtomicBoolean(ligne.trim().startsWith("[") && ligne.trim().endsWith("]"));

            String newLine = "";

            char[] characters = ligne.toCharArray();

            if(isList.get()){
                StringBuilder currentSyntaxe = new StringBuilder();
                for(char character : characters){
                    ligne = this.IdentifySyntaxe(caracteristiquePath,ligne,currentSyntaxe,isSyntaxe,character,'#');
                }
//                System.out.println("Variable: " + ligne);

                AtomicBoolean isListVariable = new AtomicBoolean(false);
                StringBuilder currentListVariable = new StringBuilder();
                StringBuilder validListVariable = new StringBuilder();
                for(char character : ligne.toCharArray()){
                    ligne = this.IdentifyListVariable(validListVariable,ligne,mappingListes,currentListVariable,isListVariable,character,'$');
                }

            }else{
                StringBuilder currentSyntaxe = new StringBuilder();
                StringBuilder currentVariable = new StringBuilder();

                for(char character : characters){
                    ligne = this.IdentifySyntaxe(caracteristiquePath,ligne,currentSyntaxe,isSyntaxe,character,'#');
                    ligne = this.IdentifyVariable(ligne,mappingVariables,currentVariable,isVariable,character,'$');
                }
            }
            lignes.add(ligne);
        }

//        for(String ligne : lignes){
//            System.out.println(ligne);
//        }

        // ------- Recuperation des fichiers requis -------
        List<String> requiredFiles = ReadCaracteristique("[","RequiredFiles","]",caracteristiquePath);
        for(String requiredFile : requiredFiles){
            String[] fileNames = requiredFile.split("[/\\\\]");
            String fileName = fileNames[fileNames.length - 1];

            InputStream requiredFileIn = ClassLoader.getSystemResourceAsStream(requiredFolderPath + "/" + fileName);
            if(requiredFileIn == null){
                throw new Exception("Cannot find requiredFile \""+requiredFile+"\" in required files folder path \""+requiredFolderPath+"\"");
            }

            String folderDestination = generatePath + File.separator + requiredFile;
            // Enlever le nom du fichier de son chemin pour obtenir son repertoire parent
            folderDestination = folderDestination.replace(fileName,"");
            File folder = new File(folderDestination);
            if(!folder.exists()){
                boolean created = folder.mkdirs();
            }

            Path destinationPath = Path.of(folderDestination + File.separator + fileName);
            if(!Files.exists(destinationPath)){
                methods.copyFile(requiredFileIn, (folderDestination + "/" + fileName));
            }
        }
        // --------------------------------------------------
        // ------- Creation et ecriture dans un fichier -------
        Optional<String> optionalExtension = ReadCaracteristique("[","Extension","]",caracteristiquePath).stream().findFirst();
        Optional<String> optionalClassPath = ReadCaracteristique("[","ClassPath","]",caracteristiquePath).stream().findFirst();

        if(optionalExtension.isPresent() && optionalClassPath.isPresent()){
            String extension = optionalExtension.get();

            File classDir = new File(generatePath + File.separator + optionalClassPath.get());
            if(!classDir.exists()){
                boolean created = classDir.mkdir();
            }
            File classFile = new File(classDir.getCanonicalPath() + File.separator + methods.UpperFirstChar(tableName) + "." + extension);
//            System.out.println("Chemin d'acces: "+classDir.getCanonicalPath());
            if(!classFile.exists()){
                boolean created = classFile.createNewFile();
            }

            Files.write(Path.of(classFile.getCanonicalPath()), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            for(String ligne : lignes){
                Files.write(Path.of(classFile.getCanonicalPath()), (ligne + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
            }
        }else{
            throw new Exception("The default [Extension] and/or [ClassPath] to generate Class in the caracteristics file maybe missing");
        }
        // ----------------------------------------------------

        if(closed){
            cnx.close();
        }
    }

    public String getLoginFunction(String templateFolder, String tableName, String language, Map<String,String> mappingVariables) throws IOException {
        String loginFunctionPath = templateFolder + "/" + language.toLowerCase() + "/" + language.toLowerCase() + "LoginFunction.cfg";

        StringBuilder replaced = new StringBuilder();
        List<String> lignes = methods.readLines(loginFunctionPath);
        for(String ligne : lignes){
            ligne = ReplaceSimpleVariable(ligne, mappingVariables);
            if(lignes.indexOf(ligne) == lignes.size()-1){
                replaced.append(ligne);
            }else{
                replaced.append(ligne).append("\n");
            }
        }

        return replaced.toString();
    }
    public String getAuthentificationChecking(String caracteristiquePath, Map<String,String> mappingVariables) throws IOException, URISyntaxException {
        List<String> authCheckLines = ReadCaracteristique("[","AuthChecking","]", caracteristiquePath);

        StringBuilder replaced = new StringBuilder();
        for(String ligne : authCheckLines){
            ligne = ReplaceSimpleVariable(ligne, mappingVariables);
            if(authCheckLines.indexOf(ligne) == authCheckLines.size()-1){
                replaced.append(ligne);
            }else{
                replaced.append(ligne).append("\n");
            }
        }

        return replaced.toString();
    }

    public void GenerateController(Connection cnx, String templateFolder, String generatePath, String tableName, String language, String packageName, LoginInfo loginInfo) throws Exception {
        List<String> finalLignes = new ArrayList<>();

        boolean closed = false;
        if(cnx.isClosed()){
            cnx = Connex.getConnection();
            closed = true;
        }

        System.out.println("    generating controller for "+methods.UpperFirstChar(tableName)+" ...");

        if(!TableExist(cnx, tableName)){
            throw new Exception("Table "+tableName+" does not exist in "+Connex.getDbName());
        }

        String caracteristiquePath = templateFolder + "/" + language.toLowerCase() + "/" + language.toLowerCase() + "Caracteristique.cfg";
        String controllerPath = templateFolder + "/" + language.toLowerCase() + "/" + language.toLowerCase() + "Controller.cfg";

        List<String> listImports = this.ReadCaracteristique("[","ControllerImport","]", caracteristiquePath);

        List<TypeAndName> fields = GetTableFields(cnx, templateFolder, language, tableName);
        TypeAndName classPK = GetPrimaryKey(fields);

        Map<String, List<String>> mappingListes = new HashMap<>();
        mappingListes.put("import", listImports);

        Map<String, String> mappingVariables = new HashMap<>();
        mappingVariables.put("package", packageName);
        mappingVariables.put("class", methods.UpperFirstChar(tableName));
        mappingVariables.put("classVariable", tableName.toLowerCase());
        mappingVariables.put("classPrefixe", methods.GetPrefixe(tableName, 3));
        if(classPK != null){
            mappingVariables.put("classPK", classPK.getColumnName().toLowerCase());
        }else{
            mappingVariables.put("classPK", "id");
        }
        String loginLines = "";
        if(loginInfo.isLogin()){
            loginLines = getLoginFunction(templateFolder, tableName, language, mappingVariables);
        }
        mappingVariables.put("LoginFunction", loginLines);

        List<String> allSyntaxes = ReadCaracteristique("[","Syntaxe","]",caracteristiquePath);
        Map<String, String> mappingSyntaxes = new HashMap<>();
        for(String syntaxe : allSyntaxes){
            String[] syntaxes = methods.SplitInTwo(syntaxe, ":");
            mappingSyntaxes.put(syntaxes[0].replace("#",""), syntaxes[1]);
        }


        List<String> controllerLignes = methods.readLines(controllerPath);
        for(String ligne : controllerLignes){
            if(ligne.contains("classPK") && ligne.contains("classPrefixe") && classPK != null){
                if(!classPK.getDatabaseType().contains("char")){
                    ligne = "";
                }
            }
            ligne = ReplaceVerticaleVariableWithoutField(ligne,mappingListes,"[[","]]");
            ligne = ReplaceVerticaleVariable(ligne,fields,mappingListes,"[[","]]");
            ligne = ReplaceSyntaxe(ligne, mappingSyntaxes);
            ligne = ReplaceSimpleVariable(ligne,mappingVariables);

            finalLignes.add(ligne);
        }

//        for(String ligne : finalLignes){
//            System.out.println(ligne);
//        }

        // ------- Creation et ecriture dans un fichier -------
        Optional<String> optionalExtension = ReadCaracteristique("[","Extension","]",caracteristiquePath).stream().findFirst();
        Optional<String> optionalControllerPath = ReadCaracteristique("[","ControllerPath","]",caracteristiquePath).stream().findFirst();

        if(optionalExtension.isPresent() && optionalControllerPath.isPresent()){
            String extension = optionalExtension.get();

            File controllerDir = new File(generatePath + File.separator + optionalControllerPath.get());
            if(!controllerDir.exists()){
                boolean created = controllerDir.mkdir();
            }
            File classFile = new File(controllerDir.getCanonicalPath() + File.separator + methods.UpperFirstChar(tableName) + "Controller." + extension);
//            System.out.println("Chemin d'acces: "+controllerDir.getCanonicalPath());
            if(!classFile.exists()){
                boolean created = classFile.createNewFile();
            }

            Files.write(Path.of(classFile.getCanonicalPath()), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            for(String ligne : finalLignes){
                Files.write(Path.of(classFile.getCanonicalPath()), (ligne + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
            }
        }else{
            throw new Exception("The default [Extension] and/or [Controller] to generate Controller in the caracteristics file maybe missing");
        }
        // ----------------------------------------------------

        if(closed){
            cnx.close();
        }
    }

// -------------------------- VIEW ---------------------------------
    public String GetFieldFormEquivalence(Connection cnx ,TypeAndName field, String templateFolder, String language, String viewFormsPath, List<String> inputSyntaxes, List<String> selectSyntaxes, Map<String,String> mappingViewFormType,Map<String,String> mappingVariables) throws Exception {
        mappingVariables.put("field", field.getColumnName().toLowerCase());
        mappingVariables.put("fieldFirstUpper", methods.UpperFirstChar(field.getColumnName()));
        mappingVariables.put("hideValue", "");

        List<String> tempForm = new ArrayList<>();

        if(field.isPrimaryKey()){
            mappingVariables.put("fieldFormType", "hidden");
            mappingVariables.put("hideValue", "hidden");
            for(String input : inputSyntaxes){
                input = ReplaceSimpleVariable(input,mappingVariables);
                tempForm.add(input);
            }
        }else if(field.isForeignKey()){
            List<TypeAndName> fkFields = GetTableFields(cnx,templateFolder,language,field.getReferencedTable());

            String fkClassVariable = field.getReferencedTable().toLowerCase();
            String fkClassPK = "";
            TypeAndName tempFkClassPK = GetPrimaryKey(fkFields);
            if(tempFkClassPK != null){
                fkClassPK = tempFkClassPK.getColumnName().toLowerCase();
            }
            String fkClassLibelle = GetLibelle(fkFields);

            mappingVariables.put("fkClassVariable", fkClassVariable);
            mappingVariables.put("fkClassPK", fkClassPK);
            mappingVariables.put("fkClassLibelle", fkClassLibelle);

            for(String select : selectSyntaxes){
                select = ReplaceSimpleVariable(select,mappingVariables);
                tempForm.add(select);
            }
        }else{
            mappingVariables.put("fieldFormType", mappingViewFormType.get(field.getDatabaseType().toLowerCase()));
            for(String input : inputSyntaxes){
                input = ReplaceSimpleVariable(input,mappingVariables);
                tempForm.add(input);
            }
        }

        StringBuilder sbForm = new StringBuilder();
        for(String form : tempForm){
            sbForm.append(form);
        }

        List<String> commonFormGroup = ReadCaracteristique("[", "form-group","]", viewFormsPath);
        mappingVariables.put("forms", sbForm.toString());

        StringBuilder sbFormGroup = new StringBuilder();
        int i = 0;
        for(String ligne : commonFormGroup){
            ligne = ReplaceSimpleVariable(ligne,mappingVariables);
            if(i == commonFormGroup.size()-1){
                sbFormGroup.append(ligne);
            }else{
                sbFormGroup.append(ligne).append("\n");
            }
            i++;
        }
        return sbFormGroup.toString();
    }

    public void writeNewRouterModule(File appModuleFile, String caracteristiquePath, List<String> newRouters) throws IOException, URISyntaxException {
        List<String> routerCaracs = ReadCaracteristique("[","Router","]", caracteristiquePath);
        String beginning = "";
        String ending = "";
        for(String line : routerCaracs){
            if(line.trim().startsWith("router-beginning")){
                beginning = methods.SplitInTwo(line, ":")[1].trim();
            }
            if(line.trim().startsWith("router-ending")){
                ending = methods.SplitInTwo(line, ":")[1].trim();
            }
        }

        Path appModulePaths = appModuleFile.toPath();
        List<String> appModuleLines = Files.readAllLines(appModulePaths);

        List<String> existingRouter = new ArrayList<>();
        List<String> finalLines = new ArrayList<>();
        boolean begin = false;
        for(int i=0; i< appModuleLines.size(); i++){
            finalLines.add(appModuleLines.get(i));
            if(begin){
                existingRouter.add(appModuleLines.get(i).trim());
            }
            if(methods.RemoveSpaceBetween(appModuleLines.get(i)).equals(methods.RemoveSpaceBetween(beginning))){
                begin = true;
            }
            if(begin && methods.RemoveSpaceBetween(appModuleLines.get(i+1)).equals(methods.RemoveSpaceBetween(ending))){
                String indentation = methods.getIndentation(appModuleLines.get(i));
                for(String router : newRouters){
                    if(!existingRouter.contains(router.trim())){
                        finalLines.add(indentation + router);
                    }
                }
                begin = false;
            }
        }

        // --- creer un fichier pour sauvegarder le precedent contenu du fichier de module ---
        //System.out.println("================= APP MODULE ================");
        File appModuleDirectory = appModuleFile.getParentFile();
//        long currentTime = System.currentTimeMillis();
        File tempAppModule = new File(appModuleDirectory.getCanonicalPath() + File.separator + ".tempAppModule");
        if(tempAppModule.createNewFile()){
            for(String line : appModuleLines){
                Files.write(tempAppModule.toPath(), (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
            }
            Files.setAttribute(tempAppModule.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
        }
        // --- Ici, on remplace les lignes du fichier ---
        Files.write(appModulePaths, "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        for(String line : finalLines){
            Files.write(appModulePaths, (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
        }
        // ----------------------------------------------

    }

    public void writeNewContextProxyConfig(File proxyConfigFile, String caracteristiquePath, List<String> newRouters) throws IOException, URISyntaxException {
        List<String> apiProxyCaracs = ReadCaracteristique("[","ApiProxy","]", caracteristiquePath);
        String beginning = "";
        String ending = "";
        for(String line : apiProxyCaracs){
            if(line.trim().startsWith("api-proxy-beginning")){
                beginning = methods.SplitInTwo(line, ":")[1].trim();
            }
            if(line.trim().startsWith("api-proxy-ending")){
                ending = methods.SplitInTwo(line, ":")[1].trim();
            }
        }

        Path proxyConfigPaths = proxyConfigFile.toPath();
        List<String> proxyConfigLines = Files.readAllLines(proxyConfigPaths);

        List<String> existingContext = new ArrayList<>();
        List<String> finalLines = new ArrayList<>();
        boolean begin = false;
        for(int i=0; i< proxyConfigLines.size(); i++){
            finalLines.add(proxyConfigLines.get(i));
            if(begin){
                existingContext.add(proxyConfigLines.get(i).trim());
            }
            if(methods.RemoveSpaceBetween(proxyConfigLines.get(i)).equals(methods.RemoveSpaceBetween(beginning))){
                begin = true;
            }
            if(begin && methods.RemoveSpaceBetween(proxyConfigLines.get(i+1)).equals(methods.RemoveSpaceBetween(ending))){
                String indentation = methods.getIndentation(proxyConfigLines.get(i));
                for(String router : newRouters){
                    if(!existingContext.contains(router.trim())){
                        finalLines.add(indentation + router);
                    }
                }
                begin = false;
            }
        }

        // --- creer un fichier pour sauvegarder le precedent contenu du fichier de module ---
        File proxyConfigDirectory = proxyConfigFile.getParentFile();
        File tempAppModule = new File(proxyConfigDirectory.getCanonicalPath() + File.separator + ".tempProxyConfig");
        if(tempAppModule.createNewFile()){
            for(String line : proxyConfigLines){
                Files.write(tempAppModule.toPath(), (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
            }
            Files.setAttribute(tempAppModule.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
        }
        // --- Ici, on remplace les lignes du fichier ---
        Files.write(proxyConfigPaths, "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        for(String line : finalLines){
            Files.write(proxyConfigPaths, (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
        }
        // ----------------------------------------------
    }

    public void ManageMenu(String templateFolder,String generatePath, String language, String viewFramework, Map<String,String> mappingVariables, List<String> cruds, List<String> menuCaracteristiques, List<String> menuConfigs) throws Exception {
        String viewFolder = templateFolder + "/" + language.toLowerCase() + "/" + "views" + "/" + viewFramework.toLowerCase();
        String caracteristiquePath = viewFolder + "/" + viewFramework.toLowerCase() + "Caracteristique.cfg";

        Optional<String> optionalViewPath = ReadCaracteristique("[","ViewPath","]",caracteristiquePath).stream().findFirst();
        String viewGenerationPath = "";
        if(optionalViewPath.isPresent()){
            viewGenerationPath = generatePath + File.separator + optionalViewPath.get();
        }else{
            throw new Exception("[ViewPath] from "+viewFramework+" caracteristic file maybe missing ?");
        }

        String menuConfigPath = viewFolder + "/" + ReadCaracAttribut("menu-config-path", menuCaracteristiques);

        String fileToCreateInClientApp = ReadCaracAttribut("menu-in-client-app", menuConfigs);
        fileToCreateInClientApp = ReplaceSimpleVariable(fileToCreateInClientApp, mappingVariables);

        List<String> menuTemplateContent = methods.readLines(viewFolder + "/" + ReadCaracAttribut("menu-in-template", menuConfigs));

        String requireCreateFileCommande = ReadCaracAttribut("command-required", menuConfigs);
        if(requireCreateFileCommande.equalsIgnoreCase("true")){
            String currentCommand = ReadCaracAttribut("command-file-generation", menuConfigs);
            currentCommand = ReplaceSimpleVariable(currentCommand, mappingVariables);

            System.out.println(currentCommand + " ...");
            ProcessBuilder genProcessBuilder = new ProcessBuilder("cmd.exe", "/c", currentCommand);
            genProcessBuilder.directory(new File(viewGenerationPath));
            Process genProcess = genProcessBuilder.start();
            genProcess.waitFor();

            int genExitCode = genProcess.exitValue();
            File fileInClientApp = methods.findFile(new File(viewGenerationPath), fileToCreateInClientApp);
            if(genExitCode != 0){
                System.out.println("    "+currentCommand+" exit code "+genExitCode);
            }else{
                Files.write(fileInClientApp.toPath(), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                for(String line : menuTemplateContent){
                    Files.write(fileInClientApp.toPath(), (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                }
            }
        }else{
            File fileInClientApp = new File(viewGenerationPath + File.separator + fileToCreateInClientApp);
            if(!fileInClientApp.exists()){
                boolean parentCreated = fileInClientApp.getParentFile().mkdirs();
                boolean fileCreated = fileInClientApp.createNewFile();

                for(String line : menuTemplateContent){
                    Files.write(fileInClientApp.toPath(), (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                }
            }
        }

        boolean requireMenuRouter = ReadCaracAttribut("menu-router-required", menuConfigs).equalsIgnoreCase("true");
        if(requireMenuRouter){
            String menuRouterLink = ReadCaracAttribut("menu-router-link", menuConfigs);
            menuRouterLink = ReplaceSimpleVariable(menuRouterLink, mappingVariables);
            List<String> newRouters = List.of(new String[]{menuRouterLink});

            List<String> routerConfigs = ReadCaracteristique("[","Router","]",caracteristiquePath);
            String routerModuleFileString = ReadCaracAttribut("router-module-file",routerConfigs);
            File routerModuleFile = methods.findFile(new File(viewGenerationPath), routerModuleFileString);

            if(routerModuleFile != null){
                writeNewRouterModule(routerModuleFile, caracteristiquePath, newRouters);
            }
        }

        List<String> rowsChild = ReadCaracteristique("[","NewRowChild","]", menuConfigPath);
        List<String> colsChild = ReadCaracteristique("[","NewColChildren","]", menuConfigPath);

        StringBuilder colChildrenSB = new StringBuilder();
        String defaultIndentation = methods.getDefaultIndentation(rowsChild);
        for(String crud : cruds){
            mappingVariables.put("crud", crud.toLowerCase());
            mappingVariables.put("crudUpperFirst", methods.UpperFirstChar(crud));
            for(String line : colsChild){
                String replacedLine = ReplaceSimpleVariable(line, mappingVariables);
                if(colChildrenSB.isEmpty()){
                    colChildrenSB.append(replacedLine).append(System.lineSeparator());
                }else{
                    colChildrenSB.append(defaultIndentation).append(replacedLine).append(System.lineSeparator());
                }
            }
        }
        mappingVariables.put("colChildren", colChildrenSB.toString());

        File fileInClientApp = methods.findFile(new File(viewGenerationPath), fileToCreateInClientApp);
        if(fileInClientApp != null){
            Path fileInClientPath = fileInClientApp.toPath();
            String menuListBeginning = ReadCaracAttribut("menu-list-beginning", menuConfigs);
            List<String> lines = Files.readAllLines(fileInClientPath);
            if(lines.isEmpty()){
                Files.write(fileInClientApp.toPath(), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                for(String line : menuTemplateContent){
                    Files.write(fileInClientApp.toPath(), (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                }
                lines = Files.readAllLines(fileInClientPath);
            }else{
                if(lines.size() == 1 && lines.get(0).isBlank()){
                    Files.write(fileInClientApp.toPath(), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                    for(String line : menuTemplateContent){
                        Files.write(fileInClientApp.toPath(), (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                    }
                    lines = Files.readAllLines(fileInClientPath);
                }
            }
            List<String> finalLines = new ArrayList<>();
            for(String line : lines){
                finalLines.add(line);
                if(line.equals(menuListBeginning)){
                    for(String rowLine : rowsChild){
                        String replacedLine = ReplaceSimpleVariable(rowLine, mappingVariables);
                        finalLines.add(replacedLine);
                    }
                }
            }

            Files.write(fileInClientPath, "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            for(String line : finalLines){
                Files.write(fileInClientPath, (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
            }
        }
    }

    public void GenerateView(Connection cnx, String templateFolder, String generatePath, String tableName, String language,String viewFramework, LoginInfo loginInfo, String authTable) throws Exception {
        if(!TableExist(cnx, tableName)){
            throw new Exception("Table "+tableName+" does not exist in "+Connex.getDbName());
        }

        String viewFolder = templateFolder + "/" + language.toLowerCase() + "/" + "views" + "/" + viewFramework.toLowerCase();

        String caracteristiquePath = viewFolder + "/" + viewFramework.toLowerCase() + "Caracteristique.cfg";
        String viewFormsPath = viewFolder + "/" + viewFramework.toLowerCase() + "Forms.cfg";

        File generateFile = new File(generatePath);

        Optional<String> optionalViewPath = ReadCaracteristique("[","ViewPath","]",caracteristiquePath).stream().findFirst();
        String viewGenerationPath = "";
        if(optionalViewPath.isPresent()){
            viewGenerationPath = optionalViewPath.get();
        }else{
            throw new Exception("[ViewPath] from "+viewFramework+" caracteristic file maybe missing ?");
        }
        viewGenerationPath = generatePath + File.separator + viewGenerationPath.trim();
//        System.out.println("VIEW GENERATION PATH: "+viewGenerationPath);

        String className = methods.UpperFirstChar(tableName);
        String classVariable = tableName.toLowerCase();
        List<TypeAndName> fields = GetTableFields(cnx,(templateFolder + "/" + language.toLowerCase() + "/" + "views"),viewFramework,tableName);
        TypeAndName classPK = GetPrimaryKey(fields);

        // ------ MENU -------
        List<String> menuCaracteristiques = ReadCaracteristique("[","NavMenu","]",caracteristiquePath);
        String menuConfigPath = viewFolder + "/" + ReadCaracAttribut("menu-config-path", menuCaracteristiques);
        List<String> menuConfigs = ReadCaracteristique("[","Config","]",menuConfigPath);

        String menuName = ReadCaracAttribut("menu-name", menuConfigs);
        String menuLink = ReadCaracAttribut("menu-link", menuConfigs);
        // -------------------

        String loginUsermail = "";
        String loginUsermailUpperFirst = "";
        String loginKey = "";
        String loginKeyUpperFirst = "";
        if(loginInfo.isLogin()){
            loginUsermail = loginInfo.getUsermail();
            loginKey = loginInfo.getKey();
            loginUsermailUpperFirst = methods.UpperFirstChar(loginUsermail);
            loginKeyUpperFirst = methods.UpperFirstChar(loginKey);
        }
        String authTableUpperFirst = "";
        if(!authTable.isBlank()){
            authTableUpperFirst = methods.UpperFirstChar(authTable);
        }

        Map<String,String> mappingVariables = new HashMap<String, String>();
        mappingVariables.put("class", className);
        mappingVariables.put("classVariable", classVariable);
        mappingVariables.put("classLibelle", GetLibelle(fields));
        if(classPK != null){
            mappingVariables.put("classPK", GetPrimaryKey(fields).getColumnName().toLowerCase());
        }else{
            mappingVariables.put("classPK", "id");
        }
        mappingVariables.put("menuName", menuName);
        mappingVariables.put("menuNameUpperFirst", methods.UpperFirstChar(menuName));
        mappingVariables.put("menuLink", menuLink);
        mappingVariables.put("authTableVariable", authTable.toLowerCase());
        mappingVariables.put("authTable", authTableUpperFirst);
        mappingVariables.put("usermail", loginUsermail);
        mappingVariables.put("usermailFirstUpper", loginUsermailUpperFirst);
        mappingVariables.put("key", loginKey);
        mappingVariables.put("keyFirstUpper", loginKeyUpperFirst);
        String authCheckLines = "";
        if(loginInfo.isNeedAuth()){
            authCheckLines = getAuthentificationChecking(caracteristiquePath, mappingVariables);
        }
        mappingVariables.put("authChecking", authCheckLines);

        Map<String,List<String>> mappingHV = new HashMap<String, List<String>>();

        List<String> classVariables = new ArrayList<>();
        List<String> listClassVariables = new ArrayList<>();
        List<String> fieldsString = new ArrayList<>();
        List<String> fieldsLowerString = new ArrayList<>();
        List<String> listFieldsString = new ArrayList<>();
        List<String> listFieldsLowerString = new ArrayList<>();
        List<String> foreignKeysRefs = new ArrayList<>();
        for(TypeAndName field : fields){
            classVariables.add(classVariable);
            listClassVariables.add(field.getReferencedTable().toLowerCase());

            fieldsString.add(field.getColumnName());
            fieldsLowerString.add(field.getColumnName().toLowerCase());

            if(field.isForeignKey()){
                String fkLibelle = GetLibelle(GetTableFields(cnx,(templateFolder + "/" + language.toLowerCase() + "/" + "views"),viewFramework,field.getReferencedTable()));
                listFieldsString.add(methods.UpperFirstChar(fkLibelle));
                listFieldsLowerString.add(fkLibelle.toLowerCase());
            }else{
                listFieldsString.add(field.getColumnName());
                listFieldsLowerString.add(field.getColumnName().toLowerCase());
            }
        }
        mappingHV.put("classVariable", classVariables);
        mappingHV.put("listClassVariable", listClassVariables);
        mappingHV.put("field", fieldsString);
        mappingHV.put("fieldLower", fieldsLowerString);
        mappingHV.put("listField", listFieldsString);
        mappingHV.put("listFieldLower", listFieldsLowerString);

        // --------- Creating formular group ----------
        List<String> formGroup = new ArrayList<>();

        Map<String,String> mappingViewFormType = GetViewFormType(caracteristiquePath, viewFramework);

        List<String> selectSyntaxes = ReadCaracteristique("[","select","]",viewFormsPath);
        List<String> inputSyntaxes = ReadCaracteristique("[","input","]",viewFormsPath);

        List<String> fieldsForms = new ArrayList<>();
        for(TypeAndName field : fields){
            String fieldFormEquivalence = GetFieldFormEquivalence(cnx,field,templateFolder,language,viewFormsPath,inputSyntaxes,selectSyntaxes,mappingViewFormType,mappingVariables);
            fieldsForms.add(fieldFormEquivalence);
            if(field.isForeignKey()){
                foreignKeysRefs.add(field.getReferencedTable().toLowerCase());
            }
        }

        mappingHV.put("formGroup", fieldsForms);
        mappingHV.put("classFK", foreignKeysRefs);

        // --------------------------------------------

        List<String> listCrud = ReadCaracteristique("[", "ListCRUD", "]", caracteristiquePath);
        if(loginInfo.isLogin()){
            listCrud.add("Login");
        }
        List<String> filesToCreates = ReadCaracteristique("[", "FileToCreate", "]", caracteristiquePath);

    // ----- Si la creation de fichier necessite une commande speciale au framework -----
        List<String> createFileCommands = ReadCaracteristique("[","CreateFileCommand","]",caracteristiquePath);
        boolean needCommand = ReadCaracAttribut("command-required", createFileCommands).equalsIgnoreCase("true");
        String commandToCreateFile = "";

    // ------ Creation du fichier ------
        if(needCommand){
            commandToCreateFile = ReadCaracAttribut("command-file-generation", createFileCommands);
        }
    // ---------------------------------
    // ----------------------------------------------------------------------------------

        // -------- Ecrire les Router si requis -----------
        List<String> routerConfigs = ReadCaracteristique("[","Router","]", caracteristiquePath);

        boolean routerRequired = ReadCaracAttribut("router-required", routerConfigs).equalsIgnoreCase("true");
        String routerModuleFileName = "";
        if(routerRequired){
            routerModuleFileName = ReadCaracAttribut("router-module-file", routerConfigs);
        }

        File routerModuleFile = methods.findFile(generateFile, routerModuleFileName);
        if(routerModuleFile == null){
            throw new Exception("Cannot find router module file \""+routerModuleFileName+"\" in \""+generatePath+"\" \n     Advice: disable Router or verify 'router-module-file' in caracteristique or else verify file existence");
        }
        // ----------- Si un on a besoin d'ecrire les routes dans un Proxy ----------
        List<String> proxyConfigs = ReadCaracteristique("[","ApiProxy","]", caracteristiquePath);

        boolean apiProxyRequired = ReadCaracAttribut("api-proxy-required", proxyConfigs).equalsIgnoreCase("true");
        String apiProxyFileName = "";
        if(apiProxyRequired){
            apiProxyFileName = ReadCaracAttribut("api-proxy-file", proxyConfigs);
        }

        File apiProxyConfigFile = methods.findFile(generateFile, apiProxyFileName);
        if(apiProxyConfigFile == null){
            throw new Exception("Cannot find api proxy config file \""+apiProxyFileName+"\" in \""+generatePath+"\" \n     Advice: disable Proxy Config or verify 'api-proxy-file' in caracteristique or else verify file existence");
        }
        // --------------------------------------------------------------------------

        for(String crud : listCrud){
            mappingVariables.put("crud", crud.toLowerCase());
            mappingVariables.put("crudUpperFirst", methods.UpperFirstChar(crud));
            String crudTemplatePath = viewFolder + "/" + "crud" + "/" + crud + "Template.cfg";


            // *** EXECUTER LA COMMANDE commandToCreateFile ICI ***
            if(needCommand){
                String currentCommand = ReplaceSimpleVariable(commandToCreateFile, mappingVariables);
//                System.out.println("Commande to create file: "+currentCommand);

                System.out.println(currentCommand + " ...");
                ProcessBuilder genProcessBuilder = new ProcessBuilder("cmd.exe", "/c", currentCommand);
                genProcessBuilder.directory(new File(viewGenerationPath));
//                System.out.println("Gen WORKING DIRECTORY: "+genProcessBuilder.directory().getAbsolutePath());
                Process genProcess = genProcessBuilder.start();
                genProcess.waitFor();

                int genExitCode = genProcess.exitValue();
                if(genExitCode != 0){
                    System.out.println("    "+currentCommand+" exit code "+genExitCode);
                }
            }else{  // creer manuellement les fichiers de FileCreate
                System.out.println(" ");
            }

            // changer de directory dans "viewGenerationPath" puis executer la commande
            for(String file : filesToCreates){
                List<String> replacedLignes = new ArrayList<>();

                String fileType = file.split(":")[0];
                String fileName = file.split(":")[1];
                fileName = ReplaceSimpleVariable(fileName,mappingVariables);
//                System.out.println("-----------"+fileName+"-----------");

                InputStream crudTemplateIn = ClassLoader.getSystemResourceAsStream(crudTemplatePath);
                if(crudTemplateIn != null){
                    List<String> fileTemplateContents = ReadCaracteristiqueIncludeBlank("[[", fileType, "]]", crudTemplatePath);
                    for(String templateLine : fileTemplateContents){
                        templateLine = ReplaceVerticaleVariable(templateLine,fields,mappingHV,"[","]");
                        templateLine = ReplaceSimpleVariable(templateLine,mappingVariables);

                        replacedLignes.add(templateLine);
//                        System.out.println(templateLine);
                    }
                }

                // *** ECRIRE TOUS replacedLignes DANS LE FICHIER TS ET HTML ***
                File componentFile = methods.findFile(generateFile, fileName);
                if(componentFile == null){
                    throw new Exception("Cannot find "+fileName+" in "+viewGenerationPath+" to write in");
                }else{
                    Files.write(componentFile.toPath(), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                    for(String line : replacedLignes){
                        Files.write(componentFile.toPath(), (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                    }
                }
            }

            // ----- maintenant si le framework a besoin de router,on ecrit chaque router dans un fichier -----
            if(routerRequired){
                List<String> routerPathsTemplate = ReadCaracteristique("[","ListRouter","]", caracteristiquePath);
                List<String> listRouterPaths = new ArrayList<>();
                for(String routerPath : routerPathsTemplate){
                    listRouterPaths.add(ReplaceSimpleVariable(routerPath, mappingVariables));
                }
                // ----- ECRIRE DANS LE FICHIER CIBLE -----
                writeNewRouterModule(routerModuleFile, caracteristiquePath, listRouterPaths);
            }
            if(apiProxyRequired){
                List<String> listContextProxyTemplate = ReadCaracteristique("[","ProxyConfContext","]", caracteristiquePath);
                List<String> listContextProxy = new ArrayList<>();
                for(String context : listContextProxyTemplate){
                    listContextProxy.add(ReplaceSimpleVariable(context, mappingVariables));
                }
                // ----- ECRIRE DANS LE FICHIER CIBLE -----
                writeNewContextProxyConfig(apiProxyConfigFile, caracteristiquePath, listContextProxy);
            }
            // ------------------------------------------------------------------------------------------------
        }
        ManageMenu(templateFolder,generatePath,language,viewFramework,mappingVariables,listCrud,menuCaracteristiques,menuConfigs);

        System.out.println("\n      Done !");
    }
}

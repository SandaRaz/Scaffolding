package generator;

import cnx.Connex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Generator {
    DaoGenerator daoGenerator;
    public Generator(){
        this.daoGenerator = new DaoGenerator(this);
    }

    // --------------- FUNCTION -----------------
    public String UpperFirstChar(String name){
        return name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();
    }
    public String GetExtension(String filename){
        String extension = "";
        int i = filename.lastIndexOf('.');
        if(i > 0){
            extension = filename.substring(i + 1);
        }
        return extension;
    }

    public String GetIndentation(String ligne){
        Pattern pattern = Pattern.compile("^(\\s+)");
        Matcher matcher = pattern.matcher(ligne);
        String indentation = "";
        if(matcher.find()){
            indentation = matcher.group(1);
        }
        return indentation;
    }

    public String RemoveSpace(String word){
        return word.replace(" ","");
    }
    public String[] SplitInTwo(String word, String splitter){
        String[] tab = new String[2];
        int i = word.indexOf(splitter);
        if(i > 0){
            tab[0] = word.substring(0,i);
            tab[1] = word.substring(i + 1);
        }else{
            tab[0] = word;
            tab[1] = "";
        }
        return tab;
    }
    public String GetPrefixe(String word, int len){
        if(word.length() < len){
            return word.toUpperCase();
        }else{
            return word.substring(0, len).toUpperCase();
        }
    }
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
    public List<String> ReadCaracteristique(String opening, String caracteristiques, String closing, String filePath) throws IOException {
        List<String> lines = new ArrayList<>();

        List<String> fileLines = Files.readAllLines(Paths.get(filePath));
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

    public List<String> ReadCaracteristiqueIncludeBlank(String opening, String caracteristiques, String closing, String filePath) throws IOException {
        List<String> lines = new ArrayList<>();

        List<String> fileLines = Files.readAllLines(Paths.get(filePath));
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

    public String GetSyntaxe(String syntaxe, String filePath) throws IOException {
        String languageSyntaxe = "";

        List<String> fileLines = this.ReadCaracteristique("[","Syntaxe","]", filePath);
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

    public Map<String,String> GetTypeDatabaseMatching(String templatePath, String language) throws Exception {
        String caracteristiquesPath = templatePath + "/" + language.toLowerCase() + "/" + language.toLowerCase()+"Caracteristique.cfg";
        List<String> typesMatching = ReadCaracteristique("[","TypeDatabaseMatching","]", caracteristiquesPath);

        Map<String,String> map = new HashMap<String,String>();
        for(String line : typesMatching){
            String[] imports = line.split(":");
            if(imports.length == 2){
                map.put(imports[0],imports[1]);
            }
        }
        return map;
    }

    public Map<String,String> GetViewFormType(String caracteristiquesPath, String viewFramework) throws Exception {
        List<String> typesMatching = ReadCaracteristique("[","ViewFormType","]", caracteristiquesPath);

        Map<String,String> map = new HashMap<String,String>();
        for(String line : typesMatching){
            String[] imports = line.split(":");
            if(imports.length == 2){
                map.put(imports[0],imports[1]);
            }
        }
        return map;
    }

    public List<TypeAndName> GetTableFields(Connection cnx, String templatePath, String language,String table) throws Exception {
        table = table.toLowerCase();

        List<TypeAndName> fields = new ArrayList<>();

        boolean closed = false;
        if(cnx.isClosed()){
            cnx = Connex.getConnection();
            closed = true;
        }

        Map<String,String> fieldRealType = GetTypeDatabaseMatching(templatePath, language);

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
                    referencedTable = UpperFirstChar(foreignKeys.getString("PKTABLE_NAME"));
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

    public Map<String, String> GetFieldRequiredImports(String fileCaracPath) throws IOException {
        List<String> lines = this.ReadCaracteristique("[","ClassFieldImport","]", fileCaracPath);
        Map<String,String> map = new HashMap<String,String>();
        for(String line : lines){
            String[] imports = line.split(":");
            if(imports.length == 2){
                map.put(imports[0],imports[1]);
            }
        }
        return map;
    }

    public String GettersSetters(String templatePath, String language, TypeAndName field) throws IOException {
        StringBuilder validGetterSetter = new StringBuilder();

        String caracteristiquePath = templatePath + "/" + language.toLowerCase() + "/" + language.toLowerCase() + "Caracteristique.cfg";
        List<String> getsetLines = this.ReadCaracteristique("[","GetSet","]", caracteristiquePath);

        Map<String, String> variableMapping = new HashMap<>();

        String type = field.getColumnType();
        String FieldName = UpperFirstChar(field.getColumnName());
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

    public void MakeConstructor(String fileCaracPath, Map<String,String> mapppingVariables) throws IOException {
        List<String> constructorLines = this.ReadCaracteristique("[","VoidConstructor","]", fileCaracPath);

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

    public String IdentifySyntaxe(String caracteristiquePath, String ligne, StringBuilder currentSyntaxe, AtomicBoolean isSyntaxe, char character, char equivalence) throws IOException {
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

    public void GenerateClass(Connection cnx, String templatePath, String generatePath, String tableName, String language, String packageName) throws Exception {
        List<String> lignes = new ArrayList<>();

        boolean closed = false;
        if(cnx.isClosed()){
            cnx = Connex.getConnection();
            closed = true;
        }

        String caracteristiquePath = templatePath + "/" + language.toLowerCase() + "/" + language.toLowerCase() + "Caracteristique.cfg";
        String buildPath = templatePath + File.separator + language.toLowerCase() + File.separator + "build";

        Map<String, String> importsMap = this.GetFieldRequiredImports(caracteristiquePath);

        List<String> listImports = new ArrayList<>();
        List<String> listFields = new ArrayList<>();
        List<String> listGetSet = new ArrayList<>();

        List<TypeAndName> fields = this.GetTableFields(cnx, templatePath, language, tableName);
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
            listGetSet.add(this.GettersSetters(templatePath,language,field));
        }
        // Collection.addAll(Collection)
        listImports.addAll(this.ReadCaracteristique("[","DaoImport","]", caracteristiquePath));

        Map<String, List<String>> mappingListes = new HashMap<>();
        mappingListes.put("import", listImports);
        mappingListes.put("fields", listFields);
        mappingListes.put("GetSet", listGetSet);
        Map<String, String> mappingVariables = new HashMap<>();
        mappingVariables.put("package", packageName);
        mappingVariables.put("class", UpperFirstChar(tableName));
        daoGenerator.crudDAOToMap(cnx,tableName,templatePath, language, mappingVariables);
        this.MakeConstructor(caracteristiquePath, mappingVariables);

        List<String> mappingLignes = Files.readAllLines(Paths.get(templatePath + "/mapping.cfg"));
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
            //System.out.println(ligne);
//        }

        // ------- Recuperation des fichiers requis -------
        List<String> requiredFiles = ReadCaracteristique("[","RequiredFiles","]",caracteristiquePath);
        for(String file : requiredFiles){
            String[] fileNames = file.split("[/\\\\]");
            String fileName = fileNames[fileNames.length - 1];

            String filePathInBuild = "";
            filePathInBuild = findFile(filePathInBuild,fileName,buildPath);
            if(filePathInBuild.isBlank()){
                throw new Exception("Cannot find requiredFile \""+file+"\" in buildPath \""+buildPath+"\"");
            }

            String folderDestination = generatePath + File.separator + file;
            folderDestination = folderDestination.replace(fileName,"");
            File folder = new File(folderDestination);
            if(!folder.exists()){
                boolean created = folder.mkdirs();
            }

            Path destinationPath = Path.of(folderDestination + File.separator + fileName);
            if(!Files.exists(destinationPath)){
                Files.copy(Path.of(filePathInBuild), destinationPath);
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
            File classFile = new File(classDir.getCanonicalPath() + File.separator + UpperFirstChar(tableName) + "." + extension);
            System.out.println("Chemin d'acces: "+classDir.getCanonicalPath());
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

    public void GenerateController(Connection cnx, String templatePath, String generatePath, String tableName, String language, String packageName) throws Exception {
        List<String> finalLignes = new ArrayList<>();

        boolean closed = false;
        if(cnx.isClosed()){
            cnx = Connex.getConnection();
            closed = true;
        }

        String caracteristiquePath = templatePath + "/" + language.toLowerCase() + "/" + language.toLowerCase() + "Caracteristique.cfg";
        String controllerPath = templatePath + "/" + language.toLowerCase() + "/" + language.toLowerCase() + "Controller.cfg";

        List<String> listImports = this.ReadCaracteristique("[","ControllerImport","]", caracteristiquePath);

        List<TypeAndName> fields = GetTableFields(cnx, templatePath, language, tableName);
        TypeAndName classPK = GetPrimaryKey(fields);

        Map<String, List<String>> mappingListes = new HashMap<>();
        mappingListes.put("import", listImports);

        Map<String, String> mappingVariables = new HashMap<>();
        mappingVariables.put("package", packageName);
        mappingVariables.put("class", UpperFirstChar(tableName));
        mappingVariables.put("classVariable", tableName.toLowerCase());
        mappingVariables.put("classPrefixe", GetPrefixe(tableName, 3));
        if(classPK != null){
            mappingVariables.put("classPK", classPK.getColumnName().toLowerCase());
        }else{
            mappingVariables.put("classPK", "id");
        }

        List<String> allSyntaxes = ReadCaracteristique("[","Syntaxe","]",caracteristiquePath);
        Map<String, String> mappingSyntaxes = new HashMap<>();
        for(String syntaxe : allSyntaxes){
            String[] syntaxes = SplitInTwo(syntaxe, ":");
            mappingSyntaxes.put(syntaxes[0].replace("#",""), syntaxes[1]);
        }


        List<String> controllerLignes = Files.readAllLines(Paths.get(controllerPath));
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

        // ------- Creation et ecriture dans un fichier -------
        Optional<String> optionalExtension = ReadCaracteristique("[","Extension","]",caracteristiquePath).stream().findFirst();
        Optional<String> optionalControllerPath = ReadCaracteristique("[","ControllerPath","]",caracteristiquePath).stream().findFirst();

        if(optionalExtension.isPresent() && optionalControllerPath.isPresent()){
            String extension = optionalExtension.get();

            File controllerDir = new File(generatePath + File.separator + optionalControllerPath.get());
            if(!controllerDir.exists()){
                boolean created = controllerDir.mkdir();
            }
            File classFile = new File(controllerDir.getCanonicalPath() + File.separator + UpperFirstChar(tableName) + "Controller." + extension);
            System.out.println("Chemin d'acces: "+controllerDir.getCanonicalPath());
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
    public String GetFieldFormEquivalence(Connection cnx ,TypeAndName field, String templatePath, String language, String viewFormsPath, List<String> inputSyntaxes, List<String> selectSyntaxes, Map<String,String> mappingViewFormType,Map<String,String> mappingVariables) throws Exception {
        mappingVariables.put("field", field.getColumnName().toLowerCase());
        mappingVariables.put("fieldFirstUpper", UpperFirstChar(field.getColumnName()));
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
            List<TypeAndName> fkFields = GetTableFields(cnx,templatePath,language,field.getReferencedTable());

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

    public String findFile(String filePath, String fileName, String searchPath) throws IOException {
        File searchPathFile = new File(searchPath);
        File[] files = searchPathFile.listFiles();
        assert files != null;
        for(File file : files){
            if(file.getName().trim().equalsIgnoreCase("node_modules")){
                continue;
            }

            if(file.isDirectory()){
                filePath = findFile(filePath, fileName, file.getCanonicalPath());
            }else{
                if(file.getName().trim().equalsIgnoreCase(fileName)){
                    //System.out.println("FileName: "+file.getName());
                    filePath = file.getCanonicalPath();
                    break;
                }
            }
        }

        return filePath;
    }

    public void writeNewRouterModule(String appModulePath, String caracPath, List<String> newRouters) throws IOException {
        List<String> routerCaracs = ReadCaracteristique("[","Router","]", caracPath);
        String beginning = "";
        String ending = "";
        for(String line : routerCaracs){
            if(line.trim().startsWith("router-beginning")){
                beginning = SplitInTwo(line, ":")[1].trim();
            }
            if(line.trim().startsWith("router-ending")){
                ending = SplitInTwo(line, ":")[1].trim();
            }
        }

        Path appModulePaths = Paths.get(appModulePath);
        List<String> appModuleLines = Files.readAllLines(appModulePaths);

        List<String> existingRouter = new ArrayList<>();
        List<String> finalLines = new ArrayList<>();
        boolean begin = false;
        for(int i=0; i< appModuleLines.size(); i++){
            finalLines.add(appModuleLines.get(i));
            if(begin){
                existingRouter.add(appModuleLines.get(i).trim());
            }
            if(RemoveSpace(appModuleLines.get(i)).equals(RemoveSpace(beginning))){
                begin = true;
            }
            if(begin && RemoveSpace(appModuleLines.get(i+1)).equals(RemoveSpace(ending))){
                Pattern pattern = Pattern.compile("^(\\s+)");
                Matcher matcher = pattern.matcher(appModuleLines.get(i));
                String indentation = "";
                if(matcher.find()){
                    indentation = matcher.group(1);
                }
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
        File appModuleDirectory = new File(appModulePath).getParentFile();
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

    public void writeNewContextProxyConfig(String proxyConfigPath, String caracPath, List<String> newRouters) throws IOException {
        List<String> apiProxyCaracs = ReadCaracteristique("[","ApiProxy","]", caracPath);
        String beginning = "";
        String ending = "";
        for(String line : apiProxyCaracs){
            if(line.trim().startsWith("api-proxy-beginning")){
                beginning = SplitInTwo(line, ":")[1].trim();
            }
            if(line.trim().startsWith("api-proxy-ending")){
                ending = SplitInTwo(line, ":")[1].trim();
            }
        }

        Path proxyConfigPaths = Paths.get(proxyConfigPath);
        List<String> proxyConfigLines = Files.readAllLines(proxyConfigPaths);

        List<String> existingContext = new ArrayList<>();
        List<String> finalLines = new ArrayList<>();
        boolean begin = false;
        for(int i=0; i< proxyConfigLines.size(); i++){
            finalLines.add(proxyConfigLines.get(i));
            if(begin){
                existingContext.add(proxyConfigLines.get(i).trim());
            }
            if(RemoveSpace(proxyConfigLines.get(i)).equals(RemoveSpace(beginning))){
                begin = true;
            }
            if(begin && RemoveSpace(proxyConfigLines.get(i+1)).equals(RemoveSpace(ending))){
                Pattern pattern = Pattern.compile("^(\\s+)");
                Matcher matcher = pattern.matcher(proxyConfigLines.get(i));
                String indentation = "";
                if(matcher.find()){
                    indentation = matcher.group(1);
                }
                for(String router : newRouters){
                    if(!existingContext.contains(router.trim())){
                        finalLines.add(indentation + router);
                    }
                }
                begin = false;
            }
        }

        // --- creer un fichier pour sauvegarder le precedent contenu du fichier de module ---
        File proxyConfigDirectory = new File(proxyConfigPath).getParentFile();
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

    public void GenerateView(Connection cnx, String templatePath, String generatePath, String tableName, String language,String viewFramework) throws Exception {
        String viewPath = templatePath + File.separator + language.toLowerCase() + File.separator + "views" + File.separator + viewFramework.toLowerCase();
        String caracteristiquePath = viewPath + File.separator + viewFramework.toLowerCase() + "Caracteristique.cfg";
        String viewFormsPath = viewPath + File.separator + viewFramework.toLowerCase() + "Forms.cfg";

        Optional<String> optionalViewPath = ReadCaracteristique("[","ViewPath","]",caracteristiquePath).stream().findFirst();
        String viewGenerationPath = "";
        if(optionalViewPath.isPresent()){
            viewGenerationPath = optionalViewPath.get();
        }else{
            throw new Exception("[ViewPath] from "+viewFramework+" caracteristic file maybe missing ?");
        }
        viewGenerationPath = generatePath + File.separator + viewGenerationPath.trim();
//        System.out.println("VIEW GENERATION PATH: "+viewGenerationPath);

        String className = UpperFirstChar(tableName);
        String classVariable = tableName.toLowerCase();
        List<TypeAndName> fields = GetTableFields(cnx,(templatePath + File.separator + language.toLowerCase() + File.separator + "views"),viewFramework,tableName);
        TypeAndName classPK = GetPrimaryKey(fields);

        Map<String,String> mappingVariables = new HashMap<String, String>();
        mappingVariables.put("class", className);
        mappingVariables.put("classVariable", classVariable);
        mappingVariables.put("classLibelle", GetLibelle(fields));
        if(classPK != null){
            mappingVariables.put("classPK", GetPrimaryKey(fields).getColumnName().toLowerCase());
        }else{
            mappingVariables.put("classPK", "id");
        }

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
                String fkLibelle = GetLibelle(GetTableFields(cnx,(templatePath + File.separator + language.toLowerCase() + File.separator + "views"),viewFramework,field.getReferencedTable()));
                listFieldsString.add(UpperFirstChar(fkLibelle));
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
            String fieldFormEquivalence = GetFieldFormEquivalence(cnx,field,templatePath,language,viewFormsPath,inputSyntaxes,selectSyntaxes,mappingViewFormType,mappingVariables);
            fieldsForms.add(fieldFormEquivalence);
            if(field.isForeignKey()){
                foreignKeysRefs.add(field.getReferencedTable().toLowerCase());
            }
        }

        mappingHV.put("formGroup", fieldsForms);
        mappingHV.put("classFK", foreignKeysRefs);

        // --------------------------------------------

        List<String> listCrud = ReadCaracteristique("[", "ListCRUD", "]", caracteristiquePath);
        List<String> filesToCreates = ReadCaracteristique("[", "FileToCreate", "]", caracteristiquePath);


    // ----- Si la creation de fichier necessite une commande speciale au framework -----
        List<String> createFileCommand = ReadCaracteristique("[","CreateFileCommand","]",caracteristiquePath);
        boolean needCommand = false;
        String commandToCreateFile = "";
        for(String line : createFileCommand){
            if(line.trim().startsWith("command-required") && line.contains("true")){
                needCommand = true;
                break;
            }
        }

    // ------ Creation du fichier ------
        if(needCommand){
            for(String line : createFileCommand){
                if(line.trim().startsWith("command-file-generation")){
                    commandToCreateFile = line.split(":")[1];
                    break;
                }
            }
        }
    // ---------------------------------
    // ----------------------------------------------------------------------------------

        // -------- Ecrire les Router si requis -----------
        List<String> routerConfigs = ReadCaracteristique("[","Router","]", caracteristiquePath);

        String routerModuleFileName = "";
        boolean routerRequired = false;
        for(String config : routerConfigs){
            if(config.toLowerCase().contains("router-required") && config.toLowerCase().contains("true")){
                routerRequired = true;
                break;
            }
        }
        if(routerRequired){
            for(String config : routerConfigs){
                if(config.toLowerCase().trim().startsWith("router-module-file")){
                    routerModuleFileName = config.split(":")[1];
                }
            }
        }
        String routerModulePath = "";
        routerModulePath = findFile(routerModulePath,routerModuleFileName,generatePath);
        if(routerRequired && routerModulePath.isBlank()){
            throw new Exception("Cannot find router module file \""+routerModuleFileName+"\" in \""+generatePath+"\" \n     Advice: disable Router or verify 'router-module-file' in caracteristique or else verify file existence");
        }else{
            //System.out.println("ROUTER FILE PATH: "+routerModulePath);
        }
        // ------------------------------------------------
        // ----------- Si un on a besoin d'ecrire les routes dans un Proxy ----------
        List<String> proxyConfigs = ReadCaracteristique("[","ApiProxy","]", caracteristiquePath);

        String apiProxyFileName = "";
        boolean apiProxyRequired = false;
        for(String config : proxyConfigs){
            if(config.toLowerCase().contains("api-proxy-required") && config.toLowerCase().contains("true")){
                apiProxyRequired = true;
                break;
            }
        }
        if(apiProxyRequired){
            for(String config : proxyConfigs){
                if(config.toLowerCase().trim().startsWith("api-proxy-file")){
                    apiProxyFileName = config.split(":")[1];
                }
            }
        }
        String apiProxyPath = "";
        apiProxyPath = findFile(apiProxyPath,apiProxyFileName,generatePath);
        if(apiProxyRequired && apiProxyPath.isBlank()){
            throw new Exception("Cannot find api proxy config file \""+apiProxyFileName+"\" in \""+generatePath+"\" \n     Advice: disable Proxy Config or verify 'api-proxy-file' in caracteristique or else verify file existence");
        }else{
            //System.out.println("PROXY-CONF FILE PATH: "+apiProxyPath);
        }
        // --------------------------------------------------------------------------

        for(String crud : listCrud){
            mappingVariables.put("crud", crud.toLowerCase());
            mappingVariables.put("crudUpperFirst", UpperFirstChar(crud));
            String crudTemplatePath = viewPath + File.separator + "crud" + File.separator + crud + "Template.cfg";


            // *** EXECUTER LA COMMANDE commandToCreateFile ICI ***
            if(needCommand){
                String currentCommand = ReplaceSimpleVariable(commandToCreateFile, mappingVariables);
//                System.out.println("Commande to create file: "+currentCommand);

                ProcessBuilder cdProcessBuilder = new ProcessBuilder("cmd.exe", "/c", "cd "+viewGenerationPath);
                cdProcessBuilder.directory(new File(viewGenerationPath));
                Process cdProcess = cdProcessBuilder.start();
                cdProcess.waitFor();
                System.out.println(cdProcessBuilder.command().get(2));

                int cdExitCode = cdProcess.exitValue();
                if(cdExitCode != 0){
                    System.out.println("    cd exit code: "+cdExitCode);
                }

                System.out.println(currentCommand + " ...");
                ProcessBuilder genProcessBuilder = new ProcessBuilder("cmd.exe", "/c", currentCommand);
                genProcessBuilder.directory(new File(viewGenerationPath));
//                System.out.println("Gen WORKING DIRECTORY: "+genProcessBuilder.directory().getAbsolutePath());
                Process genProcess = genProcessBuilder.start();
                genProcess.waitFor();

                int genExitCode = genProcess.exitValue();
                if(genExitCode != 0){
                    System.out.println("    "+commandToCreateFile+" exit code "+genExitCode);
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
                System.out.println("-----------"+fileName+"-----------");

                File crudTemplate = new File(crudTemplatePath);
                if(crudTemplate.exists()){
                    List<String> fileTemplateContents = ReadCaracteristiqueIncludeBlank("[[", fileType, "]]", crudTemplatePath);
                    for(String templateLine : fileTemplateContents){
                        templateLine = ReplaceVerticaleVariable(templateLine,fields,mappingHV,"[","]");
                        templateLine = ReplaceSimpleVariable(templateLine,mappingVariables);

                        replacedLignes.add(templateLine);
                        //System.out.println(templateLine);
                    }
                }

                // *** ECRIRE TOUS replacedLignes DANS LE FICHIER TS ET HTML ***
                String filePathResult = "";
                filePathResult = findFile(filePathResult,fileName,viewGenerationPath);
                if(!filePathResult.isBlank()){
                    File fileResult = new File(filePathResult);
                    Files.write(fileResult.toPath(), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                    for(String line : replacedLignes){
                        Files.write(fileResult.toPath(), (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
                    }
                }else{
                    throw new Exception("Cannot find "+fileName+" in "+viewGenerationPath+" to write in");
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
                writeNewRouterModule(routerModulePath, caracteristiquePath, listRouterPaths);
            }
            if(apiProxyRequired){
                List<String> listContextProxyTemplate = ReadCaracteristique("[","ListContext","]", caracteristiquePath);
                List<String> listContextProxy = new ArrayList<>();
                for(String context : listContextProxyTemplate){
                    listContextProxy.add(ReplaceSimpleVariable(context, mappingVariables));
                }
                // ----- ECRIRE DANS LE FICHIER CIBLE -----
                writeNewContextProxyConfig(apiProxyPath, caracteristiquePath, listContextProxy);
            }
            // ------------------------------------------------------------------------------------------------
        }
    }
}

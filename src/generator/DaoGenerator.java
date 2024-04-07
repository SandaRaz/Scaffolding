package generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DaoGenerator {
    Generator generator;
    public DaoGenerator(Generator generator){
        this.generator = generator;
    }

    public List<String> ListDaoFunctions(String templatePath, String language) throws IOException {
        String daoPath = templatePath + "/" + language.toLowerCase() + "/" + language.toLowerCase() + "DAO.cfg";

        List<String> daoFileLines = Files.readAllLines(Paths.get(daoPath));
        List<String> daoFunctions = new ArrayList<>();
        for(String daoFileLine : daoFileLines){
            String currentLine = daoFileLine.replaceAll("\\s+$", "");
            if(currentLine.startsWith("[") && currentLine.endsWith("]")){
                String fonction = daoFileLine.trim().replace("[", "").replace("]","");
                daoFunctions.add(fonction);
            }
        }
        return daoFunctions;
    }

    public String GetDaoGetter(String templatePath, String language, String type) throws IOException {
        String getter = "";

        String caracteristiquePath = templatePath + "/" + language.toLowerCase() + "/" + language.toLowerCase()+"Caracteristique.cfg";
        List<String> daoGetters = generator.ReadCaracteristique("[","DaoGetter","]",caracteristiquePath);
        for(String daoGetter : daoGetters){
            if(daoGetter.contains(type)){
                getter = daoGetter.trim().split(":")[1];
                break;
            }
        }
        return getter;
    }

    public String ReplaceSimpleVariable(String ligne, Map<String, String> mapping){
        String replaced = ligne;
        if(!(ligne.trim().startsWith("[") && ligne.trim().endsWith("]"))){
            List<String> variables = generator.GetVariablesFromWord(replaced,'$');
            for(String variable : variables){
                //System.out.println("Variable de replacement: "+variable);
                replaced = replaced.replace("$"+variable+"$", mapping.get(variable));
            }
        }

        return replaced;
    }

    public String ReplaceHorizontaleVariable(String ligne, Map<String, List<String>> mappingVariables){
        int crochetOuvert = 0;
        AtomicBoolean estVarHorizontal = new AtomicBoolean(false);
        StringBuilder varHorizontalActuel = new StringBuilder();

        for(char character : ligne.toCharArray()){
            if(character == '['){
                crochetOuvert++;
            }
            if(character == ']'){
                crochetOuvert--;
            }

            if(estVarHorizontal.get()){
                if(crochetOuvert == 0){
                    String rechangeKey = generator.GetVariableFromWord(varHorizontalActuel.toString(),'$');
//                    System.out.println("Rechange Key: "+ rechangeKey);
                    StringBuilder variableHorizontales = new StringBuilder();

                    List<String> listeRechange = mappingVariables.get(rechangeKey);
                    Map<String, String> mappingRechanges = new HashMap<>();

                    for(String rechange : listeRechange){
                        String tempVarHorizontal = varHorizontalActuel.toString();
                        StringBuilder currentVariable = new StringBuilder();
                        AtomicBoolean isVariable = new AtomicBoolean(false);

                        mappingRechanges.put(rechangeKey, rechange);
                        for(char character2 : tempVarHorizontal.toCharArray()){
                            tempVarHorizontal = generator.IdentifyVariable(tempVarHorizontal,mappingRechanges,currentVariable,isVariable,character2,'$');
                        }

                        variableHorizontales.append(tempVarHorizontal).append(",");
                    }

                    if(variableHorizontales.toString().endsWith(",")){
                        variableHorizontales.deleteCharAt(variableHorizontales.length()-1);
                    }
                    String variableToReplace = "[[" + varHorizontalActuel + "]]";
                    ligne = ligne.replace(variableToReplace, variableHorizontales.toString());

                    varHorizontalActuel.setLength(0);
                    estVarHorizontal.set(false);
                }else{
                    if(character != ']'){
                        varHorizontalActuel.append(character);
                    }
                }
            }else{
                if(crochetOuvert == 2){
                    estVarHorizontal.set(true);
                }
            }
        }
//        System.out.println("LIGNE: "+ligne);

        return ligne;
    }

    public String ReplaceVerticaleVariable(String ligne, List<TypeAndName> fields, Map<String, List<String>> mappingVariables){
        String horizontalLigne = ligne;
        StringBuilder buildResultat = new StringBuilder();

        List<String> lignesToReplaces = new ArrayList<>();
        for(int i=0; i<fields.size(); i++){
            lignesToReplaces.add(ligne);
        }

        if(ligne.trim().startsWith("[") && ligne.trim().endsWith("]")){
            List<String> allVariables = generator.GetVariablesFromWord(ligne, '$');
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
                toReplace = toReplace.replace("[","").replace("]","");

                if(i == lignesToReplaces.size()-1){
                    buildResultat.append(toReplace);
                }else{
                    buildResultat.append(toReplace).append("\n    ");
                }
            }

            horizontalLigne = buildResultat.toString();
        }
        return horizontalLigne;
    }

    public void crudDAOToMap(Connection cnx, String tableName, String templatePath, String language, Map<String, String> mappingVariable) throws Exception {
        List<String> daoFunctions = this.ListDaoFunctions(templatePath, language);

        String daoPath = templatePath + "/" + language.toLowerCase() + "/" + language.toLowerCase() + "DAO.cfg";
        String className = tableName.substring(0,1).toUpperCase() + tableName.substring(1).toLowerCase();
        String classVariable = tableName.toLowerCase();

        List<TypeAndName> fields = generator.GetTableFields(cnx,templatePath,language,tableName);
        List<String> fieldReplacements = new ArrayList<>();
        List<String> fieldTypeReplacements = new ArrayList<>();
        List<String> updateFieldReplacements = new ArrayList<>();
        List<String> classVariableReplacements = new ArrayList<>();
        List<String> DaoGetterReplacements = new ArrayList<>();
        List<String> fieldNumStart0Replacements = new ArrayList<>();
        for(TypeAndName field : fields){
            fieldReplacements.add(field.getColumnName());
            fieldTypeReplacements.add(field.getColumnType());
            updateFieldReplacements.add(field.getColumnName());
            classVariableReplacements.add(tableName.toLowerCase());
            DaoGetterReplacements.add(GetDaoGetter(templatePath,language,field.getColumnType()));
            fieldNumStart0Replacements.add(String.valueOf(fields.indexOf(field)));
        }

        if(!updateFieldReplacements.isEmpty()){
            updateFieldReplacements.remove(0);
        }

        TypeAndName classPK = generator.GetPrimaryKey(fields);

        Map<String, String> mappingVariableDao = new HashMap<>();
        mappingVariableDao.put("className", className);
        mappingVariableDao.put("classVariable", classVariable);
        mappingVariableDao.put("classPrefixe", generator.GetPrefixe(tableName, 3));
        if(classPK != null){
            mappingVariableDao.put("classPK", classPK.getColumnName().toLowerCase());
        }else{
            mappingVariableDao.put("classPK", "id");
        }

        Map<String, List<String>> mappingListVariableDao = new HashMap<>();
        mappingListVariableDao.put("field", fieldReplacements);
        mappingListVariableDao.put("fieldType", fieldTypeReplacements);
        mappingListVariableDao.put("updateField",updateFieldReplacements);
        mappingListVariableDao.put("DaoGetter", DaoGetterReplacements);
        mappingListVariableDao.put("classVariable", classVariableReplacements);
        mappingListVariableDao.put("fieldNumStart0", fieldNumStart0Replacements);
        // *** Mapping list de tous les primary key utilisant
        for(String function : daoFunctions){
            StringBuilder resultDaoFunction = new StringBuilder();
            List<String> functionLines = generator.ReadCaracteristiqueIncludeBlank("[",function,"]", daoPath);
            List<String> replacedLine = new ArrayList<>();
            for(String functionLine : functionLines){
                if(functionLine.contains("classPK") && functionLine.contains("classPrefixe") && classPK != null){
                    if(!classPK.getDatabaseType().contains("char")){
                        functionLine = "";
                    }
                }

                functionLine = this.ReplaceVerticaleVariable(functionLine, fields, mappingListVariableDao);
                functionLine = this.ReplaceHorizontaleVariable(functionLine,mappingListVariableDao);
                functionLine = this.ReplaceSimpleVariable(functionLine, mappingVariableDao);

                replacedLine.add(functionLine);
            }

            for(String line : replacedLine){
                if(replacedLine.indexOf(line) == replacedLine.size()-1){
                    resultDaoFunction.append(line);
                }else{
                    resultDaoFunction.append(line).append("\n    ");
                }
            }

            mappingVariable.put(function, resultDaoFunction.toString());
        }
    }
}

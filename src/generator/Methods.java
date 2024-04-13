package generator;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Methods {
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

    public File findFile(File searchDirectory, String fileName) throws FileNotFoundException {
        if(searchDirectory.exists()){
            File[] files = searchDirectory.listFiles();
            if(files != null){
                for(File file : files){
                    if(file.isDirectory()){
                        File foundFile = findFile(file, fileName);
                        if(foundFile != null){
                            return foundFile;
                        }
                    }else if(file.getName().equals(fileName)){
                        return file;
                    }
                }
            }
        }else{
            throw new FileNotFoundException("Search Directory "+searchDirectory.toPath()+" does not exist. "+searchDirectory.getAbsolutePath());
        }
        return null;
    }

    public InputStream findFile(String fileName) throws IOException {
        String filePathInJar = "/" + fileName;
        String jarPath = Core.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        try(ZipFile zipFile = new ZipFile(jarPath)){
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                if(entry.getName().equals(filePathInJar)){
                    return zipFile.getInputStream(entry);
                }
            }
        }

        throw new IOException("Le fichier "+fileName+" n'a pas ete trouve dans le JAR.");
    }

    public List<String> readLines(String resourceFile) throws IOException {
        InputStream fileIn = ClassLoader.getSystemResourceAsStream(resourceFile);

        List<String> lines = new ArrayList<>();
        assert fileIn != null;
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(fileIn))){
            String line;
            while((line = reader.readLine()) != null){
                lines.add(line);
            }
        }
        return lines;
    }

    public void copyFile(InputStream inputStream, String destinationPath) throws IOException {
        try(OutputStream outputStream = new FileOutputStream(destinationPath)){
            byte[] buffer = new byte[1024];
            int bytesRead;
            while((bytesRead = inputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}

package generator;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipManipulation {
    public void extractZipFile(String archivePath, String destination) throws IOException {
        File destDir = new File(destination);
        if(!destDir.exists()){
            boolean created = destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(archivePath));
        ZipEntry entry = zipIn.getNextEntry();

        System.out.println("... Extracting angular source file ...");
        while(entry != null){
            String filePath = destination + File.separator + entry.getName();
            if(!entry.isDirectory()){
                extractFile(zipIn, filePath);
            }else{
                File dir = new File(filePath);
                boolean created = dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        System.out.println("Extraction done !");
        zipIn.close();
    }

    public void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read;
        while((read = zipIn.read(bytesIn)) != -1){
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public static void main(String[] args) throws IOException {
        ZipManipulation zm = new ZipManipulation();

        String filePath = "./src/template/csharp/views/angular/Temp";
        String zipPath = "./src/template/csharp/views/angular/ClientApp.zip";

        zm.extractZipFile(zipPath, filePath);

        File path = new File(filePath);
        System.out.println("Path: "+path.getCanonicalPath());
    }
}

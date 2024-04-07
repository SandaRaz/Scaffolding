package generator;

import java.io.IOException;
import java.util.List;

public class Core {
    public void CommandLine(){

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
}

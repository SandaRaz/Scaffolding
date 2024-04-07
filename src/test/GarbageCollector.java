package test;

import java.lang.ref.WeakReference;

public class GarbageCollector {
    public static void main(String[] args) {
        String data = "Hello World";

        WeakReference<String> weakRef = new WeakReference<>(data);

        String retrievedData = weakRef.get();
        System.out.println("Donnees recuperes: " + retrievedData + "\n");

        /* ----- Memory Information ----- */
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();

        System.out.println("Total Memory: " + (totalMemory / 10e6) + " mo");
        System.out.println("Free Memory: " + (freeMemory / 10e6) + " mo");
        System.out.println("Max Memory: " + (maxMemory / 10e6) + " mo");
        /* ------------------------------ */

        // Supprimer la reference forte
        data = null;

        // Forcer le garbage collector a s'executer
        System.gc();

        String newData = weakRef.get();

        if(newData != null){
            System.out.println("Donnees encore disponible: " + newData);
        }else{
            System.out.println("Donnees liberes par le Garbage Collector");
        }

        System.out.println("Total Memory: " + (totalMemory / 10e6) + " mo");
        System.out.println("Free Memory: " + (freeMemory / 10e6) + " mo");
        System.out.println("Max Memory: " + (maxMemory / 10e6) + " mo");

        /* -----
        --- Effacer manuellement la reference
        --- clear() n'est pas necessaire grace au GC
        weakRef.clear();
        ----- */
    }
}

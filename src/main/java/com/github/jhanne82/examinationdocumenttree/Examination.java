package com.github.jhanne82.examinationdocumenttree;

import com.github.jhanne82.examinationdocumenttree.configuration.Configuration;
import com.github.jhanne82.examinationdocumenttree.configuration.Setup;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Examination {


    private static final String LINE_SEP = System.getProperty("line.separator");

    private void writeInFile( String fileName, String content )
            throws IOException {

        String path = "Z:\\Results\\Auswertung";

        FileUtils.forceMkdir( new File( path ) );

        PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( path + File.separator + fileName, false ) ) );
        writer.println( content );
        writer.close();
    }



    private List<File> getFiles( String fileName, String searchModel, Configuration parameter ) {

        List<File> files = new ArrayList<>();
        for( File folder : getListOfResultFolders() ) {

            String path = folder.getAbsolutePath() + File.separator + "Results_DocumentTree"
                    + File.separator + searchModel;

            path += parameter.isCluster() ? File.separator + "clustered"
                    : File.separator + "not_clustered";

            path = path + File.separator + parameter.getSearchType()
                    + File.separator + parameter.getDistributionForDocument()
                    + File.separator + parameter.getDistributionForSearch()
                    + File.separator + fileName;

            File file = new File(path);
            if (file.exists()) {
                files.add(file);
            }
        }
        return files;
    }




    private List<File> getListOfResultFolders() {
        List<File> folders = new ArrayList<>();
        File folder = new File("Z:\\Results");

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                folders.add(file);
            }
        }
        return folders;
    }



    private double getAverageHitRate(String searchModel, Configuration parameter ) throws IOException {

        int allHits = 0;
        int numberOfLines = 0;

        for( File file : getFiles( "HitMissRate.txt", searchModel, parameter ) ) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine();

                while (line != null) {
                    if (!line.trim().isEmpty()) {
                        try {
                            allHits += Integer.valueOf(line.substring(0, line.indexOf('/')));
                            numberOfLines++;
                        } catch (StringIndexOutOfBoundsException e) {
                            System.out.println(line);
                            System.out.println(file.getAbsolutePath());
                            throw new IOException("could not parse file");
                        }
                    }
                    line = br.readLine();
                }
            }
        }

        double averageHits = allHits/numberOfLines;
        double hitRate = averageHits/parameter.getMaxCountOfCreatedSearches();

        return hitRate*100;
    }



    private void storeHitRate() throws IOException {

        StringBuilder builder = new StringBuilder();

        for( Configuration configuration : Setup.SETUP_LIST ) {

            builder.append("localKnowledge").append("_")
                   .append(configuration.isCluster()?"cluster":"noCluster").append("_")
                   .append(configuration.getDistributionForDocument()).append("_")
                   .append(configuration.getDistributionForSearch()).append(":").append('\t')
                   .append(getAverageHitRate( "localKnowledge", configuration)).append(LINE_SEP);

            builder.append("stressReduced").append("_")
                    .append(configuration.isCluster()?"cluster":"noCluster").append("_")
                    .append(configuration.getDistributionForDocument()).append("_")
                    .append(configuration.getDistributionForSearch()).append(":").append('\t')
                    .append(getAverageHitRate( "stressReduced", configuration)).append(LINE_SEP).append(LINE_SEP);
        }

        writeInFile("HitRate.txt", builder.toString());
    }


     public double getAverageNumberOfDocumentsOnCorrectLevel( String searchModel, Configuration parameter ) throws IOException {

         int correctDocuments = 0;
         int numberOfLines = 0;

         for( File file : getFiles( "DocumentsOnCorrectLevelInTree.txt", searchModel, parameter ) ) {
             try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                 String line = br.readLine();

                 while (line != null) {
                     if (!line.trim().isEmpty()) {
                         try {
                             correctDocuments += Integer.valueOf(line);
                             numberOfLines++;
                         } catch (StringIndexOutOfBoundsException e) {
                             System.out.println(line);
                             System.out.println(file.getAbsolutePath());
                             throw new IOException("could not parse file");
                         }
                     }
                     line = br.readLine();
                 }
             }
         }

         double averageHits = correctDocuments/numberOfLines;
         double hitRate = averageHits/parameter.getMaxCountOfCreatedDocuments();

         return hitRate*100;

     }


    private void storeDocumentOnCorrectLevel() throws IOException {

        StringBuilder builder = new StringBuilder();

        for( Configuration configuration : Setup.SETUP_LIST ) {

            builder.append("globalKnowledge").append("_")
                    .append(configuration.isCluster()?"cluster":"noCluster").append("_")
                    .append(configuration.getDistributionForDocument()).append("_")
                    .append(configuration.getDistributionForSearch()).append(":").append('\t')
                    .append(getAverageNumberOfDocumentsOnCorrectLevel( "globalKnowledge", configuration)).append(LINE_SEP);

            builder.append("localKnowledge").append("_")
                    .append(configuration.isCluster()?"cluster":"noCluster").append("_")
                    .append(configuration.getDistributionForDocument()).append("_")
                    .append(configuration.getDistributionForSearch()).append(":").append('\t')
                    .append(getAverageNumberOfDocumentsOnCorrectLevel( "localKnowledge", configuration)).append(LINE_SEP);

            builder.append("stressReduced").append("_")
                    .append(configuration.isCluster()?"cluster":"noCluster").append("_")
                    .append(configuration.getDistributionForDocument()).append("_")
                    .append(configuration.getDistributionForSearch()).append(":").append('\t')
                    .append(getAverageNumberOfDocumentsOnCorrectLevel( "stressReduced", configuration)).append(LINE_SEP).append(LINE_SEP);
        }

        writeInFile("DocumentsOnCorrectLevelInTree.txt", builder.toString());

    }


    public static void main(String[] args ) throws IOException {

        Examination examination = new Examination();
        examination.storeHitRate();
        examination.storeDocumentOnCorrectLevel();

    }
}

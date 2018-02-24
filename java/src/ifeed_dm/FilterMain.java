package ifeed_dm;

import ifeed_dm.EOSS.EOSSParams;
import ifeed_dm.binaryInput.BinaryInputArchitecture;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;
import java.util.ArrayList;


public class FilterMain {

    public static boolean tallMatrix = true;

    public List<BinaryInputArchitecture> data;
    public List<String> features;

    public static void main(String [] args) {

        String featureDataFile = "/Users/bang/Desktop/test/features.txt";
        String labeledDataFile = "/Users/bang/Desktop/test/labels.csv";

        List<BinaryInputArchitecture> data = registerData(labeledDataFile);
        List<String> features = registerFeature(featureDataFile);
    }



    /**
     * Reads in bit strings from a file.
     *
     * @param labledDataFile: Path to the file containing the bitStrings
     * @return architectures: ArrayList of integer arrays
     */
    private static List<BinaryInputArchitecture> registerData(String labledDataFile) {

        List<BinaryInputArchitecture> data = new ArrayList<>();

        String line = "";
        String splitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(labledDataFile))) {

            //skip header
            line = br.readLine();

            int id = 0;

            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] rowSplit = line.split(splitBy);
                // The first column is the label
                boolean label = rowSplit[0].equals("1");
                StringBuilder sb = new StringBuilder();

                // Skip first variables since it is the number of satellites per plane
                BitSet inputs = new BitSet(EOSSParams.num_orbits * EOSSParams.num_instruments);

                for (int i = 0; i < 60; i++) {
                    if(Integer.parseInt(rowSplit[i + 2]) == 1){
                        inputs.set(i);
                    }
                }

                data.add(new BinaryInputArchitecture(id++, inputs, new double[0]));
            }
        } catch (IOException e) {
            System.out.println("Exception in parsing labeled data file");
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Reads in the feature expressions from a file.
     *
     * @param featureDataFile: Path to the file containing the list of features
     * to apply
     * @return combinedFeatures: list of strings containing the feature
     * expressions
     */
    private static ArrayList<String> registerFeature(String featureDataFile) {

        String line = "";
        String splitBy = ",";

        ArrayList<String> combinedFeatures = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(featureDataFile))) {

            //skip header
            line = br.readLine();

            while ((line = br.readLine()) != null) {
                String feature = line.split("//")[1].trim();
                feature = feature.replaceAll("],", "]&&");
                combinedFeatures.add(feature);
            }
        } catch (IOException e) {
            System.out.println("Exception in parsing labeled data file");
            e.printStackTrace();
        }
        return combinedFeatures;
    }




//    public void writeToFile(List<Feature> baseFeatures){
//
//        File file = new File("/Users/bang/workspace/FeatureExtractionGA/data/baseFeatures");
//        File file2 = new File("/Users/bang/workspace/FeatureExtractionGA/data/featureNames");
//        File file3 = new File("/Users/bang/workspace/FeatureExtractionGA/data/labels");
//
//        try{
//
//            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//
//            BufferedWriter featureNameWriter = new BufferedWriter(new FileWriter(file2));
//
//            String printRow = "";
//
//            for(int j=0;j<baseFeatures.size();j++){
//
//                BitSet bs = baseFeatures.get(j).getMatches();
//                int nbits = bs.size();
//
//                final StringBuilder buffer = new StringBuilder(nbits);
//                IntStream.range(0, nbits).mapToObj(i -> bs.get(i) ? '1' : '0').forEach(buffer::append);
//
//                writer.write(buffer.toString() + "\n");
//                featureNameWriter.write(baseFeatures.get(j).getName() + "\n");
//            }
//
//            System.out.println("Done");
//            writer.close();
//            featureNameWriter.close();
//
//        }catch(IOException e){
//            System.out.println(e.getMessage());
//        }
//
//        try{
//
//            BufferedWriter writer = new BufferedWriter(new FileWriter(file3));
//            String printRow = "";
//
//            BitSet bs = this.labels;
//            int nbits = bs.size();
//
//            final StringBuilder buffer = new StringBuilder(nbits);
//            IntStream.range(0, nbits).mapToObj(i -> bs.get(i) ? '1' : '0').forEach(buffer::append);
//
//            writer.write(buffer.toString() + "\n");
//
//            System.out.println("Done");
//            writer.close();
//
//        }catch(IOException e){
//            System.out.println(e.getMessage());
//        }
//    }

}

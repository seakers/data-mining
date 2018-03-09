package ifeed_dm.local;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.BitSet;
import java.util.List;
import java.util.ArrayList;
import java.util.StringJoiner;

import ifeed_dm.EOSS.EOSSFeatureFetcher;
import ifeed_dm.EOSS.EOSSParams;
import ifeed_dm.FeatureExpressionHandler;
import ifeed_dm.FeatureFetcher;
import ifeed_dm.Utils;
import ifeed_dm.logic.Connective;
import ifeed_dm.binaryInput.BinaryInputArchitecture;

public class FilterMain {

    public final static boolean tallMatrix = true;

    public List<BinaryInputArchitecture> data;
    public List<String> features;

    public static void main(String [] args) {

        String featureDataFile = "/Users/bang/Desktop/test/features_test.txt";
        String labeledDataFile = "/Users/bang/Desktop/test/labels.csv";
        String outputFile = "/Users/bang/Desktop/test/out.csv";

        List<BinaryInputArchitecture> data = registerData(labeledDataFile);

        System.out.println("Data size: " + data.size());

        List<String> features = registerFeature(featureDataFile);

        FeatureFetcher featureFetcher = new EOSSFeatureFetcher(data);
        FeatureExpressionHandler expressionHandler = new FeatureExpressionHandler(featureFetcher);
        List<BitSet> matches = new ArrayList<>();

        for(String featureString:features){
            Connective root = expressionHandler.generateFeatureTree(featureString);
            System.out.println(root.getName());
            matches.add(root.getMatches());
        }

        exportFilterOutput(outputFile, matches, features, data.size());
    }


    public void run(String featureDataFile, String labeledDataFile, String outputFile){

        List<BinaryInputArchitecture> data = registerData(labeledDataFile);

        System.out.println("Sample size: " + data.size());

        List<String> features = registerFeature(featureDataFile);

        FeatureFetcher featureFetcher = new EOSSFeatureFetcher(data);
        FeatureExpressionHandler expressionHandler = new FeatureExpressionHandler(featureFetcher);
        List<BitSet> matches = new ArrayList<>();

        for(String featureString:features){
            Connective root = expressionHandler.generateFeatureTree(featureString);
            System.out.println(root.getName());
            matches.add(root.getMatches());
        }

        exportFilterOutput(outputFile, matches, features, data.size());
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

                for (int i = 0; i < EOSSParams.num_orbits * EOSSParams.num_instruments; i++) {
                    if(Integer.parseInt(rowSplit[i + 2]) == 1){
                        inputs.set(i);
                    }else if(Integer.parseInt(rowSplit[i + 2]) == 0){
                        // Do nothing
                    }else{
                        throw new RuntimeException("The data format does not match");
                    }
                }

                if(tallMatrix){
                    BitSet temp = new BitSet(EOSSParams.num_orbits * EOSSParams.num_instruments);
                    for(int i = 0; i < EOSSParams.num_orbits; i++){
                        for(int j = 0; j < EOSSParams.num_instruments; j++){
                            if(inputs.get(j * EOSSParams.num_orbits + i)) {
                                temp.set(i * EOSSParams.num_instruments + j);
                            }
                        }
                    }
                    inputs = temp;
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

                StringJoiner combined = new StringJoiner("&&");

                feature = feature.substring(0, feature.length() - 1);
                String[] baseFeatures = feature.split("],");

                for(String f:baseFeatures){
                    // For each individual feature
                    String type = f.split("\\[")[0];
                    String args = f.split("\\[")[1];

                    // Split arg string to arguments of different types
                    String[] argType = args.split(";");
                    StringJoiner argTypeNew = new StringJoiner(";");

                    for(String argOfEachType : argType){
                        // For specific type of arguments

                        StringJoiner argSplitNew = new StringJoiner(",");
                        String[] argSplit = argOfEachType.split(",");

                        for(String arg:argSplit){
                            // For each argument

                            boolean modified = false;

                            // Check orbit names
                            for(int i = 0; i < EOSSParams.orbit_list.length; i++){
                                String orbit = EOSSParams.orbit_list[i];
                                if(orbit.equalsIgnoreCase(arg)){
                                    argSplitNew.add(Integer.toString(i));
                                    modified = true;
                                    break;
                                }
                            }

                            // Check instrument names
                            if(!modified){
                                for(int i = 0; i < EOSSParams.instrument_list.length; i++){
                                    String instrument = EOSSParams.instrument_list[i];
                                    if(instrument.equalsIgnoreCase(arg)){
                                        argSplitNew.add(Integer.toString(i));
                                        modified = true;
                                        break;
                                    }
                                }
                            }

                            // If no matching name was found, return the same arg string
                            if(!modified){
                                argSplitNew.add(arg);
                            }
                        }

                        // Join arguments of the same type (e.g. orbit, instruments)
                        argTypeNew.add(argSplitNew.toString());
                    }

                    String newArgs = argTypeNew.toString();
                    int numDelimiter = Utils.countMatchesInString(newArgs, ";");
                    for(int i = 0; i < 2 - numDelimiter; i++){
                        newArgs = newArgs + ";";
                    }

                    combined.add("{" + type + "[" + newArgs + "]}");
                }
                combinedFeatures.add(combined.toString());
            }
        } catch (IOException e) {
            System.out.println("Exception in parsing labeled data file");
            e.printStackTrace();
        }
        return combinedFeatures;
    }



    /**
     * Writes a csv file with binary arrays.
     *
     * @param filename: Path to the file to write
     * @param out: ArrayList of integer arrays. Each integer array contains the
     * result of applying one combined feature
     * @return
     */
    public static boolean exportFilterOutput(String filename, List<BitSet> out, List<String> features, int dataSize) {
        try {

            PrintWriter w = new PrintWriter(filename, "UTF-8");

            StringBuilder sb = new StringBuilder();

            for (String feat : features) {
                sb.append(feat).append("||");
            }
            sb.delete(sb.length() - 2, sb.length());

            for (int i = 0; i < out.size(); i++) {

                sb.append("\n");

                BitSet bs = out.get(i);

                for (int j = 0; j < dataSize; j++) {
                    boolean b = bs.get(j);
                    if (j > 0) {
                        sb.append(",");
                    }
                    if (b) {
                        sb.append("1");
                    } else {
                        sb.append("0");
                    }
                }
            }

            w.print(sb.toString());
            w.flush();
            w.close();

        } catch (Exception e) {
            System.out.println("Exception in exporting output");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

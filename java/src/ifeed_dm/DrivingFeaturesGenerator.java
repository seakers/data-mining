package ifeed_dm;


/*
This class reads in result file in csv format, and minds driving features
Member functions
    computeMetrics: computes metrics used to evaluate ARM (e.g. support, lift, confidence)
    getDrivingFeatures: mines driving features and returns an ArrayList<DrivingFeature>
    exportDrivingFeatures: writes a csv file with compact representation of driving features
    sortDrivingFeatures: sorts driving features based on different ARM measures
    checkThreshold: checks if the ARM measures are above threshold
    parseCSV: reads in a result file in csv format
    bitString2intArr: Modifies bitString to integer array
    booleanToInt: Modifies boolean array to integer array
 */
import java.util.ArrayList;

import ifeed_dm.EOSS.EOSSParams;
import ifeed_dm.EOSS.Present;
import ifeed_dm.EOSS.Absent;
import ifeed_dm.EOSS.InOrbit;
import ifeed_dm.EOSS.NotInOrbit;
import ifeed_dm.EOSS.Together;
import ifeed_dm.EOSS.Separate;
import ifeed_dm.EOSS.EmptyOrbit;
import ifeed_dm.EOSS.NumOrbits;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.List;



/**
 *
 * @author Bang
 */
public class DrivingFeaturesGenerator {
    
//    public int norb;
//    public int ninstr;
//    public String[] orbit_list;
//    public String[] instrument_list;
    
    
    
    
    
    
    
    

    private final int numberOfVariables;

    private double supp_threshold;
    private double conf_threshold;
    private double lift_threshold;

    private ArrayList<Integer> behavioral;
    private ArrayList<Integer> non_behavioral;
    private ArrayList<Integer> population;

    private ArrayList<Architecture> architectures;
    private List<DrivingFeature> presetDrivingFeatures;
    private ArrayList<int[]> presetDrivingFeatures_satList;
    private List<DrivingFeature> drivingFeatures;

    double[][] dataFeatureMat;
    private BitSet labels;

    double[] thresholds;

    private int maxIter;
    private int minRuleNum;
    private int maxRuleNum;
    private double adaptSupp;

    public boolean tallMatrix;
    public int maxLength;
    public boolean run_mRMR;
    public int max_number_of_features_before_mRMR;

    

    

    public DrivingFeaturesGenerator() {

        
        // Adaptive support threshold
        this.adaptSupp = (double) behavioral.size() / population.size() * 0.5;
        
        this.numberOfVariables = norb*ninstr;

        
        this.supp_threshold = DataMiningParams.support_threshold;
        this.conf_threshold = DataMiningParams.confidence_threshold;
        this.lift_threshold = DataMiningParams.lift_threshold;

        this.thresholds = new double[3];
        thresholds[0] = supp_threshold;
        thresholds[1] = lift_threshold;
        thresholds[2] = conf_threshold;

        this.architectures = new ArrayList<>();

        this.behavioral = new ArrayList<>();
        this.non_behavioral = new ArrayList<>();
        this.presetDrivingFeatures = new ArrayList<>();

        this.maxIter = DataMiningParams.maxIter;
        this.minRuleNum = DataMiningParams.minRuleNum;
        this.maxRuleNum = DataMiningParams.maxRuleNum;

        this.tallMatrix = DataMiningParams.tallMatrix;
        this.maxLength = DataMiningParams.maxLength;
        this.run_mRMR = DataMiningParams.run_mRMR;

        this.max_number_of_features_before_mRMR = DataMiningParams.max_number_of_features_before_mRMR;
        
    }
    
    
    public void setInputData(ArrayList<Integer> behavioral, ArrayList<Integer> nonbehavioral, ArrayList<Architecture> allArchs,
                            double supp, double conf, double lift){
    
        this.supp_threshold = supp;
        this.conf_threshold = conf;
        this.lift_threshold = lift;
        
        this.thresholds = new double[3];
        thresholds[0] = supp_threshold;
        thresholds[1] = lift_threshold;
        thresholds[2] = conf_threshold;

        this.architectures = allArchs;
        this.behavioral = behavioral;
        this.non_behavioral = nonbehavioral;

        this.population = new ArrayList<>();
        this.population.addAll(this.behavioral);
        this.population.addAll(this.non_behavioral);
        // Adaptive support threshold
        this.adaptSupp = (double) behavioral.size() / population.size() * 0.5;
    }
    
    
    
    
    public ArrayList<DrivingFeature> run(int topN) {

        long t0 = System.currentTimeMillis();

        generatePrimitiveFeatures();
        
//    	System.out.println("...Starting Apriori");
        this.drivingFeatures = getDrivingFeatures();

        System.out.println("...[DrivingFeatures] Number of features before mRMR: " + drivingFeatures.size() + ", with max confidence of " + drivingFeatures.get(0).getFConfidence());

        if (this.run_mRMR) {
            MRMR mRMR = new MRMR();
            this.drivingFeatures = mRMR.minRedundancyMaxRelevance( population.size(), getDataMat(this.drivingFeatures), this.labels, this.drivingFeatures, topN);
        }

        // Printout result
        //exportDrivingFeatures(saveDataFile, topN);

        long t1 = System.currentTimeMillis();
        System.out.println("...[DrivingFeature] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
        
        return (ArrayList) this.drivingFeatures;
    }

    
    
    
    
    
    
    
    
    
    
    
    
    public List<DrivingFeature> generatePrimitiveFeatures() {

        long t0 = System.currentTimeMillis();



            int iter = 0;
            ArrayList<Integer> addedFeatureIndices = new ArrayList<>();
            double[] bounds = new double[2];
            bounds[0] = 0;
            bounds[1] = (double) behavioral.size() / population.size();

            boolean apriori = true;
            if (apriori) {
                while (addedFeatureIndices.size() < minRuleNum || addedFeatureIndices.size() > maxRuleNum) {

                    iter++;
                    if (iter > maxIter) {
                        break;
                    } else if (iter > 1) {
                        // max supp threshold is support_S
                        // min supp threshold is 0
                        double a;
                        if (addedFeatureIndices.size() > maxRuleNum) { // Too many rules -> increase threshold
                            bounds[0] = this.adaptSupp;
                            a = bounds[1];
                        } else { // too few rules -> decrease threshold
                            bounds[1] = this.adaptSupp;
                            a = bounds[0];
                        }
                        // Bisection
                        this.adaptSupp = (double) (this.adaptSupp + a) * 0.5;
                    }
                    addedFeatureIndices = new ArrayList<>();
                    for (int i = 0; i < featureData_name.size(); i++) {
                        double[] metrics = featureData_metrics.get(i);
                        if (metrics[0] > adaptSupp) {
                            addedFeatureIndices.add(i);
                            if (addedFeatureIndices.size() > this.maxRuleNum && iter < maxIter) {
                                break;
                            } else if ((candidate_features.size() - (i + 1)) + addedFeatureIndices.size() < this.minRuleNum) {
                                break;
                            }
                        }
                    }
                    System.out.println("...[DrivingFeatures] number of preset rules found: " + addedFeatureIndices.size() + " with treshold: " + this.adaptSupp);
                }
                System.out.println("...[DrivingFeatures] preset features extracted in " + iter + " steps with size: " + addedFeatureIndices.size());
            } else {
                for (int i = 0; i < featureData_name.size(); i++) {
                    double[] metrics = featureData_metrics.get(i);
                    if (metrics[0] > thresholds[0] && metrics[1] > thresholds[1] && metrics[2] > thresholds[2] && metrics[3] > thresholds[2]) {
                        addedFeatureIndices.add(i);
                    }
                }
            }

            for (int ind : addedFeatureIndices) {
                BitSet bs = new BitSet(population.size());
                for (int j = 0; j < population.size(); j++) {

                    if(featureData_satList.get(ind)[j] > 0.0001){
                        bs.set(j);
                    }
                }
                this.presetDrivingFeatures.add(new DrivingFeature(featureData_exp.get(ind),bs));
                presetDrivingFeatures_satList.add(featureData_satList.get(ind));
            }

            long t1 = System.currentTimeMillis();
            System.out.println("...[DrivingFeatures] preset feature evaluation done in: " + String.valueOf(t1 - t0) + " msec");

            //if(apriori) return getDrivingFeatures();
            return this.presetDrivingFeatures;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

//    public void setDrivingFeatureSatisfactionData() {
//
//        // Get feature satisfaction matrix
////        this.presetDrivingFeatures = presetDrivingFeatures.subList(0, 50);
//        this.dataFeatureMat = new double[population.size()][presetDrivingFeatures.size()];
//        this.labels = new BitSet(population.size());
//
//        for (int i = 0; i < population.size(); i++) {
//            for (int j = 0; j < presetDrivingFeatures.size(); j++) {
//
//                DrivingFeature df = presetDrivingFeatures.get(j);
//                int index = df.getID();
//                this.dataFeatureMat[i][j] = (double) presetDrivingFeatures_satList.get(index)[i];
//            }
//            if (behavioral.contains(population.get(i))) {
//                labels.set(i, true);
//            }
//        }
//    }

    /**
     * Runs Apriori and returns the top n features discovered from Apriori. Features are ordered by fconfidence in descending order.
     * @return 
     */
    public List<DrivingFeature> getDrivingFeatures() {

        this.labels = new BitSet(population.size());
        for (int i = 0; i < population.size(); i++) {
            if (behavioral.contains(population.get(i))) {
                labels.set(i, true);
            }
        }
        
        Apriori2 ap2 = new Apriori2(population.size(), presetDrivingFeatures);
                
        ap2.run(labels, thresholds[0], thresholds[2], maxLength);

        return ap2.getTopFeatures(max_number_of_features_before_mRMR, DataMiningParams.metric);
    }

    public void RecordSingleFeature(PrintWriter w, DrivingFeature df) {

        String expression = df.getName();

        //{present[orb;instr;num]}&&{absent[orb;instr;num]}
        String[] individual_features = expression.split("&&");

        for (int t = 0; t < individual_features.length; t++) 
    }

    public void recordMetaInfo(PrintWriter w, DrivingFeature2 feature) {
        String expression = feature.getName();
        String[] individual_features = expression.split("&&");

        String name = "";
        try {
            for (String expr : individual_features) {
                if (expr.startsWith("{") && expr.endsWith("}")) {
                    expr = expr.substring(1, expr.length() - 1);
                }

                String type = expr.split("\\[")[0];
                String params = expr.split("\\[")[1];
                params = params.substring(0, params.length() - 1);
                String[] paramsSplit = params.split(";");
                String orb = "";
                String instr = "";
                String num = "";

                if (!paramsSplit[0].isEmpty()) {
                    int o = Integer.parseInt(paramsSplit[0]);
                    orb = DataMiningParams.orbit_list[o];
                }
                if (paramsSplit.length > 1) {
                    if (!paramsSplit[1].contains(",")) {
                        if (paramsSplit[1].isEmpty()) {
                            instr = "";
                        } else {
                            int i = Integer.parseInt(paramsSplit[1]);
                            instr = DataMiningParams.instrument_list[i];
                        }
                    } else {
                        String[] instrSplit = paramsSplit[1].split(",");
                        instr = "";
                        for (String temp : instrSplit) {
                            if (!temp.isEmpty()) {
                                int i = Integer.parseInt(temp);
                                instr = instr + "," + DataMiningParams.instrument_list[i];
                            }
                        }
                        if (instr.startsWith(",")) {
                            instr = instr.substring(1);
                        }
                    }
                }
                if (paramsSplit.length > 2) {
                    num = paramsSplit[2];
                }

                name = name + "," + type + "[" + orb + ";" + instr + ";" + num + "]";
            }
            if (name.startsWith(",")) {
                name = name.substring(1);
            }

            w.print("/" + feature.getSupport() + "/" + feature.getLift() +
                    "/" + feature.getFConfidence() + 
                    "/" + feature.getRConfidence() + "// " + name + "\n");

        } catch (Exception e) {
            System.out.println("Exception in printing feature names:" + expression);
            e.printStackTrace();
        }
    }

    /**
     * Saves the topN driving features in an ordered list based on (0: support,
     * 1: lift, 2: confidence)
     *
     * @param features the features to be exported
     * @param filename path and filename to save features
     * @param topN only save the top N features
     */
    public boolean exportDrivingFeatures(String filename, int topN) {
        try {

            PrintWriter w = new PrintWriter(filename, "UTF-8");
            w.println("// (mode,arg,orb,inst)/support/lift");

            int count = 1;

            for (DrivingFeature2 feature : this.drivingFeatures) {
                if (count > topN) {
                    break;
                }

                this.RecordSingleFeature(w, feature);
                this.recordMetaInfo(w, feature);
                count++;
            }

            w.flush();
            w.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    
    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    public void parseCSV(String path) {
        String line = "";
        String splitBy = ",";

        architectures = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            //skip header
            line = br.readLine();

            int id = 0;
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] tmp = line.split(splitBy);
                // The first column is the label
                boolean label = tmp[0].equals("1");
                StringBuilder sb = new StringBuilder();
                //skip first variables ince it is the number of satellites per plane
                for (int i = 0; i < numberOfVariables; i++) {
                    sb.append(tmp[2].charAt(i));
                }
                architectures.add(new Architecture(id, label, bitString2intArr(sb.toString())));
                if (label) {
                    this.behavioral.add(id);
                } else {
                    this.non_behavioral.add(id);
                }

                id++;

            }
        } catch (IOException e) {
            System.out.println("Exception in parsing labeled data file");
            e.printStackTrace();
        }

        this.population = new ArrayList<>();
        this.population.addAll(behavioral);
        this.population.addAll(non_behavioral);
        this.feh.setArchs(this.architectures, this.behavioral, this.non_behavioral, this.population);
        // Adaptive support threshold
        this.adaptSupp = (double) behavioral.size() / population.size() * 0.5;
    }

    private int[][] bitString2intArr(String input) {
        int[][] output = new int[norb][ninstr];

        int cnt = 0;
        if (DataMiningParams.tallMatrix) {
            for (int i = 0; i < ninstr; i++) {
                for (int o = 0; o < norb; o++) {
                    int thisBit;
                    if (cnt == input.length() - 1) {
                        thisBit = Integer.parseInt(input.substring(cnt));
                    } else {
                        thisBit = Integer.parseInt(input.substring(cnt, cnt + 1));
                    }
                    output[o][i] = thisBit;
                    cnt++;
                }
            }
        } else {
            for (int i = 0; i < norb; i++) {
                for (int j = 0; j < ninstr; j++) {
                    int thisBit;
                    if (cnt == input.length() - 1) {
                        thisBit = Integer.parseInt(input.substring(cnt));
                    } else {
                        thisBit = Integer.parseInt(input.substring(cnt, cnt + 1));
                    }
                    output[i][j] = thisBit;
                    cnt++;
                }
            }
            cnt++;
        }
        return output;
    }

    public int[][] booleanToInt(boolean[][] b) {
        int[][] intVector = new int[b.length][b[0].length];
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b[0].length; ++j) {
                intVector[i][j] = b[i][j] ? 1 : 0;
            }
        }
        return intVector;
    }

    public BitSet[] getDataMat(List<DrivingFeature2> dfs) {
        BitSet[] mat = new BitSet[dfs.size()];
        for (int i = 0; i < dfs.size(); i++) {
            mat[i] = dfs.get(i).getMatches();
        }
        return mat;
    }


//    public class Architecture {
//
//        int id;
//        boolean label;
//        double[] objectives;
//        int[][] booleanMatrix;
//
//        public Architecture(int id, boolean label, int[][] mat, double[] objectives) {
//            this.id = id;
//            this.label = label;
//            this.booleanMatrix = mat;
//            this.objectives = objectives;
//        }
//        
//        public Architecture(int id, boolean label, String bitString){
//            this.id=id;
//            this.label=label;
//            this.booleanMatrix = bitString2intArr(bitString);
//        }
//
//        public Architecture(int id, boolean label, int[][] mat) {
//            this.id = id;
//            this.label = label;
//            this.booleanMatrix = mat;
//        }
//
//        public int getID() {
//            return id;
//        }
//
//        public boolean getLabel() {
//            return label;
//        }
//
//        public int[][] getBooleanMatrix() {
//            return booleanMatrix;
//        }
//
//        public double[] getObjectives() {
//            return objectives;
//        }
//
//    }

}

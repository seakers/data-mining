/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.local;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.io.InputDatasetReader;
import ifeed.mining.arm.Apriori;
import ifeed.problem.assigning.AssociationRuleMining;
import ifeed.problem.assigning.Params;
import org.moeaframework.core.*;
import org.moeaframework.util.TypedProperties;
import weka.core.Attribute;
import weka.core.BinarySparseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 *
 *
 * @author hsbang
 */

public class WekaTest {

    // Instruments and orbits
    public static String[] instrumentList = {
            "ACE_ORCA","ACE_POL","ACE_LID",
            "CLAR_ERB","ACE_CPR","DESD_SAR",
            "DESD_LID","GACM_VIS","GACM_SWIR",
            "HYSP_TIR","POSTEPS_IRS","CNES_KaRIN"};

    public static String[] orbitList = {"LEO-600-polar-NA","SSO-600-SSO-AM","SSO-600-SSO-DD","SSO-800-SSO-DD","SSO-800-SSO-PM"};

    /**
     * pool of resources
     */
    private static ExecutorService pool;

    /**
     * List of future tasks to perform
     */
    private static ArrayList<Future<Algorithm>> futures;

    /**
     * First argument is the path to the project folder. Second argument is the
     * mode. Third argument is the number of ArchitecturalEvaluators to
     * initialize.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Params params = new Params();

        // Basic setups
        String path = System.getProperty("user.dir");
        String runName = "";
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());

        if(runName.isEmpty()){
            runName = timestamp;

        }else{
            runName = runName + "_" + timestamp;
        }

        // Set path to the input data file
        String inputDataFile = path + File.separator + "data" + File.separator + "fuzzy_pareto_7.selection";
        InputDatasetReader reader = new InputDatasetReader(inputDataFile);
        reader.setInputType(InputDatasetReader.InputType.BINARY_BITSTRING);
        reader.setColumnInfo(InputDatasetReader.ColumnType.CLASSLABEL,1);
        reader.setColumnInfo(InputDatasetReader.ColumnType.DECISION, 2);
        reader.readData();

        List<AbstractArchitecture> architectures = reader.getArchs();
        BitSet label = reader.getLabel();
        List<Integer> behavioral = new ArrayList<>();
        List<Integer> non_behavioral = new ArrayList<>();
        for(int i = 0; i < architectures.size(); i++){
            if(label.get(i)){
                behavioral.add(i);
            }else{
                non_behavioral.add(i);
            }
        }

        System.out.println("Path set to " + path);

        // Settings for Apriori algorithm
        double supp = 0.158;
        double conf = 0.50;

        //parameters and operators for search
        TypedProperties properties = new TypedProperties();

        properties.setString("description","Apriori");
        properties.setDouble("supportThreshold", supp);
        properties.setDouble("confidenceThreshold", conf);

        AssociationRuleMining arm = new AssociationRuleMining(params, architectures, behavioral, non_behavioral, supp, conf, 1.0);
        List<Feature> baseFeatures = arm.generateBaseFeatures();
        BitSet labels = arm.getLabels();

        String filename = path + File.separator + "temp" + File.separator + "baseFeatures.csv";
        File baseFeatureFile = new File(filename);

        // Convert data into Weka Instances
        ArrayList<Attribute> attribs = new ArrayList<>();
        List<String> nomVals = new ArrayList(2);
        nomVals.add("0");
        nomVals.add("1");
        for(int j = 0; j < baseFeatures.size() + 1; j++){
            attribs.add(new Attribute(Integer.toString(j), nomVals));
        }
        Instances data = new Instances("baseFeatures", attribs, labels.size());

        // Create new instances
        for(int i = 0; i < architectures.size(); i++){
            Instance instance = new BinarySparseInstance(baseFeatures.size() + 1);
            instance.setDataset(data);

            for(int j = 0; j < baseFeatures.size(); j++){
                if(baseFeatures.get(j).getMatches().get(i)){
                    instance.setValue(j, "1");
                }else{
                    instance.setValue(j, "0");
                }

            }

            // Set label
            if(labels.get(i)){
                instance.setValue(baseFeatures.size(), "1");
            }else{
                instance.setValue(baseFeatures.size(), "0");
            }

            data.add(instance);
        }
        data.setClassIndex(data.numAttributes() - 1);

        try{
            System.out.println("Starting Weka's association rule mining");

            // build associator
            weka.associations.Apriori apriori = new weka.associations.Apriori();
            apriori.setClassIndex(data.classIndex());
            apriori.setCar(true);


            apriori.buildAssociations(data);

            // output associator
            System.out.println(apriori);

        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println(data.numInstances() + " x " + data.numAttributes());

//        boolean useOnlyInputFeatures = false;
//
//        if(useOnlyInputFeatures){
//            params.setUseOnlyInputFeatures();
//        }
//
//        AssociationRuleMining arm = new AssociationRuleMining(params, architectures, behavioral, non_behavioral, supp, conf, 1.0);
//
//        List<Feature> features = arm.run();
//
//        String savePath = path + File.separator + "results" + File.separator + runName;
//        String filename = savePath + File.separator + Apriori.class.getSimpleName() + "_" + runName;
//
//        AprioriFeatureIO featureIO = new AprioriFeatureIO(params, properties);
//        featureIO.saveFeaturesCSV(  filename + ".all_features" , features, true);

//                // Constrain the number of base features
//                public static boolean adjustRuleSize = false;
//
//                // Maximum number of iterations for adjusting the number of rules based on a given support threshold
//                public static int adjustRuleSizeMaxIter = 30;
//
//                // Number of rules required
//                public static int minRuleNum = 100;
//                public static int maxRuleNum = 1000;
//
//                // Maximum length of features
//                public static int maxLength = 2;
//
//                // Sorting metric
//                public static FeatureMetric sortBy = FeatureMetric.FCONFIDENCE;
//
//                // Use only inOrbit and notInOrbit
//                public static boolean use_only_primitive_features = false;

//                File file = new File(filename);
//                System.out.println("Writing configuration into a file");
//
//                try (FileWriter writer = new FileWriter(file)) {
//
//                    int populationSize = ((AbstractEvolutionaryAlgorithm) alg).getPopulation().size();
//                    int archiveSize = ((AbstractEvolutionaryAlgorithm) alg).getArchive().size();
//                    int maxEvals = properties.getInt("maxEvaluations", -1);
//
//                    double mutationProbability = properties.getDouble("mutationProbability",-1.0);
//                    double crossoverProbability = properties.getDouble("crossoverProbability",-1.0);
//
//                    double pmin = properties.getDouble("pmin", -1);
//                    double epsilon = properties.getDouble("epsilon", -1);
//
//                    StringJoiner content = new StringJoiner("\n");
//                    content.add("populationSize: " + populationSize);
//                    content.add("archiveSize: " + archiveSize);
//                    content.add("maxEvaluations: " + maxEvals);
//                    content.add("mutationProbability: " + mutationProbability);
//                    content.add("crossoverProbability: " + crossoverProbability);
//                    content.add("executionTime: " + executionTime);
//
//                    if(pmin > 0){
//                        content.add("pmin: " + pmin);
//                    }
//                    if(epsilon > 0){
//                        content.add("epsilon: " + epsilon);
//                    }
//
//                    writer.append(content.toString());
//                    writer.flush();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

    }

}

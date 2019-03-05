/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.local;

import ifeed.architecture.AbstractArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.io.InputDatasetReader;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.FeatureGenerator;
import ifeed.problem.assigning.Params;
import org.moeaframework.core.Algorithm;
import org.moeaframework.util.TypedProperties;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 *
 *
 * @author hsbang
 */

public class FPGrowthTest {

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

        // Set params obejct
        OntologyManager manager = new OntologyManager(path + File.separator + "ontology", "ClimateCentric");
        Params params = new Params();
        params.setOntologyManager(manager);
        params.setLeftSet(instrumentList);
        params.setRightSet(orbitList);

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

        // Settings for AbstractApriori algorithm
        double supp = 0.158;
        double conf = 0.3;

        //parameters and operators for search
        TypedProperties properties = new TypedProperties();

        properties.setString("description","AbstractApriori");
        properties.setDouble("supportThreshold", supp);
        properties.setDouble("confidenceThreshold", conf);









        FeatureGenerator generator = new FeatureGenerator(params);
//        AbstractFPGrowth fpGrowth = new FP_GROWTH(params, architectures, behavioral, non_behavioral, supp, conf, 1.0);

        boolean useOnlyInputFeatures = false;
        if(useOnlyInputFeatures){
            params.setUseOnlyInputFeatures();
        }

        List<AbstractFilter> a = generator.generateCandidates();
        System.out.println(a.size());

        //AbstractFPGrowth fpGrowthWithGeneralization = new FP_GROWTH_WITH_GENERALIZED_VARIABLES(params, architectures, behavioral, non_behavioral, supp, conf, 1.0);

        List<AbstractFilter> b = generator.generateCandidatesWithGeneralizedVariables();
        System.out.println(b.size());

//        List<Feature> features = fpGrowth.run();





//        String savePath = path + File.separator + "results" + File.separator + runName;
//        String filename = savePath + File.separator + AbstractApriori.class.getSimpleName() + "_" + runName;
//
//        ARMFeatureIO featureIO = new ARMFeatureIO(params, properties);
//        featureIO.saveFeaturesCSV(  filename + ".all_features" , features, true);
    }
}

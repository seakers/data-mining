/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.local;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.AbstractFeatureGeneralizer;
import ifeed.feature.Feature;
import ifeed.feature.FeatureWithDescription;
import ifeed.feature.logic.Connective;
import ifeed.io.InputDatasetReader;
import ifeed.mining.moea.GPMOEABase;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.FeatureGeneralizer;
import ifeed.problem.assigning.FeatureGeneralizerWithMarginalEA;
import ifeed.problem.assigning.GPMOEA;
import ifeed.problem.assigning.Params;
import org.moeaframework.core.Algorithm;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 *
 *
 * @author hsbang
 */

public class GeneralizerTest {

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

        String path = System.getProperty("user.dir");

        String inputDataFile = "/Users/bang/workspace/daphne/data-mining/data/data.csv";
        InputDatasetReader reader = new InputDatasetReader(inputDataFile);
        reader.setInputType(InputDatasetReader.InputType.BINARY_BITSTRING);
        reader.setColumnInfo(InputDatasetReader.ColumnType.CLASSLABEL,0);
        reader.setColumnInfo(InputDatasetReader.ColumnType.DECISION, 1);
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

        OntologyManager manager = new OntologyManager(path + File.separator + "ontology","ClimateCentric");

        Params params = new Params();
        params.setOntologyManager(manager);
        params.setLeftSet(instrumentList);
        params.setRightSet(orbitList);

        GPMOEABase base = new GPMOEA(params, architectures, behavioral, non_behavioral);

        AbstractFeatureGeneralizer generalizer = new FeatureGeneralizer(params, architectures, behavioral, non_behavioral, manager);

//        String rootExpression = "(({inOrbit[0;6,11;]}||{inOrbit[4;1,7,10;]})&&{separate[;3,4,11;]}&&{separate[;5,8,10;]})";
        String rootExpression = "({inOrbit[0;6,11;]}&&{inOrbit[1;10;]}&&{notInOrbit[2;5,8,9;]}&&{separate[;4,5,11;]})";
//        String rootExpression = "({notInOrbit[2;2,5;]}&&{inOrbit[4;1,7,10;]}&&{inOrbit[0;0,6,11;]})";

        Connective root = base.getFeatureHandler().generateFeatureTree(rootExpression);

        double[] metrics = Utils.computeMetricsSetNaNZero(root.getMatches(), label, architectures.size());
        Feature inputFeature = new Feature(root.getName(), root.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3]);

        Set<FeatureWithDescription> generalizedFeatures = new HashSet<>();

        generalizedFeatures = generalizer.generalize(rootExpression, null);

        if(generalizedFeatures.isEmpty()){
            System.out.println("No generalized feature found");
        }

        System.out.println("Input feature");
        System.out.println(root.getName());
        System.out.println("coverage: " + inputFeature.getRecall() + ", specificity: " + inputFeature.getPrecision());

        int i = 0;
        for(Feature feature: generalizedFeatures){
            System.out.println("----" + i++ + "-----");
            System.out.println(feature.getName());
            System.out.println("coverage: " + feature.getRecall() + ", specificity: " + feature.getPrecision());
        }
    }
}

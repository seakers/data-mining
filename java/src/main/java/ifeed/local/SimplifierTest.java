/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.local;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.AbstractFeatureGeneralizer;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.io.InputDatasetReader;
import ifeed.mining.moea.MOEABase;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.*;
import org.moeaframework.core.Algorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 *
 *
 * @author hsbang
 */

public class SimplifierTest {

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

        //OntologyManager manager = new OntologyManager(path + File.separator + "ontology","ClimateCentric");

        Params params = new Params();
        //params.setOntologyManager(manager);
        params.setLeftSet(instrumentList);
        params.setRightSet(orbitList);

        MOEABase base = new MOEA(params, architectures, behavioral, non_behavioral);

        FeatureFetcher featureFetcher = new FeatureFetcher(params, base.getBaseFeatures(), architectures);
        FilterFetcher filterFetcher = (FilterFetcher) featureFetcher.getFilterFetcher();

        FeatureSimplifier simplifier = new FeatureSimplifier(params, featureFetcher, filterFetcher);

        String expression = "({notInOrbit[2;2,5;]}&&{notInOrbit[4;1,7,10;]}&&{notInOrbit[4;0,6,11;]})";

        Connective root = base.getFeatureHandler().generateFeatureTree(expression);

        simplifier.simplify(root);

        System.out.println(root.getName());

    }
}

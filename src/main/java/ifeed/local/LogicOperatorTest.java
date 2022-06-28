/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.local;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.io.InputDatasetReader;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.*;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.LocalSearch;
import ifeed.problem.assigning.GPMOEA;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbitInstrGeneralizer;
import org.moeaframework.core.*;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 *
 *
 * @author hsbang
 */

public class LogicOperatorTest {

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

        System.out.println(behavioral.size());
        System.out.println(non_behavioral.size());
        System.out.println(architectures.size());

        OntologyManager manager = new OntologyManager(path + File.separator + "ontology",3);

        Params params = new Params();
        params.setOntologyManager(manager);
        params.setLeftSet(instrumentList);
        params.setRightSet(orbitList);

        for(int i = 0; i < params.getLeftSet().size(); i++){
            params.getLeftSetSuperclass(i);
        }

        for(int i = 0; i < params.getRightSet().size(); i++){
            params.getRightSetSuperclass(i);
        }

        AbstractMOEABase base = new GPMOEA(params, architectures, behavioral, non_behavioral);
        AbstractLocalSearch localSearch = new LocalSearch(params, architectures, behavioral, non_behavioral);


//        InOrbit2PresentWithMEA operator = new InOrbit2PresentWithMEA(params, base, localSearch);
//        String expression = "({notInOrbit[2;0,5,10;]}&&{inOrbit[0;7,6;]}&&{notInOrbit[3;0,6,10;]})";

//        InOrbit2TogetherWithMEA operator = new InOrbit2TogetherWithMEA(params, base, localSearch);
//        String expression = "({notInOrbit[2;0,5,10;]}&&{inOrbit[0;7,6;]}&&{notInOrbit[3;0,6,10;]})";

//        NotInOrbit2AbsentWithMEA operator = new NotInOrbit2AbsentWithMEA(params, base, localSearch);
//        String expression = "({absent[;2;]}&&{absent[;3;]}&&{notInOrbit[0;5,6,10;]})";

//        NotInOrbit2EmptyOrbitWithMEA operator = new NotInOrbit2EmptyOrbitWithMEA(params, base, localSearch);
//        String expression = "({absent[;2;]}&&{absent[;3;]}&&{notInOrbit[0;5,6,10;]})";

//        Separate2AbsentWithMEA operator = new Separate2AbsentWithMEA(params, base, localSearch);
//        String expression = "({separate[;2,4;]}&&{present[;0;]}&&{notInOrbit[0;5,6,10;]})";

//        InOrbits2PresentWithLocalSearch operator = new InOrbits2PresentWithLocalSearch(params, base, localSearch);
//        String expression = "(({inOrbit[1;2,6,4;]}||{inOrbit[2;7,3,2;]})&&{notInOrbit[3;1,2,3;]})";

//        NotInOrbits2AbsentWithLocalSearch operator = new NotInOrbits2AbsentWithLocalSearch(params, base, localSearch);
//        String expression = "(({notInOrbit[0;7,6;]}&&{notInOrbit[1;2,6,4;]}&&{inOrbit[2;7,3,2;]})&&{notInOrbit[3;1,2,3;]})";

//        OrbitGeneralizer operator = new OrbitGeneralizer(params, base);
//        String expression = "({notInOrbit[0;0,5,10;]}&&{inOrbit[0;7,6;]})";

//        InstrumentGeneralizationWithLocalSearch operator = new InstrumentGeneralizationWithLocalSearch(params, base, localSearch);
//        String expression = "({notInOrbit[2;0,5,10;]}&&{inOrbit[0;7,6;]}&&{notInOrbit[3;0,6,10;]})";

//        InOrbitsOrbGeneralizer operator = new InOrbitsOrbGeneralizer(params, base);
//        String expression = "(({inOrbit[3;0,5,10;]}||{inOrbit[4;7,6,5;]})&&{notInOrbit[3;0,6,10;]})";

//        InOrbitsInstrGeneralizer operator = new InOrbitsInstrGeneralizer(params, base);
//        String expression = "(({inOrbit[1;0,5,10;]}||{inOrbit[1;7,6,5;]})&&{notInOrbit[3;0,6,10;]})";

//        NotInOrbitsOrbGeneralizer operator = new NotInOrbitsOrbGeneralizer(params, base);
//        String expression = "(({notInOrbit[3;0,5,10;]}&&{notInOrbit[4;7,6,5;]})||{inOrbit[3;0,6,10;]})";

        NotInOrbitInstrGeneralizer operator = new NotInOrbitInstrGeneralizer(params, base);
        String expression = "(({notInOrbit[3;0,5,10;]}&&{notInOrbit[4;7,6,5;]})||{inOrbit[3;0,6,10;]})";

        System.out.println("Testing operator: " + operator.getClass().getSimpleName());


//        String expression = "(({inOrbit[0;7,6;]}&&{inOrbit[1;7,6;]})||{notInOrbit[2;4,5,6;]}||{inOrbit[3;0,6,10;]}||{notInOrbit[3;1,2,3;]})";
//        String expression = "({notInOrbit[2;0,5,10;]}&&{inOrbit[0;7,6;]}&&{inOrbit[3;0,6,10;]})";
//        String expression = "({notInOrbit[2;0,5,10;]}&&{separate[;1,7,6;]}&&{notInOrbit[3;0,6,10;]})";
//        String expression = "({notInOrbit[2;0,5,10;]}||{separate[;1,7,6;]}||{notInOrbit[3;0,6,10;]})";
        //String expression = "({notInOrbit[2;0,5,10;]}&&({inOrbit[0;7,6;]}||{inOrbit[1;7,10,11;]})&&{notInOrbit[3;0,6,10;]})";
//        String expression = "({notInOrbit[2;0,5,10;]}&&({inOrbit[0;1,2,7,6,11;]}||{inOrbit[1;2,7,10,11;]})&&{notInOrbit[3;0,6,10;]})";
//        String expression = "({notInOrbit[2;0,5,10;]}&&{notInOrbit[3;0,6,10;]}&&{inOrbit[4;0,6;]}&&/**/{notInOrbit[2;6,11;]})";
        //String expression = "({notInOrbit[3;0,6,10;]}&&{inOrbit[4;0,6;]}&&{inOrbit[4;10,11,0;]})";
        //String expression = "({notInOrbit[3;0,6,10;]}&&{notInOrbit[2;0,6,10,7;]}&&{notInOrbit[2;10,11,0;]})";
//        String expression = "({notInOrbit[2;0,5,10;]}&&{inOrbit[0;7,6;]}&&{inOrbit[3;0,6,10;]})";
        //String expression = "({inOrbit[3;0,1;]})";
//        String expression = "({notInOrbit[2;0,5,10;]}&&{inOrbit[0;7,6;]}&&{notInOrbit[3;0,6,10;]})";
//        String expression = "({notInOrbit[2;0,5,10;]}&&({inOrbit[0;7,6;]}||{inOrbit[1;7,10,11;]})&&{notInOrbit[3;0,6,10;]})";
//        String expression = "({notInOrbit[2;2,5;]}&&{inOrbit[4;1,7,10;]}&&{inOrbit[0;1,6,11;]})";







        Connective root = base.getFeatureHandler().generateFeatureTree(expression);

        // Evaluate the original feature
        double[] metrics = Utils.computeMetricsSetNaNZero(root.getMatches(), base.getLabels(), architectures.size());
        System.out.println(root.getName() + "| precision: " + metrics[2] + ", recall: " + metrics[3]);

        FeatureTreeVariable featureTree = new FeatureTreeVariable(base, new Connective(LogicalConnectiveType.AND));
        featureTree.setRoot(root);

        boolean applicability = operator.checkApplicability(root);

        System.out.println("Check applicability: " + applicability);

        if(applicability){
            FeatureTreeSolution sol = new FeatureTreeSolution(featureTree, 3);

            Solution[] sols = new Solution[1];
            sols[0] = sol;

            Solution[] newSols = operator.evolve(sols);

            sol = (FeatureTreeSolution) newSols[0];

            FeatureTreeVariable var = (FeatureTreeVariable) sol.getVariable(0);

            Connective root2 = var.getRoot();

            // Evaluate the original feature
            metrics = Utils.computeMetricsSetNaNZero(root2.getMatches(), base.getLabels(), architectures.size());
            System.out.println(root2.getName() + "| precision: " + metrics[2] + ", recall: " + metrics[3]);

        }else{
            System.out.println("The operator not applicable for given feature");
        }


    }
}

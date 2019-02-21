/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.local;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.io.InputDatasetReader;
import ifeed.mining.moea.*;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.MOEA;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.logicOperators.generalizationCombined.SharedInOrbit2PresentPlusCond;
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

        OntologyManager manager = new OntologyManager(path + File.separator + "ontology","ClimateCentric");

        Params params = new Params();
        params.setOntologyManager(manager);
        params.setLeftSet(instrumentList);
        params.setRightSet(orbitList);
        MOEABase base = new MOEA(params, architectures, behavioral, non_behavioral);



//        InOrbit2Present operator = new InOrbit2Present(params, base);
//        InOrbit2Together operator = new InOrbit2Together(params, base);
//        NotInOrbit2Absent operator = new NotInOrbit2Absent(params, base);
//        NotInOrbit2EmptyOrbit operator = new NotInOrbit2EmptyOrbit(params, base);
//        Separate2Absent operator = new Separate2Absent(params, base);
//        SharedNotInOrbit2AbsentPlusCond operator = new SharedNotInOrbit2AbsentPlusCond(params, base);
//        SharedInOrbit2Present operator = new SharedInOrbit2Present(params, base);
        //CombineInOrbits operator = new CombineInOrbits(params, base);
        //CombineNotInOrbits operator = new CombineNotInOrbits(params, base);
//        InstrumentGeneralizer operator = new InstrumentGeneralizer(params, base);
//        OrbitGeneralizer operator = new OrbitGeneralizer(params, base);

        SharedInOrbit2PresentPlusCond operator = new SharedInOrbit2PresentPlusCond(params, base);
//        SharedNotInOrbit2AbsentPlusCond operator = new SharedNotInOrbit2AbsentPlusCond(params, base);


        System.out.println("Testing operator: " + operator.getClass().getName());


//        String expression = "({notInOrbit[2;0,5,10;]}||{inOrbit[0;7,6;]}||{inOrbit[3;0,6,10;]})";
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
        //String expression = "({notInOrbit[2;0,5,10;]}&&{inOrbit[0;7,6;]}&&{notInOrbit[3;0,6,10;]})";
//        String expression = "({notInOrbit[2;0,5,10;]}&&({inOrbit[0;7,6;]}||{inOrbit[1;7,10,11;]})&&{notInOrbit[3;0,6,10;]})";
        String expression = "({notInOrbit[2;2,5;]}||{inOrbit[4;1,7,10;]}||{inOrbit[0;1,6,11;]})";






        Connective root = base.getFeatureHandler().generateFeatureTree(expression);
        System.out.println(root.getName());

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

            System.out.println(var.getRoot().getName());

        }else{
            System.out.println("The operator not applicable for given feature");
        }


    }
}

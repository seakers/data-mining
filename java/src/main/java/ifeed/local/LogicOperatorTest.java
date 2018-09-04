/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.local;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.io.InputDatasetReader;
import ifeed.mining.AbstractLocalSearch;
import ifeed.mining.moea.*;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.MOEA;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.logicOperators.generalization.*;
import ifeed.problem.assigning.logicOperators.generalizationPlusCondition.SharedInstrument2Absent;
import ifeed.problem.assigning.logicOperators.generalizationPlusCondition.SharedInstrument2Present;
import org.moeaframework.core.*;
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

        OntologyManager manager = new OntologyManager("ClimateCentric");

        Params params = new Params();
        params.setOntologyManager(manager);
        params.setInstrumentList(instrumentList);
        params.setOrbitList(orbitList);
        MOEABase base = new MOEA(params, architectures, behavioral, non_behavioral);

        AbstractLocalSearch localSearch = new ifeed.problem.assigning.LocalSearch(params, null, architectures, behavioral, non_behavioral);
        base.setLocalSearch(localSearch);

        //SharedInstrument2Absent operator = new SharedInstrument2Absent(params, base);
        //SharedInstrument2Present operator = new SharedInstrument2Present(params, base);
        //InOrbit2Together operator = new InOrbit2Together(params, base);
        //NotInOrbit2EmptyOrbit operator = new NotInOrbit2EmptyOrbit(params, base);
        //CombineInOrbits operator = new CombineInOrbits(params, base);
        //CombineNotInOrbits operator = new CombineNotInOrbits(params, base);
        //InstrumentSetGeneralizer operator = new InstrumentSetGeneralizer(params, base);
        //OrbitGeneralizer operator = new OrbitGeneralizer(params, base);
        //SharedInstrument2Absent operator = new SharedInstrument2Absent(params, base);
        SharedInstrument2Present operator = new SharedInstrument2Present(params, base);


        System.out.println("Testing operator: " + operator.getClass().getName());

        //String expression = "({notInOrbit[2;0,5,10;]}&&{inOrbit[0;7,6;]}&&{notInOrbit[3;0,6,10;]})";
        //String expression = "({notInOrbit[2;0,5,10;]}&&({inOrbit[0;7,6;]}||{inOrbit[1;7,10,11;]})&&{notInOrbit[3;0,6,10;]})";
        //String expression = "({notInOrbit[2;0,5,10;]}&&({inOrbit[0;1,2,7,6,11;]}||{inOrbit[1;2,7,10,11;]})&&{notInOrbit[3;0,6,10;]})";
        //String expression = "({notInOrbit[2;0,5,10;]}&&{notInOrbit[3;0,6,10;]}&&{inOrbit[4;0,6;]}&&{notInOrbit[2;6,11;]})";
        //String expression = "({notInOrbit[3;0,6,10;]}&&{inOrbit[4;0,6;]}&&{inOrbit[4;10,11,0;]})";
        //String expression = "({notInOrbit[3;0,6,10;]}&&{notInOrbit[2;0,6,10,7;]}&&{notInOrbit[2;10,11,0;]})";
        //String expression = "({inOrbit[3;0,1,2;]})";
        //String expression = "({inOrbit[3;0,1;]})";
        //String expression = "({notInOrbit[2;0,5,10;]}&&{inOrbit[0;7,6;]}&&{notInOrbit[3;0,6,10;]})";
        String expression = "({notInOrbit[2;0,5,10;]}&&({inOrbit[0;7,6;]}||{inOrbit[1;7,10,11;]})&&{notInOrbit[3;0,6,10;]})";

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

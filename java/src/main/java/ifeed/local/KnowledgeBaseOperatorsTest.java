package ifeed.local;

import ifeed.problem.assigning.Params;
import jess.Rete;
import seakers.orekit.util.OrekitConfig;
import seakers.vassar.Resource;
import seakers.vassar.Result;
import seakers.vassar.evaluation.AbstractArchitectureEvaluator;
import seakers.vassar.evaluation.ArchitectureEvaluationManager;
import seakers.vassar.problems.Assigning.ArchitectureEvaluator;
import seakers.vassar.problems.Assigning.AssigningParams;
import seakers.vassar.problems.Assigning.ClimateCentricParams;

public class KnowledgeBaseOperatorsTest {

    public static void main(String[] args){

        String path = "/Users/bang/workspace/daphne/data-mining";

        // Initialization
        String resourcesPath = "../VASSAR_resources";

        AssigningParams params = new ClimateCentricParams(resourcesPath, "CRISP-ATTRIBUTES",
                "test", "normal");
        AbstractArchitectureEvaluator eval = new ArchitectureEvaluator();
        ArchitectureEvaluationManager AE = new ArchitectureEvaluationManager(params, eval);

//        Rete r = new Rete();
//        QueryBuilder qb = new QueryBuilder(r);
        //MatlabFunctions m = new MatlabFunctions(this);
        //r.addUserfunction(m);
        //JessInitializer.getInstance().initializeJess(r, qb, null);

        AE.init(1);
        OrekitConfig.init(1, params.orekitResourcesPath);

        Resource resource = AE.getResourcePool().getResource();
        Rete r = resource.getRete();

        OrekitConfig.end();
        AE.clear();

        System.out.println("Jess Initialized");

    }
}

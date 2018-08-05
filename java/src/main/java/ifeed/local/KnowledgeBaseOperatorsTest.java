package ifeed.local;

import jess.Rete;
import rbsa.eoss.ArchitectureEvaluator;
import rbsa.eoss.JessInitializer;
import rbsa.eoss.MatlabFunctions;
import rbsa.eoss.QueryBuilder;
import rbsa.eoss.Resource;
import rbsa.eoss.local.Params;

public class KnowledgeBaseOperatorsTest {

    public static void main(String[] args){

        String path = "/Users/bang/workspace/daphne/data-mining";

        // Initialization
        String search_clps = "";
        Params params = Params.initInstance(path, "FUZZY-ATTRIBUTES", "test","normal", search_clps);//FUZZY or CRISP
        ArchitectureEvaluator AE = ArchitectureEvaluator.getInstance();
        AE.init(1);

//        Rete r = new Rete();
//        QueryBuilder qb = new QueryBuilder(r);
        //MatlabFunctions m = new MatlabFunctions(this);
        //r.addUserfunction(m);
        //JessInitializer.getInstance().initializeJess(r, qb, null);

        Resource resource = AE.getResourcePool().getResource();
        Rete r = resource.getRete();



        System.out.println("Jess Initialized");

    }
}

package ifeed.feature;

import ifeed.architecture.AbstractArchitecture;
import ifeed.local.params.BaseParams;
import ifeed.mining.InteractiveSearch;
import ifeed.ontology.OntologyManager;

import java.util.List;
import java.util.Set;

public abstract class AbstractFeatureGeneralizer implements InteractiveSearch{

    protected BaseParams params;
    protected List<AbstractArchitecture> architectures;
    protected List<Integer> behavioral;
    protected List<Integer> non_behavioral;
    protected OntologyManager ontologyManager;
    protected volatile boolean exit;

    public AbstractFeatureGeneralizer(BaseParams params,
                                      List<AbstractArchitecture> architectures,
                                      List<Integer> behavioral,
                                      List<Integer> non_behavioral,
                                      OntologyManager ontologyManager){
        this.params = params;
        this.architectures = architectures;
        this.behavioral = behavioral;
        this.non_behavioral = non_behavioral;
        this.ontologyManager = ontologyManager;
        this.exit = false;
    }

    @Override
    public void stop(){
        this.exit = true;
    }

    @Override
    public boolean getExitFlag(){
        return this.exit;
    }

    public BaseParams getParams() {
        return params;
    }

    public abstract Set<FeatureWithDescription> generalize(String rootFeatureExpression, String nodeFeatureExpression);
}

package ifeed.local.params;

import ifeed.ontology.OntologyManager;

public abstract class BaseParams {

    protected OntologyManager ontologyManager;

    public void setOntologyManager(OntologyManager ontologyManager){
        this.ontologyManager = ontologyManager;
    }

    public OntologyManager getOntologyManager() {
        return ontologyManager;
    }

    public boolean generalizationEnabled(){
        if(this.ontologyManager != null){
            return true;
        }else{
            return false;
        }
    }

}

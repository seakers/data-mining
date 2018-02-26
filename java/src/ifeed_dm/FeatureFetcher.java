package ifeed_dm;

import java.util.ArrayList;
import java.util.List;


public abstract class FeatureFetcher {

    private List<Feature> baseFeatures;

    public FeatureFetcher(List<Feature> baseFeatures){
        this.baseFeatures = baseFeatures;
    }

    public FeatureFetcher(){
        this.baseFeatures = new ArrayList<>();
    }

    public boolean emptyBaseFeature(){
        return this.baseFeatures.isEmpty();
    }

    public List<Feature> getBaseFeatures(){
        return this.baseFeatures;
    }

    public abstract Feature fetch(String type, String[] args);

    public Feature fetch(String fullExpression){

        Feature match = null;

        // Examples of feature expressions: {name[arguments]}
        try{

            for(Feature feature:this.baseFeatures){
                if(fullExpression.equals(feature.getName())){
                    match = feature;
                    break;
                }
            }

            if(match == null){

                String e;
                if(fullExpression.startsWith("{") && fullExpression.endsWith("}")){
                    e = fullExpression.substring(1,fullExpression.length()-1);
                }else{
                    e = fullExpression;
                }

                if(e.split("\\[").length==1){
                    throw new Exception("Filter expression without brackets: " + fullExpression);
                }

                String type = e.split("\\[")[0];
                String argsCombined = e.substring(0,e.length()-1).split("\\[")[1];
                String[] args = argsCombined.split(";");

                match = this.fetch(type, args);
                this.baseFeatures.add(match);
            }

        }catch(Exception e){
            System.out.println("Exc in fetching a feature from an expression");
            e.printStackTrace();
            return null;
        }

        return match;
    }
}

package ifeed_dm;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


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

    public abstract boolean emptyArchitectures();

    public List<Feature> getBaseFeatures(){
        return this.baseFeatures;
    }

    public abstract Filter fetchFilter(String type, String[] args);

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
                String[] nameAndArgs = getNameAndArgs(fullExpression);
                String type = nameAndArgs[0];
                String[] args = Arrays.copyOfRange(nameAndArgs, 1, nameAndArgs.length + 1);

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

    protected String[] getNameAndArgs(String expression){
        String e = expression;
        if(e.startsWith("{") && e.endsWith("}")){
            e = e.substring(1,e.length()-1);
        }else{
            e = e;
        }

        if(e.split("\\[").length==1){
            throw new RuntimeException("Filter expression without brackets: " + expression);
        }

        String type = e.split("\\[")[0];
        String argsCombined = e.substring(0,e.length()-1).split("\\[")[1];
        String[] args = argsCombined.split(";");

        String[] out = new String[args.length+1];
        out[0] = type;
        for(int i = 0; i < args.length; i++){
            out[i+1] = args[i];
        }
        return out;
    }
}

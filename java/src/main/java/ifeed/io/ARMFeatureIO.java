package ifeed.io;


import ifeed.local.params.BaseParams;
import org.moeaframework.util.TypedProperties;

import java.io.FileWriter;
import java.io.IOException;
import java.util.StringJoiner;

public class ARMFeatureIO extends AbstractFeatureIO{

    private static String delimiter = " "; // csv
    private BaseParams params;
    private TypedProperties properties;

    public ARMFeatureIO(BaseParams params){
        this.params = params;
        this.properties = null;
    }

    public ARMFeatureIO(BaseParams params, TypedProperties properties){
        this.params = params;
        this.properties = properties;
    }

    @Override
    public void writeHeader(FileWriter writer){
        try{
            // Write header
            if(this.properties != null){
                StringJoiner header = new StringJoiner(", ");
                header.add(this.properties.getString("description","No description provided"));

                if(this.properties != null){
                    double supportThreshold = properties.getDouble("supportThreshold", -1.0);
                    double confidenceThreshold = properties.getDouble("confidenceThreshold", -1.0);
                    int maxFeatureLength = properties.getInt("maxFeatureLength", -1);

                    if(supportThreshold > 0){
                        header.add("Support threshold: " + supportThreshold);
                    }

                    if(confidenceThreshold > 0){
                        header.add("Confidence threshold: " + confidenceThreshold);
                    }

                    if(maxFeatureLength > 0){
                        header.add("Max feature length: " + maxFeatureLength);
                    }
                }
                writer.append("# Header: " + header.toString() + "\n");
            }
        }catch (IOException exc){
            exc.printStackTrace();
        }
    }

    public String recordEvaluatedFeature(String delimiter, int index, String name, double coverage, double specificity, double complexity){
        StringJoiner sj  = new StringJoiner(delimiter);
        sj.add(Integer.toString(index));
        sj.add(name);
        sj.add(Double.toString(coverage));
        sj.add(Double.toString(specificity));
        sj.add(Double.toString(complexity));
        return sj.toString();
    }
}

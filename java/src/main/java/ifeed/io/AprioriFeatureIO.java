package ifeed.io;


import ifeed.Utils;
import ifeed.feature.logic.Connective;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.mining.moea.MOEABase;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.util.TypedProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

public class AprioriFeatureIO extends AbstractFeatureIO{

    private static String delimiter = " "; // csv
    private BaseParams params;
    private TypedProperties properties;

    public AprioriFeatureIO(BaseParams params){
        this.params = params;
        this.properties = null;
    }

    public AprioriFeatureIO(BaseParams params, TypedProperties properties){
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

                    if(supportThreshold > 0){
                        header.add("Support threshold: " + supportThreshold);
                    }

                    if(confidenceThreshold > 0){
                        header.add("Confidence threshold: " + confidenceThreshold);
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
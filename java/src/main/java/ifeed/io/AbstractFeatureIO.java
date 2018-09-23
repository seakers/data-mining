package ifeed.io;


import ifeed.feature.Feature;
import ifeed.feature.FeatureExpressionHandler;
import ifeed.feature.logic.Connective;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringJoiner;
import java.util.List;

public abstract class AbstractFeatureIO {

    private static String delimiter = " "; // csv
    private FeatureExpressionHandler handler;

    public AbstractFeatureIO(){
        handler = new FeatureExpressionHandler();
    }

    public abstract void writeHeader(FileWriter writer);

    public void saveFeaturesCSV(String filename, List<Feature> features) {

        File results = new File(filename);
        results.getParentFile().mkdirs();

        System.out.println("Saving " + features.size() + " features in a file");

        try (FileWriter writer = new FileWriter(results)) {

            this.writeHeader(writer);

            int index = 0;
            for(Feature feature: features){
                Connective root = handler.generateFeatureTree(feature.getName());
                double coverage = feature.getRecall();
                double specificity = feature.getPrecision();
                double complexity = root.getDescendantLiterals(true).size();

                writer.append(writeEvaluatedFeature2String(this.delimiter, index, "", coverage, specificity, complexity));
                writer.append("\n");
                index++;
            }
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String writeEvaluatedFeature2String(String delimiter, int index, String name, double coverage, double specificity, double complexity){
        StringJoiner sj  = new StringJoiner(delimiter);
        sj.add(Integer.toString(index));
        sj.add(name);
        sj.add(Double.toString(coverage));
        sj.add(Double.toString(specificity));
        sj.add(Double.toString(complexity));
        return sj.toString();
    }
}

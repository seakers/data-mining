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

    public void saveFeaturesCSV(String filename, List<Feature> features){
        saveFeaturesCSV(filename, features, false);
    }

    public void saveFeaturesCSV(String filename, List<Feature> features, boolean saveName){
        saveFeaturesCSV(filename, features, saveName, false);
    }

    public void saveFeaturesCSV(String filename, List<Feature> features, boolean saveName, boolean saveSupport) {

        File results = new File(filename);
        results.getParentFile().mkdirs();

        System.out.println("Saving " + features.size() + " features in a file");

        try (FileWriter writer = new FileWriter(results)) {

            this.writeHeader(writer);

            int index = 0;
            for(Feature feature: features){
                Connective root = handler.generateFeatureTree(feature.getName());
                double support = feature.getSupport();
                double lift = feature.getLift();
                double coverage = feature.getRecall();
                double specificity = feature.getPrecision();
                double complexity = root.getDescendantLiterals(true).size();

                if(saveName){
                    if(saveSupport){
                        writer.append(writeEvaluatedFeature2String(this.delimiter, index, root.getName(), support, lift, coverage, specificity, complexity));
                    }else{
                        writer.append(writeEvaluatedFeature2String(this.delimiter, index, root.getName(), coverage, specificity, complexity));
                    }
                }else{
                    if(saveSupport){
                        writer.append(writeEvaluatedFeature2String(this.delimiter, index, "", support, lift, coverage, specificity, complexity));
                    }else{
                        writer.append(writeEvaluatedFeature2String(this.delimiter, index, "", coverage, specificity, complexity));
                    }
                }

                writer.append("\n");
                index++;
            }
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String writeEvaluatedFeature2String(String delimiter, int index, String name, double support, double lift, double coverage, double specificity, double complexity){
        StringJoiner sj  = new StringJoiner(delimiter);
        sj.add(Integer.toString(index));
        sj.add(name);
        sj.add(Double.toString(support));
        sj.add(Double.toString(lift));
        sj.add(Double.toString(coverage));
        sj.add(Double.toString(specificity));
        sj.add(Double.toString(complexity));
        return sj.toString();
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

package ifeed.io;


import ifeed.Utils;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

import java.util.BitSet;
import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringJoiner;
import java.util.List;

import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.feature.logic.Connective;
import ifeed.mining.moea.MOEABase;
import org.moeaframework.util.TypedProperties;

public class FeatureIO {

    private static String delimiter = " "; // csv
    private MOEABase base;
    private TypedProperties properties;

    public FeatureIO(MOEABase base){
        this.base = base;
    }

    public FeatureIO(MOEABase base, TypedProperties properties){
        this.base = base;
        this.properties = properties;
    }

    public void writeHeader(FileWriter writer){

        try{
            // Write header
            if(this.properties != null){

                StringJoiner header = new StringJoiner(", ");
                header.add(this.properties.getString("description","No description provided"));

                String maxEvals = Integer.toString(this.properties.getInt("maxEvaluations",-1));
                String popSize = Integer.toString(this.properties.getInt("populationSize",-1));

                if(!maxEvals.equals("-1")){
                    header.add("Max evals: " + maxEvals);
                }

                if(!popSize.equals("-1")){
                    header.add("Population size: " + popSize);
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

    public void savePopulationCSV(Population pop, String filename) {

        File results = new File(filename);
        System.out.println("Saving a population in a csv file");

        try (FileWriter writer = new FileWriter(results)) {

            this.writeHeader(writer);

            Iterator<Solution> iter = pop.iterator();
            int i = 0;
            while(iter.hasNext()){

                Solution sol = iter.next();
                FeatureTreeVariable tree = (FeatureTreeVariable) sol.getVariable(0);
                Connective root = tree.getRoot();

                BitSet featureMatches = root.getMatches();
                double[] metrics = Utils.computeMetricsSetNaNZero(featureMatches, this.base.getLabels(), this.base.getPopulation().size());
                double coverage = metrics[2];
                double specificity = metrics[3];
                double complexity = tree.getRoot().getDescendantLiterals(true).size();

                writer.append(recordEvaluatedFeature(this.delimiter, i, root.getName(), coverage, specificity, complexity));
                writer.append("\n");
                i++;
            }
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAllFeaturesCSV(String filename) {

        File results = new File(filename);
        System.out.println("Saving all evaluated features in a file");

        try (FileWriter writer = new FileWriter(results)) {

            this.writeHeader(writer);

            List<MOEABase.FeatureRecord> recordedList = this.base.getRecordedFeatures();
            for(MOEABase.FeatureRecord entry:recordedList){

                double[] objectives = entry.getObjectives();
                double coverage = objectives[0];
                double specificity = objectives[1];
                double complexity = objectives[2];

                writer.append(recordEvaluatedFeature(this.delimiter, entry.getIndex(), entry.getName(), coverage, specificity, complexity));
                writer.append("\n");
            }

            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public static void saveSearchMetrics(InstrumentedAlgorithm instAlgorithm, String filename) {
//        Accumulator accum = instAlgorithm.getAccumulator();
//
//        File results = new File(filename + ".res");
//        System.out.println("Saving metrics");
//
//        try (FileWriter writer = new FileWriter(results)) {
//            Set<String> keys = accum.keySet();
//            Iterator<String> keyIter = keys.iterator();
//            while (keyIter.hasNext()) {
//                String key = keyIter.next();
//                int dataSize = accum.size(key);
//                writer.append(key).append(",");
//                for (int i = 0; i < dataSize; i++) {
//                    writer.append(accum.get(key, i).toString());
//                    if (i + 1 < dataSize) {
//                        writer.append(",");
//                    }
//                }
//                writer.append("\n");
//            }
//            writer.flush();
//
//        } catch (IOException ex) {
//            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}

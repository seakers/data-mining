package ifeed.io;


import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringJoiner;
import java.util.List;

import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.feature.logic.Connective;
import ifeed.mining.moea.MOEABase;

public class FeatureIO {

    private static String delimiter = ","; // csv
    private MOEABase base;

    public FeatureIO(MOEABase base){
        this.base = base;
    }

    public void savePopulationCSV(Population pop, String filename) {

        File results = new File(filename + "_res.csv");
        System.out.println("Saving features in a csv file");


        try (FileWriter writer = new FileWriter(results)) {

            Iterator<Solution> iter = pop.iterator();
            while(iter.hasNext()){

                StringJoiner sj = new StringJoiner(delimiter); // csv

                Solution sol = iter.next();
                FeatureTreeVariable tree = (FeatureTreeVariable) sol.getVariable(0);
                Connective root = tree.getRoot();

                sj.add(root.getName());
                writer.append(sj.toString());
                writer.append("\n");
            }

            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAllFeaturesCSV(String filename) {

        File results = new File(filename + "_res.csv");
        System.out.println("Saving all evaluated features in a csv file");

        try (FileWriter writer = new FileWriter(results)) {

            List<MOEABase.FeatureRecord> recordedList = this.base.getRecordedFeatures();
            for(MOEABase.FeatureRecord entry:recordedList){

                StringJoiner sj = new StringJoiner(delimiter); // csv

                sj.add(Integer.toString(entry.getIndex()));
                sj.add(entry.getName());

                double[] objectives = entry.getObjectives();
                double coverage = objectives[0];
                double specificity = objectives[1];
                double complexity = objectives[2];

                sj.add(Double.toString(coverage));
                sj.add(Double.toString(specificity));
                sj.add(Double.toString(complexity));

                writer.append(sj.toString());
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

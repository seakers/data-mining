package ifeed.io;


import ifeed.Utils;
import ifeed.feature.logic.Connective;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.mining.moea.GPMOEABase;
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

public class MOEAFeatureIO extends AbstractFeatureIO {

    private static String delimiter = " "; // csv
    private AbstractMOEABase base;
    private TypedProperties properties;

    public MOEAFeatureIO(AbstractMOEABase base, TypedProperties properties){
        this.base = base;
        this.properties = properties;
    }

    @Override
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

    public void savePopulationCSV(Population pop, String filename) {

        File results = new File(filename);
        results.getParentFile().mkdirs();

        System.out.println("Saving a samples in a csv file");

        try (FileWriter writer = new FileWriter(results)) {

            this.writeHeader(writer);

            Iterator<Solution> iter = pop.iterator();
            int i = 0;
            while(iter.hasNext()){

                Solution sol = iter.next();
                FeatureTreeVariable tree = (FeatureTreeVariable) sol.getVariable(0);
                Connective root = tree.getRoot();

                BitSet featureMatches = root.getMatches();
                double[] metrics = Utils.computeMetricsSetNaNZero(featureMatches, this.base.getLabels(), this.base.getSamples().size());
                double support = metrics[0];
                double lift = metrics[1];
                double precision = metrics[2];
                double recall = metrics[3];
                double complexity = tree.getRoot().getDescendantLiterals().size();

                writer.append(writeEvaluatedFeature2String(this.delimiter, i, root.getName(), support, lift, precision, recall, complexity));
                //writer.append(writeEvaluatedFeature2String(this.delimiter, i, root.getNames(), coverage, specificity, complexity));
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
        results.getParentFile().mkdirs();

        System.out.println("Saving all evaluated features in a file");

        try (FileWriter writer = new FileWriter(results)) {

            this.writeHeader(writer);

            List<GPMOEABase.FeatureRecord> recordedList = this.base.getRecordedFeatures();
            for(GPMOEABase.FeatureRecord entry:recordedList){

                String name = entry.getName();
                double[] objectives = entry.getObjectives();
                double precision = objectives[0];
                double recall = objectives[1];
                double complexity = objectives[2];

                writer.append(writeEvaluatedFeature2String(this.delimiter, entry.getIndex(), name, precision, recall, complexity));
                writer.append("\n");
            }

            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.moea;


import aos.history.OperatorSelectionHistory;
import architecture.io.ResultIO;
import ifeed.Utils;
import ifeed.io.FeatureIO;

import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.feature.logic.Connective;
import ifeed.mining.moea.MOEABase;
import ifeed.mining.moea.operators.AbstractLogicOperator;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.operator.GAVariation;
import org.moeaframework.util.TypedProperties;

import aos.aos.AOS;
import aos.history.AOSHistoryIO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

/**
 *
 * @author hsbang
 */
public class InstrumentedSearch implements Callable<Algorithm> {

    protected final String savePath;
    protected final String name;
    protected final Algorithm alg;
    protected final TypedProperties properties;
    protected MOEABase base;

    public InstrumentedSearch(Algorithm alg, TypedProperties properties, String savePath, String name, MOEABase base) {
        this.alg = alg;
        this.properties = properties;
        this.savePath = savePath;
        this.name = name;
        this.base = base;
    }

    @Override
    public Algorithm call() throws IOException  {

        int populationSize = (int) properties.getDouble("populationSize", 200);
        int maxEvaluations = (int) properties.getDouble("maxEvaluations", 1000);

        // run the executor using the listener to collect results
        System.out.println("Starting " + alg.getClass().getSimpleName() + " on " + alg.getProblem().getName() + " with pop size: " + populationSize);
        alg.step();
        long startTime = System.currentTimeMillis();
        long lastTime = System.currentTimeMillis();

        Population initPop = ((AbstractEvolutionaryAlgorithm) alg).getPopulation();
        for (int i = 0; i < initPop.size(); i++) {
            initPop.get(i).setAttribute("NFE", 0);
        }

        // Keep track of the number of times each operator was called
        Map<Variation, Integer> selectionCounter = new HashMap<>();

        while (!alg.isTerminated() && (alg.getNumberOfEvaluations() < maxEvaluations)) {
            if (alg.getNumberOfEvaluations() % 500 == 0) {
                System.out.println("-----------");
                System.out.println("NFE: " + alg.getNumberOfEvaluations());
                System.out.print("Popsize: " + ((AbstractEvolutionaryAlgorithm) alg).getPopulation().size());
                System.out.println("  Archivesize: " + ((AbstractEvolutionaryAlgorithm) alg).getArchive().size());

                if(alg instanceof AOS){
                    AOS algAOS = (AOS) alg;
                    OperatorSelectionHistory selectionHistory = algAOS.getSelectionHistory();
                    Collection<Variation> operators = selectionHistory.getOperators();

                    int logicOperatorCnt = 0;
                    for(Variation operator: operators){
                        int cnt = selectionHistory.getSelectionCount(operator);
                        int diff;

                        if(selectionCounter.keySet().contains(operator)){
                            diff = cnt - selectionCounter.get(operator);
                        }else{
                            diff = cnt;
                            selectionCounter.put(operator, cnt);
                        }
                        selectionCounter.put(operator, cnt);

                        String operatorName;
                        if(operator instanceof CompoundVariation){
                            operatorName = ((CompoundVariation)operator).getName();

                        }else{
                            String[] str = operator.toString().split("operator.");
                            String[] splitName = str[str.length - 1].split("@");
                            operatorName = splitName[0];
                        }
                        System.out.println(operatorName + " called : " + diff);

                        if(operatorName.equalsIgnoreCase("OrbitGeneralizer") || operatorName.equalsIgnoreCase("InstrumentGeneralizer") ||
                                operatorName.equalsIgnoreCase("SharedInstrument2Present") || operatorName.equalsIgnoreCase("SharedInstrument2Absent")){
                            logicOperatorCnt += diff;
                        }
                    }
                    long elapsedTime = System.currentTimeMillis() - lastTime;

                    if(logicOperatorCnt != 0){
                        long elapsedTimePerOperator = (elapsedTime / logicOperatorCnt);
                        System.out.println( "Time elapsed per logic operator : " + elapsedTimePerOperator + " ms");
                    }
                }

                long elapsedTime = System.currentTimeMillis() - lastTime;
                lastTime = System.currentTimeMillis();
                System.out.println("Elapsed time: " + (elapsedTime / 1000) + " s");
            }
            alg.step();
        }

        alg.terminate();
        long finishTime = System.currentTimeMillis();
        double executionTime = ((finishTime - startTime) / 1000);

        System.out.println("Done with optimization. Execution time: " + ((finishTime - startTime) / 1000) + "s");

        Population archive = ((AbstractEvolutionaryAlgorithm) alg).getArchive();

        for(int i = 0; i < archive.size(); i++){
            FeatureTreeVariable var = (FeatureTreeVariable) archive.get(i).getVariable(0);
            Connective root = var.getRoot();
            System.out.println(root.getDescendantLiterals(true).size() + ": " + root.getName());
        }

        if(this.base.isSaveResult()){

            String filename = savePath + File.separator + alg.getClass().getSimpleName() + "_" + name;

            FeatureIO featureIO = new FeatureIO(base, properties);
            featureIO.savePopulationCSV( archive,  filename + ".archive" );
            featureIO.saveAllFeaturesCSV(  filename + ".all_features" );
            saveProblemSpecificInfo( filename + ".params");
            writeRunConfiguration(filename + ".config", executionTime);

            if (alg instanceof AOS) {
                AOS algAOS = (AOS) alg;
                if (properties.getBoolean("saveQuality", false)) {
                    AOSHistoryIO.saveQualityHistory(algAOS.getQualityHistory(), new File(filename + ".qual"), ",");
                }
                if (properties.getBoolean("saveCredits", false)) {
                    AOSHistoryIO.saveCreditHistory(algAOS.getCreditHistory(), new File(filename + ".credit"), ",");
                }
                if (properties.getBoolean("saveSelection", false)) {
                    AOSHistoryIO.saveSelectionHistory(algAOS.getSelectionHistory(), new File(filename + ".hist"), ",");
                }
            }
        }
        return alg;
    }

    protected void saveProblemSpecificInfo(String filename){
        System.out.println("Problem-specific info not defined.");
    }

    protected void writeRunConfiguration(String filename, double executionTime){
        File file = new File(filename);
        System.out.println("Writing configuration into a file");

        try (FileWriter writer = new FileWriter(file)) {

            int populationSize = ((AbstractEvolutionaryAlgorithm) alg).getPopulation().size();
            int archiveSize = ((AbstractEvolutionaryAlgorithm) alg).getArchive().size();
            int maxEvals = properties.getInt("maxEvaluations", -1);

            double mutationProbability = properties.getDouble("mutationProbability",-1.0);
            double crossoverProbability = properties.getDouble("crossoverProbability",-1.0);

            double pmin = properties.getDouble("pmin", -1);

            StringJoiner content = new StringJoiner("\n");
            content.add("populationSize: " + populationSize);
            content.add("archiveSize: " + archiveSize);
            content.add("maxEvaluations: " + maxEvals);
            content.add("mutationProbability: " + mutationProbability);
            content.add("crossoverProbability: " + crossoverProbability);
            content.add("executionTime: " + executionTime);

            if(pmin > 0){
                content.add("pmin: " + pmin);
            }

            writer.append(content.toString());
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

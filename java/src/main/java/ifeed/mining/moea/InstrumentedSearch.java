/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.moea;


import seakers.aos.history.OperatorSelectionHistory;

import ifeed.feature.logic.Connective;
import ifeed.io.MOEAFeatureIO;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Population;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.util.TypedProperties;

import seakers.aos.aos.AOS;
import seakers.aos.history.AOSHistoryIO;

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
    protected AbstractMOEABase base;

    protected boolean suppressPrintout;

    public InstrumentedSearch(Algorithm alg, TypedProperties properties, String savePath, String name, AbstractMOEABase base) {
        this.alg = alg;
        this.properties = properties;
        this.savePath = savePath;
        this.name = name;
        this.base = base;
        this.suppressPrintout = false;
    }

    @Override
    public Algorithm call() {

        int populationSize = (int) properties.getDouble("populationSize", 200);
        int maxEvaluations = (int) properties.getDouble("maxEvaluations", 1000);

        // run the executor using the listener to collect results
        if(!this.suppressPrintout){
            System.out.println("Starting " + alg.getClass().getSimpleName() + " on " + alg.getProblem().getName() + " with pop size: " + populationSize);
        }
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

            if(!this.suppressPrintout){

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
                                operatorName = ((CompoundVariation) operator).getName();

                            }else{
                                String[] str = operator.toString().split("operator.");
                                String[] splitName = str[str.length - 1].split("@");
                                operatorName = splitName[0];
                            }
                            System.out.println(operatorName + " called : " + diff);

                            if(operatorName.equalsIgnoreCase("OrbitGeneralizerWithMEA") || operatorName.equalsIgnoreCase("InstrumentGeneralizerWithMEA") ||
                                    operatorName.equalsIgnoreCase("SharedInOrbit2PresentPlusCond") || operatorName.equalsIgnoreCase("SharedNotInOrbit2AbsentPlusCond")){
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
            }
            alg.step();
        }

        alg.terminate();

        Population archive = ((AbstractEvolutionaryAlgorithm) alg).getArchive();

        long finishTime = System.currentTimeMillis();
        double executionTime = ((finishTime - startTime) / 1000);

        if(!this.suppressPrintout){
            System.out.println("Done with optimization. Execution time: " + ((finishTime - startTime) / 1000) + "s");

            for(int i = 0; i < archive.size(); i++){
                FeatureTreeVariable var = (FeatureTreeVariable) archive.get(i).getVariable(0);
                Connective root = var.getRoot();
                System.out.println(root.getDescendantLiterals().size() + ": " + root.getName());
            }
        }

        if(this.base.isSaveResult()){

            String filename = savePath + File.separator + alg.getClass().getSimpleName() + "_" + name;
            MOEAFeatureIO featureIO = new MOEAFeatureIO(base, properties);
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

    public void setSuppressPrintout() {
        this.suppressPrintout = true;
    }

    public void setSuppressPrintout(boolean suppressPrintout) {
        this.suppressPrintout = suppressPrintout;
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
            double epsilon = properties.getDouble("epsilon", -1);

            String selector = properties.getString("selector", "not_specified");
            String operators = properties.getString("operators", "not_specified");

            StringJoiner content = new StringJoiner("\n");
            content.add("populationSize: " + populationSize);
            content.add("archiveSize: " + archiveSize);
            content.add("maxEvaluations: " + maxEvals);
            content.add("mutationProbability: " + mutationProbability);
            content.add("crossoverProbability: " + crossoverProbability);
            content.add("executionTime: " + executionTime);
            content.add("selector: " + selector);
            content.add("operators: " + operators);

            if(pmin > 0){
                content.add("pmin: " + pmin);
            }
            if(epsilon > 0){
                content.add("epsilon: " + epsilon);
            }

            writer.append(content.toString());
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

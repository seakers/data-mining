/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.moea.search;


import architecture.io.ResultIO;
import ifeed.io.FeatureIO;

import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.feature.logic.Connective;
import ifeed.mining.moea.MOEABase;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.util.TypedProperties;

import aos.aos.AOS;
import aos.history.AOSHistoryIO;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 *
 * @author hsbang
 */
public class InstrumentedSearch implements Callable<Algorithm> {

    private final String savePath;
    private final String name;
    private final Algorithm alg;
    private final TypedProperties properties;
    private MOEABase base;

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

        //HashSet<Solution> allSolutions = new HashSet();
        Population initPop = ((AbstractEvolutionaryAlgorithm) alg).getPopulation();
        for (int i = 0; i < initPop.size(); i++) {
            initPop.get(i).setAttribute("NFE", 0);
            //allSolutions.add( initPop.get(i));
        }

        while (!alg.isTerminated() && (alg.getNumberOfEvaluations() < maxEvaluations)) {
            if (alg.getNumberOfEvaluations() % 500 == 0) {
                System.out.println("NFE: " + alg.getNumberOfEvaluations());
                System.out.print("Popsize: " + ((AbstractEvolutionaryAlgorithm) alg).getPopulation().size());
                System.out.println("  Archivesize: " + ((AbstractEvolutionaryAlgorithm) alg).getArchive().size());
            }
            alg.step();
            Population pop = ((AbstractEvolutionaryAlgorithm) alg).getPopulation();
            for(int i=1; i<3; i++){
                Solution s = pop.get(pop.size() - i);
                s.setAttribute("NFE", alg.getNumberOfEvaluations());
                //allSolutions.add(s);
            }
        }

        alg.terminate();
        long finishTime = System.currentTimeMillis();
        System.out.println("Done with optimization. Execution time: " + ((finishTime - startTime) / 1000) + "s");

        String filename = savePath + File.separator + alg.getClass().getSimpleName() + "_" + name;

        Population pop = ((AbstractEvolutionaryAlgorithm) alg).getArchive();

        for(int i = 0; i < pop.size(); i++){
            FeatureTreeVariable var = (FeatureTreeVariable) pop.get(i).getVariable(0);
            Connective root = var.getRoot();
            System.out.println(root.getDescendantLiterals(true).size() + ": " + root.getName());
        }

        //ResultIO.savePopulation(((AbstractEvolutionaryAlgorithm) alg).getPopulation(), filename);
        //ResultIO.savePopulation(new Population(allSolutions), filename + "_all");
        //ResultIO.saveObjectives(alg.getResult(), filename);

        FeatureIO featureIO = new FeatureIO(base, properties);
        featureIO.saveAllFeaturesCSV( filename  );


        if (alg instanceof AOS) {
            AOS algAOS = (AOS) alg;
            if (properties.getBoolean("saveQuality", false)) {
                AOSHistoryIO.saveQualityHistory(algAOS.getQualityHistory(), new File(savePath + File.separator + name + ".qual"), ",");
            }
            if (properties.getBoolean("saveCredits", false)) {
                AOSHistoryIO.saveCreditHistory(algAOS.getCreditHistory(), new File(savePath + File.separator + name + ".credit"), ",");
            }
            if (properties.getBoolean("saveSelection", false)) {
                AOSHistoryIO.saveSelectionHistory(algAOS.getSelectionHistory(), new File(savePath + File.separator + name + ".hist"), ",");
            }
        }
        return alg;
    }

}

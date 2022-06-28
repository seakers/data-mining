package ifeed.problem.assigning;

import com.google.gson.Gson;
import ifeed.mining.moea.AbstractMOEABase;
import org.moeaframework.core.Algorithm;
import org.moeaframework.util.TypedProperties;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class InstrumentedSearch extends ifeed.mining.moea.InstrumentedSearch{

    private Params params;

    public InstrumentedSearch(Algorithm alg, TypedProperties properties, String savePath, String name, AbstractMOEABase base) {
        super(alg, properties, savePath, name, base);
        this.params = (Params) base.getParams();
    }

    public InstrumentedSearch(Algorithm alg, TypedProperties properties, String savePath, String name, AbstractMOEABase base, Params params) {
        super(alg, properties, savePath, name, base);
        this.params = params;
    }

    @Override
    protected void saveProblemSpecificInfo(String filename){

        File results = new File(filename);
        if(!this.params.leftSet.isEmpty() && !this.params.rightSet.isEmpty()){

            System.out.println("Saving AssigningProblem-specific info in a csv file");

            List<String> leftSet = new ArrayList<>();
            List<String> rightSet = new ArrayList<>();

            for(String instr: this.params.getLeftSet()){
                leftSet.add(instr);
            }

            for(String instr: this.params.getLeftSetGeneralizedConcepts()){
                leftSet.add(instr);
            }

            for(String orb: this.params.getRightSet()){
                rightSet.add(orb);
            }

            for(String orb: this.params.getRightSetGeneralizedConcepts()){
                rightSet.add(orb);
            }

            Map<String, List<String>> params = new HashMap<>();
            params.put("leftSet", leftSet);
            params.put("rightSet", rightSet);

            try (FileWriter writer = new FileWriter(results)) {
                writer.append(new Gson().toJson(params));
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

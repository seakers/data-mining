package ifeed.problem.constellation;

import ifeed.mining.moea.GPMOEABase;
import org.moeaframework.core.Algorithm;
import org.moeaframework.util.TypedProperties;

public class InstrumentedSearch extends ifeed.mining.moea.InstrumentedSearch{

    private AbstractConstellationProblemParams params;

    public InstrumentedSearch(Algorithm alg, TypedProperties properties, String savePath, String name, GPMOEABase base) {
        super(alg, properties, savePath, name, base);
        this.params = (AbstractConstellationProblemParams) base.getParams();
    }

    public InstrumentedSearch(Algorithm alg, TypedProperties properties, String savePath, String name, GPMOEABase base, AbstractConstellationProblemParams params) {
        super(alg, properties, savePath, name, base);
        this.params = params;
    }

    @Override
    protected void saveProblemSpecificInfo(String filename){

//        File results = new File(filename);
//
//        if(this.params.getOrbitIndex2Name() != null && this.params.getInstrumentIndex2Name() != null){
//
//            System.out.println("Saving problem-specific info in a csv file");
//
//            try (FileWriter writer = new FileWriter(results)) {
//
//                writer.append("#orbitList,instrumentList");
//                writer.append("\n");
//
//                StringJoiner orbitNames = new StringJoiner(",");
//                for(int i = 0; i < this.params.getOrbitIndex2Name().size(); i++){
//                    String name = this.params.getOrbitIndex2Name().get(i);
//                    orbitNames.add(name);
//                }
//
//                StringJoiner instrumentNames = new StringJoiner(",");
//                for(int i = 0; i < this.params.getInstrumentIndex2Name().size(); i++){
//                    String name = this.params.getInstrumentIndex2Name().get(i);
//                    instrumentNames.add(name);
//                }
//
//                String out = orbitNames.toString() + "\n" + instrumentNames.toString();
//                writer.append(out);
//                writer.flush();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
}

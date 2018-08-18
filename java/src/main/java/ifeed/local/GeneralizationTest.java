package ifeed.local;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.BitSet;

public class GeneralizationTest {

    // Instruments and orbits
    public static String[] instrumentList = {
            "ACE_ORCA","ACE_POL","ACE_LID",
            "CLAR_ERB","ACE_CPR","DESD_SAR",
            "DESD_LID","GACM_VIS","GACM_SWIR",
            "HYSP_TIR","POSTEPS_IRS","CNES_KaRIN"};
    public static String[] orbitList = {"LEO-600-polar-NA","SSO-600-SSO-AM","SSO-600-SSO-DD","SSO-800-SSO-DD","SSO-800-SSO-PM"};


    public static void main(String[] args){

        OntologyManager manager = new OntologyManager("ClimateCentric");

        Params params = new Params();
        params.setOntologyManager(manager);
        params.setInstrumentList(instrumentList);
        params.setOrbitList(orbitList);

        params.addInstrumentClass("ActiveInstrument");
        int index = params.getInstrumentName2Index().get("ActiveInstrument");

        BitSet input = new BitSet(60);
        for(int i = 0; i < params.getNumOrbits(); i++){
            for(int j = 0; j < params.getNumInstruments(); j++){

                set(params, input, "LEO-600-polar-NA","ACE_LID", i, j);
                set(params, input, "LEO-600-polar-NA","DESD_LID", i, j);

                set(params, input, "SSO-600-SSO-AM","GACM_VIS", i, j);
                set(params, input, "SSO-600-SSO-AM","ACE_LID", i, j);
                //set(params, input, "SSO-600-SSO-AM","ACE_ORCA", i, j);
            }
        }

        Multiset<Integer> set = HashMultiset.create();
        set.add(index);
        set.add(index);
        AbstractFilter filter = new InOrbit(params, 0, set);

        System.out.println(filter.toString());
        System.out.println(params.getInstrumentIndex2Name().get(index));

        if(filter.apply(input)){
            System.out.println("Passes test!");
        }else{
            System.out.println("Test failed!");
        }
    }

    public static int indexOf(String[] arr, String target){
        for(int i = 0; i < arr.length; i++){
            if(arr[i].equalsIgnoreCase(target)){
                return i;
            }
        }
        return -1;
    }

    public static void set(Params params, BitSet input, String orbit, String instrument, int i, int j){
        if(indexOf(orbitList, orbit) == i &&
                indexOf(instrumentList, instrument) == j){
            input.set(i * params.getNumInstruments() + j);
        }
    }
}

package ifeed.local;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import ifeed.filter.AbstractFilter;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.*;

import java.io.File;
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

        String path = System.getProperty("user.dir");

        OntologyManager manager = new OntologyManager(path + File.separator + "ontology","ClimateCentric");

        Params params = new Params();
        params.setOntologyManager(manager);
        params.setInstrumentList(instrumentList);
        params.setOrbitList(orbitList);

        params.addInstrumentClass("PassiveInstrument");
        params.addInstrumentClass("LowPowerInstrument");
        params.addInstrumentClass("ActiveInstrument");
        int activeInstrumentIndex = params.getInstrumentName2Index().get("ActiveInstrument");
        int passiveInstrumentIndex = params.getInstrumentName2Index().get("PassiveInstrument");
        int lowPowerInstrumentIndex = params.getInstrumentName2Index().get("LowPowerInstrument");

        params.addOrbitClass("Sun-synchronousOrbit");
        params.addOrbitClass("Dawn-DuskOrbit");
        params.addOrbitClass("PolarOrbit");
        params.addOrbitClass("Altitude600Orbit");
        params.addOrbitClass("AMOrbit");
        int ssoIndex = params.getOrbitName2Index().get("Sun-synchronousOrbit");
        int ddIndex = params.getOrbitName2Index().get("Dawn-DuskOrbit");
        int polarOrbitIndex = params.getOrbitName2Index().get("PolarOrbit");
        int alt600OrbitIndex = params.getOrbitName2Index().get("Altitude600Orbit");
        int amOrbitIndex = params.getOrbitName2Index().get("AMOrbit");

        BitSet input = new BitSet(60);
        for(int i = 0; i < params.getNumOrbits(); i++){
            for(int j = 0; j < params.getNumInstruments(); j++){

                set(params, input, "LEO-600-polar-NA","POSTEPS_IRS", i, j);
                set(params, input, "LEO-600-polar-NA","HYSP_TIR", i, j);
                set(params, input, "LEO-600-polar-NA","DESD_LID", i, j);

//                set(params, input, "SSO-800-SSO-PM","CNES_KaRIN", i, j);
//                set(params, input, "SSO-800-SSO-PM","CNES_KaRIN", i, j);
            }
        }

        Multiset<Integer> set = HashMultiset.create();
        set.add(params.getInstrumentName2Index().get("POSTEPS_IRS"));
        set.add(lowPowerInstrumentIndex);
        set.add(lowPowerInstrumentIndex);

        AbstractFilter filter = new InOrbit(params, params.getOrbitName2Index().get("LEO-600-polar-NA"), set);
        System.out.println(filter.toString());

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

package ifeed_dm.EOSS.filtersWithRepairOperator;

import ifeed_dm.Filter;
import java.util.BitSet;

public interface RepairOperators {

    public BitSet repair(BitSet input);

    public BitSet disrupt(BitSet input);

}

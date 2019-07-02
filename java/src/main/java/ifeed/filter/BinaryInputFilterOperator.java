package ifeed.filter;

import java.util.BitSet;
import java.util.List;

public interface BinaryInputFilterOperator extends FilterOperator {

    BitSet repair(BitSet input);
    BitSet disrupt(BitSet input);
    BitSet breakSpecifiedCondition(BitSet input, List<Integer> params);

}

package ifeed.filter;

import java.util.BitSet;

public interface BinaryInputFilterOperator extends FilterOperator {

    BitSet repair(BitSet input);
    BitSet disrupt(BitSet input);

}

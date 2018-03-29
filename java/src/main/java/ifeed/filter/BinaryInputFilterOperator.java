package ifeed.filter;

import java.util.BitSet;

public interface BinaryInputFilterOperator extends FilterOperator {

    public BitSet repair(BitSet input);
    public BitSet disrupt(BitSet input);

}

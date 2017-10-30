/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.ArrayList;
import java.util.BitSet;

/**
 *
 * @author bang
 */


public interface FilterExpressionHandler {
    
    public BitSet processSingleFilterExpression(String inputExpression);    

    public BitSet processFilterExpression(String filterExpression);

    public BitSet processFilterExpression(String filterExpression, BitSet matches);

}

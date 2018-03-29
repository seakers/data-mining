package ifeed.io;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputDatasetReader {

    private String filePath;
    private BitSet label;
    private List<AbstractArchitecture> architectures;
    private int numberOfObservations;

    private InputType inputType;
    private Integer colIndex_label;
    private List<Integer> colIndex_objectives;
    private List<Integer> colIndex_decisions;

    public InputDatasetReader(String filePath){
        this.filePath = filePath;
        this.architectures = new ArrayList<>();
        this.inputType = null;
        this.colIndex_label = null;
        this.colIndex_decisions = new ArrayList<>();
        this.colIndex_objectives = new ArrayList<>();
    }

    public void setInputType(InputType inputType){
        this.inputType = inputType;
    }

    public void setColumnInfo(ColumnType key, int colIndex){
        switch (key){
            case CLASSLABEL:
                this.colIndex_label = colIndex;
                break;
            case DECISION:
                this.colIndex_decisions.add(colIndex);
                break;
            case OBJECTIVE:
                this.colIndex_objectives.add(colIndex);
                break;
            default:
                    break;
        }
    }

    public boolean readData(){

        if(this.colIndex_label == null || this.colIndex_decisions.isEmpty()){
            throw new IllegalStateException("Column indices for input decisions and class labels should be set up to read data");

        }else if(this.inputType == null){
            throw new IllegalStateException("Input type should be set up to read data");
        }

        String dataFileContent = "";
        try(FileReader fr = new FileReader(new File(this.filePath))){
            BufferedReader bufferedReader = new BufferedReader(fr);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line.trim());
                stringBuffer.append("\n");
            }
            fr.close();
            dataFileContent = stringBuffer.toString().trim();

        }catch(IOException exc){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, exc);
            return false;
        }

        try{
            String[] lines = dataFileContent.split("\n");

            this.numberOfObservations = lines.length;
            this.label = new BitSet(lines.length);

            for(int i = 0; i < lines.length; i++){
                String[] lineSplit = lines[i].split(",");
                // label, bitString, objectives
                String _label = lineSplit[this.colIndex_label];
                if(Integer.parseInt(_label) == 1){
                    label.set(i);
                }

                double[] outputs = new double[this.colIndex_objectives.size()];
                for(int j = 0; j < this.colIndex_objectives.size(); j++){
                    outputs[j] = Double.parseDouble(lineSplit[this.colIndex_objectives.get(j)]);
                }

                String[] inputsString = new String[this.colIndex_decisions.size()];
                for(int j = 0; j < this.colIndex_decisions.size(); j++){
                    inputsString[j] = lineSplit[this.colIndex_decisions.get(j)];
                }

                switch (this.inputType){
                    case BINARY:
                        // To be implemented

                    case BINARY_BITSTRING:
                        String _inputs = inputsString[0];
                        BitSet binaryInputs = new BitSet(_inputs.length());
                        char[] _inputs_char = _inputs.toCharArray();
                        for(int j = 0; j < _inputs.length(); j++){
                            char bit = _inputs_char[j];
                            if(Character.getNumericValue(bit) == 1){
                                binaryInputs.set(j);
                            }
                        }
                        this.architectures.add(new BinaryInputArchitecture(i, binaryInputs, outputs));
                        break;

                    case DISCRETE:
                        int[] intInputs = new int[inputsString.length];
                        for(int j = 0; j < inputsString.length; j++){
                            intInputs[j] = Integer.parseInt(inputsString[j]);
                        }
                        this.architectures.add(new DiscreteInputArchitecture(i, intInputs, outputs));
                        break;

                    case CONTINUOUS:
                        // To be implemented
                        break;

                    default:
                        throw new IllegalArgumentException("Input type not supported");
                }
            }

        }catch(Exception exc){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, exc);
            return false;
        }
        return true;
    }

    public BitSet getLabel(){
        return this.label;
    }

    public List<AbstractArchitecture> getArchs(){
        return this.architectures;
    }

    public int getNumberOfObservations(){
        return this.numberOfObservations;
    }

    public enum ColumnType{
        CLASSLABEL,
        DECISION,
        OBJECTIVE
    }

    public enum InputType{
        BINARY_BITSTRING,
        BINARY,
        DISCRETE,
        CONTINUOUS
    }
}

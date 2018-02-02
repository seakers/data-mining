/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package javaInterface;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.11.0)", date = "2018-01-29")
public class DiscreteInputArchitecture implements org.apache.thrift.TBase<DiscreteInputArchitecture, DiscreteInputArchitecture._Fields>, java.io.Serializable, Cloneable, Comparable<DiscreteInputArchitecture> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("DiscreteInputArchitecture");

  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField INPUTS_FIELD_DESC = new org.apache.thrift.protocol.TField("inputs", org.apache.thrift.protocol.TType.LIST, (short)2);
  private static final org.apache.thrift.protocol.TField OUTPUTS_FIELD_DESC = new org.apache.thrift.protocol.TField("outputs", org.apache.thrift.protocol.TType.LIST, (short)3);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new DiscreteInputArchitectureStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new DiscreteInputArchitectureTupleSchemeFactory();

  public int id; // required
  public java.util.List<java.lang.Integer> inputs; // required
  public java.util.List<java.lang.Double> outputs; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ID((short)1, "id"),
    INPUTS((short)2, "inputs"),
    OUTPUTS((short)3, "outputs");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // ID
          return ID;
        case 2: // INPUTS
          return INPUTS;
        case 3: // OUTPUTS
          return OUTPUTS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __ID_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32        , "int")));
    tmpMap.put(_Fields.INPUTS, new org.apache.thrift.meta_data.FieldMetaData("inputs", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32            , "int"))));
    tmpMap.put(_Fields.OUTPUTS, new org.apache.thrift.meta_data.FieldMetaData("outputs", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(DiscreteInputArchitecture.class, metaDataMap);
  }

  public DiscreteInputArchitecture() {
  }

  public DiscreteInputArchitecture(
    int id,
    java.util.List<java.lang.Integer> inputs,
    java.util.List<java.lang.Double> outputs)
  {
    this();
    this.id = id;
    setIdIsSet(true);
    this.inputs = inputs;
    this.outputs = outputs;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public DiscreteInputArchitecture(DiscreteInputArchitecture other) {
    __isset_bitfield = other.__isset_bitfield;
    this.id = other.id;
    if (other.isSetInputs()) {
      java.util.List<java.lang.Integer> __this__inputs = new java.util.ArrayList<java.lang.Integer>(other.inputs.size());
      for (java.lang.Integer other_element : other.inputs) {
        __this__inputs.add(other_element);
      }
      this.inputs = __this__inputs;
    }
    if (other.isSetOutputs()) {
      java.util.List<java.lang.Double> __this__outputs = new java.util.ArrayList<java.lang.Double>(other.outputs);
      this.outputs = __this__outputs;
    }
  }

  public DiscreteInputArchitecture deepCopy() {
    return new DiscreteInputArchitecture(this);
  }

  @Override
  public void clear() {
    setIdIsSet(false);
    this.id = 0;
    this.inputs = null;
    this.outputs = null;
  }

  public int getId() {
    return this.id;
  }

  public DiscreteInputArchitecture setId(int id) {
    this.id = id;
    setIdIsSet(true);
    return this;
  }

  public void unsetId() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __ID_ISSET_ID);
  }

  /** Returns true if field id is set (has been assigned a value) and false otherwise */
  public boolean isSetId() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __ID_ISSET_ID);
  }

  public void setIdIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __ID_ISSET_ID, value);
  }

  public int getInputsSize() {
    return (this.inputs == null) ? 0 : this.inputs.size();
  }

  public java.util.Iterator<java.lang.Integer> getInputsIterator() {
    return (this.inputs == null) ? null : this.inputs.iterator();
  }

  public void addToInputs(int elem) {
    if (this.inputs == null) {
      this.inputs = new java.util.ArrayList<java.lang.Integer>();
    }
    this.inputs.add(elem);
  }

  public java.util.List<java.lang.Integer> getInputs() {
    return this.inputs;
  }

  public DiscreteInputArchitecture setInputs(java.util.List<java.lang.Integer> inputs) {
    this.inputs = inputs;
    return this;
  }

  public void unsetInputs() {
    this.inputs = null;
  }

  /** Returns true if field inputs is set (has been assigned a value) and false otherwise */
  public boolean isSetInputs() {
    return this.inputs != null;
  }

  public void setInputsIsSet(boolean value) {
    if (!value) {
      this.inputs = null;
    }
  }

  public int getOutputsSize() {
    return (this.outputs == null) ? 0 : this.outputs.size();
  }

  public java.util.Iterator<java.lang.Double> getOutputsIterator() {
    return (this.outputs == null) ? null : this.outputs.iterator();
  }

  public void addToOutputs(double elem) {
    if (this.outputs == null) {
      this.outputs = new java.util.ArrayList<java.lang.Double>();
    }
    this.outputs.add(elem);
  }

  public java.util.List<java.lang.Double> getOutputs() {
    return this.outputs;
  }

  public DiscreteInputArchitecture setOutputs(java.util.List<java.lang.Double> outputs) {
    this.outputs = outputs;
    return this;
  }

  public void unsetOutputs() {
    this.outputs = null;
  }

  /** Returns true if field outputs is set (has been assigned a value) and false otherwise */
  public boolean isSetOutputs() {
    return this.outputs != null;
  }

  public void setOutputsIsSet(boolean value) {
    if (!value) {
      this.outputs = null;
    }
  }

  public void setFieldValue(_Fields field, java.lang.Object value) {
    switch (field) {
    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((java.lang.Integer)value);
      }
      break;

    case INPUTS:
      if (value == null) {
        unsetInputs();
      } else {
        setInputs((java.util.List<java.lang.Integer>)value);
      }
      break;

    case OUTPUTS:
      if (value == null) {
        unsetOutputs();
      } else {
        setOutputs((java.util.List<java.lang.Double>)value);
      }
      break;

    }
  }

  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case ID:
      return getId();

    case INPUTS:
      return getInputs();

    case OUTPUTS:
      return getOutputs();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case ID:
      return isSetId();
    case INPUTS:
      return isSetInputs();
    case OUTPUTS:
      return isSetOutputs();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof DiscreteInputArchitecture)
      return this.equals((DiscreteInputArchitecture)that);
    return false;
  }

  public boolean equals(DiscreteInputArchitecture that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_id = true;
    boolean that_present_id = true;
    if (this_present_id || that_present_id) {
      if (!(this_present_id && that_present_id))
        return false;
      if (this.id != that.id)
        return false;
    }

    boolean this_present_inputs = true && this.isSetInputs();
    boolean that_present_inputs = true && that.isSetInputs();
    if (this_present_inputs || that_present_inputs) {
      if (!(this_present_inputs && that_present_inputs))
        return false;
      if (!this.inputs.equals(that.inputs))
        return false;
    }

    boolean this_present_outputs = true && this.isSetOutputs();
    boolean that_present_outputs = true && that.isSetOutputs();
    if (this_present_outputs || that_present_outputs) {
      if (!(this_present_outputs && that_present_outputs))
        return false;
      if (!this.outputs.equals(that.outputs))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + id;

    hashCode = hashCode * 8191 + ((isSetInputs()) ? 131071 : 524287);
    if (isSetInputs())
      hashCode = hashCode * 8191 + inputs.hashCode();

    hashCode = hashCode * 8191 + ((isSetOutputs()) ? 131071 : 524287);
    if (isSetOutputs())
      hashCode = hashCode * 8191 + outputs.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(DiscreteInputArchitecture other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetId()).compareTo(other.isSetId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.id, other.id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetInputs()).compareTo(other.isSetInputs());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetInputs()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.inputs, other.inputs);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetOutputs()).compareTo(other.isSetOutputs());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOutputs()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.outputs, other.outputs);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("DiscreteInputArchitecture(");
    boolean first = true;

    sb.append("id:");
    sb.append(this.id);
    first = false;
    if (!first) sb.append(", ");
    sb.append("inputs:");
    if (this.inputs == null) {
      sb.append("null");
    } else {
      sb.append(this.inputs);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("outputs:");
    if (this.outputs == null) {
      sb.append("null");
    } else {
      sb.append(this.outputs);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class DiscreteInputArchitectureStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public DiscreteInputArchitectureStandardScheme getScheme() {
      return new DiscreteInputArchitectureStandardScheme();
    }
  }

  private static class DiscreteInputArchitectureStandardScheme extends org.apache.thrift.scheme.StandardScheme<DiscreteInputArchitecture> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, DiscreteInputArchitecture struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.id = iprot.readI32();
              struct.setIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // INPUTS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list24 = iprot.readListBegin();
                struct.inputs = new java.util.ArrayList<java.lang.Integer>(_list24.size);
                int _elem25;
                for (int _i26 = 0; _i26 < _list24.size; ++_i26)
                {
                  _elem25 = iprot.readI32();
                  struct.inputs.add(_elem25);
                }
                iprot.readListEnd();
              }
              struct.setInputsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // OUTPUTS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list27 = iprot.readListBegin();
                struct.outputs = new java.util.ArrayList<java.lang.Double>(_list27.size);
                double _elem28;
                for (int _i29 = 0; _i29 < _list27.size; ++_i29)
                {
                  _elem28 = iprot.readDouble();
                  struct.outputs.add(_elem28);
                }
                iprot.readListEnd();
              }
              struct.setOutputsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, DiscreteInputArchitecture struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(ID_FIELD_DESC);
      oprot.writeI32(struct.id);
      oprot.writeFieldEnd();
      if (struct.inputs != null) {
        oprot.writeFieldBegin(INPUTS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, struct.inputs.size()));
          for (int _iter30 : struct.inputs)
          {
            oprot.writeI32(_iter30);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.outputs != null) {
        oprot.writeFieldBegin(OUTPUTS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.DOUBLE, struct.outputs.size()));
          for (double _iter31 : struct.outputs)
          {
            oprot.writeDouble(_iter31);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class DiscreteInputArchitectureTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public DiscreteInputArchitectureTupleScheme getScheme() {
      return new DiscreteInputArchitectureTupleScheme();
    }
  }

  private static class DiscreteInputArchitectureTupleScheme extends org.apache.thrift.scheme.TupleScheme<DiscreteInputArchitecture> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, DiscreteInputArchitecture struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetId()) {
        optionals.set(0);
      }
      if (struct.isSetInputs()) {
        optionals.set(1);
      }
      if (struct.isSetOutputs()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetId()) {
        oprot.writeI32(struct.id);
      }
      if (struct.isSetInputs()) {
        {
          oprot.writeI32(struct.inputs.size());
          for (int _iter32 : struct.inputs)
          {
            oprot.writeI32(_iter32);
          }
        }
      }
      if (struct.isSetOutputs()) {
        {
          oprot.writeI32(struct.outputs.size());
          for (double _iter33 : struct.outputs)
          {
            oprot.writeDouble(_iter33);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, DiscreteInputArchitecture struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.id = iprot.readI32();
        struct.setIdIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list34 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.I32, iprot.readI32());
          struct.inputs = new java.util.ArrayList<java.lang.Integer>(_list34.size);
          int _elem35;
          for (int _i36 = 0; _i36 < _list34.size; ++_i36)
          {
            _elem35 = iprot.readI32();
            struct.inputs.add(_elem35);
          }
        }
        struct.setInputsIsSet(true);
      }
      if (incoming.get(2)) {
        {
          org.apache.thrift.protocol.TList _list37 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.DOUBLE, iprot.readI32());
          struct.outputs = new java.util.ArrayList<java.lang.Double>(_list37.size);
          double _elem38;
          for (int _i39 = 0; _i39 < _list37.size; ++_i39)
          {
            _elem38 = iprot.readDouble();
            struct.outputs.add(_elem38);
          }
        }
        struct.setOutputsIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}


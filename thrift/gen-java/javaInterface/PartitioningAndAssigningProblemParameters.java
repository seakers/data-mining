/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package javaInterface;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.11.0)", date = "2018-08-16")
public class PartitioningAndAssigningProblemParameters implements org.apache.thrift.TBase<PartitioningAndAssigningProblemParameters, PartitioningAndAssigningProblemParameters._Fields>, java.io.Serializable, Cloneable, Comparable<PartitioningAndAssigningProblemParameters> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("PartitioningAndAssigningProblemParameters");

  private static final org.apache.thrift.protocol.TField ORBIT_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("orbitList", org.apache.thrift.protocol.TType.LIST, (short)1);
  private static final org.apache.thrift.protocol.TField INSTRUMENT_LIST_FIELD_DESC = new org.apache.thrift.protocol.TField("instrumentList", org.apache.thrift.protocol.TType.LIST, (short)2);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new PartitioningAndAssigningProblemParametersStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new PartitioningAndAssigningProblemParametersTupleSchemeFactory();

  public java.util.List<java.lang.String> orbitList; // required
  public java.util.List<java.lang.String> instrumentList; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ORBIT_LIST((short)1, "orbitList"),
    INSTRUMENT_LIST((short)2, "instrumentList");

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
        case 1: // ORBIT_LIST
          return ORBIT_LIST;
        case 2: // INSTRUMENT_LIST
          return INSTRUMENT_LIST;
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
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ORBIT_LIST, new org.apache.thrift.meta_data.FieldMetaData("orbitList", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.INSTRUMENT_LIST, new org.apache.thrift.meta_data.FieldMetaData("instrumentList", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(PartitioningAndAssigningProblemParameters.class, metaDataMap);
  }

  public PartitioningAndAssigningProblemParameters() {
  }

  public PartitioningAndAssigningProblemParameters(
    java.util.List<java.lang.String> orbitList,
    java.util.List<java.lang.String> instrumentList)
  {
    this();
    this.orbitList = orbitList;
    this.instrumentList = instrumentList;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public PartitioningAndAssigningProblemParameters(PartitioningAndAssigningProblemParameters other) {
    if (other.isSetOrbitList()) {
      java.util.List<java.lang.String> __this__orbitList = new java.util.ArrayList<java.lang.String>(other.orbitList);
      this.orbitList = __this__orbitList;
    }
    if (other.isSetInstrumentList()) {
      java.util.List<java.lang.String> __this__instrumentList = new java.util.ArrayList<java.lang.String>(other.instrumentList);
      this.instrumentList = __this__instrumentList;
    }
  }

  public PartitioningAndAssigningProblemParameters deepCopy() {
    return new PartitioningAndAssigningProblemParameters(this);
  }

  @Override
  public void clear() {
    this.orbitList = null;
    this.instrumentList = null;
  }

  public int getOrbitListSize() {
    return (this.orbitList == null) ? 0 : this.orbitList.size();
  }

  public java.util.Iterator<java.lang.String> getOrbitListIterator() {
    return (this.orbitList == null) ? null : this.orbitList.iterator();
  }

  public void addToOrbitList(java.lang.String elem) {
    if (this.orbitList == null) {
      this.orbitList = new java.util.ArrayList<java.lang.String>();
    }
    this.orbitList.add(elem);
  }

  public java.util.List<java.lang.String> getOrbitList() {
    return this.orbitList;
  }

  public PartitioningAndAssigningProblemParameters setOrbitList(java.util.List<java.lang.String> orbitList) {
    this.orbitList = orbitList;
    return this;
  }

  public void unsetOrbitList() {
    this.orbitList = null;
  }

  /** Returns true if field orbitList is set (has been assigned a value) and false otherwise */
  public boolean isSetOrbitList() {
    return this.orbitList != null;
  }

  public void setOrbitListIsSet(boolean value) {
    if (!value) {
      this.orbitList = null;
    }
  }

  public int getInstrumentListSize() {
    return (this.instrumentList == null) ? 0 : this.instrumentList.size();
  }

  public java.util.Iterator<java.lang.String> getInstrumentListIterator() {
    return (this.instrumentList == null) ? null : this.instrumentList.iterator();
  }

  public void addToInstrumentList(java.lang.String elem) {
    if (this.instrumentList == null) {
      this.instrumentList = new java.util.ArrayList<java.lang.String>();
    }
    this.instrumentList.add(elem);
  }

  public java.util.List<java.lang.String> getInstrumentList() {
    return this.instrumentList;
  }

  public PartitioningAndAssigningProblemParameters setInstrumentList(java.util.List<java.lang.String> instrumentList) {
    this.instrumentList = instrumentList;
    return this;
  }

  public void unsetInstrumentList() {
    this.instrumentList = null;
  }

  /** Returns true if field instrumentList is set (has been assigned a value) and false otherwise */
  public boolean isSetInstrumentList() {
    return this.instrumentList != null;
  }

  public void setInstrumentListIsSet(boolean value) {
    if (!value) {
      this.instrumentList = null;
    }
  }

  public void setFieldValue(_Fields field, java.lang.Object value) {
    switch (field) {
    case ORBIT_LIST:
      if (value == null) {
        unsetOrbitList();
      } else {
        setOrbitList((java.util.List<java.lang.String>)value);
      }
      break;

    case INSTRUMENT_LIST:
      if (value == null) {
        unsetInstrumentList();
      } else {
        setInstrumentList((java.util.List<java.lang.String>)value);
      }
      break;

    }
  }

  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case ORBIT_LIST:
      return getOrbitList();

    case INSTRUMENT_LIST:
      return getInstrumentList();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case ORBIT_LIST:
      return isSetOrbitList();
    case INSTRUMENT_LIST:
      return isSetInstrumentList();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof PartitioningAndAssigningProblemParameters)
      return this.equals((PartitioningAndAssigningProblemParameters)that);
    return false;
  }

  public boolean equals(PartitioningAndAssigningProblemParameters that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_orbitList = true && this.isSetOrbitList();
    boolean that_present_orbitList = true && that.isSetOrbitList();
    if (this_present_orbitList || that_present_orbitList) {
      if (!(this_present_orbitList && that_present_orbitList))
        return false;
      if (!this.orbitList.equals(that.orbitList))
        return false;
    }

    boolean this_present_instrumentList = true && this.isSetInstrumentList();
    boolean that_present_instrumentList = true && that.isSetInstrumentList();
    if (this_present_instrumentList || that_present_instrumentList) {
      if (!(this_present_instrumentList && that_present_instrumentList))
        return false;
      if (!this.instrumentList.equals(that.instrumentList))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetOrbitList()) ? 131071 : 524287);
    if (isSetOrbitList())
      hashCode = hashCode * 8191 + orbitList.hashCode();

    hashCode = hashCode * 8191 + ((isSetInstrumentList()) ? 131071 : 524287);
    if (isSetInstrumentList())
      hashCode = hashCode * 8191 + instrumentList.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(PartitioningAndAssigningProblemParameters other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetOrbitList()).compareTo(other.isSetOrbitList());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOrbitList()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.orbitList, other.orbitList);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetInstrumentList()).compareTo(other.isSetInstrumentList());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetInstrumentList()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.instrumentList, other.instrumentList);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("PartitioningAndAssigningProblemParameters(");
    boolean first = true;

    sb.append("orbitList:");
    if (this.orbitList == null) {
      sb.append("null");
    } else {
      sb.append(this.orbitList);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("instrumentList:");
    if (this.instrumentList == null) {
      sb.append("null");
    } else {
      sb.append(this.instrumentList);
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
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class PartitioningAndAssigningProblemParametersStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public PartitioningAndAssigningProblemParametersStandardScheme getScheme() {
      return new PartitioningAndAssigningProblemParametersStandardScheme();
    }
  }

  private static class PartitioningAndAssigningProblemParametersStandardScheme extends org.apache.thrift.scheme.StandardScheme<PartitioningAndAssigningProblemParameters> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, PartitioningAndAssigningProblemParameters struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ORBIT_LIST
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list72 = iprot.readListBegin();
                struct.orbitList = new java.util.ArrayList<java.lang.String>(_list72.size);
                java.lang.String _elem73;
                for (int _i74 = 0; _i74 < _list72.size; ++_i74)
                {
                  _elem73 = iprot.readString();
                  struct.orbitList.add(_elem73);
                }
                iprot.readListEnd();
              }
              struct.setOrbitListIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // INSTRUMENT_LIST
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list75 = iprot.readListBegin();
                struct.instrumentList = new java.util.ArrayList<java.lang.String>(_list75.size);
                java.lang.String _elem76;
                for (int _i77 = 0; _i77 < _list75.size; ++_i77)
                {
                  _elem76 = iprot.readString();
                  struct.instrumentList.add(_elem76);
                }
                iprot.readListEnd();
              }
              struct.setInstrumentListIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, PartitioningAndAssigningProblemParameters struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.orbitList != null) {
        oprot.writeFieldBegin(ORBIT_LIST_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.orbitList.size()));
          for (java.lang.String _iter78 : struct.orbitList)
          {
            oprot.writeString(_iter78);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.instrumentList != null) {
        oprot.writeFieldBegin(INSTRUMENT_LIST_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, struct.instrumentList.size()));
          for (java.lang.String _iter79 : struct.instrumentList)
          {
            oprot.writeString(_iter79);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class PartitioningAndAssigningProblemParametersTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public PartitioningAndAssigningProblemParametersTupleScheme getScheme() {
      return new PartitioningAndAssigningProblemParametersTupleScheme();
    }
  }

  private static class PartitioningAndAssigningProblemParametersTupleScheme extends org.apache.thrift.scheme.TupleScheme<PartitioningAndAssigningProblemParameters> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, PartitioningAndAssigningProblemParameters struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetOrbitList()) {
        optionals.set(0);
      }
      if (struct.isSetInstrumentList()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetOrbitList()) {
        {
          oprot.writeI32(struct.orbitList.size());
          for (java.lang.String _iter80 : struct.orbitList)
          {
            oprot.writeString(_iter80);
          }
        }
      }
      if (struct.isSetInstrumentList()) {
        {
          oprot.writeI32(struct.instrumentList.size());
          for (java.lang.String _iter81 : struct.instrumentList)
          {
            oprot.writeString(_iter81);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, PartitioningAndAssigningProblemParameters struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list82 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.orbitList = new java.util.ArrayList<java.lang.String>(_list82.size);
          java.lang.String _elem83;
          for (int _i84 = 0; _i84 < _list82.size; ++_i84)
          {
            _elem83 = iprot.readString();
            struct.orbitList.add(_elem83);
          }
        }
        struct.setOrbitListIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TList _list85 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.instrumentList = new java.util.ArrayList<java.lang.String>(_list85.size);
          java.lang.String _elem86;
          for (int _i87 = 0; _i87 < _list85.size; ++_i87)
          {
            _elem86 = iprot.readString();
            struct.instrumentList.add(_elem86);
          }
        }
        struct.setInstrumentListIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package javaInterface;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.11.0)", date = "2018-08-20")
public class TaxonomicScheme implements org.apache.thrift.TBase<TaxonomicScheme, TaxonomicScheme._Fields>, java.io.Serializable, Cloneable, Comparable<TaxonomicScheme> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TaxonomicScheme");

  private static final org.apache.thrift.protocol.TField INSTANCE_MAP_FIELD_DESC = new org.apache.thrift.protocol.TField("instanceMap", org.apache.thrift.protocol.TType.MAP, (short)1);
  private static final org.apache.thrift.protocol.TField SUPERCLASS_MAP_FIELD_DESC = new org.apache.thrift.protocol.TField("superclassMap", org.apache.thrift.protocol.TType.MAP, (short)2);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new TaxonomicSchemeStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new TaxonomicSchemeTupleSchemeFactory();

  public java.util.Map<java.lang.String,java.util.List<java.lang.String>> instanceMap; // required
  public java.util.Map<java.lang.String,java.util.List<java.lang.String>> superclassMap; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    INSTANCE_MAP((short)1, "instanceMap"),
    SUPERCLASS_MAP((short)2, "superclassMap");

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
        case 1: // INSTANCE_MAP
          return INSTANCE_MAP;
        case 2: // SUPERCLASS_MAP
          return SUPERCLASS_MAP;
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
    tmpMap.put(_Fields.INSTANCE_MAP, new org.apache.thrift.meta_data.FieldMetaData("instanceMap", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
                new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)))));
    tmpMap.put(_Fields.SUPERCLASS_MAP, new org.apache.thrift.meta_data.FieldMetaData("superclassMap", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
                new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TaxonomicScheme.class, metaDataMap);
  }

  public TaxonomicScheme() {
  }

  public TaxonomicScheme(
    java.util.Map<java.lang.String,java.util.List<java.lang.String>> instanceMap,
    java.util.Map<java.lang.String,java.util.List<java.lang.String>> superclassMap)
  {
    this();
    this.instanceMap = instanceMap;
    this.superclassMap = superclassMap;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TaxonomicScheme(TaxonomicScheme other) {
    if (other.isSetInstanceMap()) {
      java.util.Map<java.lang.String,java.util.List<java.lang.String>> __this__instanceMap = new java.util.HashMap<java.lang.String,java.util.List<java.lang.String>>(other.instanceMap.size());
      for (java.util.Map.Entry<java.lang.String, java.util.List<java.lang.String>> other_element : other.instanceMap.entrySet()) {

        java.lang.String other_element_key = other_element.getKey();
        java.util.List<java.lang.String> other_element_value = other_element.getValue();

        java.lang.String __this__instanceMap_copy_key = other_element_key;

        java.util.List<java.lang.String> __this__instanceMap_copy_value = new java.util.ArrayList<java.lang.String>(other_element_value);

        __this__instanceMap.put(__this__instanceMap_copy_key, __this__instanceMap_copy_value);
      }
      this.instanceMap = __this__instanceMap;
    }
    if (other.isSetSuperclassMap()) {
      java.util.Map<java.lang.String,java.util.List<java.lang.String>> __this__superclassMap = new java.util.HashMap<java.lang.String,java.util.List<java.lang.String>>(other.superclassMap.size());
      for (java.util.Map.Entry<java.lang.String, java.util.List<java.lang.String>> other_element : other.superclassMap.entrySet()) {

        java.lang.String other_element_key = other_element.getKey();
        java.util.List<java.lang.String> other_element_value = other_element.getValue();

        java.lang.String __this__superclassMap_copy_key = other_element_key;

        java.util.List<java.lang.String> __this__superclassMap_copy_value = new java.util.ArrayList<java.lang.String>(other_element_value);

        __this__superclassMap.put(__this__superclassMap_copy_key, __this__superclassMap_copy_value);
      }
      this.superclassMap = __this__superclassMap;
    }
  }

  public TaxonomicScheme deepCopy() {
    return new TaxonomicScheme(this);
  }

  @Override
  public void clear() {
    this.instanceMap = null;
    this.superclassMap = null;
  }

  public int getInstanceMapSize() {
    return (this.instanceMap == null) ? 0 : this.instanceMap.size();
  }

  public void putToInstanceMap(java.lang.String key, java.util.List<java.lang.String> val) {
    if (this.instanceMap == null) {
      this.instanceMap = new java.util.HashMap<java.lang.String,java.util.List<java.lang.String>>();
    }
    this.instanceMap.put(key, val);
  }

  public java.util.Map<java.lang.String,java.util.List<java.lang.String>> getInstanceMap() {
    return this.instanceMap;
  }

  public TaxonomicScheme setInstanceMap(java.util.Map<java.lang.String,java.util.List<java.lang.String>> instanceMap) {
    this.instanceMap = instanceMap;
    return this;
  }

  public void unsetInstanceMap() {
    this.instanceMap = null;
  }

  /** Returns true if field instanceMap is set (has been assigned a value) and false otherwise */
  public boolean isSetInstanceMap() {
    return this.instanceMap != null;
  }

  public void setInstanceMapIsSet(boolean value) {
    if (!value) {
      this.instanceMap = null;
    }
  }

  public int getSuperclassMapSize() {
    return (this.superclassMap == null) ? 0 : this.superclassMap.size();
  }

  public void putToSuperclassMap(java.lang.String key, java.util.List<java.lang.String> val) {
    if (this.superclassMap == null) {
      this.superclassMap = new java.util.HashMap<java.lang.String,java.util.List<java.lang.String>>();
    }
    this.superclassMap.put(key, val);
  }

  public java.util.Map<java.lang.String,java.util.List<java.lang.String>> getSuperclassMap() {
    return this.superclassMap;
  }

  public TaxonomicScheme setSuperclassMap(java.util.Map<java.lang.String,java.util.List<java.lang.String>> superclassMap) {
    this.superclassMap = superclassMap;
    return this;
  }

  public void unsetSuperclassMap() {
    this.superclassMap = null;
  }

  /** Returns true if field superclassMap is set (has been assigned a value) and false otherwise */
  public boolean isSetSuperclassMap() {
    return this.superclassMap != null;
  }

  public void setSuperclassMapIsSet(boolean value) {
    if (!value) {
      this.superclassMap = null;
    }
  }

  public void setFieldValue(_Fields field, java.lang.Object value) {
    switch (field) {
    case INSTANCE_MAP:
      if (value == null) {
        unsetInstanceMap();
      } else {
        setInstanceMap((java.util.Map<java.lang.String,java.util.List<java.lang.String>>)value);
      }
      break;

    case SUPERCLASS_MAP:
      if (value == null) {
        unsetSuperclassMap();
      } else {
        setSuperclassMap((java.util.Map<java.lang.String,java.util.List<java.lang.String>>)value);
      }
      break;

    }
  }

  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case INSTANCE_MAP:
      return getInstanceMap();

    case SUPERCLASS_MAP:
      return getSuperclassMap();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case INSTANCE_MAP:
      return isSetInstanceMap();
    case SUPERCLASS_MAP:
      return isSetSuperclassMap();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof TaxonomicScheme)
      return this.equals((TaxonomicScheme)that);
    return false;
  }

  public boolean equals(TaxonomicScheme that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_instanceMap = true && this.isSetInstanceMap();
    boolean that_present_instanceMap = true && that.isSetInstanceMap();
    if (this_present_instanceMap || that_present_instanceMap) {
      if (!(this_present_instanceMap && that_present_instanceMap))
        return false;
      if (!this.instanceMap.equals(that.instanceMap))
        return false;
    }

    boolean this_present_superclassMap = true && this.isSetSuperclassMap();
    boolean that_present_superclassMap = true && that.isSetSuperclassMap();
    if (this_present_superclassMap || that_present_superclassMap) {
      if (!(this_present_superclassMap && that_present_superclassMap))
        return false;
      if (!this.superclassMap.equals(that.superclassMap))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetInstanceMap()) ? 131071 : 524287);
    if (isSetInstanceMap())
      hashCode = hashCode * 8191 + instanceMap.hashCode();

    hashCode = hashCode * 8191 + ((isSetSuperclassMap()) ? 131071 : 524287);
    if (isSetSuperclassMap())
      hashCode = hashCode * 8191 + superclassMap.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(TaxonomicScheme other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetInstanceMap()).compareTo(other.isSetInstanceMap());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetInstanceMap()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.instanceMap, other.instanceMap);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetSuperclassMap()).compareTo(other.isSetSuperclassMap());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSuperclassMap()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.superclassMap, other.superclassMap);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("TaxonomicScheme(");
    boolean first = true;

    sb.append("instanceMap:");
    if (this.instanceMap == null) {
      sb.append("null");
    } else {
      sb.append(this.instanceMap);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("superclassMap:");
    if (this.superclassMap == null) {
      sb.append("null");
    } else {
      sb.append(this.superclassMap);
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

  private static class TaxonomicSchemeStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TaxonomicSchemeStandardScheme getScheme() {
      return new TaxonomicSchemeStandardScheme();
    }
  }

  private static class TaxonomicSchemeStandardScheme extends org.apache.thrift.scheme.StandardScheme<TaxonomicScheme> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TaxonomicScheme struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // INSTANCE_MAP
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map88 = iprot.readMapBegin();
                struct.instanceMap = new java.util.HashMap<java.lang.String,java.util.List<java.lang.String>>(2*_map88.size);
                java.lang.String _key89;
                java.util.List<java.lang.String> _val90;
                for (int _i91 = 0; _i91 < _map88.size; ++_i91)
                {
                  _key89 = iprot.readString();
                  {
                    org.apache.thrift.protocol.TList _list92 = iprot.readListBegin();
                    _val90 = new java.util.ArrayList<java.lang.String>(_list92.size);
                    java.lang.String _elem93;
                    for (int _i94 = 0; _i94 < _list92.size; ++_i94)
                    {
                      _elem93 = iprot.readString();
                      _val90.add(_elem93);
                    }
                    iprot.readListEnd();
                  }
                  struct.instanceMap.put(_key89, _val90);
                }
                iprot.readMapEnd();
              }
              struct.setInstanceMapIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // SUPERCLASS_MAP
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map95 = iprot.readMapBegin();
                struct.superclassMap = new java.util.HashMap<java.lang.String,java.util.List<java.lang.String>>(2*_map95.size);
                java.lang.String _key96;
                java.util.List<java.lang.String> _val97;
                for (int _i98 = 0; _i98 < _map95.size; ++_i98)
                {
                  _key96 = iprot.readString();
                  {
                    org.apache.thrift.protocol.TList _list99 = iprot.readListBegin();
                    _val97 = new java.util.ArrayList<java.lang.String>(_list99.size);
                    java.lang.String _elem100;
                    for (int _i101 = 0; _i101 < _list99.size; ++_i101)
                    {
                      _elem100 = iprot.readString();
                      _val97.add(_elem100);
                    }
                    iprot.readListEnd();
                  }
                  struct.superclassMap.put(_key96, _val97);
                }
                iprot.readMapEnd();
              }
              struct.setSuperclassMapIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, TaxonomicScheme struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.instanceMap != null) {
        oprot.writeFieldBegin(INSTANCE_MAP_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.LIST, struct.instanceMap.size()));
          for (java.util.Map.Entry<java.lang.String, java.util.List<java.lang.String>> _iter102 : struct.instanceMap.entrySet())
          {
            oprot.writeString(_iter102.getKey());
            {
              oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, _iter102.getValue().size()));
              for (java.lang.String _iter103 : _iter102.getValue())
              {
                oprot.writeString(_iter103);
              }
              oprot.writeListEnd();
            }
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.superclassMap != null) {
        oprot.writeFieldBegin(SUPERCLASS_MAP_FIELD_DESC);
        {
          oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.LIST, struct.superclassMap.size()));
          for (java.util.Map.Entry<java.lang.String, java.util.List<java.lang.String>> _iter104 : struct.superclassMap.entrySet())
          {
            oprot.writeString(_iter104.getKey());
            {
              oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, _iter104.getValue().size()));
              for (java.lang.String _iter105 : _iter104.getValue())
              {
                oprot.writeString(_iter105);
              }
              oprot.writeListEnd();
            }
          }
          oprot.writeMapEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TaxonomicSchemeTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TaxonomicSchemeTupleScheme getScheme() {
      return new TaxonomicSchemeTupleScheme();
    }
  }

  private static class TaxonomicSchemeTupleScheme extends org.apache.thrift.scheme.TupleScheme<TaxonomicScheme> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TaxonomicScheme struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetInstanceMap()) {
        optionals.set(0);
      }
      if (struct.isSetSuperclassMap()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetInstanceMap()) {
        {
          oprot.writeI32(struct.instanceMap.size());
          for (java.util.Map.Entry<java.lang.String, java.util.List<java.lang.String>> _iter106 : struct.instanceMap.entrySet())
          {
            oprot.writeString(_iter106.getKey());
            {
              oprot.writeI32(_iter106.getValue().size());
              for (java.lang.String _iter107 : _iter106.getValue())
              {
                oprot.writeString(_iter107);
              }
            }
          }
        }
      }
      if (struct.isSetSuperclassMap()) {
        {
          oprot.writeI32(struct.superclassMap.size());
          for (java.util.Map.Entry<java.lang.String, java.util.List<java.lang.String>> _iter108 : struct.superclassMap.entrySet())
          {
            oprot.writeString(_iter108.getKey());
            {
              oprot.writeI32(_iter108.getValue().size());
              for (java.lang.String _iter109 : _iter108.getValue())
              {
                oprot.writeString(_iter109);
              }
            }
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TaxonomicScheme struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TMap _map110 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.LIST, iprot.readI32());
          struct.instanceMap = new java.util.HashMap<java.lang.String,java.util.List<java.lang.String>>(2*_map110.size);
          java.lang.String _key111;
          java.util.List<java.lang.String> _val112;
          for (int _i113 = 0; _i113 < _map110.size; ++_i113)
          {
            _key111 = iprot.readString();
            {
              org.apache.thrift.protocol.TList _list114 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
              _val112 = new java.util.ArrayList<java.lang.String>(_list114.size);
              java.lang.String _elem115;
              for (int _i116 = 0; _i116 < _list114.size; ++_i116)
              {
                _elem115 = iprot.readString();
                _val112.add(_elem115);
              }
            }
            struct.instanceMap.put(_key111, _val112);
          }
        }
        struct.setInstanceMapIsSet(true);
      }
      if (incoming.get(1)) {
        {
          org.apache.thrift.protocol.TMap _map117 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.LIST, iprot.readI32());
          struct.superclassMap = new java.util.HashMap<java.lang.String,java.util.List<java.lang.String>>(2*_map117.size);
          java.lang.String _key118;
          java.util.List<java.lang.String> _val119;
          for (int _i120 = 0; _i120 < _map117.size; ++_i120)
          {
            _key118 = iprot.readString();
            {
              org.apache.thrift.protocol.TList _list121 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRING, iprot.readI32());
              _val119 = new java.util.ArrayList<java.lang.String>(_list121.size);
              java.lang.String _elem122;
              for (int _i123 = 0; _i123 < _list121.size; ++_i123)
              {
                _elem122 = iprot.readString();
                _val119.add(_elem122);
              }
            }
            struct.superclassMap.put(_key118, _val119);
          }
        }
        struct.setSuperclassMapIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}


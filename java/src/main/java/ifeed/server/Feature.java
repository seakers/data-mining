/**
 * Autogenerated by Thrift Compiler (0.11.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package ifeed.server;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.11.0)", date = "2019-04-24")
public class Feature implements org.apache.thrift.TBase<Feature, Feature._Fields>, java.io.Serializable, Cloneable, Comparable<Feature> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Feature");

  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("name", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField EXPRESSION_FIELD_DESC = new org.apache.thrift.protocol.TField("expression", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField METRICS_FIELD_DESC = new org.apache.thrift.protocol.TField("metrics", org.apache.thrift.protocol.TType.LIST, (short)4);
  private static final org.apache.thrift.protocol.TField COMPLEXITY_FIELD_DESC = new org.apache.thrift.protocol.TField("complexity", org.apache.thrift.protocol.TType.DOUBLE, (short)5);
  private static final org.apache.thrift.protocol.TField DESCRIPTION_FIELD_DESC = new org.apache.thrift.protocol.TField("description", org.apache.thrift.protocol.TType.STRING, (short)6);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new FeatureStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new FeatureTupleSchemeFactory();

  public int id; // required
  public java.lang.String name; // required
  public java.lang.String expression; // required
  public java.util.List<java.lang.Double> metrics; // required
  public double complexity; // required
  public java.lang.String description; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ID((short)1, "id"),
    NAME((short)2, "name"),
    EXPRESSION((short)3, "expression"),
    METRICS((short)4, "metrics"),
    COMPLEXITY((short)5, "complexity"),
    DESCRIPTION((short)6, "description");

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
        case 2: // NAME
          return NAME;
        case 3: // EXPRESSION
          return EXPRESSION;
        case 4: // METRICS
          return METRICS;
        case 5: // COMPLEXITY
          return COMPLEXITY;
        case 6: // DESCRIPTION
          return DESCRIPTION;
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
  private static final int __COMPLEXITY_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32        , "int")));
    tmpMap.put(_Fields.NAME, new org.apache.thrift.meta_data.FieldMetaData("name", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.EXPRESSION, new org.apache.thrift.meta_data.FieldMetaData("expression", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.METRICS, new org.apache.thrift.meta_data.FieldMetaData("metrics", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE))));
    tmpMap.put(_Fields.COMPLEXITY, new org.apache.thrift.meta_data.FieldMetaData("complexity", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.DOUBLE)));
    tmpMap.put(_Fields.DESCRIPTION, new org.apache.thrift.meta_data.FieldMetaData("description", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Feature.class, metaDataMap);
  }

  public Feature() {
  }

  public Feature(
    int id,
    java.lang.String name,
    java.lang.String expression,
    java.util.List<java.lang.Double> metrics,
    double complexity,
    java.lang.String description)
  {
    this();
    this.id = id;
    setIdIsSet(true);
    this.name = name;
    this.expression = expression;
    this.metrics = metrics;
    this.complexity = complexity;
    setComplexityIsSet(true);
    this.description = description;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Feature(Feature other) {
    __isset_bitfield = other.__isset_bitfield;
    this.id = other.id;
    if (other.isSetName()) {
      this.name = other.name;
    }
    if (other.isSetExpression()) {
      this.expression = other.expression;
    }
    if (other.isSetMetrics()) {
      java.util.List<java.lang.Double> __this__metrics = new java.util.ArrayList<java.lang.Double>(other.metrics);
      this.metrics = __this__metrics;
    }
    this.complexity = other.complexity;
    if (other.isSetDescription()) {
      this.description = other.description;
    }
  }

  public Feature deepCopy() {
    return new Feature(this);
  }

  @Override
  public void clear() {
    setIdIsSet(false);
    this.id = 0;
    this.name = null;
    this.expression = null;
    this.metrics = null;
    setComplexityIsSet(false);
    this.complexity = 0.0;
    this.description = null;
  }

  public int getId() {
    return this.id;
  }

  public Feature setId(int id) {
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

  public java.lang.String getName() {
    return this.name;
  }

  public Feature setName(java.lang.String name) {
    this.name = name;
    return this;
  }

  public void unsetName() {
    this.name = null;
  }

  /** Returns true if field name is set (has been assigned a value) and false otherwise */
  public boolean isSetName() {
    return this.name != null;
  }

  public void setNameIsSet(boolean value) {
    if (!value) {
      this.name = null;
    }
  }

  public java.lang.String getExpression() {
    return this.expression;
  }

  public Feature setExpression(java.lang.String expression) {
    this.expression = expression;
    return this;
  }

  public void unsetExpression() {
    this.expression = null;
  }

  /** Returns true if field expression is set (has been assigned a value) and false otherwise */
  public boolean isSetExpression() {
    return this.expression != null;
  }

  public void setExpressionIsSet(boolean value) {
    if (!value) {
      this.expression = null;
    }
  }

  public int getMetricsSize() {
    return (this.metrics == null) ? 0 : this.metrics.size();
  }

  public java.util.Iterator<java.lang.Double> getMetricsIterator() {
    return (this.metrics == null) ? null : this.metrics.iterator();
  }

  public void addToMetrics(double elem) {
    if (this.metrics == null) {
      this.metrics = new java.util.ArrayList<java.lang.Double>();
    }
    this.metrics.add(elem);
  }

  public java.util.List<java.lang.Double> getMetrics() {
    return this.metrics;
  }

  public Feature setMetrics(java.util.List<java.lang.Double> metrics) {
    this.metrics = metrics;
    return this;
  }

  public void unsetMetrics() {
    this.metrics = null;
  }

  /** Returns true if field metrics is set (has been assigned a value) and false otherwise */
  public boolean isSetMetrics() {
    return this.metrics != null;
  }

  public void setMetricsIsSet(boolean value) {
    if (!value) {
      this.metrics = null;
    }
  }

  public double getComplexity() {
    return this.complexity;
  }

  public Feature setComplexity(double complexity) {
    this.complexity = complexity;
    setComplexityIsSet(true);
    return this;
  }

  public void unsetComplexity() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __COMPLEXITY_ISSET_ID);
  }

  /** Returns true if field complexity is set (has been assigned a value) and false otherwise */
  public boolean isSetComplexity() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __COMPLEXITY_ISSET_ID);
  }

  public void setComplexityIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __COMPLEXITY_ISSET_ID, value);
  }

  public java.lang.String getDescription() {
    return this.description;
  }

  public Feature setDescription(java.lang.String description) {
    this.description = description;
    return this;
  }

  public void unsetDescription() {
    this.description = null;
  }

  /** Returns true if field description is set (has been assigned a value) and false otherwise */
  public boolean isSetDescription() {
    return this.description != null;
  }

  public void setDescriptionIsSet(boolean value) {
    if (!value) {
      this.description = null;
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

    case NAME:
      if (value == null) {
        unsetName();
      } else {
        setName((java.lang.String)value);
      }
      break;

    case EXPRESSION:
      if (value == null) {
        unsetExpression();
      } else {
        setExpression((java.lang.String)value);
      }
      break;

    case METRICS:
      if (value == null) {
        unsetMetrics();
      } else {
        setMetrics((java.util.List<java.lang.Double>)value);
      }
      break;

    case COMPLEXITY:
      if (value == null) {
        unsetComplexity();
      } else {
        setComplexity((java.lang.Double)value);
      }
      break;

    case DESCRIPTION:
      if (value == null) {
        unsetDescription();
      } else {
        setDescription((java.lang.String)value);
      }
      break;

    }
  }

  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case ID:
      return getId();

    case NAME:
      return getName();

    case EXPRESSION:
      return getExpression();

    case METRICS:
      return getMetrics();

    case COMPLEXITY:
      return getComplexity();

    case DESCRIPTION:
      return getDescription();

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
    case NAME:
      return isSetName();
    case EXPRESSION:
      return isSetExpression();
    case METRICS:
      return isSetMetrics();
    case COMPLEXITY:
      return isSetComplexity();
    case DESCRIPTION:
      return isSetDescription();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof Feature)
      return this.equals((Feature)that);
    return false;
  }

  public boolean equals(Feature that) {
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

    boolean this_present_name = true && this.isSetName();
    boolean that_present_name = true && that.isSetName();
    if (this_present_name || that_present_name) {
      if (!(this_present_name && that_present_name))
        return false;
      if (!this.name.equals(that.name))
        return false;
    }

    boolean this_present_expression = true && this.isSetExpression();
    boolean that_present_expression = true && that.isSetExpression();
    if (this_present_expression || that_present_expression) {
      if (!(this_present_expression && that_present_expression))
        return false;
      if (!this.expression.equals(that.expression))
        return false;
    }

    boolean this_present_metrics = true && this.isSetMetrics();
    boolean that_present_metrics = true && that.isSetMetrics();
    if (this_present_metrics || that_present_metrics) {
      if (!(this_present_metrics && that_present_metrics))
        return false;
      if (!this.metrics.equals(that.metrics))
        return false;
    }

    boolean this_present_complexity = true;
    boolean that_present_complexity = true;
    if (this_present_complexity || that_present_complexity) {
      if (!(this_present_complexity && that_present_complexity))
        return false;
      if (this.complexity != that.complexity)
        return false;
    }

    boolean this_present_description = true && this.isSetDescription();
    boolean that_present_description = true && that.isSetDescription();
    if (this_present_description || that_present_description) {
      if (!(this_present_description && that_present_description))
        return false;
      if (!this.description.equals(that.description))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + id;

    hashCode = hashCode * 8191 + ((isSetName()) ? 131071 : 524287);
    if (isSetName())
      hashCode = hashCode * 8191 + name.hashCode();

    hashCode = hashCode * 8191 + ((isSetExpression()) ? 131071 : 524287);
    if (isSetExpression())
      hashCode = hashCode * 8191 + expression.hashCode();

    hashCode = hashCode * 8191 + ((isSetMetrics()) ? 131071 : 524287);
    if (isSetMetrics())
      hashCode = hashCode * 8191 + metrics.hashCode();

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(complexity);

    hashCode = hashCode * 8191 + ((isSetDescription()) ? 131071 : 524287);
    if (isSetDescription())
      hashCode = hashCode * 8191 + description.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(Feature other) {
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
    lastComparison = java.lang.Boolean.valueOf(isSetName()).compareTo(other.isSetName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.name, other.name);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetExpression()).compareTo(other.isSetExpression());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetExpression()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.expression, other.expression);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetMetrics()).compareTo(other.isSetMetrics());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetMetrics()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.metrics, other.metrics);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetComplexity()).compareTo(other.isSetComplexity());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetComplexity()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.complexity, other.complexity);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetDescription()).compareTo(other.isSetDescription());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDescription()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.description, other.description);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("Feature(");
    boolean first = true;

    sb.append("id:");
    sb.append(this.id);
    first = false;
    if (!first) sb.append(", ");
    sb.append("name:");
    if (this.name == null) {
      sb.append("null");
    } else {
      sb.append(this.name);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("expression:");
    if (this.expression == null) {
      sb.append("null");
    } else {
      sb.append(this.expression);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("metrics:");
    if (this.metrics == null) {
      sb.append("null");
    } else {
      sb.append(this.metrics);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("complexity:");
    sb.append(this.complexity);
    first = false;
    if (!first) sb.append(", ");
    sb.append("description:");
    if (this.description == null) {
      sb.append("null");
    } else {
      sb.append(this.description);
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

  private static class FeatureStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public FeatureStandardScheme getScheme() {
      return new FeatureStandardScheme();
    }
  }

  private static class FeatureStandardScheme extends org.apache.thrift.scheme.StandardScheme<Feature> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, Feature struct) throws org.apache.thrift.TException {
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
          case 2: // NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.name = iprot.readString();
              struct.setNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // EXPRESSION
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.expression = iprot.readString();
              struct.setExpressionIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // METRICS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                struct.metrics = new java.util.ArrayList<java.lang.Double>(_list0.size);
                double _elem1;
                for (int _i2 = 0; _i2 < _list0.size; ++_i2)
                {
                  _elem1 = iprot.readDouble();
                  struct.metrics.add(_elem1);
                }
                iprot.readListEnd();
              }
              struct.setMetricsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // COMPLEXITY
            if (schemeField.type == org.apache.thrift.protocol.TType.DOUBLE) {
              struct.complexity = iprot.readDouble();
              struct.setComplexityIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // DESCRIPTION
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.description = iprot.readString();
              struct.setDescriptionIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, Feature struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(ID_FIELD_DESC);
      oprot.writeI32(struct.id);
      oprot.writeFieldEnd();
      if (struct.name != null) {
        oprot.writeFieldBegin(NAME_FIELD_DESC);
        oprot.writeString(struct.name);
        oprot.writeFieldEnd();
      }
      if (struct.expression != null) {
        oprot.writeFieldBegin(EXPRESSION_FIELD_DESC);
        oprot.writeString(struct.expression);
        oprot.writeFieldEnd();
      }
      if (struct.metrics != null) {
        oprot.writeFieldBegin(METRICS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.DOUBLE, struct.metrics.size()));
          for (double _iter3 : struct.metrics)
          {
            oprot.writeDouble(_iter3);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(COMPLEXITY_FIELD_DESC);
      oprot.writeDouble(struct.complexity);
      oprot.writeFieldEnd();
      if (struct.description != null) {
        oprot.writeFieldBegin(DESCRIPTION_FIELD_DESC);
        oprot.writeString(struct.description);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class FeatureTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public FeatureTupleScheme getScheme() {
      return new FeatureTupleScheme();
    }
  }

  private static class FeatureTupleScheme extends org.apache.thrift.scheme.TupleScheme<Feature> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, Feature struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetId()) {
        optionals.set(0);
      }
      if (struct.isSetName()) {
        optionals.set(1);
      }
      if (struct.isSetExpression()) {
        optionals.set(2);
      }
      if (struct.isSetMetrics()) {
        optionals.set(3);
      }
      if (struct.isSetComplexity()) {
        optionals.set(4);
      }
      if (struct.isSetDescription()) {
        optionals.set(5);
      }
      oprot.writeBitSet(optionals, 6);
      if (struct.isSetId()) {
        oprot.writeI32(struct.id);
      }
      if (struct.isSetName()) {
        oprot.writeString(struct.name);
      }
      if (struct.isSetExpression()) {
        oprot.writeString(struct.expression);
      }
      if (struct.isSetMetrics()) {
        {
          oprot.writeI32(struct.metrics.size());
          for (double _iter4 : struct.metrics)
          {
            oprot.writeDouble(_iter4);
          }
        }
      }
      if (struct.isSetComplexity()) {
        oprot.writeDouble(struct.complexity);
      }
      if (struct.isSetDescription()) {
        oprot.writeString(struct.description);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, Feature struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(6);
      if (incoming.get(0)) {
        struct.id = iprot.readI32();
        struct.setIdIsSet(true);
      }
      if (incoming.get(1)) {
        struct.name = iprot.readString();
        struct.setNameIsSet(true);
      }
      if (incoming.get(2)) {
        struct.expression = iprot.readString();
        struct.setExpressionIsSet(true);
      }
      if (incoming.get(3)) {
        {
          org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.DOUBLE, iprot.readI32());
          struct.metrics = new java.util.ArrayList<java.lang.Double>(_list5.size);
          double _elem6;
          for (int _i7 = 0; _i7 < _list5.size; ++_i7)
          {
            _elem6 = iprot.readDouble();
            struct.metrics.add(_elem6);
          }
        }
        struct.setMetricsIsSet(true);
      }
      if (incoming.get(4)) {
        struct.complexity = iprot.readDouble();
        struct.setComplexityIsSet(true);
      }
      if (incoming.get(5)) {
        struct.description = iprot.readString();
        struct.setDescriptionIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}


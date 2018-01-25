#
# Autogenerated by Thrift Compiler (0.11.0)
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#
#  options string: py
#

from thrift.Thrift import TType, TMessageType, TFrozenDict, TException, TApplicationException
from thrift.protocol.TProtocol import TProtocolException
from thrift.TRecursive import fix_spec

import sys

from thrift.transport import TTransport
all_structs = []


class Feature(object):
    """
    Attributes:
     - id
     - name
     - expression
     - metrics
    """


    def __init__(self, id=None, name=None, expression=None, metrics=None,):
        self.id = id
        self.name = name
        self.expression = expression
        self.metrics = metrics

    def read(self, iprot):
        if iprot._fast_decode is not None and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None:
            iprot._fast_decode(self, iprot, [self.__class__, self.thrift_spec])
            return
        iprot.readStructBegin()
        while True:
            (fname, ftype, fid) = iprot.readFieldBegin()
            if ftype == TType.STOP:
                break
            if fid == 1:
                if ftype == TType.I32:
                    self.id = iprot.readI32()
                else:
                    iprot.skip(ftype)
            elif fid == 2:
                if ftype == TType.STRING:
                    self.name = iprot.readString().decode('utf-8') if sys.version_info[0] == 2 else iprot.readString()
                else:
                    iprot.skip(ftype)
            elif fid == 3:
                if ftype == TType.STRING:
                    self.expression = iprot.readString().decode('utf-8') if sys.version_info[0] == 2 else iprot.readString()
                else:
                    iprot.skip(ftype)
            elif fid == 4:
                if ftype == TType.LIST:
                    self.metrics = []
                    (_etype3, _size0) = iprot.readListBegin()
                    for _i4 in range(_size0):
                        _elem5 = iprot.readDouble()
                        self.metrics.append(_elem5)
                    iprot.readListEnd()
                else:
                    iprot.skip(ftype)
            else:
                iprot.skip(ftype)
            iprot.readFieldEnd()
        iprot.readStructEnd()

    def write(self, oprot):
        if oprot._fast_encode is not None and self.thrift_spec is not None:
            oprot.trans.write(oprot._fast_encode(self, [self.__class__, self.thrift_spec]))
            return
        oprot.writeStructBegin('Feature')
        if self.id is not None:
            oprot.writeFieldBegin('id', TType.I32, 1)
            oprot.writeI32(self.id)
            oprot.writeFieldEnd()
        if self.name is not None:
            oprot.writeFieldBegin('name', TType.STRING, 2)
            oprot.writeString(self.name.encode('utf-8') if sys.version_info[0] == 2 else self.name)
            oprot.writeFieldEnd()
        if self.expression is not None:
            oprot.writeFieldBegin('expression', TType.STRING, 3)
            oprot.writeString(self.expression.encode('utf-8') if sys.version_info[0] == 2 else self.expression)
            oprot.writeFieldEnd()
        if self.metrics is not None:
            oprot.writeFieldBegin('metrics', TType.LIST, 4)
            oprot.writeListBegin(TType.DOUBLE, len(self.metrics))
            for iter6 in self.metrics:
                oprot.writeDouble(iter6)
            oprot.writeListEnd()
            oprot.writeFieldEnd()
        oprot.writeFieldStop()
        oprot.writeStructEnd()

    def validate(self):
        return

    def __repr__(self):
        L = ['%s=%r' % (key, value)
             for key, value in self.__dict__.items()]
        return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

    def __eq__(self, other):
        return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

    def __ne__(self, other):
        return not (self == other)


class BinaryInputArchitecture(object):
    """
    Attributes:
     - id
     - inputs
     - outputs
    """


    def __init__(self, id=None, inputs=None, outputs=None,):
        self.id = id
        self.inputs = inputs
        self.outputs = outputs

    def read(self, iprot):
        if iprot._fast_decode is not None and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None:
            iprot._fast_decode(self, iprot, [self.__class__, self.thrift_spec])
            return
        iprot.readStructBegin()
        while True:
            (fname, ftype, fid) = iprot.readFieldBegin()
            if ftype == TType.STOP:
                break
            if fid == 1:
                if ftype == TType.I32:
                    self.id = iprot.readI32()
                else:
                    iprot.skip(ftype)
            elif fid == 2:
                if ftype == TType.LIST:
                    self.inputs = []
                    (_etype10, _size7) = iprot.readListBegin()
                    for _i11 in range(_size7):
                        _elem12 = iprot.readBool()
                        self.inputs.append(_elem12)
                    iprot.readListEnd()
                else:
                    iprot.skip(ftype)
            elif fid == 3:
                if ftype == TType.LIST:
                    self.outputs = []
                    (_etype16, _size13) = iprot.readListBegin()
                    for _i17 in range(_size13):
                        _elem18 = iprot.readDouble()
                        self.outputs.append(_elem18)
                    iprot.readListEnd()
                else:
                    iprot.skip(ftype)
            else:
                iprot.skip(ftype)
            iprot.readFieldEnd()
        iprot.readStructEnd()

    def write(self, oprot):
        if oprot._fast_encode is not None and self.thrift_spec is not None:
            oprot.trans.write(oprot._fast_encode(self, [self.__class__, self.thrift_spec]))
            return
        oprot.writeStructBegin('BinaryInputArchitecture')
        if self.id is not None:
            oprot.writeFieldBegin('id', TType.I32, 1)
            oprot.writeI32(self.id)
            oprot.writeFieldEnd()
        if self.inputs is not None:
            oprot.writeFieldBegin('inputs', TType.LIST, 2)
            oprot.writeListBegin(TType.BOOL, len(self.inputs))
            for iter19 in self.inputs:
                oprot.writeBool(iter19)
            oprot.writeListEnd()
            oprot.writeFieldEnd()
        if self.outputs is not None:
            oprot.writeFieldBegin('outputs', TType.LIST, 3)
            oprot.writeListBegin(TType.DOUBLE, len(self.outputs))
            for iter20 in self.outputs:
                oprot.writeDouble(iter20)
            oprot.writeListEnd()
            oprot.writeFieldEnd()
        oprot.writeFieldStop()
        oprot.writeStructEnd()

    def validate(self):
        return

    def __repr__(self):
        L = ['%s=%r' % (key, value)
             for key, value in self.__dict__.items()]
        return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

    def __eq__(self, other):
        return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

    def __ne__(self, other):
        return not (self == other)


class DiscreteInputArchitecture(object):
    """
    Attributes:
     - id
     - inputs
     - outputs
    """


    def __init__(self, id=None, inputs=None, outputs=None,):
        self.id = id
        self.inputs = inputs
        self.outputs = outputs

    def read(self, iprot):
        if iprot._fast_decode is not None and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None:
            iprot._fast_decode(self, iprot, [self.__class__, self.thrift_spec])
            return
        iprot.readStructBegin()
        while True:
            (fname, ftype, fid) = iprot.readFieldBegin()
            if ftype == TType.STOP:
                break
            if fid == 1:
                if ftype == TType.I32:
                    self.id = iprot.readI32()
                else:
                    iprot.skip(ftype)
            elif fid == 2:
                if ftype == TType.LIST:
                    self.inputs = []
                    (_etype24, _size21) = iprot.readListBegin()
                    for _i25 in range(_size21):
                        _elem26 = iprot.readI32()
                        self.inputs.append(_elem26)
                    iprot.readListEnd()
                else:
                    iprot.skip(ftype)
            elif fid == 3:
                if ftype == TType.LIST:
                    self.outputs = []
                    (_etype30, _size27) = iprot.readListBegin()
                    for _i31 in range(_size27):
                        _elem32 = iprot.readDouble()
                        self.outputs.append(_elem32)
                    iprot.readListEnd()
                else:
                    iprot.skip(ftype)
            else:
                iprot.skip(ftype)
            iprot.readFieldEnd()
        iprot.readStructEnd()

    def write(self, oprot):
        if oprot._fast_encode is not None and self.thrift_spec is not None:
            oprot.trans.write(oprot._fast_encode(self, [self.__class__, self.thrift_spec]))
            return
        oprot.writeStructBegin('DiscreteInputArchitecture')
        if self.id is not None:
            oprot.writeFieldBegin('id', TType.I32, 1)
            oprot.writeI32(self.id)
            oprot.writeFieldEnd()
        if self.inputs is not None:
            oprot.writeFieldBegin('inputs', TType.LIST, 2)
            oprot.writeListBegin(TType.I32, len(self.inputs))
            for iter33 in self.inputs:
                oprot.writeI32(iter33)
            oprot.writeListEnd()
            oprot.writeFieldEnd()
        if self.outputs is not None:
            oprot.writeFieldBegin('outputs', TType.LIST, 3)
            oprot.writeListBegin(TType.DOUBLE, len(self.outputs))
            for iter34 in self.outputs:
                oprot.writeDouble(iter34)
            oprot.writeListEnd()
            oprot.writeFieldEnd()
        oprot.writeFieldStop()
        oprot.writeStructEnd()

    def validate(self):
        return

    def __repr__(self):
        L = ['%s=%r' % (key, value)
             for key, value in self.__dict__.items()]
        return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

    def __eq__(self, other):
        return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

    def __ne__(self, other):
        return not (self == other)


class Architecture(object):
    """
    Attributes:
     - id
     - inputs
     - outputs
    """


    def __init__(self, id=None, inputs=None, outputs=None,):
        self.id = id
        self.inputs = inputs
        self.outputs = outputs

    def read(self, iprot):
        if iprot._fast_decode is not None and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None:
            iprot._fast_decode(self, iprot, [self.__class__, self.thrift_spec])
            return
        iprot.readStructBegin()
        while True:
            (fname, ftype, fid) = iprot.readFieldBegin()
            if ftype == TType.STOP:
                break
            if fid == 1:
                if ftype == TType.I32:
                    self.id = iprot.readI32()
                else:
                    iprot.skip(ftype)
            elif fid == 2:
                if ftype == TType.LIST:
                    self.inputs = []
                    (_etype38, _size35) = iprot.readListBegin()
                    for _i39 in range(_size35):
                        _elem40 = iprot.readDouble()
                        self.inputs.append(_elem40)
                    iprot.readListEnd()
                else:
                    iprot.skip(ftype)
            elif fid == 3:
                if ftype == TType.LIST:
                    self.outputs = []
                    (_etype44, _size41) = iprot.readListBegin()
                    for _i45 in range(_size41):
                        _elem46 = iprot.readDouble()
                        self.outputs.append(_elem46)
                    iprot.readListEnd()
                else:
                    iprot.skip(ftype)
            else:
                iprot.skip(ftype)
            iprot.readFieldEnd()
        iprot.readStructEnd()

    def write(self, oprot):
        if oprot._fast_encode is not None and self.thrift_spec is not None:
            oprot.trans.write(oprot._fast_encode(self, [self.__class__, self.thrift_spec]))
            return
        oprot.writeStructBegin('Architecture')
        if self.id is not None:
            oprot.writeFieldBegin('id', TType.I32, 1)
            oprot.writeI32(self.id)
            oprot.writeFieldEnd()
        if self.inputs is not None:
            oprot.writeFieldBegin('inputs', TType.LIST, 2)
            oprot.writeListBegin(TType.DOUBLE, len(self.inputs))
            for iter47 in self.inputs:
                oprot.writeDouble(iter47)
            oprot.writeListEnd()
            oprot.writeFieldEnd()
        if self.outputs is not None:
            oprot.writeFieldBegin('outputs', TType.LIST, 3)
            oprot.writeListBegin(TType.DOUBLE, len(self.outputs))
            for iter48 in self.outputs:
                oprot.writeDouble(iter48)
            oprot.writeListEnd()
            oprot.writeFieldEnd()
        oprot.writeFieldStop()
        oprot.writeStructEnd()

    def validate(self):
        return

    def __repr__(self):
        L = ['%s=%r' % (key, value)
             for key, value in self.__dict__.items()]
        return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

    def __eq__(self, other):
        return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

    def __ne__(self, other):
        return not (self == other)
all_structs.append(Feature)
Feature.thrift_spec = (
    None,  # 0
    (1, TType.I32, 'id', None, None, ),  # 1
    (2, TType.STRING, 'name', 'UTF8', None, ),  # 2
    (3, TType.STRING, 'expression', 'UTF8', None, ),  # 3
    (4, TType.LIST, 'metrics', (TType.DOUBLE, None, False), None, ),  # 4
)
all_structs.append(BinaryInputArchitecture)
BinaryInputArchitecture.thrift_spec = (
    None,  # 0
    (1, TType.I32, 'id', None, None, ),  # 1
    (2, TType.LIST, 'inputs', (TType.BOOL, None, False), None, ),  # 2
    (3, TType.LIST, 'outputs', (TType.DOUBLE, None, False), None, ),  # 3
)
all_structs.append(DiscreteInputArchitecture)
DiscreteInputArchitecture.thrift_spec = (
    None,  # 0
    (1, TType.I32, 'id', None, None, ),  # 1
    (2, TType.LIST, 'inputs', (TType.I32, None, False), None, ),  # 2
    (3, TType.LIST, 'outputs', (TType.DOUBLE, None, False), None, ),  # 3
)
all_structs.append(Architecture)
Architecture.thrift_spec = (
    None,  # 0
    (1, TType.I32, 'id', None, None, ),  # 1
    (2, TType.LIST, 'inputs', (TType.DOUBLE, None, False), None, ),  # 2
    (3, TType.LIST, 'outputs', (TType.DOUBLE, None, False), None, ),  # 3
)
fix_spec(all_structs)
del all_structs

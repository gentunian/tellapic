import abc

class Base(object):
    __metaclass__ = abc.ABCMeta
    
    @abc.abstractproperty
    def value(self):
        return 'Should never see this'
    
    @value.setter
    def value(self, newvalue):
        self.value = newvalue


class Implementation(Base):
    
    _value = 'Default value'
    
    @property
    def value(self):
        return self._value



i = Implementation()
print ('Implementation.value:', i.value)

i.value = 'New value'
print ('Changed value:', i.value)

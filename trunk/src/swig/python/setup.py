#!/usr/bin/env python

"""
setup module for building SWIG tellapic library extension bindings
"""

from distutils.core import setup, Extension
import TellapicConfig

pytellapic_module = Extension("_"+TellapicConfig.EXTENSION_NAME,
                              #sources=[TellapicConfig.SWIG_GENERATED_FILE]+TellapicConfig.SOURCE_FILES,
                              sources=[TellapicConfig.SWIG_GENERATED_FILE],
                              swig_opts=['module '+TellapicConfig.EXTENSION_NAME,'cpperraswarn', TellapicConfig.OS_DEFINITIONS],
                              )

setup(name=TellapicConfig.EXTENSION_NAME,
      version=TellapicConfig.EXTENSION_VERSION,
      author="Sebastian Treu",
      description='Tellapic protocol library extension.',
      ext_modules=[pytellapic_module],
      py_modules=[TellapicConfig.EXTENSION_NAME],
      )
      

# FIND_PACKAGE(SWIG REQUIRED)
# INCLUDE(${SWIG_USE_FILE})
# 
# FIND_PACKAGE(PythonLibs)
# INCLUDE_DIRECTORIES(${PYTHON_INCLUDE_PATH})
# 
# INCLUDE_DIRECTORIES(${CMAKE_CURRENT_SOURCE_DIR})
# 
# SET(CMAKE_SWIG_FLAGS "")
# 
# SET_SOURCE_FILES_PROPERTIES(example.i PROPERTIES CPLUSPLUS ON)
# SET_SOURCE_FILES_PROPERTIES(example.i PROPERTIES SWIG_FLAGS "-includeall")
# SWIG_ADD_MODULE(example python
#   example.i example.cxx)
# SWIG_LINK_LIBRARIES(example ${PYTHON_LIBRARIES})

#include(FindSWIG)
find_package(SWIG REQUIRED)
find_package(PythonLibs)

include(FindJNI)
include(${SWIG_USE_FILE})

include_directories(${PYTHON_INCLUDE_PATH})
include_directories(${JNI_INCLUDE_DIRS})
#include_directories(${CMAKE_CURRENT_SOURCE_DIR})
include_directories(${INCDIR})

############################################################################################
# 21.12.2010
# ----------
#
# We need to somehow manage a little path problem:
#
# _pytellapic.so won't be imported if it's not in the same path of the 
# generated tellapic.py by the SWIG wrapper. Ideal structure is like this:
#
# + build
# +---+ python 
# |   | <and client dirs>
# |   +---+ lib
# |
# +---+ java
# |   | <and client dirs>
# |   +---+ lib
# |
# +---+ <languaje>
# |   | <and client dirs>
# |   +---+ lib
# |
# +---+ lib
#
#
# One approach was setting: set(LIBRARY_OUTPUT_PATH ${PROJECT_BINARY_DIR}/build/python/lib) 
# before calling swig_link_library() that works fine when we were building only
# python's bindings. But another call to set LIBRARY_OUTPUT_PATH *after*
# building python module and before building the java module won't work. Seems
# that the linking system is lazy in the terms that linking is done all together at last
# despite of module being independent each other. So, the LIBRARY_OUTPUT_PATH solution in
# this cmake file should be discarted.
#
# NOTES:
# -----
#
# 1) Python library name should be called _pytellapic.so.
# 2) Java library name should be calleb libtellapicjava.so.
#
##############################################################################################


# Sets the interface file for the swig wrapper.
set(INTERFACE_FILE ${PROJECT_SOURCE_DIR}/swig/tellapic.i)

# Sets  -outdir <dir>   - Set language specific files output directory to <dir>
# This is were tellapic.py will go it should be in the same place as _pytellapic.so
set(CMAKE_SWIG_OUTDIR ${PROJECT_BINARY_DIR}/build/python/lib) 

# Add the python module and link it
swig_add_module(pytellapic python ${INTERFACE_FILE} ${SRCDIR}/lib/tellapic.c)
swig_link_libraries(pytellapic ${PYTHON_LIBRARIES})

# Sets  -outdir <dir>   - Set language specific files output directory to <dir>
# This is where .java generetad files will go. Better path? It will flood the dir with .java files
set(CMAKE_SWIG_OUTDIR ${PROJECT_BINARY_DIR}/build/java/lib) 

# Add the java module and link it
swig_add_module(tellapicjava java ${INTERFACE_FILE} ${SRCDIR}/lib/tellapic.c)
swig_link_libraries(tellapicjava ${JAVA_LIBRARIES})

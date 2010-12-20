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

include(FindSWIG)
find_package(SWIG REQUIRED)
find_package(PythonLibs)

#include(FindJNI)
include(${SWIG_USE_FILE})

include_directories(${PYTHON_INCLUDE_PATH})
#include_directories(${JNI_INCLUDE_DIRS})
include_directories(${CMAKE_CURRENT_SOURCE_DIR})

# Sets the interface file for the swig wrapper.
set(INTERFACE_FILE ${PROJECT_SOURCE_DIR}/swig/tellapic.i)

# Sets the python bindings output path for the generated swig library.
set(LIBRARY_OUTPUT_PATH ${PROJECT_BINARY_DIR}/build/python/lib) 

#execute_process(COMMAND ${SWIG_EXECUTABLE} -python -I${PYTHON_INCLUDE_PATH} -I${INCDIR} ${INTERFACE_FILE})
#add_library(pytellapic SHARED ${SRCDIR}/lib/tellapic.c ${CMAKE_CURRENT_SOURCE_DIR}/tellapic_wrap.c)

swig_add_module(tellapic python ${INTERFACE_FILE} ${SRCDIR}/lib/tellapic.c)
swig_link_libraries(tellapic "")

# Sets the java bindings output path for the generated swig library.
#set(LIBRARY_OUTPUT_PATH ${PROJECT_BINARY_DIR}/build/java/lib) 
#swig_add_module(tellapicjava java ${INTERFACE_FILE} ${SRCDIR}/lib/tellapic.c)
#swig_link_libraries(tellapicjava ${JAVA_LIBRARIES})

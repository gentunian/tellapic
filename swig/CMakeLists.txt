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

find_package(SWIG REQUIRED)
include(${SWIG_USE_FILE})

find_package(PythonLibs)
include_directories(${PYTHON_INCLUDE_PATH})
include_directories(${CMAKE_CURRENT_SOURCE_DIR})

set(CMAKE_SWIG_FLAGS "")

message(STATUS ${CMAKE_CURRENT_SOURCE_DIR})

swig_add_module(tellapic python tellapic.i ${CMAKE_CURRENT_SOURCE_DIR}/../${SRCDIR}/lib/tellapic.c)
swig_link_libraries(tellapic ${PYTHON_LIBRARIES})
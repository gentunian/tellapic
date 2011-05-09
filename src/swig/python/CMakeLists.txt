#include(FindSWIG)
find_package(PythonLibs)

include(${SWIG_USE_FILE})

include_directories(${PYTHON_INCLUDE_PATH})
#include_directories(${CMAKE_CURRENT_SOURCE_DIR})
include_directories(${INCDIR})

#
#set(LIBRARY_OUTPUT_PATH ${EXECUTABLE_OUTPUT_PATH}/lib/python) 

# Sets the interface file for the swig wrapper.
set(INTERFACE_FILE module.i)

#
set(CMAKE_SWIG_FLAGS "-module" "pytellapic" "-cpperraswarn")

# Sets  -outdir <dir>   - Set language specific files output directory to <dir>
# This is were tellapic.py will go it should be in the same place as _pytellapic.so
#set(CMAKE_SWIG_OUTDIR ${SRCDIR}/swig/python) 

# Add the python module and link it
swig_add_module(pytellapic python ${INTERFACE_FILE} ${SRCDIR}/lib/tellapic.c)
swig_link_libraries(pytellapic ${PYTHON_LIBRARIES})

#
#set(PY_FILE ${CMAKE_SWIG_OUTDIR}/pytellapic.py)

# Copy the .py file to the same dir where .so lives
#add_custom_command(TARGET ${SWIG_MODULE_pytellapic_REAL_NAME} POST_BUILD COMMAND ${CMAKE_COMMAND} ARGS -E copy ${PY_FILE} ${EXECUTABLE_OUTPUT_PATH}/lib/python)

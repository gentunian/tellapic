#include(FindSWIG)
find_package(SWIG REQUIRED)

include(FindJNI)
include(${SWIG_USE_FILE})

include_directories(${JNI_INCLUDE_DIRS})
include_directories(${INCDIR})

# Sets the name of the java package 
set(SWIG_JAVA_PACKAGE_NAME "ar.com.tellapic.lib")

# Sets the interface file for the swig wrapper.
set(INTERFACE_FILE "module.i")

set(CMAKE_SWIG_FLAGS "-package" ${SWIG_JAVA_PACKAGE_NAME})

# Sets  -outdir <dir>   - Set language specific files output directory to <dir>
# This is where .java generetad files will go. Better path? It will flood the dir with .java files
set(CMAKE_SWIG_OUTDIR ${SRCDIR}/swig/java) 

#
#set_source_files_properties(${INTERFACE_FILE} PROPERTIES SWIG_FLAGS "-package ${SWIG_JAVA_PACKAGE_NAME}")

#
set(LIBRARY_OUTPUT_PATH ${EXECUTABLE_OUTPUT_PATH}/lib/java) 

# Add the java module and link it
swig_add_module(tellapicjava java ${INTERFACE_FILE} ${SRCDIR}/lib/tellapic.c)
swig_link_libraries(tellapicjava ${JAVA_LIBRARIES})

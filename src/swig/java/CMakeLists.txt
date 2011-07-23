find_package(Java)
if (Java_FOUND)
  include(FindJNI)
  include(${SWIG_USE_FILE})
  include_directories(${JNI_INCLUDE_DIRS})
  include_directories(${INCDIR})

  # Sets the name of the java package 
  set(SWIG_JAVA_PACKAGE_NAME "ar.com.tellapic.lib")

  # Sets the interface file for the swig wrapper.
  set(INTERFACE_FILE "module.i")

  # 
  set(CMAKE_SWIG_FLAGS "-package" ${SWIG_JAVA_PACKAGE_NAME} "-cpperraswarn" ${OS_DEFINITION} "-Wall" )

  # Sets  -outdir <dir>   - Set language specific files output directory to <dir>
  # This is where .java generetad files will go. Better path? It will flood the dir with .java files
  set(CMAKE_SWIG_OUTDIR ${CMAKE_CURRENT_BINARY_DIR}/ar/com/tellapic/lib) 

  # Add the java module and link it
  swig_add_module(tellapicjava java ${INTERFACE_FILE} ${TELLAPIC_FILE} ${TELLAPIC_COMMON} ${POSH_FILE})
  
  # 
  swig_link_libraries(tellapicjava ${JAVA_LIBRARIES})
  #set(SWIG_MODULE_tellapicjava_EXTRA_DEPS ${SRCDIR}/posh/posh.c)
  #target_link_libraries(tellapicjava ${CMAKE_REQUIRED_LIBRARIES} tellapic posh)
  
  
  # Set the debug flag for javac
  if(CMAKE_BUILD_TYPE MATCHES "Debug")
    set(JAVA_DEBUG_FLAG "-g")
  endif()
  
  
  # Compile the source files
  add_custom_command(
    TARGET tellapicjava 
    POST_BUILD 
    COMMAND cmake -E chdir ${CMAKE_CURRENT_BINARY_DIR} ${Java_JAVAC_EXECUTABLE} ARGS ${JAVA_DEBUG_FLAG} ar/com/tellapic/lib/*.java
    COMMAND cmake -E chdir ${CMAKE_CURRENT_BINARY_DIR} ${Java_JAR_EXECUTABLE} ARGS -cfv tellapicjava.jar ar
    )
else()
  message("Java not found. You won't be able to build tellapic Java bindings.")
endif()

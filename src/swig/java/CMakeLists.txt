find_package(Java)

if (JAVA_COMPILE)
  include(FindJNI)
  include(${SWIG_USE_FILE})
  include_directories(${JNI_INCLUDE_DIRS})
  include_directories(${INCDIR})
  set(SWIG_JAVA_PACKAGE_NAME "ar.com.tellapic.lib")
  set(INTERFACE_FILE "module.i")
  set(CMAKE_SWIG_FLAGS "-package" ${SWIG_JAVA_PACKAGE_NAME} "-cpperraswarn" ${OS_DEFINITION} "-Wall" )
  set(OUTDIR ${tellapic_BINARY_DIR}/lib/swig/java)
  # Sets  -outdir <dir>   - Set language specific files output directory to <dir>
  # This is where .java generetad files will go. Better path? It will flood the dir with .java files
  set(CMAKE_SWIG_OUTDIR ${OUTDIR}/ar/com/tellapic/lib) 

  # Add the java module and link it
  swig_add_module(tellapicjava java ${INTERFACE_FILE} ${TELLAPIC_FILE} ${TELLAPIC_COMMON} ${POSH_FILE})
  swig_link_libraries(tellapicjava ${JAVA_LIBRARIES})
  set_target_properties(tellapicjava PROPERTIES LIBRARY_OUTPUT_DIRECTORY ${OUTDIR})
  add_dependencies(tellapicjava DEPENDS tellapicjava_dirs)
  add_custom_target(
  	tellapicjava_dirs
  	COMMAND /usr/bin/test -d ${CMAKE_SWIG_OUTDIR} || mkdir -p ${CMAKE_SWIG_OUTDIR}
  	)
  	
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
    COMMAND ${JAVA_COMPILE} ARGS ${JAVA_DEBUG_FLAG} ${OUTDIR}/ar/com/tellapic/lib/*.java
    COMMAND ${JAVA_ARCHIVE} ARGS -cfv ${OUTDIR}/tellapicjava.jar -C ${OUTDIR} ar
    )
else()
  message("Java not found. You won't be able to build tellapic Java bindings.")
endif()

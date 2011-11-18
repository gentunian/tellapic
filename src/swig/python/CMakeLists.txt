include(FindPythonInterp)

if (PYTHON_EXECUTABLE)
  # Sets the interface file for the swig wrapper.
  set(INTERFACE_FILE      "${CMAKE_CURRENT_SOURCE_DIR}/module.i")
  set(EXTENSION_NAME      "pytellapic")
  set(SETUP_PY            "${CMAKE_CURRENT_SOURCE_DIR}/setup.py")
  set(PYTHONPATH          "${CMAKE_CURRENT_BINARY_DIR}")
  set(INCLUDE_DIR         "-I${PROJECT_SOURCE_DIR}/include")
  set(OUTDIR              "${tellapic_BINARY_DIR}/lib/swig/python")
  set(SWIG_GENERATED_FILE "tellapicPYTHON_wrap.c")
  set(CMAKE_SWIG_FLAGS 
   						  "-module" ${EXTENSION_NAME}
    					  "-cpperraswarn" 
    					  ${OS_DEFINITION} 
    					  "-threads" 
    					  ${INCLUDE_DIR} 
    					  "-o" ${OUTDIR}/${SWIG_GENERATED_FILE}
    					  "-outdir" ${OUTDIR}
    					  "-python" ${INTERFACE_FILE}
     )
  if (NOT DEFINED PYTHON_VERSIONS)
    message(WARNING "\nYou didn't provide PYTHON_VERSIONS.\ne.g., $ cmake -DPYTHON_VERSIONS=\"3;2.7\"\nPython bindings will be built against ${PYTHON_EXECUTABLE}")
    string(REGEX REPLACE "[a-zA-Z/\\]+" "" PYTHON_VERSIONS ${PYTHON_EXECUTABLE})
  endif()
  add_custom_target(pytellapic DEPENDS tellapic)
  foreach(version ${PYTHON_VERSIONS})
  	add_custom_command(
	  	TARGET pytellapic
	  	POST_BUILD
	   	COMMAND ${CMAKE_COMMAND} -E cmake_echo_color --red --bold "Creating directories if needed..."
	   	COMMAND /usr/bin/test -d ${OUTDIR}/${version} || mkdir -p ${OUTDIR}/${version}
	    COMMAND ${CMAKE_COMMAND} -E cmake_echo_color --red --bold "Making the SWIG pass..." 
	    COMMAND ${SWIG_EXECUTABLE} "-module" ${EXTENSION_NAME} "-cpperraswarn" ${OS_DEFINITION} "-threads" ${INCLUDE_DIR} "-o" ${OUTDIR}/${version}/${SWIG_GENERATED_FILE} "-outdir" ${OUTDIR}/${version}/ "-python" ${INTERFACE_FILE}
	    COMMAND ${CMAKE_COMMAND} -E cmake_echo_color --red --bold "Configuring pytellapic extension..." 
	    COMMAND python${version} ${CMAKE_CURRENT_SOURCE_DIR}/configure_setup.py -f ${OUTDIR}/${version}/${SWIG_GENERATED_FILE} -n ${EXTENSION_NAME} --outdir ${OUTDIR}/${version}/
  		COMMAND ${CMAKE_COMMAND} -E cmake_echo_color --red --bold "Creating pytellapic extension for python${version}"
	    COMMAND PYTHONPATH=${OUTDIR}/${version} python${version} ${SETUP_PY}
			build_ext
	       --include-dirs=${PROJECT_SOURCE_DIR}/include 
	       --library-dirs=${PROJECT_BINARY_DIR}/lib/${ARCH}
	       --libraries=tellapic
	       --build-lib=${OUTDIR}/${version}/
	    )
  endforeach(version)
else()
  message("Python libs not found. You won't be able to build tellapic python bindings.")
endif()

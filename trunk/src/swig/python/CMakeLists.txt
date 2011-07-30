find_package(PythonLibs)
if (PYTHONLIBS_FOUND)
  # Sets the interface file for the swig wrapper.
  set(INTERFACE_FILE "${CMAKE_CURRENT_SOURCE_DIR}/module.i")
  set(EXTENSION_NAME "pytellapic")
  set(SWIG_GENERATED_FILE "tellapicPYTHON_wrap.c")
  set(SETUP_PY "${CMAKE_CURRENT_SOURCE_DIR}/setup.py")
  set(PYTHONPATH ${CMAKE_CURRENT_BINARY_DIR})
  set(INCLUDE_DIR "-I${PROJECT_SOURCE_DIR}/include")

  set(CMAKE_SWIG_FLAGS 
    "-module" ${EXTENSION_NAME}
    "-cpperraswarn" 
    ${OS_DEFINITION} 
    "-threads" 
    ${INCLUDE_DIR} 
    "-o" ${SWIG_GENERATED_FILE}
    "-outdir" ${CMAKE_CURRENT_BINARY_DIR} 
    "-python" ${INTERFACE_FILE})

  if (DEFINED PYTHON_VERSIONS)

  endif()

  #add_custom_target(pytellapic2.7 DEPENDS pytellapic_setup2.7)
  #add_custom_target(pytellapic_setup2.7 DEPENDS pytellapic_configure)

  add_custom_target(pytellapic DEPENDS pytellapic_setup)
  add_custom_target(pytellapic_swig_pass)
  add_custom_target(pytellapic_configure DEPENDS pytellapic_swig_pass)
  add_custom_target(pytellapic_setup DEPENDS pytellapic_configure)

  add_custom_command(
    TARGET pytellapic_swig_pass 
    COMMAND swig 
    ARGS  ${CMAKE_SWIG_FLAGS}
    )
  add_custom_command(
    TARGET pytellapic_configure
    COMMAND ${CMAKE_CURRENT_SOURCE_DIR}/configure_setup.py
    ARGS -f ${CMAKE_CURRENT_BINARY_DIR}/${SWIG_GENERATED_FILE}
    ARGS -n ${EXTENSION_NAME}
    )

  foreach(version ${PYTHON_VERSIONS})
    add_custom_command(
      TARGET pytellapic_setup
      COMMAND PYTHONPATH=${CMAKE_CURRENT_BINARY_DIR} python${version} ${SETUP_PY}
      ARGS build_ext
      ARGS --include-dirs=${PROJECT_SOURCE_DIR}/include 
      ARGS --library-dirs=${PROJECT_BINARY_DIR}/src/lib/
      ARGS --libraries=tellapic
      ARGS --build-lib=${CMAKE_CURRENT_BINARY_DIR}
      )
  endforeach(version)

else()
  message("Python libs not found. You won't be able to build tellapic python bindings.")
endif()

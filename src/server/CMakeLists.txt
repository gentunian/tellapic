#
# SERVER CMake file
#
set(COMMON_FILES ${COMMON_FILES} ${tellapic_SOURCE_DIR}/src/common/list.c ${tellapic_SOURCE_DIR}/src/common/common.c ${POSH_FILE})

if (CDK_FOUND MATCHES TRUE)
  set(CDK_FILES ${COMMON_FILES} ${tellapic_SOURCE_DIR}/src/common/console.c)
  set(CDK_COMPILE_FLAG "-DCDK_AWARE=1")
endif()

# Set the server.c file to compile.
set(SERVER_FILE ${COMMON_FILES} ${CDK_FILES} server.c)

#
include_directories(${INCDIR})

# Add the executable.
add_executable(server ${SERVER_FILE})

if(CMAKE_BUILD_TYPE MATCHES "Debug")
  set(DEBUG_COMPILE_FLAG "-DDEBUG=1 -g3")
endif()

#
set_target_properties(server PROPERTIES COMPILE_FLAGS "${CDK_COMPILE_FLAG} ${DEBUG_COMPILE_FLAG} -DBIG_ENDIAN_VALUE=${BIG_ENDIAN_VALUE}")
set_target_properties(server PROPERTIES RUNTIME_OUTPUT_DIRECTORY ${tellapic_BINARY_DIR}/bin/server)
 	 
# 
target_link_libraries(server tellapic ${CMAKE_REQUIRED_LIBRARIES} ${CDK_LIBRARIES} ${LIBRARIES} m)

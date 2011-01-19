#
set(COMMON_FILES ${tellapic_SOURCE_DIR}/src/common/list.c ${tellapic_SOURCE_DIR}/src/common/common.c)

if (DEFINED CDK_AWARE)
  set(COMMON_FILES ${COMMON_FILES} ${tellapic_SOURCE_DIR}/src/common/console.c)
endif()
# Set the server.c file to compile.
set(SERVER_FILE ${COMMON_FILES} server.c)

#
include_directories(${INCDIR})

# Add the executable.
add_executable(server ${SERVER_FILE})

if(CMAKE_BUILD_TYPE MATCHES "Debug")
  set(DEBUG_COMPILE_FLAG "-DDEBUG=1")
endif()

if(DEFINED CDK_FOUND)
  set(CDK_COMPILE_FLAG "-DCDK_AWARE=1")
endif()

#
set_target_properties(server PROPERTIES COMPILE_FLAGS "${CDK_COMPILE_FLAG} ${DEBUG_COMPILE_FLAG} -DBIG_ENDIAN_VALUE=${BIG_ENDIAN_VALUE}")

  
# 
target_link_libraries(server tellapic ${CMAKE_REQUIRED_LIBRARIES} ${CDK_LIBRARIES} ${LIBRARIES})

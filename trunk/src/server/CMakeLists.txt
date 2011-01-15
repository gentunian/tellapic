#
set(COMMON_FILES ${tellapic_SOURCE_DIR}/src/common/list.c ${tellapic_SOURCE_DIR}/src/common/console.c ${tellapic_SOURCE_DIR}/src/common/common.c)

# Set the server.c file to compile.
set(SERVER_FILE ${COMMON_FILES} server.c)

#
include_directories(${INCDIR})

# Add the executable.
add_executable(server ${SERVER_FILE})

#
set_target_properties(server PROPERTIES COMPILE_FLAGS -DBIG_ENDIAN_VALUE=${BIG_ENDIAN_VALUE})

# 
target_link_libraries(server tellapic ${CMAKE_REQUIRED_LIBRARIES} ${CDK_LIBRARIES} ${LIBRARIES})

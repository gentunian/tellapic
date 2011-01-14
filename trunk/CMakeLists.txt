cmake_minimum_required(VERSION 2.8)
project(tellapic)
include(TestBigEndian)
set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/Modules")
find_package(CDK REQUIRED)
find_package(OpenSSL REQUIRED)

set(LIBRARIES pthread)

#Sets the root build directory.
set(EXECUTABLE_OUTPUT_PATH ${PROJECT_BINARY_DIR}/build) 

# Sets the python bindings output path for the generated swig library.
set(LIBRARY_OUTPUT_PATH ${EXECUTABLE_OUTPUT_PATH}/lib) 

# Sets the root source directory
set(SRCDIR ${PROJECT_SOURCE_DIR}/src)

# Sets the root include directory
set(INCDIR ${PROJECT_SOURCE_DIR}/include)

set(COMMON_FILES ${SRCDIR}/common/list.c
  ${SRCDIR}/common/common.c
  ${SRCDIR}/common/console.c
)

set(SERVER_FILE ${COMMON_FILES} ${SRCDIR}/server/server.c)

# Includes the include root directory.
include_directories(${INCDIR})

add_library(tellapic SHARED ${SRCDIR}/lib/tellapic.c)

add_executable(server ${SERVER_FILE})

test_big_endian(BIG_ENDIAN_VALUE)

#if(CMAKE_BUILD_TYPE MATCHES "Debug")

set_target_properties(server PROPERTIES COMPILE_FLAGS -DBIG_ENDIAN_VALUE=${BIG_ENDIAN_VALUE})
set_target_properties(server PROPERTIES COMPILE_FLAGS "-DBIG_ENDIAN_VALUE=${BIG_ENDIAN_VALUE} -DDEBUG")

#endif()
target_link_libraries(server tellapic ${CDK_LIBRARIES} ${OPENSSL_LIBRARIES} ${LIBRARIES})

# Now create the SWIG wrappers
add_subdirectory(${SRCDIR}/swig)

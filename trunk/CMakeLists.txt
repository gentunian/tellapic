cmake_minimum_required(VERSION 2.8)
project(tellapic)

set(CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/Modules")
find_package(CDK REQUIRED)
find_package(OpenSSL REQUIRED)

set(LIBRARIES pthread)

set(SRCDIR src)

set(COMMON_SRCS ${SRCDIR}/common/list.c
  ${SRCDIR}/common/common.c
  ${SRCDIR}/common/console.c
)

set(SERVER_SRCS ${COMMON_SRCS} ${SRCDIR}/server/server.c)

include_directories(include)

add_library(tellapic SHARED src/lib/tellapic.c)

add_executable(server ${SERVER_SRCS})
if(CMAKE_BUILD_TYPE MATCHES "Debug")
    set_target_properties(server PROPERTIES COMPILE_FLAGS -DDEBUG)
endif()
target_link_libraries(server tellapic ${CDK_LIBRARIES} ${OPENSSL_LIBRARIES} ${LIBRARIES})

# Now create the SWIG wrappers
add_subdirectory(swig)

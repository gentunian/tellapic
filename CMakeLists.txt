# Project Name
project(tellapic)

# Minimum CMake version supported
cmake_minimum_required(VERSION 2.8)

# CMake modules
set(CMAKE_MODULE_PATH ${tellapic_SOURCE_DIR}/Modules ${CMAKE_MODULE_PATH})

find_package(CDK QUIET)
find_package(pthread REQUIRED)
#find_package(OpenSSL REQUIRED)

# We should know the endianness of this machine
include(TestBigEndian)

# Dont build in.source. Enrique's idea.
include(MacroOutOfSourceBuild)

# Ensure the above.
macro_ensure_out_of_source_build("${PROJECT_NAME}")

set(LIBRARIES pthread)

#Sets the root build directory.
#set(EXECUTABLE_OUTPUT_PATH ${PROJECT_BINARY_DIR}/build) 

# Sets the python bindings output path for the generated swig library.
#set(LIBRARY_OUTPUT_PATH ${EXECUTABLE_OUTPUT_PATH}/lib) 

# Sets the root source directory
set(SRCDIR ${PROJECT_SOURCE_DIR}/src)

# Sets the root include directory
set(INCDIR ${PROJECT_SOURCE_DIR}/include)

# is big endian?
test_big_endian(BIG_ENDIAN_VALUE)

add_subdirectory(src)

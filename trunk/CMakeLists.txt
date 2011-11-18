# CMake Main Project file
# Project Name
# 
project(tellapic)

# Minimum CMake version supported
cmake_minimum_required(VERSION 2.6)

# CMake modules
set(CMAKE_MODULE_PATH ${tellapic_SOURCE_DIR}/Modules)

find_package(Curses QUIET)

# This is a bug workaround. Doing find_package(CDK QUIET) will not work if Curses aren't present on the system.
# (IMHO it should, instead of quitting cmake).
# FindCDK should propagate the QUIET, REQUIRED, EXACT, etc argument to the find_package(CURSES REQUIRED) call.
if (CURSES_FOUND MATCHES TRUE)
  find_package(CDK QUIET)
endif()

find_package(pthread REQUIRED)
#find_package(OpenSSL REQUIRED)

# We should know the endianness of this machine
include(TestBigEndian)

# Dont build in.source. Enrique's idea.
include(MacroOutOfSourceBuild)

# Ensure the above.
macro_ensure_out_of_source_build("${PROJECT_NAME}")

set(LIBRARIES pthread)

# Sets the root source directory and include path
set(SRCDIR ${PROJECT_SOURCE_DIR}/src)
set(INCDIR ${PROJECT_SOURCE_DIR}/include)
set(INCDIR ${INCDIR} ${PROJECT_SOURCE_DIR}/include/posh)
set(TELLAPIC_COMMON ${SRCDIR}/lib/tellapic_common.c)
set(POSH_FILE ${SRCDIR}/posh/posh.c)

if (DEFINED UNIX)
  set(TELLAPIC_FILE ${SRCDIR}/lib/linux/tellapic.c)
  set(OS_DEFINITION "-DLINUX")
  add_definitions("-Wall")
  set(ARCH "linux")
elseif(DEFINED WIN32)
  set(TELLAPIC_FILE ${SRCDIR}/lib/win32/tellapic.c)
  set(OS_DEFINITION "-DWIN32")
  #~ set_target_properties(tellapic PROPERTIES DEFINE_SYMBOL  "TELLAPIC_DLL")
  add_definitions("-W4 -DTELLAPIC_DLL")
  set(ARCH "win32")
elseif(DEFINED _WIN32)
  set(TELLAPIC_FILE ${SRCDIR}/lib/win32/tellapic.c)
  set(OS_DEFINITION "-D_WIN32")
  #~ set_target_properties(tellapic PROPERTIES DEFINE_SYMBOL  "TELLAPIC_DLL")
  add_definitions("-W4 -DTELLAPIC_DLL")
  set(ARCH "win32")
else()
  message("No target OS defined!!")
  # how to abort???
endif()

include_directories(${INCDIR})

# is big endian?
test_big_endian(BIG_ENDIAN_VALUE)
if (NOT BIG_ENDIAN_VALUE)
  set(CMAKE_C_FLAGS "-DLITTLE_ENDIAN_VALUE=1")
endif()

add_subdirectory(src)
add_subdirectory(test)

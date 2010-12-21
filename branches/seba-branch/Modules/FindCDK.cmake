if(NOT CDK_FIND_QUIETLY)
  message(STATUS "Looking for CDK...")
endif()

find_path(CDK_INCLUDE_DIR NAMES cdk/cdk.h PATHS /usr/include /usr/local/include)
find_library(CDK_LIBRARY NAMES libcdk cdk PATHS /usr/lib /usr/local/lib)

set(CURSES_NEED_NCURSES TRUE)
find_package(Curses REQUIRED)

if(NOT CDK_FOUND)
  if(CDK_INCLUDE_DIR AND CDK_LIBRARY AND CURSES_FOUND)
    set(CDK_FOUND TRUE)
    set(CDK_INCLUDE_DIRS ${CDK_INCLUDE_DIR} ${Curses_INCLUDE_DIRS})
    set(CDK_LIBRARIES ${CDK_LIBRARY} ${CURSES_LIBRARIES})
    if(NOT CDK_FIND_QUIETLY)
      message(STATUS "Found CDK: ${CDK_LIBRARIES}")
    endif()
  else()
    if(NOT CDK_FIND_QUIETLY)
      message(STATUS "Could not find CDK.")
    endif()
    if(CDK_FIND_REQUIRED)
      message(FATAL_ERROR "Required library CDK not found!")
    endif()
  endif()
  mark_as_advanced(CDK_INCLUDE_DIR CDK_LIBRARY)
endif()

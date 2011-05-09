# Set the tellapic common library file
set(TELLAPIC_COMMON tellapic_common.c)


# Include headers.
#include_directories(${INCDIR})
 
# 
#add_library(tellapic SHARED  ${TELLAPIC_FILE})

#
#target_link_libraries(tellapic ${CMAKE_REQUIRED_LIBRARIES})

if (DEFINED UNIX)
  add_subdirectory(linux)
elseif(DEFINED WIN32)
  add_subdirectory(win32)
else()
  message("No target OS defined!!")
endif()

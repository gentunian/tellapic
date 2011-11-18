# Tellapic linux CMake library file
# Set the tellapic library function.
set(TELLAPIC_FILE tellapic.c)
include_directories(${INCDIR})
add_library(tellapic SHARED ${TELLAPIC_FILE}  ${TELLAPIC_COMMON})
set_target_properties(tellapic PROPERTIES LIBRARY_OUTPUT_DIRECTORY ${tellapic_BINARY_DIR}/lib/linux)
target_link_libraries(tellapic ${CMAKE_REQUIRED_LIBRARIES})

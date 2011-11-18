# Tellapic Win32 CMake library file 
set(TELLAPIC_FILE tellapic.c)
add_library(tellapic SHARED  ${TELLAPIC_FILE} ${TELLAPIC_COMMON} ${POSH_FILE})
set_target_properties(tellapic PROPERTIES LIBRARY_OUTPUT_DIRECTORY ${tellapic_BINARY_DIR}/lib/win32)
set_target_properties(tellapic PROPERTIES DEFINE_SYMBOL  "TELLAPIC_DLL")
target_link_libraries(tellapic ${CMAKE_REQUIRED_LIBRARIES})


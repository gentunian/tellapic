include_directories(${INCDIR})
add_library(tellapic SHARED ${TELLAPIC_COMMON} ${TELLAPIC_FILE} ${POSH_FILE})
set_target_properties(tellapic PROPERTIES DEFINE_SYMBOL  "TELLAPIC_DLL")
target_link_libraries(tellapic ${CMAKE_REQUIRED_LIBRARIES})

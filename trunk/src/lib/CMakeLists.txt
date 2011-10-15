add_library(tellapic SHARED ${TELLAPIC_COMMON} ${TELLAPIC_FILE} ${POSH_FILE})
target_link_libraries(tellapic ${CMAKE_REQUIRED_LIBRARIES})

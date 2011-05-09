# Set the tellapic library function.
set(TELLAPIC_FILE tellapic.c)

# Include headers.
include_directories(${INCDIR})
 
# 
add_library(tellapic SHARED ${TELLAPIC_FILE}  ../${TELLAPIC_COMMON})

#
target_link_libraries(tellapic ${CMAKE_REQUIRED_LIBRARIES})

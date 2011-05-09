# Set the tellapic library function.
set(TELLAPIC_FILE tellapic.c)

# Include headers.
include_directories(${INCDIR})
 
# 
#add_library(tellapic SHARED  ${TELLAPIC_FILE})
#set_target_properties(tellapic PROPERTIES DEFINE_SYMBOL  "TELLAPIC_DLL")

#target_link_libraries(tellapic ${CMAKE_REQUIRED_LIBRARIES} tellapic_common posh)

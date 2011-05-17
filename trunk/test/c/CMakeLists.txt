set(TEST_FILES client_using_pen.c)
add_executable(test ${TEST_FILES})
target_link_libraries(test tellapic m)

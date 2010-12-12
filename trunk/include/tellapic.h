#include "types.h"

stream_t tellapic_read_data_b(int fd, header_t header);
header_t tellapic_read_header_b(int fd);
int tellapic_send_data(int socket, stream_t *stream);
int tellapic_connect_to(const char *hostname, int port);
void tellapic_close_fd(int fd);
stream_t tellapic_read_stream_b(int fd);

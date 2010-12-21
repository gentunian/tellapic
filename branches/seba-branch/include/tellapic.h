#include "types.h"

char *
tellapic_read_pwd(int fd, char *, int *len);

stream_t
tellapic_read_data_b(int fd, header_t header);

header_t
tellapic_read_header_b(int fd);

int 
tellapic_send(int socket, stream_t *stream);

int
tellapic_connect_to(const char *hostname, int port);

void
tellapic_close_fd(int fd);

stream_t
tellapic_read_stream_b(int fd);

int
tellapic_send_text(int fd, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1, int style, int facelen, char *face, int textlen, char *text);

int
tellapic_send_fig(int fd, int tool, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1, int x2, int y2, int lj, int ec, float ml, float dp, float da[]);

int
tellapic_send_chatp(int fd, int idfrom, int idto, int textlen, char* text);

int
tellapic_send_chatb(int fd, int idfrom, int textlen, char* text);

int
tellapic_send_ctlext(int fd, int idfrom, int ctle, int infolen, char *info);

int
tellapic_send_ctl(int fd, int idfrom, int ctl);

stream_t 
tellapic_build_ctle(int ctl, int idfrom, int infosize, char *info);

stream_t
tellapic_build_ctl(int ctl, int idfrom);

stream_t 
tellapic_build_chat(int cbyte, int idfrom, int idto, int textsize, char *text);



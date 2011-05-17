/**
 *   Copyright (c) 2010 Sebastián Treu.
 *
 *   This file is part of tellapic library.
 *
 *   Tellapic library is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation; version 3 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 */
#ifndef __TELLAPIC__H__
#define __TELLAPIC__H__

#if defined TELLAPIC_DLL
#  define POSH_DLL
#else
#  undef POSH_DLL
#endif


#ifndef POSH_BUILDING_LIB
#define POSH_BUILDING_LIB
#endif

#include <stdarg.h>
#include "tellapic/types.h"
#include "tellapic/constants.h"


/**
 *
 */
POSH_PUBLIC_API(byte_t *)
tellapic_build_rawstream(byte_t ctlbyte, ...);

/**
 *
 */
POSH_PUBLIC_API(byte_t *)
tellapic_rawread_b(tellapic_socket_t socket);

/**
 *
 */
POSH_PUBLIC_API(char *)
tellapic_read_pwd(tellapic_socket_t socket, char *, int *len);

/**
 *
 */
POSH_PUBLIC_API(stream_t)
tellapic_read_data_b(tellapic_socket_t socket, header_t header);

/**
 *
 */
POSH_PUBLIC_API(byte_t *)
tellapic_read_bytes_b(tellapic_socket_t socket, size_t chunk);

/**
 *
 */
POSH_PUBLIC_API(header_t)
tellapic_read_header_b(tellapic_socket_t socket);

/**
 *
 */
POSH_PUBLIC_API(stream_t)
tellapic_read_data_nb(tellapic_socket_t socket, header_t header);

/**
 *
 */
POSH_PUBLIC_API(header_t)
tellapic_read_header_nb(tellapic_socket_t socket);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send(tellapic_socket_t socket, stream_t *stream);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_connect_to(const char *hostname, const char *port);

/**
 *
 */
POSH_PUBLIC_API(void)
tellapic_close_socket(tellapic_socket_t socket);

/**
 *
 */
POSH_PUBLIC_API(stream_t)
tellapic_read_stream_b(tellapic_socket_t socket);

/**
 *
 */
POSH_PUBLIC_API(stream_t)
tellapic_read_stream_nb(tellapic_socket_t socket);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_text(tellapic_socket_t socket, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1, int style, int facelen, char *face, int textlen, char *text);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_fig(tellapic_socket_t socket, int tool, int dcbyte_ext, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1, int x2, int y2, int lj, int ec, float ml, float dp, float da[]);


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_drw_using(tellapic_socket_t socket, int tool, int dcbyte_ext, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1);


/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_drw_init(tellapic_socket_t socket, int tool, int dcbyte_ext, int idfrom, int dnum, float w, float op, int red, int green, int blue, int x1, int y1, int x2, int y2, int lj, int ec, float ml, float dp, float da[]);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_chatp(tellapic_socket_t socket, int idfrom, int idto, int textlen, char* text);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_chatb(tellapic_socket_t socket, int idfrom, int textlen, char* text);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_ctle(tellapic_socket_t socket, int idfrom, int ctle, int infolen, char *info);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_send_ctl(tellapic_socket_t socket, int idfrom, int ctl);

/**
 *
 */
POSH_PUBLIC_API(stream_t)
tellapic_build_ctle(int ctl, int idfrom, int infosize, char *info);

/**
 *
 */
POSH_PUBLIC_API(stream_t)
tellapic_build_ctl(int ctl, int idfrom);

/**
 *
 */
POSH_PUBLIC_API(stream_t)
tellapic_build_chat(int cbyte, int idfrom, int idto, int textsize, char *text);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_isping(header_t *header);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_ispong(header_t *header);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_istimeout(header_t *header);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_ischatb(header_t *header);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_ischatp(header_t *header);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_isctl(header_t *header);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_isfile(header_t *header);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_isctle(header_t *header);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_isdrw(header_t *header);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_isfigtxt(stream_t *stream);

/**
 *
 */
POSH_PUBLIC_API(int)
tellapic_isfig(header_t *header);

/**
 *
 */
POSH_PUBLIC_API(void)
tellapic_free(stream_t *stream);

POSH_PUBLIC_API(void)
tellapic_interrupt_socket();

#endif

/*
 *   Copyright (c) 2009 Sebastián Treu and Virginia Boscaro.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   Authors: 
 *         Sebastian Treu 
 *         Virginia Boscaro
 *
 */ 
#include <openssl/ssl.h>


/* CHANGE THIS!!! */
/* TODO: More work on this header */
const char *LINE = "\n-----------------------------------------------------------------\n";
const char *NFO_WAIT  = "[NFO]:\tWaiting for incoming connections\n";
const char *NFO_HANDLE= "[NFO]:\tTrying to handle new connection on socket: %d\n";
const char *ERR_ASSOC = "[ERR:]\tCannot associate fd to ssl. (errCode: %d, ret: %d)\n";
const char *NFO_NEWFD = "<thread %d>[NFO]:\tNew file descriptor %d associated to SSL on connection attempt from: %s\n";
const char *WRN_SSLACC= "[WRN]:\tSSL_accept(): [ErrCode: %d,retCode: %d] try number: %d\n";
const char *NFO_ACCEPT= "<thread %d>[NFO]:\tNew connection accepted from %s on socket %d.\n";
const char *NFO_CIPHER= "<thread %d>[NFO]:\tClient version %s and cipher %s\n";
const char *ERR_SSLWR = "[ERR]:\tSSL_write() not successfull to socket %d: (errCode: %d, ret: %d)\n";
const char *NFO_NEWCON= "<thread %d>[NFO]:\tNew connection from %s on socket %d\n";
const char *NFO_FDASS = "[NFO]:\tfd %d associated to ssl when reading\n";
const char *NFO_TREAD = "[NFO]:\tTrying to read client data from fd socket %d\n";
const char *WRN_SSLRD = "[WRN]:\tSSL_read() was unsuccessful on socket %d. (%d,%d)\n";
const char *NFO_FDCLO = "[NFO]:\tFile descriptor %d closed and removed from MASTER set.\n";
const char *NFO_READOK= "[NFO]:\tSSL_read() was successful on fd socket %d ('%s')\n";
const char *NFO_BDCAST= "[NFO]:\tStarting the broadcast...[listener=%d, me=%d]\n";
const char *NFO_FDWR  = "[NFO]:\tfd %d associated to ssl when writing\n";
const char *NFO_FWD   = "[NFO]:\tTrying to forward ('%s') from socket %d to socket %d: ";

#define MAX_CLIENTS 32
#define BUFFER_SIZE 1024

#define TIMEOUT 1000
#define S_GETLIST 0
#define S_NOLIST  1

#define FATAL -1
#define WARN  -2
#define NO_THREAD -1
#define SV_THREAD     0
#define AVAIL_THREAD -1
#define MUTEX_NUM     2
#define COND_NUM      2
#define SSL_MUTEX     0
#define STATE_MUTEX   1

#define THREAD_ERR_EBUSY  10
#define THREAD_ERR_EINVAL 11
#define THREAD_ERR_ENOMEM 12
#define THREAD_ERR_EPERM  13
#define THREAD_ERR_EAGAIN 14

typedef enum {
  CL_STATE_FREE,
  CL_STATE_READY,
  CL_STATE_BUSY

} cl_state_t;


/* Client hold structure */
typedef struct cl {
  int                fd;
  cl_state_t         state;
  SSL                *ssl;
  char               *cl_info;
  char               buffer[1024];
  int                nbytes;
  pthread_mutex_t    mutex[MUTEX_NUM];
  pthread_cond_t     cond[COND_NUM];
  struct sockaddr_in address;

} client_t;


/* Thread data structure */
typedef struct tdata {
  client_t *client;
  int       id;

 } tdata_t;


void load_certificates(SSL_CTX * ctx, char * CertFile, char *KeyFile);
int show_certs(SSL *ssl);
int open_listener(int port, struct sockaddr_in *addr);
SSL_CTX *init_server_ctx(void);

int wait_for_client(SSL *ssl, int tries);

void signal_handler(int sig);

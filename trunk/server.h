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
#ifndef _SERVER_H_
#define _SERVER_H_

#include <openssl/ssl.h>
#include "list.h"

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

#define MUTEX_TYPE           pthread_mutex_t
#define MUTEX_SETUP(mutex)   pthread_mutex_init(&(mutex), NULL)
#define MUTEX_CLEANUP(mutex) pthread_mutex_destroy(&(mutex))
#define MUTEX_LOCK(mutex)    pthread_mutex_lock(&(mutex))
#define MUTEX_UNLOCK(mutex)  pthread_mutex_unlock(&(mutex))
#define THREAD_ID            pthread_self()


#define MAX_CLIENTS 32
#define BUFFER_SIZE 1024
#define MAX_PWD_TRIES 5
#define TIMEOUT 1000
#define S_GETLIST 0
#define S_NOLIST  1

#define FATAL -1
#define WARN  -2
#define NO_THREAD -1
#define SV_THREAD     MAX_CLIENTS
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
  THREAD_STATE_NEW,
  THREAD_STATE_FREE,
  THREAD_STATE_INIT,
  THREAD_STATE_ACTIVE

} thread_state_t;

typedef enum {
  CLIENT_STATE_INIT,
  CLIENT_STATE_READY

} client_state_t;


/* Client hold structure */
typedef struct cl {
  int                clidx;
  int                fd;
  SSL                *ssl;
  char               *clinfo;
  char               buffer[1024];
  pthread_mutex_t    mutex;
  struct sockaddr_in address;
  client_state_t     state;

} client_t;


/* Thread data structure */
typedef struct tdata {
  pthread_t       tid;
  client_t        *client;
  thread_state_t  state;
  pthread_mutex_t mutex;
  int             tnum;

 } tdata_t;


typedef struct queue_item {
  char buffer[BUFFER_SIZE];  //define a value here
  int  nbytes;
  int  from;
} queue_item_t;


typedef struct args {
  int         port;
  int         svfd;
  list_t      *imglist;
  list_node_t *current;
  char        *pwd;

} args_t;


int             open_listener(int port, struct sockaddr_in *addr);
SSL_CTX         *init_server_ctx(void);
void            *iterate_id(void **arg);
int             wait_for_client(client_t *client);
void            signal_handler(int sig);

#endif

/*****************************************************************************
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
 *
 * ===========================================================================
 * 'server.h' header file.
 *  ---------------------
 *
 *****************************************************************************/
#ifndef _SERVER_H_
#define _SERVER_H_


#include <openssl/ssl.h>
//#include "common.h"
#include "list.h"
//#include "stream.h"


/**********************************/
/* states where a thread could be */
/**********************************/
typedef enum {
  THREAD_STATE_NEW,
  THREAD_STATE_FREE,
  THREAD_STATE_INIT,
  THREAD_STATE_ACT,
  THREAD_STATE_RH,
  THREAD_STATE_RHOK,
  THREAD_STATE_RD,
  THREAD_STATE_RDOK,
  THREAD_STATE_WR,
  THREAD_STATE_WANTR,
  THREAD_STATE_WANTWR,
  THREAD_STATE_WAIT,
  THREAD_STATE_END,

} thread_state_t;


/*************************/
/* Client hold structure */
/*************************/
typedef struct cl {
  int                clidx;
  int                fd;
  SSL                *ssl;
  char               *clinfo;//what for?
  char               buffer[1024]; //what for?
  struct sockaddr_in address;
  int32_t            fwdbitlist; //this must be 32bit in any platform. This soft is not intended to run on 16-bit systems.

} client_t;


/*************************/
/* Thread data structure */
/*************************/
typedef struct tdata {
  pthread_t          tid;
  int                tnum;
  char               shouldwrite;
  client_t           *client;
  thread_state_t     tstate;
  pthread_mutex_t    stmutex;
  pthread_mutex_t    queuemutex;
  list_t             *queue;
  pthread_cond_t     queuecond;
  int                pipefd[2];
  int                event_reader;
  int                event_writer;
  int                error;

 } tdata_t;


/**************************/
/* main program arguments */
/**************************/
typedef struct args {
  int                port;
  int                svfd;
  list_t             *imglist;
  list_node_t        *currimg;
  char               *pwd;

} args_t;


/* CHANGE THIS!!! */
/* TODO: More work on this header */
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


/***************************/
/* approach for portability */
/***************************/
#define MUTEX_TYPE           pthread_mutex_t
#define MUTEX_SETUP(mutex)   pthread_mutex_init(&(mutex), NULL)
#define MUTEX_CLEANUP(mutex) pthread_mutex_destroy(&(mutex))
#define MUTEX_LOCK(mutex)    pthread_mutex_lock(&(mutex))
#define MUTEX_UNLOCK(mutex)  pthread_mutex_unlock(&(mutex))
#define THREAD_ID            pthread_self()


/***************************************************************************/
/* define some special constants values to be used during server execution */
/***************************************************************************/
#define MAX_CLIENTS        32             /* define the maximum threads number */
#define SV_THREAD           MAX_CLIENTS   /* signal thread will be the last one */
#define BUFFER_SIZE      1024             /* buffer size */
#define MSG_FROM_SERVER     0             /* the stream came from the server instead of a particular client */
#define MAX_PWD_TRIES       5             /* number of retries for password fail */
#define FATAL              -1             /* fatal error flag */
#define WARN               -2             /* warning flag */
#define THREAD_ERR_EBUSY   10             /* thread error wrapped from pthread */
#define THREAD_ERR_EINVAL  11             /* thread error wrapped from pthread */
#define THREAD_ERR_ENOMEM  12             /* thread error wrapped from pthread */
#define THREAD_ERR_EPERM   13             /* thread error wrapped from pthread */
#define THREAD_ERR_EAGAIN  14             /* thread error wrapped from pthread */
#define THREAD_ERROR_NONE   0             /* thread error wrapped from pthread */
#define THREAD_SOME_ERROR -99             /* some error on thread managment */


/**********************/
/* functions profiles */
/**********************/
int                open_listener(int port, struct sockaddr_in *addr);
SSL_CTX            *init_server_ctx(void);
int                wait_for_client(client_t *client);
void               signal_handler(int sig);
void               push_item(tdata_t *thread, void *item);
void               *pop_item(tdata_t *thread);
void               *attend_client(void *arg);
void               *signal_client(void *arg);
void               signal_client_cleanup(void *arg);
void               attend_client_cleanup(void *arg);
void               *wait_for_data(tdata_t *thread);
void               free_client(client_t *client);
void               r_free();
void               thread_abort();
void               send_write_signal(tdata_t *thread);
void               args_check(int argc, char *argv[]);
void               data_init();
void               data_init_thread(tdata_t *thread, int i);
void               data_init_pipes(tdata_t *thread);
int                THREAD_get_error(int value, int severity, int tid);
int                THREAD_setup();
int                new_thread_data(int i, int clfd, struct sockaddr_in clientaddr);
int                THREAD_cleanup(void);
int                ssl_read_b(client_t *cl, int *err, void *buf, size_t size, struct timeval *tv);
int                ssl_write_b(client_t *cl, int *err, const void *buf, size_t size);
list_t             *get_connected_clients(tdata_t *thread);
int                check_pwd(void *arg);
int                check_last(void *arg);
int                check_dummy(void *arg);
void               free_thread_queue(tdata_t *thread);
void               set_tstate(tdata_t *thread, thread_state_t state);
void               *try_read(tdata_t *thread, int size);
int                try_write(tdata_t *thread);
void               free_clist_node_memory(list_t *list);

#endif

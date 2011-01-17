/**
 *   Copyright (c) 2010 Sebastián Treu.
 *
 *   This file is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 3 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 */
#ifndef _SERVER_H_
#define _SERVER_H_

//#include "common.h"
#include "list.h"
#include "types.h"


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
//#define MSG_FROM_SERVER     0             /* the stream came from the server instead of a particular client */
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
#define MAX_PWD_SIZE       128

/* states where a thread could be */
typedef enum {
  THREAD_STATE_NEW,
  THREAD_STATE_FREE,
  THREAD_STATE_INIT,
  THREAD_STATE_ACT,
  THREAD_STATE_END,

} thread_state_t;

const char *tstatus[5] = { "New", "Free", "Init", "Active", "End" };

/* Client hold structure */
typedef struct cl {
  int                clidx;
  int                fd;
  char               *name;
  int                namelen;
  struct sockaddr_in address;

} client_t;



/* Thread data structure */
typedef struct tdata {
  pthread_t          tid;
  int                tnum;
  char               shouldwrite;
  client_t           *client;
  thread_state_t     tstate;
  pthread_mutex_t    stmutex;
  int                readpipe;
  int                writepipe;

 } tdata_t;


/* main program arguments */
typedef struct args {
  int                port;
  int                svfd;
  char               pwd[MAX_PWD_SIZE];
  int                pwdlen;
  FILE               *image;
  long               filesize;
} args_t;


typedef struct msgitem {
  stream_t           stream;
  uint32_t           tothread;
  int                delivers;
  int                bsent[MAX_CLIENTS];
  pthread_mutex_t    mutex;

} mitem_t;



/**********************/
/* functions profiles */
/**********************/

//TODO: comment the profiles.

/**
 *
 */
int
start_server(int port, struct sockaddr_in *addr);

/**
 *
 */
void
signal_handler(int sig);

/**
 *
 */
void
*manage_client(void *arg);

/**
 *
 */
void
manage_client_cleanup(void *arg);

/**
 *
 */
void
thread_abort();

/**
 *
 */
int
args_check(int argc, char *argv[]);

/**
 *
 */
void
data_init();

/**
 *
 */
void
data_init_thread(tdata_t *thread, int i);

/**
 *
 */
int
THREAD_get_error(int value, int severity, int tid);

/**
 *
 */
int
new_client_thread(tdata_t *thread, int clfd, struct sockaddr_in clientaddr);

/**
 *
 */
void
set_tstate(tdata_t *thread, thread_state_t state);

/**
 *
 */
void
send_write_signal(tdata_t *thread);

/**
 *
 */
void
data_init_pipes(tdata_t *thread);

/**
 *
 */
void
allocate_and_launch(tdata_t *thread, int fd, struct sockaddr_in addr, pthread_attr_t attr);

/**
 *
 */
void
cancel_threads();
 
/**
 *
 */
void
fetch_stream(tdata_t *thread);

/**
 *
 */
void
forward_stream(tdata_t *thread);

/**
 *
 */
void
queue_message(mitem_t *item, tdata_t *thread);

/**
 *
 */
void
unqueue_message(mitem_t *item);

/**
 *
 */
int
find_free_thread(tdata_t thread_data[]);

/**
 *
 */
void *
console_thread(void *a);

/**
 *
 */
void
print_output(char *msg);

/**
 *
 */
void
process_command(char *cmd, tdata_t *thread_data);

/**
 *
 */
void
help();

/**
 *
 */
void
nocmd();

/**
 *
 */
void
send_pwdok(client_t *client);


/**
 * Taken from libc manual pages:
 * http://www.gnu.org/software/libc/manual/html_node/getpass.html#index-
 */
ssize_t
my_getpass (void *pwd);

#endif

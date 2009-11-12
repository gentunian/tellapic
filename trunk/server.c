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
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <malloc.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <resolv.h>
#include <openssl/ssl.h>
#include <openssl/err.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <pthread.h>
#include <signal.h>

#include "server.h"

int                img_fd;
int                sv_fd;
struct sockaddr_in serveraddr;
MUTEX_TYPE         stream_mutex  = PTHREAD_MUTEX_INITIALIZER;
MUTEX_TYPE         *mutex_buf;
pthread_cond_t     stream_cond   = PTHREAD_COND_INITIALIZER;
char               stream[BUFFER_SIZE];
SSL_CTX            *ctx;
int                CONTINUE = 1;



void *attend_client(void *arg);
void *send_to_clients(void *arg);
void r_free();
void thread_abort();
void free_client(client_t *client);
int THREAD_get_error(int value, int severity, int tid);
int THREAD_setup();
void client_list_init();
void cleanup(void *arg);


int main(int argc, char *argv[]) 
{
  int cl_fd = 0;
  int i     = 0;
  int rv    = 0;

  pthread_attr_t joinattr;
  struct sockaddr_in clientaddr;
  struct sigaction   sig_action;
  tdata_t            thread_data[MAX_CLIENTS + 1];

  sig_action.sa_handler = signal_handler;
  sigemptyset(&sig_action.sa_mask);
  sig_action.sa_flags = SA_RESTART;

  if ( sigaction(SIGINT, &sig_action, NULL) == -1)
    printf("[ERR]: Could not install signal handler!\n");

  /* check program arguments and return a handle to the image file */
  img_fd = args_check(argv, argc);
  
  /* initialize SSL */
  ctx = init_server_ctx();   
  SSL_CTX_use_certificate_file(ctx,"cacert.pem",SSL_FILETYPE_PEM); 
  SSL_CTX_use_PrivateKey_file(ctx,"privkey.pem",SSL_FILETYPE_PEM);
  
  /* Prepare the server for listen */
  sv_fd = open_listener(atoi(argv[1]), &serveraddr);

  /* Initialize data buffer */
  memset(&stream, 0, BUFFER_SIZE);

  /**/
  THREAD_setup();

  /* Initialize thread/clients list */

  for( i = 0; i < MAX_CLIENTS + 1; i++)
    {
      thread_data[i].tid   = 0;
      thread_data[i].tnum  = i;
      thread_data[i].state = THREAD_STATE_FREE;
      THREAD_get_error(pthread_mutex_init(&thread_data[i].mutex, NULL), FATAL, i);
    }

  pthread_attr_init(&joinattr);
  pthread_attr_setdetachstate(&joinattr, PTHREAD_CREATE_JOINABLE); /* THIS NEED TO BE FREED ON EXIT */

  /* launch sender thread */
  rv = pthread_create(&thread_data[SV_THREAD].tid, &joinattr, send_to_clients, (void *) &thread_data);

  THREAD_get_error(rv, FATAL, thread_data[SV_THREAD].tnum);

  printf("Server socket: %d\n", sv_fd);
  /* server started normally */
  while( CONTINUE  ) 
    {
      int notdone = 1;
      int addrlen = sizeof(clientaddr);
      printf(LINE);    
      printf(NFO_WAIT);

      /* TODO: Set server fd to non-blocking. Manage the program exit with CONTINUE and */
      /* release all used memory */
      if ( (cl_fd = accept(sv_fd, (struct sockaddr *)&clientaddr, &addrlen)) == -1)
	{
	  printf("[NFO]:\tConnection refused.\n");
	  close (cl_fd);
	}
      else
	{
	  printf("[NFO]:\tConnection attempt from %s.\n", inet_ntoa(clientaddr.sin_addr));

	  /* is there any place to run a thread? */
	  for( i = 0; i < MAX_CLIENTS && notdone; i++)
	    {
	      pthread_mutex_lock(&thread_data[i].mutex);
	      if ( thread_data[i].state == THREAD_STATE_FREE )
		notdone = 0;   /* try not exit a loop with break */
	      else
		pthread_mutex_unlock(&thread_data[i].mutex);
	    }
	  if ( i < MAX_CLIENTS )
	    {
	      thread_data[i].client = (client_t *) malloc(sizeof(client_t));
	      if ( thread_data[i].client == NULL)
		{
		  pthread_mutex_unlock(&thread_data[i].mutex);
		  printf("[WRN]:\tClient node allocation falied!\n");
		}
	      else
		{
		  thread_data[i].client->fd      = cl_fd;
		  thread_data[i].client->address = clientaddr;
		  thread_data[i].client->ssl     = SSL_new(ctx);
		  /* TODO: CHECK FOR RETURN VALUE */
		  pthread_mutex_init(&thread_data[i].client->mutex, NULL);

		  thread_data[i].client->cl_info = NULL;
		  thread_data[i].state = THREAD_STATE_INIT;
		  thread_data[i].tnum  = i;

		  printf("[NFO]:\tClient node allocation succeded for socket %d\n", cl_fd);
		  pthread_mutex_unlock(&thread_data[i].mutex);
		  rv = pthread_create(&thread_data[i].tid, &joinattr, attend_client, (void *) &thread_data[i]);
		  
		  if ( THREAD_get_error(rv, WARN, thread_data[i].tnum) )
		    {
		      printf("[WRN]:\tSpawning thread ocess failed! Client discarted.\n");
		      free_client(thread_data[i].client);
		    }
		  else
		    { 
		      // printf("[NFO]:\tSpawning thread process was successfull, thread id was %ld\n", tids[i]);
		      /* we add the thread id to the thread list */
		    }
		}
	    }
	  else
	    {
	      printf("[NFO]:\tServer reached full capacity.\n");
	      close(cl_fd);
	    }
	}
    }
  for( i = 0; i < MAX_CLIENTS + 1; i++)
    /* Send cancellation signal to ALL threads without checking errors */
    pthread_cancel(thread_data[i].tid);

  for( i = 0; i < MAX_CLIENTS + 1; i++)
    /* Wait for all threads to finish */
    pthread_join(thread_data[i].tid, NULL);

  /* Check if theres more data to free */
  r_free(thread_data);
  pthread_attr_destroy(&joinattr);

  /* TODO: When program is signaled to exit, wait untill all threads finish. then exit */
  printf("----END----\n");
}
  


/*********************************************************/
/* thread_abort():
 * ------------
 * aborts the server initialization freeing up used memory.
 *********************************************************/
void thread_abort()
{
  printf("[FATAL]: Could not start server.\n");
  CONTINUE = 0;
  exit(-1);
}



/*********************************************************
 * r_free():
 * ------
 * Resources are freed.
 ********************************************************/
void r_free(tdata_t threads[])
{
  int i = 0;
  int j = 0;
  for ( i = 0; i < MAX_CLIENTS + 1; i++)
    {
      if ( threads[i].state != THREAD_STATE_FREE )
	{
	  printf("--- Freeing ssl and closing %d socket ---\n", threads[i].client->fd);
	  free_client(threads[i].client);
	}
      pthread_mutex_destroy(&threads[i].client->mutex);
      pthread_mutex_destroy(&threads[i].mutex);
    }
  SSL_CTX_free(ctx);
  ERR_free_strings();
  EVP_cleanup();
  THREAD_cleanup();
  pthread_cond_destroy(&stream_cond);
  pthread_mutex_destroy(&stream_mutex);
  close(img_fd);
}



/**********************************************************
 * signal_handler(int sig):
 * --------------
 * Handle singals
 *********************************************************/
void signal_handler(int sig) 
{
  /* TODO: use this function to control program workflow and exit properly */
  int i;
  /* this function should detach and free any thread, memory, etc */
  printf("[HANDLER]: Signal %d caught.\n", sig);
  printf("--- Shutting down ---\n");
  CONTINUE = 0;
  pthread_cond_signal(&stream_cond);
}



/******************************************************************
 * args_check(char *argv[], int argc):
 * ----------
 * Checks programs arguments ( why i inversed the argS??)
 *****************************************************************/
int args_check(char *argv[], int argc)
{
  FILE *img_file;
  int fd;
  /* Check if the program was called correctly*/
  if ( argc != 4) 
    {
      printf("Usage:\n ./server <port> <path-to-file> <md5-password>\n");
      exit(1);
    }
  else 
    {
      if ( atoi(argv[1]) < 1024 ) 
	{
	  printf("Port must be higher than 1024\n");
	  exit(1);
	}
    if ( (img_file = fopen(argv[2], "r")) == NULL ) 
      {
	printf("Failed to open file: %s\n",argv[2]);
	exit(1);
      }
    }
  fd = fileno(img_file);
  fclose(img_file);

  return fd;
}



/**********************************************************************
 * send_to_clients(void *arg):
 * ---------------
 * Thread for sending data to connected clients from a specific client
 **********************************************************************/
void *send_to_clients(void *arg)
{
  client_t *to = NULL;
  int i        = 0;
  int tid      = SV_THREAD;

  printf("<thread %d>[NFO]:\tSending thread started.\n", tid);
  while( CONTINUE )
    {
      /* lock shared data */
      pthread_mutex_lock(&stream_mutex);
      
      printf("<thread %d>[NFO]:\tWaiting for data to be sent.\n", tid);
      /* wait until we are signaled */
      pthread_cond_wait(&stream_cond, &stream_mutex);
      
      printf("<thread %d>[NFO]:\tGot data or program is shutting down?\n", tid);
      /* thread is signaled. send data */      
      if ( !CONTINUE )
	{
	  printf("<thread %d>NFO]:\tWe are shutting down.\n", tid);
	  break;
	}
      printf("<thread %d>[NFO]:\tGot data. Sending...\n", tid);
    }
  printf("<thread %d>[NFO]:\tSending thread closing.\n", tid);

  pthread_exit(NULL);
}


/*******************************************************************
 * attend_client(void *arg):
 * ------------
 * Thread for receive new connections
 *******************************************************************/
void *attend_client(void *arg) 
{
  client_t *client      = ((tdata_t *) arg)->client;
  pthread_mutex_t mutex = ((tdata_t *) arg)->mutex;
  thread_state_t state  = ((tdata_t *) arg)->state;
  int tnum              = ((tdata_t *) arg)->tnum;
  /* TODO: CLEAN UP THIS DECLARATIONS */
  int ssl_err_code = 0;
  int bytesread    = 0;
  int byteswrote   = 0;
  long ssl_mode    = 0;
  int fd_flag      = 0;
  int i            = 0;
  const int chunk_size = 1024;
  char buffer[chunk_size];      /*unused till now */
  char *outstream;              /*unused till now */
  int nbytes = 0;
  int alive  = 1;               /*state of _this_ thread */
  int err;
  fd_set master, copy;
  unsigned long errsv = 0;
  FD_ZERO(&master);
  FD_ZERO(&copy);
  FD_SET(client->fd, &master);
  printf("<thread %d>[NFO]:\tInitializing server attendance client thread on socket %d\n", tnum, client->fd);

  /* get current fd flag */
  fd_flag  = fcntl(client->fd, F_GETFL);

  /* associate fd with ssl structure */
  ssl_err_code = SSL_set_fd(client->ssl, client->fd);

  printf("<thread %d>[NFO]:\tTrying to associate ssl structure with fd %d\n", tnum, client->fd);

  /* register cleanup function for this thread */
  pthread_cleanup_push(cleanup, (void *) arg);

  if ( !ssl_err_code)
    {
      printf("<thread %d>[ERR]:\tCannot associate fd with ssl structure\n", tnum);
      alive = 0;           
    }
  else
    {
      printf("<thread %d>[NFO]:\tSSL associated to socket %d\n", tnum, client->fd);
      /* set fd to non blocking */
      fcntl(client->fd, F_SETFL, fd_flag | O_NONBLOCK);
    }

  /* wait for a handshake. */
  if( alive && wait_for_client(client) <= 0 )  
    {
      printf("<thread %d>[ERR]:\tcould not hanshake with client.\n", tnum);
      alive = 0;
    }
  else 
    {
      printf(NFO_ACCEPT, tnum, inet_ntoa(client->address.sin_addr), client->fd);
      printf(NFO_CIPHER, tnum, SSL_get_version(client->ssl), SSL_get_cipher(client->ssl));
      printf("<thread %d>[NFO]:\tMain attendance loop start\n", tnum);  
    }

  /* The main client attendance */
  while( alive && CONTINUE )
    {
      copy = master;

      /* try read from the secure connection gaining exclusive access */
      /* to the client ssl structure. The 'sender thread' could       */
      /* access this structure coliding with the 'err' value and      */
      /* starting a catastrophe.                                      */
      pthread_mutex_lock(&client->mutex);
      nbytes = SSL_read(client->ssl, client->buffer, chunk_size);
      err = SSL_get_error(client->ssl, nbytes);
      errsv = ERR_get_error();
      pthread_mutex_unlock(&client->mutex);
      printf("<thread %d>[NFO]:\tSSL_read() = (%d) bytes (err=%d, str= %s)\n", tnum, nbytes, err, ERR_error_string(errsv, NULL));

      /* Thanks to David Schwartz from openssl mailing list for the tips */
      
      /* 'nbytes' is the bytes read from the client. If <=0 then action */
      /* must be taken to check the current state of the connection.    */
      if ( nbytes <= 0 )
	{
	  /* see if we need to call SSL_read() again */
	  if ( err == SSL_ERROR_WANT_READ || err == SSL_ERROR_WANT_WRITE)
	    {
	      printf("<thread %d>[NFO]:\tWANT_READ on socket %d (ERR_get_error()=%ld)\n", tnum, client->fd, errsv);
	      if ( select(client->fd+1, &copy, NULL, NULL, NULL) == -1)
		printf("<thread %d>:[ERR]:\tSelect fail\n",tnum);
	    }
	  if ( err == SSL_ERROR_SYSCALL )
	    {

	      printf("<thread %d>[NFO]:\tERR_SYSCALL on socket %d (ERR_get_error()=%ld\n", tnum, client->fd, errsv);
	      alive = 0;
	    }
	  if ( err == SSL_ERROR_ZERO_RETURN )
	    {
	      printf("<thread %d>:[NFO]:\tSocket %d hung up ( ERR_get_error()=%ld\n", tnum, errsv);
	      alive = 0;
	    }
	}
      else
	{
	  /* it will be 1 'sender thread' and 1 'reader thread' for each  */
	  /* client. Make sure to avoid data collitions.                  */
	  /* 'nbytes' avoid this. Instead of using client->nbytes to      */
	  /* temporally store data we use 'nbytes' as after the unlock    */
	  /* above, client->nbytes is used on the 'sender thread' that is */
	  /* blocked by the below mutex.                                  */

	  client->buffer[nbytes] = '\0';
	  printf("<thread %d>[NFO]:\tData read on socket %d. '%s' (%d bytes)\n", tnum, client->fd, client->buffer, nbytes);
	  /* it should be NICE to have a QUEUE! if we are locked */
	  
	  /* copy the buffer to 'stream', so sending thread will send it to clients */
	  pthread_mutex_lock(&stream_mutex);
			  
	  client->nbytes = nbytes;
	  
	  /* strncpy is safe to use here as 'nbytes'>=1, 'buffer' != NULL and 'stream' != NULL */
	  strncpy(stream, client->buffer, client->nbytes);
	  
	  /* signal sending thread */
	  pthread_cond_signal(&stream_cond);
	  
	  /* release mutex on stream buffer */
	  pthread_mutex_unlock(&stream_mutex);
	}
    }
  printf("<thread %d>[NFO]:\tAttendace thread closing on %d socket.\n", tnum, client->fd);

  pthread_cleanup_pop(1);
  /* this thread has no more reason to live */
  pthread_exit(NULL);
}




/************************************
 *
 ************************************/
void cleanup(void *arg)
{
  client_t *client = ((tdata_t *) arg)->client;
  thread_state_t s = ((tdata_t *) arg)->state;
  MUTEX_TYPE mutex = ((tdata_t *) arg)->mutex;
  int         tnum = ((tdata_t *) arg)->tnum;

  printf("<thread %d>[NFO]:\tEntering clean up function\n", tnum);

  free_client(client);
  ERR_remove_state(pthread_self());
  
  pthread_mutex_lock(&mutex);
  s = THREAD_STATE_FREE;
  pthread_mutex_unlock(&mutex);
}



/************************************
 * free_client():
 * -----------
 * free used resources
 ***********************************/
void free_client(client_t *client)
{
  /* this memory portion is available for future use */
  //client->state = CL_STATE_FREE;
  
  /* release ssl structure */
  SSL_free(client->ssl);
  
  /* close client file descriptor */
  close(client->fd);
  
  /* sanity assignment */
  client->ssl = NULL;

  pthread_mutex_destroy(&client->mutex);

  /* release client structure */
  free(client);

  /* sanity assignment */
  client = NULL;
}



/**************************************
 * wait_for_client(client_t *client):
 * ---------------
 **************************************/
int wait_for_client(client_t *client)
{
  struct timeval tv;
  fd_set master;
  fd_set copy;
  int    ssl_err_code;
  int    err;  

  FD_ZERO(&master);
  FD_ZERO(&copy);
  FD_SET(client->fd, &master);
  tv.tv_sec  = 5;
  tv.tv_usec = 0;
    
  ssl_err_code = SSL_accept(client->ssl);
  err = SSL_get_error(client->ssl, ssl_err_code);

  while( (err == SSL_ERROR_WANT_READ  ||  err == SSL_ERROR_WANT_WRITE) )
    {
      copy = master;
      if ( (select(client->fd+1, &copy, NULL, NULL, &tv)) == 0 )
	{
	  printf("[WRN]:\tselect() timeout reached\n");
	  break;
	}
      if ( FD_ISSET(client->fd, &copy) )
	{
	  ssl_err_code = SSL_accept(client->ssl);
	  err = SSL_get_error(client->ssl, ssl_err_code);
	}
    }
  return ssl_err_code;
}



/****************************************************

/****************************************************/
void client_list_init(tdata_t threads[])
{
  int i  = 0;
  /* TODO? */
}



/****************************************************

/****************************************************/
int THREAD_get_error(int value, int severity, int tid)
{
  int rc = 0;
  switch(value)
    {
    case 0:

      break;
    case EBUSY:
      printf("<thread %d>[ERR]:\tEBUSY\n", tid);
      if ( severity == FATAL)
	exit(THREAD_ERR_EBUSY);
      break;
    case EINVAL:
      printf("<thread %d>[ERR]:\tEINVAL\n", tid);
      if ( severity == FATAL )
	exit(THREAD_ERR_EINVAL);
      break;
    case ENOMEM:
      printf("<thread %d>[ERR]:\tENOMEM\n", tid);
      if ( severity == FATAL)
	exit(THREAD_ERR_ENOMEM);
      break;
    case EPERM:
      printf("<thread %d>[ERR]:\tEPERM\n", tid);
      if (severity == FATAL)
	exit(THREAD_ERR_EPERM);
      break;
    case EAGAIN:
      printf("<thread %d>[ERR]:\tEAGAIN\n", tid);
      if (severity == FATAL)
	exit(THREAD_ERR_EAGAIN);
      break;
    }
  return rc;
}



/****************************************************
 * open_listener(int port, struct sockaddr_int *addr):
 * -------------
 * Creates, sets and binds a socket to be ready for using.
 * No error checking is needed on calling this function as it
 * exits the main program. No socket created, no server running.
 ****************************************************/
int open_listener(int port, struct sockaddr_in *addr)
{   
  int sd = 0;
  int val = 1;
  if ( (sd = socket( AF_INET, SOCK_STREAM, 0 )) == -1 ) {
    perror("Server Initialization error: cannot create socket().\n");
    exit(1);
  }
  
  if ( setsockopt( sd, SOL_SOCKET, SO_REUSEADDR, &val, sizeof( int ) ) == -1 ) {
    perror("Server Socket Options: error.\n");
    close( sd );
    exit(1);
  }
  /* set addr memory to zero */
  memset(addr, 0, sizeof(*addr));
  
  /* set addr struct */
  addr->sin_family = AF_INET;
  addr->sin_port   = htons( port) ;
  addr->sin_addr.s_addr = htonl( INADDR_ANY );

  /* bind socket file descriptor to sctructure */
  if ( bind( sd, (struct sockaddr *)addr, sizeof(*addr) ) != 0 ) {
    perror("Server Initialization error: can't bind() port.\n");
    close( sd );
    abort();
  }
  if ( listen( sd, 10 ) != 0 ) {
    perror("Server Initialization error: cannot listen().\n");
    close( sd );
    abort();
  }
  printf("Server Initialization: OK...\n");
  return sd;
}



/*******************
 * init_server_ctx():
 * ----------------
 * Initializes an ssl context and return it. No error checking is
 * needed on calling this function.
 *******************/
SSL_CTX* init_server_ctx(void)
{   
  SSL_METHOD *method;
  SSL_CTX *ctx;
  int options;
  
  SSL_load_error_strings();		/* load all error messages */
  OpenSSL_add_all_algorithms();	/* load & register all cryptos, etc. */
  SSL_library_init();
  method = SSLv23_server_method();		/* for java clients compatibility */

  ERR_load_crypto_strings();
  ctx = SSL_CTX_new(method);		/* create new context from method */
  if ( ctx == NULL )  {
    ERR_print_errors_fp(stderr);
    abort();
  }
  options = SSL_CTX_get_options( ctx );
  SSL_CTX_set_options( ctx,  options | SSL_OP_NO_SSLv2 | SSL_OP_NO_SSLv3);
  return ctx;
}



/*******************************************************************
 *
 *******************************************************************/
static void locking_function(int mode, int n, const char * file, int line)
{
  if (mode & CRYPTO_LOCK)
    MUTEX_LOCK(mutex_buf[n]);
  else
    MUTEX_UNLOCK(mutex_buf[n]);
}



/*******************************************************************
 *
 *******************************************************************/
static unsigned long id_function(void)
{
  return ((unsigned long)THREAD_ID);
}



/*******************************************************************
 *
 *******************************************************************/
int THREAD_setup(void)
{
  int i;
  mutex_buf = (MUTEX_TYPE *)malloc(CRYPTO_num_locks() * sizeof(MUTEX_TYPE));
  if (!mutex_buf)
    return 0;
  for (i = 0; i < CRYPTO_num_locks(); i++)
    {
      if ( !MUTEX_SETUP(mutex_buf[i]) )
	return 0;
    }
  CRYPTO_set_id_callback(id_function);
  CRYPTO_set_locking_callback(locking_function);
  return 1;
}



/*******************************************************************
 *
 *******************************************************************/
int THREAD_cleanup(void)
{
  int i;
  if (!mutex_buf)
    return 0;
  CRYPTO_set_id_callback(NULL);
  CRYPTO_set_locking_callback(NULL);
  for (i = 0; i < CRYPTO_num_locks(); i++)
    MUTEX_CLEANUP(mutex_buf[i]);
  free(mutex_buf);
  mutex_buf = NULL;
  return 1;
}

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

int img_fd;
int sv_fd;

client_t *clients = NULL;
client_t *from    = NULL;

pthread_mutex_t stream_mutex  = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t stream_cond    = PTHREAD_COND_INITIALIZER;
pthread_t tids[MAX_CLIENTS+1];
struct sockaddr_in serveraddr;
char      stream[BUFFER_SIZE];
SSL_CTX *ctx;
int CONTINUE = 1;

void *attend_client(void *arg);
void *send_to_clients(void *arg);
void r_free();
void thread_abort();
void free_client(client_t *client);
int THREAD_get_error(int value, int severity, int tid);
void client_list_init();

int main(int argc, char *argv[]) 
{
  int cl_fd = 0;
  int i     = 0;
  int rv    = 0;

  struct sockaddr_in clientaddr;
  struct sigaction sig_action;
  tdata_t thread_data[MAX_CLIENTS];

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
  
  /* Initialize clients list */
  client_list_init();

  /* Initialize thread id array */
  for (i = 0; i < MAX_CLIENTS +1; i++)
    tids[i] = AVAIL_THREAD;

  /* launch sender thread */
  rv = pthread_create(&tids[SV_THREAD], NULL, send_to_clients, (void *) 0);

  THREAD_get_error(rv, FATAL, tids[SV_THREAD]);

  /* server started normally */
  while( CONTINUE  ) 
    {
      int addrlen = sizeof(clientaddr);
      printf(LINE);    
      printf(NFO_WAIT);
      if ( (cl_fd = accept(sv_fd, (struct sockaddr *)&clientaddr, &addrlen)) == -1)
	{
	  printf("[NFO]:\tConnection refused.\n");
	  close (cl_fd);
	}
      else
	{
	  printf("[NFO]:\tConnection attempt from %s.\n", inet_ntoa(clientaddr.sin_addr));
	  /* is there any place to run a thread? */
	  for( i = 1; i < MAX_CLIENTS +1 && tids[i] != AVAIL_THREAD; i++);
	  
	  if ( tids[i] == AVAIL_THREAD )
	    {
	      thread_data[i].client = (client_t *) malloc(sizeof(client_t));
	      if ( thread_data[i].client == NULL)
		printf("[WRN]:\tClient node allocation falied!\n");
	      else
		{
		  thread_data[i].client->fd      = cl_fd;
		  thread_data[i].client->address = clientaddr;
		  thread_data[i].client->ssl     = NULL;
		  thread_data[i].client->cl_info = NULL;
		  thread_data[i].id = i;
		  /* NOTE: this newcl mallocked memory is freed on each thread as a tmp pointer */
		  printf("[NFO]:\tClient node allocation succeded for socket %d\n", cl_fd);
		  rv = pthread_create(&tids[i], NULL, attend_client, (void *) &thread_data[i]);
		  
		  if ( THREAD_get_error(rv, WARN, thread_data[i].id) )
		    {
		      printf("[WRN]:\tSpawning thread ocess failed! Client discarted.\n");
		      close(cl_fd);
		      free(thread_data[i].client);
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
void r_free()
{
  client_t *cl = clients;
  int i = 0;
  int j = 0;
  for ( i = 0; i < MAX_CLIENTS; i++, cl++)
    {
      if ( cl->state != CL_STATE_FREE )
	{
	  printf("Freeing ssl and closing %d socket\n", cl->fd);
	  SSL_free(cl->ssl);
	  close(cl->fd);
	}
      for(j = 0; j < MUTEX_NUM; j++)
	pthread_mutex_destroy(&cl->mutex[j]);
      for(j = 0; j > COND_NUM; j++)
	pthread_cond_destroy(&cl->cond[j]);
    }
  free(clients);
  SSL_CTX_free(ctx);
  ERR_free_strings();
  EVP_cleanup();
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
  int i;
  /* this function should detach and free any thread, memory, etc */
  printf("[HANDLER]: Signal %d caught.\n", sig);
  printf("--- Shutting down ---\n");
  r_free();
  CONTINUE = 0;
  pthread_cond_signal(&stream_cond);
  exit(1);
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
  int tid      = 0;

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

      /* send 'stream' to active clients not being who send it */
      for( i = 0, to = clients; i < MAX_CLIENTS; i++, to++)
	{
	  pthread_mutex_lock(&to->mutex[SSL_MUTEX]);
	  if ( to != from &&  to->state == CL_STATE_READY )
	    {
	      printf("<thread %d>[NFO]:\tSending from %d to %d '%s' (%d bytes)\n", tid, from->fd, to->fd,stream, from->nbytes);
	      pthread_mutex_lock(&from->mutex[SSL_MUTEX]);
	      SSL_write(to->ssl, stream, from->nbytes);
	      pthread_mutex_unlock(&from->mutex[SSL_MUTEX]);
	    }
	  pthread_mutex_unlock(&to->mutex[SSL_MUTEX]);
	}

      /* set stream to zero for future use */
      memset(&stream, 0, BUFFER_SIZE);

      /* unlock shared data */
      pthread_mutex_unlock(&stream_mutex);
    }
  printf("<thread %d>[NFO]:\tSending thread closing.\n", tid);
  tids[tid] = AVAIL_THREAD;
  pthread_exit(NULL);
}


/*******************************************************************
 * attend_client(void *arg):
 * ------------
 * Thread for receive new connections
 *******************************************************************/
void *attend_client(void *arg) 
{
  client_t *tmpdata= ((tdata_t *) arg)->client; /* until now the only malloced' structure */
  int tid          = ((tdata_t *) arg)->id;
  client_t *client = NULL;
  int ssl_err_code = 0;
  int bytesread    = 0;
  int byteswrote   = 0;
  long ssl_mode    = 0;
  int fd_flag      = 0;
  int i            = 0;

  printf("<thread %d>[NFO]:\tInitializing server attendance client thread on socket %d\n", tid, tmpdata->fd);
  
  for( i = 0, client = clients; i < MAX_CLIENTS; i++, client++)
    {
      if (DEBUG)
	printf("<thread %d>[NFO]:\tSearching for space to hold new client from socket %d\n", tid, tmpdata->fd);
      /* is this memory available? */
      pthread_mutex_lock(&client->mutex[STATE_MUTEX]);
      if ( client->state == CL_STATE_FREE )
	{
	  if (DEBUG)
	    printf("<thread %d>[NFO]:\tIndex %d free to hold new client from socket %d\n", tid, i, tmpdata->fd);
	  client->state = CL_STATE_BUSY;
	  pthread_mutex_unlock(&client->mutex[STATE_MUTEX]);
	  break;
	}
      pthread_mutex_unlock(&client->mutex[STATE_MUTEX]);
    }
  
  if ( i == MAX_CLIENTS )
    {
      printf("<thread %d>[NFO]:\tMAX connections reached. No space for client from socket %d\n", tid, tmpdata->fd);
      /* no connection will be made, release this and exit thread */
      close(tmpdata->fd);
      SSL_free(tmpdata->ssl);
      free(tmpdata);
      tids[tid] = AVAIL_THREAD;
      pthread_exit(NULL);
    }
  /* now we have a pointer to a free client.   */
  /* This pointer is exclusive for each thread */
  /* and we don't need to mutex the access to  */
  /* the client except when reading from the   */
  /* ssl structure or when freeing the client. */
  /* This is because another thread may send   */
  /* data to _this_ client using his ssl field */
  /* for that purpose.                         */
  if (DEBUG)
    printf("<thread %d>[NFO]:\tClient %d state set to %d.\n", tid, i, client->state);

  /* copy new client sockaddr_in structure */
  client->address = tmpdata->address;
  if (DEBUG)
    printf("<thread %d>[NFO]:\tTemporal address %s:%d copied to client\n", tid, inet_ntoa(client->address.sin_addr), client->address.sin_port);

  /* copy new client file descriptor */
  client->fd   = tmpdata->fd;
  if (DEBUG)
    printf("<thread %d>[NFO]:\tTemporal socket %d copied to client\n", tid, client->fd);

  client->ssl  = SSL_new(ctx);
  if (DEBUG)
    printf("<thread %d>[NFO]:\tSSL structure created for socket %d\n", tid, client->fd);

  /* free the now absolete client data */
  free(tmpdata);

  /* get current fd flag */
  fd_flag  = fcntl(client->fd, F_GETFL);

  /* get current ssl mode */
  ssl_mode = SSL_get_mode(client->ssl);

  /* associate fd with ssl structure */
  ssl_err_code = SSL_set_fd(client->ssl, client->fd);

  if ( !ssl_err_code)
    {
      printf("<thread %d>[ERR]:\tCannot associate fd with ssl structure\n", tid);

      /* gain exclusive accesss to client */
      pthread_mutex_lock(&client->mutex[SSL_MUTEX]);
      pthread_mutex_lock(&client->mutex[STATE_MUTEX]);
      
      /* frees the client */
      free_client(client);
      
      /* release exclusive access to client */
      pthread_mutex_unlock(&client->mutex[STATE_MUTEX]);
      pthread_mutex_unlock(&client->mutex[SSL_MUTEX]);

      tids[tid] = AVAIL_THREAD;

      /* this thread has no more reason to live */
      pthread_exit(NULL);
    }
  printf("<thread %d>[NFO]:\tSSL associated to socket %d\n", tid, client->fd);
  /* at this point the only allocked memory is client->ssl   */
  /* and should be freed only when a connection is dropped   */

  /* set fd to non blocking */
  fcntl(client->fd, F_SETFL, fd_flag | O_NONBLOCK);

  /* wait for a handshake. Note: TIMEOUT is not actually time */
  /* though it's a multiple of it.                            */
  if( wait_for_client(client->ssl, TIMEOUT) >= TIMEOUT )  
    {
      printf("<thread %d>[ERR]:\tcould not hanshake with client.\n", tid);
     
      /* gain exclusive accesss to client */
      pthread_mutex_lock(&client->mutex[SSL_MUTEX]);
      pthread_mutex_lock(&client->mutex[STATE_MUTEX]);
      
      /* frees the client */
      free_client(client);
      
      /* release exclusive access to client */
      pthread_mutex_unlock(&client->mutex[SSL_MUTEX]);
      pthread_mutex_unlock(&client->mutex[STATE_MUTEX]);
  
      tids[tid] = AVAIL_THREAD;
      /* this thread has no more reason to live */
      pthread_exit(NULL);
    }

  /* gain exclusive access for setting the client state.    */
  /* Note that the mutex is SSL_MUTEX and not STATE_MUTEX   */
  /* The reason is that _this_ client memory portion is     */
  /* already set as used (CL_STATE_BUSY). Then, if a thread */
  /* is another _this_ thread, he can ask safely for the    */
  /* client state as _this_ client wont' be CL_STATE_FREE.  */
  /* But if a thread is a 'sender' thread, we need to block */
  /* the SSL_MUTEX 'cause that thread ask for CL_STATE_BUSY */
  /* for send data to _this_ client.                        */
  pthread_mutex_lock(&client->mutex[SSL_MUTEX]);
  client->state = CL_STATE_READY;
  pthread_mutex_unlock(&client->mutex[SSL_MUTEX]);

  /* declarations should be on top. make a read_from_client() function?*/
  const int chunk_size = 1024;
  char buffer[chunk_size];      /*unused till now */
  char *outstream;              /*unused till now */
  int nbytes = 0;
  int alive  = 1;               /*state of _this_ thread */
  int err;
  fd_set master, copy;
  FD_ZERO(&master);
  FD_ZERO(&copy);
  FD_SET(client->fd, &master);
  /* restore fd to blocking (testing purposes)*/
  //fcntl(client->fd, F_SETFL, fd_flag);
  
  printf(NFO_ACCEPT, tid, inet_ntoa(client->address.sin_addr), client->fd);
  printf(NFO_CIPHER, tid, SSL_get_version(client->ssl), SSL_get_cipher(client->ssl));
  //   SSL_set_mode(client->ssl, SSL_MODE_AUTO_RETRY);
  
  while( alive && CONTINUE )
    {
      /* The main client attendance */

      copy = master;
      if ( select(client->fd+1, &copy, NULL,NULL,NULL) == -1)
	printf("<thread %d>:[ERR]:\tSelect fail\n",tid);
      else
	{
	  if ( FD_ISSET(client->fd, &copy) )
	    {
	      /* read from the secure connection gaining exclusive access */
	      /* to the client ssl structure. The 'sender thread' could   */
	      /* access this structure coliding with the 'err' value and  */
	      /* starting a catastrophe.                                  */
	      pthread_mutex_lock(&client->mutex[SSL_MUTEX]);
	      nbytes = SSL_read(client->ssl, client->buffer, chunk_size);
	      err = SSL_get_error(client->ssl, nbytes);
	      pthread_mutex_unlock(&client->mutex[SSL_MUTEX]);
	      
	      if ( err != SSL_ERROR_WANT_READ && err != SSL_ERROR_WANT_WRITE)
		{		  
		  /* we got a disconnection or an error on the connection.     */
		  /* Change _this_ thread to dead and free the client on exit. */
		  ERR_print_errors_fp(stdout);
		  if (nbytes <= 0 )
		    {
		      /* client hung up or connection reset by peer */
		      printf("<thread %d>[NFO]:\tSocket %d hung up.\n", tid, client->fd);
		      
		      /* safe form of leaving a loop and kill the thread after that */
		      alive = 0;
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
		      /* it should be NICE to have a QUEUE! if we are locked */
		      
		      /* copy the buffer to 'stream', so sending thread will send it to clients */
		      pthread_mutex_lock(&stream_mutex);
		      
		      /* send 'stream' to active clients not being who send it */	     
		      client->nbytes = nbytes;
		      
		      /* strncpy is safe to use here as 'nbytes'>=1, 'buffer' != NULL and 'stream' != NULL */
		      strncpy(stream, client->buffer, client->nbytes);
		      
		      /* we are sending this */
		      from = client;

		      /* signal sending thread */
		      pthread_cond_signal(&stream_cond);
		      
		      /* release mutex on stream buffer */
		      pthread_mutex_unlock(&stream_mutex);
		    }
		}
	    }
	  else
	    printf("<thread %d>[NFO]:\tSelect was in another fd\n", tid);
	}
    }
  printf("<thread %d>[NFO]:\tAttendace thread closing on %d socket.\n", tid, client->fd);
  pthread_mutex_lock(&client->mutex[SSL_MUTEX]);
  pthread_mutex_lock(&client->mutex[STATE_MUTEX]);
  free_client(client);
  pthread_mutex_unlock(&client->mutex[STATE_MUTEX]);
  pthread_mutex_unlock(&client->mutex[SSL_MUTEX]);
  tids[tid] = AVAIL_THREAD;
  /* this thread has no more reason to live */
  pthread_exit(NULL);
}




/************************************
 * free_client():
 * -----------
 * free used resources
 ***********************************/
void free_client(client_t *client)
{
  /* this memory portion is available for future use */
  client->state = CL_STATE_FREE;
  
  /* release ssl structure */
  SSL_free(client->ssl);
  
  /* close client file descriptor */
  close(client->fd);
  
  /* sanity assignmnet */
  client->ssl = NULL;
}



/**************************************
 * wait_for_client(SSL *sll, int tries):
 * ---------------
 * Wait for a client the elapsed time of: 'tries'*1000usec. Upon this limit,
 * the client is disconnected assuming no handshaking by the client.
 * Returns the number of tries ('try' no 'tries') made by the server.
 **************************************/
int wait_for_client(SSL *ssl, int tries)
{
  int ssl_err_code;
  int err;
  int try = 0;
  do
    {
      try++;
      ssl_err_code = SSL_accept(ssl);
      err = SSL_get_error(ssl, ssl_err_code);
      usleep(1000);
    }
  while( (err == SSL_ERROR_WANT_READ ||
	  err == SSL_ERROR_WANT_WRITE) &&
	 try < tries );

  return try;
}



/****************************************************

/****************************************************/
void client_list_init()
{
  int i  = 0;
  int j  = 0;
  
  clients = (client_t *) malloc(sizeof(client_t) * MAX_CLIENTS);
  if ( clients == NULL)
    {
      printf("[ERR]: Cannot start server.\n");
      exit(-1);
    }
  for(i = 0; i < MAX_CLIENTS; i++)
    {
      clients[i].state   = CL_STATE_FREE;
      clients[i].ssl     = NULL;
      clients[i].cl_info = NULL;
      clients[i].nbytes  = 0;
      
      /* Initialize mutex client list */
      for(j = 0; j < MUTEX_NUM; j++)
	THREAD_get_error(pthread_mutex_init(&(clients[i].mutex[j]), NULL), FATAL, NO_THREAD);

      /* Initialize condition client list */
      for(j = 0; j < COND_NUM; j++)
	THREAD_get_error(pthread_cond_init(&(clients[i].cond[j]), NULL), FATAL, NO_THREAD);

      memset(&clients[i].buffer, 0, sizeof(clients[i].buffer));
      memset(&clients[i].address.sin_zero, 0, sizeof(clients[i].address.sin_zero));
    }
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
  method = SSLv23_method();		/* for java clients compatibility */
  
  ctx = SSL_CTX_new(method);		/* create new context from method */
  if ( ctx == NULL )  {
    ERR_print_errors_fp(stderr);
    abort();
  }
  options = SSL_CTX_get_options( ctx );
  SSL_CTX_set_options( ctx,  options | SSL_OP_NO_SSLv2 | SSL_OP_NO_SSLv3);
  return ctx;
}

/**
 *   Copyright (c) 2010 Sebastián Treu
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 3 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
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
#include <arpa/inet.h>
#include <fcntl.h>
#include <pthread.h>
#include <signal.h>
#include <stdarg.h>
#include <math.h>
//#include <openssl/sha.h>
#include <termios.h>
#ifdef CDK_AWARE
#include <ncurses.h>
#include "console.h"
#endif

#include "server.h"
#include "tellapic/types.h"
#include "tellapic/constants.h"
#include "tellapic/tellapic.h"
#include "common.h"



//TODO: Portability.
MUTEX_TYPE         *mutex_buf; //this is initialized by THREAD__setup();
MUTEX_TYPE         clcmutex = PTHREAD_MUTEX_INITIALIZER;
int                clccount = 0; //shared
uint32_t           ccbitset = 0; //connected clients bit set
MUTEX_TYPE         ccbitsetmutex = PTHREAD_MUTEX_INITIALIZER;
list_t             *msgqueue;
MUTEX_TYPE         queuemutex = PTHREAD_MUTEX_INITIALIZER;
int                CONTINUE = 1; //this NEEDS to be global as the handler interrupt manages program execution
args_t             args;

#ifdef CDK_AWARE
console_t          *console = NULL;
#endif


/**
 *
 */
int main(int argc, char *argv[]) {
  int                cMsgBufLen = 256; //TODO: #define ...
  char               cMsgBuf[cMsgBufLen];
  int                clfd = 0;                                            /* client file descriptor */
  int                i    = 0;                                            /* iterator variable */
  int                rv   = 0;                                            /* returned value for some function */
  struct sockaddr_in clientaddr;                                          /* client address structure */
  struct sockaddr_in serveraddr;                                          /* server address structure */
  struct sigaction   sig_action;                                          /* signal handler for external signal management */
  pthread_attr_t     joinattr;                                            /* thread joinable attribute for joinable threads */
  tdata_t            thread_data[MAX_CLIENTS];                            /* this is the main shared data */
  fd_set             readset;
  fd_set             copyset;

  sig_action.sa_handler = signal_handler;
  sigemptyset(&sig_action.sa_mask);
  //sigaddset(&sig_action.sa_mask, SIGINT);
  pthread_sigmask(SIG_BLOCK, &sig_action.sa_mask, NULL);
  sig_action.sa_flags = SA_RESTART;
  if ( sigaction(SIGINT, &sig_action, NULL) == -1)
    print_output("<server>[ERR]: Could not install signal handler!\n");
  if ( sigaction(SIGPIPE, &sig_action, NULL) == -1)
    print_output("<server>[ERR]: Could not install signal handler!\n");
  if ( sigaction(SIGQUIT, &sig_action, NULL) == -1)
    print_output("<server>[ERR]: Could not install signal handler!\n");


  //args.imglist = list_make_empty(args.imglist);                          /* initialize image list. TODO (24/05/10): ?!? */
  if (args_check(argc, argv) < 0)                                         /* check program arguments and fill args_t structure in args */
    exit(1);

  args.svfd = start_server(atoi(argv[PORT_ARG]), &serveraddr);             /* Prepare the server for listen */

  for(i = 0; i < MAX_CLIENTS; i++)                                         /* Initialize main data structures and global variables */
    data_init_thread(&thread_data[i], i);

  pthread_attr_init(&joinattr);                                            /* initialize and create a thread joinable attribute */
  pthread_attr_setdetachstate(&joinattr, PTHREAD_CREATE_JOINABLE); 
  msgqueue = list_make_empty(msgqueue);
  thread_data[SV_THREAD].tstate = THREAD_STATE_ACT;                        /* set server thread state as active */
  setnonblock(args.svfd);

# ifdef CDK_AWARE
  //console = console_create("<C>Server output\0", "</B/24> >>> \0");
  if (console == NULL)
    print_output("Could not start console: \n");
# else
  print_output("Could no start console. No cdk library found.");
# endif

  int fdmax = args.svfd + 1;
  FD_ZERO(&readset);
  FD_SET(args.svfd, &readset);

# ifdef CDK_AWARE
  if (console != NULL)
    {
      fdmax = MAX(args.svfd, console->infd);
      FD_SET(console->infd, &readset);
    }
# endif

  while(CONTINUE) 
    {
      socklen_t addrlen  = sizeof(clientaddr); /* the address structure length */

      copyset = readset;                 /* copy the read file descriptor set to be used and modified by select() call */

      /* TODO: add some kind of verbose level to the command line */
      print_output("[NFO]:\tWaiting for incoming connections");

      rv = pselect(fdmax + 1, &copyset, NULL, NULL, NULL, &sig_action.sa_mask);

      if (rv < 0) 
	{
	  /* Is selects exits with <0 then take that error as fatal and end the server */
	  print_output("</K/16>[ERR]:\tFatal error... Terminating server...<!K!16>");
#         ifdef CDK_AWARE
	  if (console != NULL)
	    console_destroy(console);
#         endif
	  CONTINUE = 0;
	}
      else 
	{
	  /***************************************************************************************/
	  /* Check if select() call exit because the server file descriptor was ready to be read */
	  /***************************************************************************************/
	  if (FD_ISSET(args.svfd, &copyset)) 
	    {
	      /* Try accepting the incoming connection. If we succeed, allocate memory. If we don't just inform that. */
	      if ((clfd = accept(args.svfd, (struct sockaddr *)&clientaddr, &addrlen)) == -1) 
		{
		  print_output("</B>[WRN]:<!B>\tConnection refused.");
		}
	      else 
		{
		  char buff[256];
		  sprintf(buff, "[NFO]:\tConnection attempt from %s.", inet_ntoa(clientaddr.sin_addr));
		  print_output(buff);

		  int tnum = find_free_thread(thread_data);   /* find a thread to hold the client connection */

		  if (tnum != -1) 
		    {
		      allocate_and_launch(&thread_data[tnum - 1], clfd, clientaddr, joinattr);
		    } 
		  else
		    {
		      print_output("</B>[WRN]:<!B>\tServer reached full capacity.");
		      close(clfd);
		    }
		}
	    }
	  
#         ifdef CDK_AWARE
	  /*************************************************************/
	  /* Check if select() call exit for reading a console command */
	  /*************************************************************/
	  if (console != NULL && console_isenabled(console) && FD_ISSET(console->infd, &copyset)) 
	    { 
	      read(console->infd, &cMsgBuf, cMsgBufLen);

	      /* Read command from console pipe. Quit the server if 'q' or 'quit' was issued. */
	      if (strcmp(cMsgBuf, "q") == 0 || strcmp(cMsgBuf, "quit") == 0) 
		{
		  /* Remove the console pipe from the read set */
		  FD_CLR(console->infd, &readset);

		  /* Remove the server file descriptor from the read set */
		  FD_CLR(args.svfd, &readset);

		  /* Inform the server we are going down... */
		  CONTINUE = 0;
		} 
	      else
		{
		  /* ...else, process the command issued. */
		  process_command(cMsgBuf, thread_data);
		}

	      /* Set to 0 the command line buffer */
	      memset(cMsgBuf, 0, 256);
	    }
#         endif
	}
    }

  /* Close the server file descriptor */
  close(args.svfd);

  print_output("[NFO]:\tServer is going down...");

  pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL); /* set the cancel state of this thread */
  pthread_setcanceltype(PTHREAD_CANCEL_ENABLE, NULL);  /* set the cancel type of this thread */

  cancel_threads(thread_data);

  pthread_attr_destroy(&joinattr);

  /* Destroy the console instance. */
# ifdef CDK_AWARE
  if (console != NULL) console_destroy(console);
# endif

  print_output("[NFO]:\tCurses unloaded.");

  /* Iterate over the nodes freeing its memory. */
  list_node_t *node = NULL;
  for (node = list_get_head(msgqueue); node != NULL; node = list_get_next(node)) 
    unqueue_message(node->item);

  /* iterate over the list freeing the nodes. This must be called after realising node's memory. */
  list_free(msgqueue);

  /* free the msgqueue list pointer */
  free(msgqueue);

  /* close the loaded file */
  fclose(args.image);

  print_output("[NFO]:\tServer exits normally");
  pthread_exit(NULL);
}


/**
 *
 */
void
allocate_and_launch(tdata_t *thread, int fd, struct sockaddr_in addr, pthread_attr_t attr)
{
  int rv = 0;

  if (new_client_thread(thread, fd, addr)) 
    {
      rv = pthread_create(&thread->tid, &attr, manage_client, thread);

      if (THREAD_get_error(rv, WARN, thread->tnum)) 
	{
	  close(thread->client->socket.s_socket);
	  free(thread->client);
	  set_tstate(thread, THREAD_STATE_FREE);
	}
    } 
  else 
    {
      set_tstate(thread, THREAD_STATE_FREE);
      close(fd);
      print_output("</B>[WRN]:<!B>\tClient node allocation falied!");
    }
}


/**
 *
 */
void
cancel_threads(tdata_t *thread_data) 
{
  int i;
  for( i = 0; i < MAX_CLIENTS; i++) 
    {
      if ( thread_data[i].tstate != THREAD_STATE_NEW) 
	{
	  pthread_cancel(thread_data[i].tid);
	  pthread_detach(thread_data[i].tid);
	}
    }
}


#ifdef CDK_AWARE
/**
 *
 */
void
process_command(char *cmd, tdata_t *thread_data) 
{
  char args[20][256];
  char buff[256];
  int  n;
  int  i = 0;
  int  j = 0;

  memset(args, 0, 20);
  memset(buff, 0, 256);

  // snippet stolen from internet to avoid using strtok()
  while (*cmd != '\0') 
    {
      int items_read = sscanf(cmd, "%31[^ ]%n", args[i], &n);
      i++;
      if (items_read == 1)
	cmd += n; /* advance the pointer by the number of characters read */
      if ( *cmd != ' ' ) 
	{
	  break; /* didn't find an expected delimiter, done? */
	}
      ++cmd; /* skip the delimiter */

      if (i >= 20) //avoid going off args[] limits.
	break;
    }

  // Command is tokenized in args array.
  if(strcmp(args[0], "help") == 0 && i <= 1)
    {
      help();
    }
  else if (strcmp(args[0], "threads") == 0 && i <= 1) 
    {
      int i = 0;
      pthread_mutex_lock(&ccbitsetmutex);
      for(i = 0; i < MAX_CLIENTS; i++) 
	{
	  if (ccbitset & (1 << i)) 
	    {
	      sprintf(buff, "command output: </B>Thread </24>%d<!24> is alive<!B>", i);
	      print_output(buff);
	    }
	}
      pthread_mutex_unlock(&ccbitsetmutex);
      sprintf(buff, "command output: </B>You can type </24>info <thread-number><!24> to get a more detailed information.");
      print_output(buff);
    } 
  else if (strcmp(args[0], "msglist") == 0 && i <= 1) 
    {
      list_node_t *node = NULL;
      int i = 0;
      pthread_mutex_lock(&queuemutex);
      for(node = list_get_head(msgqueue); node != NULL; node = list_get_next(node)) 
	{
	  sprintf(buff, "command output: </B>MessageList[%d] -> %p<!B>", i++, list_get_item(node));
	  print_output(buff);
	}
      pthread_mutex_unlock(&queuemutex);
      
      if (i == 0) 
	{
	  memset(buff, 0, 256);
	  sprintf(buff, "command output: </B>MessageList is empty!<!B>");
	  print_output(buff);
	}
    } 
  else if (strcmp(args[0], "info") == 0 && i <= 2) 
    {
      int tnum = strtol(args[1], NULL, 10);
      if (tnum < MAX_CLIENTS && tnum >= 0) 
	{
	  sprintf(buff, "command output: </B>Thread </24>%d<!24> status: </24>%s<!24>", tnum, tstatus[thread_data[tnum].tstate]);
	  print_output(buff);
	  pthread_mutex_lock(&ccbitsetmutex);
	  if (ccbitset & (1 << tnum)) 
	    {
	      char str[INET_ADDRSTRLEN];
	      inet_ntop(AF_INET, &(thread_data[tnum].client->address.sin_addr), str, INET_ADDRSTRLEN);
	      sprintf(buff, "command output: </B>Thread </24>%d<!24> socket number: </24>%d<!24>", tnum, thread_data[tnum].client->socket.s_socket);
	      print_output(buff);
	      sprintf(buff, "command output: </B>Thread </24>%d<!24> client name: </24>%s<!24>", tnum, thread_data[tnum].client->name);
	      print_output(buff);
	      sprintf(buff, "command output: </B>Thread </24>%d<!24> internet address: </24>%s<!24>", tnum, str);
	      print_output(buff);
	    } 
	  else
	    {
	      sprintf(buff, "command output: </B>Thread </24>%d<!24> is not connected to any client.", tnum);
	      print_output(buff);
	    }
	  pthread_mutex_unlock(&ccbitsetmutex);
	}
    } 
  else if (strcmp(args[0], "close") == 0 && i <= 2)
    { //TODO:
    
    } 
  else if (strcmp(args[0], "examine") == 0 && i <= 2) 
    { //TODO:
    
    } 
  else
    {
      nocmd();
    }
}
#endif


/**
 *
 */
void
print_output(char *msg) 
{
#ifdef CDK_AWARE
  if (console != NULL && console_isenabled(console)) 
    {
      write(console->scrollwindow->outfd, msg, 256);
    } 
  else
#endif
    {
      printf("TEST %s\n", msg);
    }
}


/**
 *
 */
int
find_free_thread(tdata_t thread_data[]) 
{
  int  notfound = 1;  /* used for find a free/new thread structure each time a client connects */
  int  i;             /* used for iterate over tdata_t structure */
  char buff[256];     /* used for printing messages on screen */

  for(i = 0; i < MAX_CLIENTS && notfound; i++)
    {

#     ifdef DEBUG
      sprintf(buff, "[NFO]:\tsearching place on %d and locking", i);
      print_output(buff);
      memset(buff, '\0', 256);
#     endif

      pthread_mutex_lock(&thread_data[i].stmutex);

      switch(thread_data[i].tstate) 
	{
	case THREAD_STATE_NEW:
#         ifdef DEBUG
	  sprintf(buff, "[NFO]:\tthread %d was never used", i);
	  print_output(buff);
	  memset(buff, '\0', 256);
#         endif
	  thread_data[i].tstate = THREAD_STATE_INIT;
	  notfound = 0;
	  break;
	  
	case THREAD_STATE_FREE:
#         ifdef DEBUG
	  sprintf(buff, "[NFO]:\tthread %d was used.", i);
	  print_output(buff);
	  memset(buff, '\0', 256);
#         endif
	  pthread_join(thread_data[i].tid, NULL);
	  thread_data[i].tstate = THREAD_STATE_INIT;
	  notfound = 0;
	  break;

	default:
#         ifdef DEBUG
	  sprintf(buff, "[NFO]:\tthread %d is busy.", i);
	  print_output(buff);
	  memset(buff, '\0', 256);
#         endif
	  break;
	}
      /**
       * MAN PAGE:
       * --------
       * After a canceled thread has terminated, a join with that  thread  using
       * pthread_join()  obtains  PTHREAD_CANCELED as the threads exit status.
       * (Joining with a thread is the only way to know  that  cancellation  has
       * completed.)
       * RETURN VALUE
       *   On  success, pthread_cancel() returns 0; on error, it returns a nonzero
       *   error number.
       */
      pthread_mutex_unlock(&thread_data[i].stmutex);
    }
  
  return (notfound)? -1 : i;
  
}



/**
 * manage_client(void *arg):
 * -------------------------
 */
void 
*manage_client(void *arg) 
{
  char      buff[256]; //TODO: #define MAX_BUFMSG_SIZE 256
  char      address[INET_ADDRSTRLEN];          /* this will hold the IP address                   */
  tdata_t   *thread = (tdata_t *) arg;         /* a thread data structure                         */
  int       fdmax   = thread->client->socket.s_socket + 1;  /* max file descriptor number to be used by select */
  fd_set    readfdset;                         /* this will hold client->fd                       */
  fd_set    fdsetcopy;                         /* this is a required copy                         */
  struct    timeval to;

  pthread_cleanup_push(manage_client_cleanup, thread);                                /* register cleanup function for this thread */
  set_tstate(thread, THREAD_STATE_INIT);                                              /* set the thread state to INIT */
  inet_ntop(AF_INET, &(thread->client->address.sin_addr), address, INET_ADDRSTRLEN);  /* copy the client IPV6 address */
# ifdef DEBUG 
  memset(buff, '\0', 256);
  sprintf(buff, "[NFO]:\tConnection attempt from: </B/24>%s<!B!24>. Asigned socket number </B/24>%d<!B!24> on thread number %d",address,  thread->client->socket.s_socket, thread->tnum);
  print_output(buff);
# endif

  if(authclient(thread) == 0) 
    {
      char buff[256];
      sprintf(buff, "</B/16>[ERR]:\tCould not auth with client. Closing link on thread number %d.<!B!16>", thread->tnum);
      print_output(buff);
      pthread_exit(NULL);
    }
# ifdef DEBUG 
  memset(buff, '\0', 256);
  sprintf(buff, "[NFO]:\tClient %d authed.", thread->client->socket.s_socket);
  print_output(buff);
# endif
  /* The client is authed (poorly) with the server now. He/she should ask for the file we are sharing/discussing. */
  tellapic_send_ctl(thread->client->socket, thread->tnum, CTL_SV_AUTHOK);
# ifdef DEBUG 
  memset(buff, '\0', 256);
  sprintf(buff, "[NFO]:\tWaiting for client %d to ask for file.", thread->client->socket.s_socket);
  print_output(buff);
# endif

  /* Take note to change this behaviour so later releases can extend the server to hold */
  /* more than just 1 image per instance. It would be nice to have an arbitrary list of images */
  /* dinamically created by the owner of this session */
  stream_t stream = tellapic_read_stream_b(thread->client->socket);
  if (stream.header.cbyte == CTL_CL_FILEASK)
    {
#     ifdef DEBUG 
      memset(buff, '\0', 256);
      sprintf(buff, "[NFO]:\tSending file to client %d.", thread->tnum);
      print_output(buff);
#     endif
      tellapic_send_file(thread->client->socket, args.image, args.filesize);
    }
  else
    pthread_exit(NULL); //it is supposed to be a CTL_CL_FILEASK stream

  FD_ZERO(&readfdset);                     /* initialize file descriptor set */
  FD_SET(thread->client->socket.s_socket, &readfdset);  /* add client socket to the set   */
  FD_SET(thread->readpipe, &readfdset);    /* add the pipe-event to the set  */
# ifdef DEBUG 
  memset(buff, '\0', 256);
  sprintf(buff, "[NFO]:\tClient connected from </B/24>%s<!B!24> to thread number </B/24>%d<!B!24> on socket number </B/24>%d<!B!24>\n", address, thread->tnum, thread->client->socket.s_socket);
  print_output(buff);
# endif
  /* send the connected clients to this client */
  int i;
  for(i = 0 - thread->tnum; i < MAX_CLIENTS - thread->tnum; i++)
    {
      pthread_mutex_lock(&thread[i].stmutex);
      if (thread[i].tstate == THREAD_STATE_ACT)
	tellapic_send_ctle(thread->client->socket, thread[i].tnum, CTL_SV_CLADD, thread[i].client->namelen, thread[i].client->name);
      pthread_mutex_unlock(&thread[i].stmutex);
    }

  pthread_mutex_lock(&ccbitsetmutex);                  /* Lock the shared resource   */
  ccbitset |= (1 << thread->tnum);                     /* Set the bit flag indicating this client thread number connection */
  pthread_mutex_unlock(&ccbitsetmutex);                /* Unlock the shared resource */
  set_tstate(thread, THREAD_STATE_ACT);                /* set the thread state to ACTIVE */
  pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL); /* set the cancel state of this thread */

  mitem_t *msg = malloc(sizeof(mitem_t));
  if (msg == NULL) 
    {
      // abort?
    }

  msg->stream   = tellapic_build_rawstream(CTL_SV_CLADD, thread->tnum, thread->client->name, thread->client->namelen);
  msg->tothread = -1; //TODO: #define or something here...
  msg->delivers = 0;
  pthread_mutex_init(&msg->mutex, NULL);
  queue_message(msg, thread);
# ifdef DEBUG 
  memset(buff, '\0', 256);
  sprintf(buff, "[NFO]:\tMain attendance loop start on thread %d", thread->tnum);
  print_output(buff);
# endif

  while(thread->tstate != THREAD_STATE_END) 
    {
      int fetchresult = 0;
      to.tv_sec = 180;
      fdsetcopy = readfdset;   /* Maintain a copy of the file descriptor set */

#     ifdef DEBUG
      memset(buff, '\0', 256);
      sprintf(buff, "[NFO]:\tEntering select() on thread %d", thread->tnum);
      print_output(buff);
#     endif

      int rv = select(fdmax, &fdsetcopy, NULL, NULL, &to);
      if (rv < 0) 
	{
	  
	} 
      else if ( rv == 0)
	{

#         ifdef DEBUG
	  memset(buff, '\0', 256);
	  sprintf(buff, "[NFO]:\tSelect timeout. Check error condition and/or continue for thread %d", thread->tnum);
	  print_output(buff);
#         endif

	  /* We got a timeout. Do we have an uncleared error on fetching streams? */
	  if (fetchresult < 0)
	    set_tstate(thread, THREAD_STATE_END);
	}
      else
	{
#         ifdef DEBUG
	  memset(buff, '\0', 256);
	  sprintf(buff, "[NFO]:\tEXIT Select on thread %d", thread->tnum);
	  print_output(buff);
#         endif


	  /* Check if select() has set the socket ready for reading. */
	  /* If so, get the stream.                                  */
	  if (FD_ISSET(thread->client->socket.s_socket, &fdsetcopy))
	    fetchresult = fetch_stream(thread);


	  /* Check if select() has set the pipe ready for reading. If so, */
	  /* forward to this  client whatever is in the message queue.    */
	  if (FD_ISSET(thread->readpipe, &fdsetcopy))
	    forward_stream(thread);
	}
    } 
  pthread_mutex_lock(&ccbitsetmutex);    /* Lock the shared resource   */
  ccbitset &= ~(1 << thread->tnum);      /* Set the bit flag without this client flag */
  pthread_mutex_unlock(&ccbitsetmutex);  /* Unlock the shared resource */
  pthread_cleanup_pop(1);                /* pop the cleanup function */

# ifdef DEBUG 
  memset(buff, '\0', 256);
  sprintf(buff, "[NFO]:\tThread %d exiting normally. Closing link with client.", thread->tnum);    /* this thread has no more reason to live */
  print_output(buff);
# endif

  pthread_exit(NULL);
}



/**
 *
 */
int
authclient(tdata_t *thread) 
{
  int      newpwdlen = 0;
  char     *pwd  = NULL;
  int      pwdok = 0;
  int      try   = 0;
  
  /* The server is the first to talk. It sends the client id. (fd) */
  int rc = tellapic_send_ctl(thread->client->socket, thread->tnum, CTL_SV_ID);
  if (rc <= 0)
    return pwdok;
  
  while(!pwdok && try < MAX_PWD_TRIES) 
    {
      
      /* we know we need to read a password. Use this library function for it. */
      pwd = tellapic_read_pwd(thread->client->socket, pwd, &newpwdlen);

#     ifdef DEBUG 
      char buff[256];
      sprintf(buff, "[NFO]:\tPassword read from client %d was %s. </16>Try number %d<!16>.", thread->client->socket.s_socket, pwd, try);
      print_output(buff);
#     endif

      if (pwd != NULL)
	{

	  /* TODO: CHECK strncmp() !! sigsegv */
	  pwdok = (args.pwdlen == newpwdlen) && (strncmp(args.pwd, pwd, newpwdlen) == 0);
	  if (pwdok) 
	    {
	      tellapic_send_ctl(thread->client->socket, thread->tnum, CTL_SV_PWDOK);                                	 /* Send CTL_SV_PWDOK to client */

	      do
		{
		  if (thread->client->name != NULL)
		    {
		      free(thread->client->name);
		      thread->client->name = NULL;
		    }

		  stream_t stream = tellapic_read_stream_b(thread->client->socket); 	                                 /* Read client response        */

		  if (stream.header.cbyte == CTL_CL_NAME && stream.data.control.idfrom == thread->tnum) 
		    {
		      /* if (thread->client->name != NULL) */
		      /* 	free(thread->client->name); */

		      thread->client->name = malloc(stream.header.ssize - HEADER_SIZE);	                    /* Allocate memory for client name  */
		      thread->client->namelen = stream.header.ssize - HEADER_SIZE - 1;
		      memcpy(thread->client->name, stream.data.control.info, thread->client->namelen);      /* Copy the client name             */
		      thread->client->name[thread->client->namelen] = '\0';                                 /* Add a trailing '\0' just in case */
		    }
		  else 
		    {
#                     ifdef DEBUG 
		      char buff[256]; 
		      sprintf(buff, "[NFO]:\tBad Response from thread %d", thread->tnum);
		      print_output(buff);
#                     endif
		      return 0;         /* If it isn't a CTL_CL_NAME packet is an invalid sequence */
		    }

#                 ifdef DEBUG 
		  char buff[256]; 
		  sprintf(buff, "[NFO]:\tResponse from was %s (len: %d) on thread %d", thread->client->name, thread->client->namelen, thread->tnum);
		  print_output(buff);
#                 endif	
		}
	      while(isnameinuse(thread) && tellapic_send_ctl(thread->client->socket, thread->tnum, CTL_SV_NAMEINUSE));
	    } 
	  else 
	    {
	      try++;
	      if (try < MAX_PWD_TRIES)
		tellapic_send_ctl(thread->client->socket, thread->tnum, CTL_SV_PWDFAIL);
	    }
	} 
      else
	{
	  try = MAX_PWD_TRIES;  /* it's not a valid sequence so, disconnect the client */
	}
    }

  return pwdok;
}


/**
 * TODO: CHECK strncmp() sigsegv
 */
int
isnameinuse(tdata_t *thread)
{
  int inuse = 0;
  int current = thread->tnum;
  int i = 0;
  for(i = 0 - current; i < MAX_CLIENTS - current && !inuse; i++)
    {
      pthread_mutex_lock(&thread[i].stmutex);
      if (thread[i].tstate == THREAD_STATE_INIT || thread[i].tstate == THREAD_STATE_ACT)
	if(thread[i].tnum != thread->tnum && 
	   thread[i].client->namelen == thread->client->namelen &&
	   (strncmp(thread->client->name, thread[i].client->name, thread[i].client->namelen) == 0))
	  inuse = 1;
      pthread_mutex_unlock(&thread[i].stmutex);
    }

  return inuse;
}


/**
 *
 */
void 
forward_stream(tdata_t *thread) 
{
  mitem_t   *message    = NULL;
  void      *msgaddress = NULL;
  int       result      = 0;

  read(thread->readpipe, &msgaddress, sizeof(msgaddress));            /* Read the message address and clear the pipe */
  message = (mitem_t *) msgaddress;                                   /* Cast to mitem_t*  */

#  ifdef DEBUG
  char buff[256];
  sprintf(buff, "[NFO]:\tMessage address was %p. Cbyte is: %d.", message, message->stream[CBYTE_INDEX]);
  print_output(buff);
# endif

  //result = tellapic_send(thread->client->socket.s_socket, &(message->stream));      /* Forward data to this client */
  
  result = tellapic_rawsend(thread->client->socket, message->stream);

  if (result)
    {

      pthread_mutex_lock(&message->mutex);
      message->delivers--;

      //      if (message->delivers == 0)
      //unqueue_message(message); 
      
      pthread_mutex_unlock(&message->mutex);
      
#     ifdef DEBUG 
      char buff[256];
      sprintf(buff, "[NFO]:\tForwarding ok on thread %d", thread->tnum);
      print_output(buff);
#     endif
    }
}


/**
 *
 */
int
fetch_stream(tdata_t *thread)
{
  int          rc = 0;
  mitem_t      *item = NULL;
  //stream_t     stream;
  //stream = tellapic_read_stream_b(thread->client->socket);
  byte_t       *rawstream = tellapic_rawread_b(thread->client->socket);

  if (rawstream == NULL)
    return rc;

  item = malloc(sizeof(mitem_t));

  if (item == NULL)
    return rc;

  pthread_mutex_init(&item->mutex, NULL);
  item->delivers = 0;
  item->stream   = rawstream;
  item->tothread = -1;       /* Set -1 by default so we simplified the switch */

  switch(rawstream[CBYTE_INDEX])
    {
    case CTL_CL_PMSG:
      item->tothread = rawstream[HEADER_SIZE + DATA_PMSG_IDTO_INDEX];
    case CTL_CL_BMSG:
    case CTL_CL_DRW:
    case CTL_CL_FIG:
      queue_message(item, thread);
      rc = 1;
      break;

    case CTL_CL_FILEASK:
      //send_file(thread->client->socket); as CTL_CL_DISC but with item->tothread = (1 << stream.data.type.control.idfrom);
      rc = 1;
      break;

    case CTL_CL_DISC:
    case CTL_NOPIPE:
      free(rawstream);
      free(item);
      //rawstream = tellapic_build_rawstream(CTL_SV_CLRM, thread->client->socket.s_socket);
      //queue_message(item, thread);
      set_tstate(thread, THREAD_STATE_END);
      rc = 1;
      break;

    case CTL_CL_TIMEOUT:
      free(rawstream);
      free(item);
      rc = 1;
      break;

    case CTL_CL_PING:
      free(rawstream);
      free(item);
      printf("Ping packet received\n");
      tellapic_send_ctl(thread->client->socket, thread->tnum, CTL_SV_PONG);
      rc = 1;
      break;

    case CTL_NOSTREAM:
      pthread_mutex_destroy(&item->mutex);
      free(rawstream);
      free(item);
      rc = 1;
      break;

    case CTL_FAIL:
      pthread_mutex_destroy(&item->mutex);
      free(rawstream);
      free(item);
      rc = -1;
      break;
    }

  //  return rc;
}


/**
 * helper function
 */
void 
queue_message(mitem_t *msg, tdata_t *thread) 
{
  int alone = 0;

  pthread_mutex_lock(&ccbitsetmutex);
  alone = (ccbitset | (1 << thread->tnum)) == (1 << thread->tnum);   // Only queue the item if we (the thread) are not alone.
  pthread_mutex_unlock(&ccbitsetmutex);

  if (alone) 
    {
      /* Destroy the message if we are alone in the server and leave. */
      pthread_mutex_destroy(&msg->mutex);
      free(msg);
      return;
    }
  else 
    {
      /* Queue the message and signal other threads */
      pthread_mutex_lock(&queuemutex);
      list_add_last_item(msgqueue, msg);
      pthread_mutex_unlock(&queuemutex);
    }

  int    i;
  int    current  = thread->tnum;    /* We are going to use local data to access the whole tdata_t structure using pointer arithmetics. */
  void   *address = msg;

  /* Who told you that C won't accept negative indexes such as a[-2]? */
  for(i = 0 - current; i < MAX_CLIENTS - current; i++) 
    {
      /* Lock the connected clients bit flag. */
      pthread_mutex_lock(&ccbitsetmutex);

      /* If thread[i] is connected with a client and this message is private, ensure that is for him. */
      /* If it isn't for him, then do nothing. Else, if it isn't private, send it anyway. */

      if ( (ccbitset & (1 << thread[i].tnum))  /* Check if thread[i].tnum is connected */
	   &&
	   (msg->tothread == -1 || msg->tothread == thread[i].tnum)
	   && current != thread[i].tnum)       /* Check if we aren't forwarding to the sender */
	{

	  /* msg is sent to N threads. When msg->delivers reachs 0, means that all threads have processed the message msg. If */
	  /* a thread is somehow blocked forever, this msg will not ever be processed. But, when the server is shutted down, it */
	  /* will removed from the queue in the resource free section. If a thread disconnects a client before the msg address */
	  /* arrives at his pipe, the thread cleanup procedure SHOULD decrement msg->delivers field. */
	  pthread_mutex_lock(&msg->mutex);
	  msg->delivers++;
	  pthread_mutex_unlock(&msg->mutex);

#         ifdef DEBUG 
	  char   buf[256];
	  sprintf(buf, "Sending msg address </B>%p<!B> to thread </B>%d<!B>. Stream cbyte is: %d", address, thread[i].tnum, msg->stream[CBYTE_INDEX]);
	  print_output(buf);
#         endif
      
	  /* We are sending a pointer address to the thread i. So, this queue behaves as an array with direct access to it. */
	  /* The thread i will read the pipe, will cast the void* to mitem_t* and it will deliver the message to the client */
	  /* that is connected to. */
	  write(thread[i].writepipe, &address, sizeof(address)); //TODO: care about write() being locked. It will lock the mutexes forever.
	}
      pthread_mutex_unlock(&ccbitsetmutex);
    }
}


/**
 * helper function
 */
void unqueue_message(mitem_t *msg){
  pthread_mutex_lock(&queuemutex);
  list_node_t *node = list_remove_item(msgqueue, msg);
  pthread_mutex_unlock(&queuemutex);
  
  pthread_mutex_destroy(&msg->mutex);
  free(msg->stream);
  free(msg);
  free(node);
}


/**
 *
 */
void manage_client_cleanup(void *arg) {
  tdata_t *thread = (tdata_t *) arg;
  char    buff[64];

  /* If thread was ACTIVE (connected) or END (ending) send */
  /* CTL_SV_CLRM so other clients can remove it   */
  /* from their connected client list.            */
  if (thread->tstate == THREAD_STATE_ACT || thread->tstate == THREAD_STATE_END)
    {
      set_tstate(thread, THREAD_STATE_FREE);
      mitem_t *item = malloc(sizeof(mitem_t));
      if (item != NULL)
	{
	  pthread_mutex_init(&item->mutex, NULL);
	  byte_t *rawstream = tellapic_build_rawstream(CTL_SV_CLRM, thread->tnum);
	  item->delivers = 0;
	  item->stream   = rawstream;
	  item->tothread = -1;       /* Set -1 by default so we simplified the switch */
	  queue_message(item, thread);
	}
      tellapic_send_ctl(thread->client->socket, thread->tnum, CTL_CL_DISC);
      free(thread->client->name);
    }
  else
    set_tstate(thread, THREAD_STATE_FREE);

  sprintf(buff, "[</8>WRN<!8>]:\tThread %d is closing link now...", thread->tnum);
  print_output(buff);
  close(thread->client->socket.s_socket);
  free(thread->client);
}


/**
 * returns a list of connected clients with the 'thread'
 * client at the beginning
 *
list_t *get_connected_clients(tdata_t *thread) 
{
  int       i      = 0;
  int       number[MAX_CLIENTS];
  tdata_t   *tmp_thread = NULL;
  list_t    *clist = list_make_empty(clist);


  // Sets 'tmp_thread' to point to the first thread and goes forward until 'thread' 
  // position, putting on 'clist' the ids of the connected clients.                 
  for(tmp_thread = thread - thread->tnum; tmp_thread != thread; thread++)  {
      pthread_mutex_lock(&tmp_thread->stmutex);
      if ( tmp_thread->tstate != THREAD_STATE_NEW  && tmp_thread->tstate != THREAD_STATE_FREE && tmp_thread->tstate != THREAD_STATE_END) 
	{
	  number[tmp_thread->tnum] = tmp_thread->tnum;
	  list_add_last_item(clist, &number[tmp_thread->tnum]);
	}
      pthread_mutex_unlock(&tmp_thread->stmutex);
    }

  // Now, 'tmp_thread' is equals 'thread' so we put  
  // 'thread' client id at the beguinning of 'clist'.
  number[tmp_thread->tnum] = tmp_thread->tnum;
  list_add_first_item(clist, &number[tmp_thread->tnum]);


  // If 'thread' was not the last thread, we need to move one and
  // continue adding the client ids until we reach the last thread
  if ( tmp_thread->tnum != MAX_CLIENTS - 1 ) {
      // move one as 'tmp_thread' have been treated above
      tmp_thread++;
      for(i = tmp_thread->tnum; i < MAX_CLIENTS; i++) {
	  pthread_mutex_lock(&tmp_thread[i].stmutex);
	  if ( tmp_thread[i].tstate != THREAD_STATE_NEW  && tmp_thread[i].tstate != THREAD_STATE_FREE && tmp_thread[i].tstate != THREAD_STATE_END) {
	      number[i] = i;
	      list_add_last_item(clist, &number[i]);
	    }
	  pthread_mutex_unlock(&tmp_thread[i].stmutex);
	}
   }
  
    // if 'tmp_thread' was the last thread, then we are done. Return the list. 
  return clist;
}
*/


/**
 * helper function to set the thread state using mutexes
 */
void set_tstate(tdata_t *thread, thread_state_t state) {
  pthread_mutex_lock(&thread->stmutex);
  thread->tstate = state;
  pthread_mutex_unlock(&thread->stmutex);
}


/**
 *
 */
void data_init_thread(tdata_t *thread, int i) {
  thread->client = NULL;
  thread->tid    = (pthread_t) 0;
  thread->tnum   = i;
  thread->tstate = THREAD_STATE_NEW;

  data_init_pipes(thread);

  /* TODO: review this function */
  THREAD_get_error(pthread_mutex_init(&thread->stmutex, NULL), FATAL, i);
}


/**
 *
 */
int THREAD_get_error(int value, int severity, int tid) {
  char buff[256];
  int rc = 0;
  switch(value) {

  case EBUSY:
    sprintf(buff, "<thread %d>[ERR]:\tEBUSY\n", tid);
    if ( severity == FATAL)
      exit(THREAD_ERR_EBUSY);
    break;

  case EINVAL:
    sprintf(buff, "<thread %d>[ERR]:\tEINVAL\n", tid);
    if ( severity == FATAL )
      exit(THREAD_ERR_EINVAL);
    break;

  case ENOMEM:
    sprintf(buff, "<thread %d>[ERR]:\tENOMEM\n", tid);
    if ( severity == FATAL)
      exit(THREAD_ERR_ENOMEM);
    break;

  case EPERM:
    sprintf(buff, "<thread %d>[ERR]:\tEPERM\n", tid);
    if (severity == FATAL)
      exit(THREAD_ERR_EPERM);
    break;

  case EAGAIN:
    sprintf(buff, "<thread %d>[ERR]:\tEAGAIN\n", tid);
    if (severity == FATAL)
      exit(THREAD_ERR_EAGAIN);
    break;
    
  default:
    return rc;
  }
  print_output(buff);
  return rc;
}



/**
 * Creates, sets and binds a socket to be ready for using.
 * No error checking is needed on calling this function as it
 * exits the main program. No socket created, no server running.
 */
int 
start_server(int port, struct sockaddr_in *addr) 
{   
  int sd = 0;
  int val = 1;
  if ((sd = socket( AF_INET, SOCK_STREAM, 0 )) == -1) 
    {
      perror("Server Initialization error: cannot create socket().\n");
      exit(1);
    }
  
  if (setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &val, sizeof(int)) == -1) 
    {
      perror("Server Socket Options: error.\n");
      close(sd);
      exit(1);
    }

  /* set addr memory to zero */
  memset(addr, 0, sizeof(*addr));
  
  /* set addr struct */
  addr->sin_family = AF_INET;
  addr->sin_port   = htons(port) ;
  addr->sin_addr.s_addr = htonl(INADDR_ANY);

  /* bind socket file descriptor to sctructure */
  if (bind(sd, (struct sockaddr *)addr, sizeof(*addr)) != 0) 
    {
      perror("Server Initialization error: can't bind() port.\n");
      close(sd);
      abort();
    }

  if (listen(sd, 10) != 0) 
    {
      perror("Server Initialization error: cannot listen().\n");
      close(sd);
      abort();
    }

  //printf("[NFO]:\tServer Initialization: OK...\n");

  return sd;
}



/**
 *
 *
void clccountinc() {
  pthread_mutex_lock(&clcmutex);
  clccount++;
  cm++;
  pthread_mutex_unlock(&clcmutex);
}
*/


/**
 *
 *
void clccountdec() {
  pthread_mutex_lock(&clcmutex);
  clccount--;
  pthread_mutex_unlock(&clcmutex);
}
*/


/**
/* thread_abort():
 * ------------
 * aborts the server initialization freeing up used memory.
 */
void thread_abort() {
  printf("[FATAL]: Could not start server.\n");
  CONTINUE = 0;
  abort();
}



/**
 * signal_handler(int sig):
 * --------------
 * Handle signals
 */
void signal_handler(int sig) 
{
  switch(sig) {
  case SIGINT:
#ifdef CDK_AWARE
    if (console != NULL && !console_isenabled(console))
      console_enable(console);
#endif
    break;

  case SIGPIPE:
    break;

  case SIGQUIT:
    close(args.svfd);
    break;
  } 
}



/**
 * args_check(int argc, char *argv[]):
 * ----------
 * Checks programs arguments.
 */
int
args_check(int argc, char *argv[]) 
{
  FILE      *file = NULL;

  /* Check if the program was called correctly */
  if (argc != 3) 
    {
      printf("Usage:\n ./server <port> <path-to-file> \n");
      return 0;
    }
  else 
    {
      /* Port number must be higher thanb 1024 */
      if (atoi(argv[PORT_ARG]) < 1024) 
	{
	  printf("Invalid port number. Must be higher than 1024\n");
	  return INVALID_PORT;
	}
      if ((file = fopen(argv[FILE_ARG], "r")) == NULL) 
	{
	  printf("Failed to open file: %s\n",argv[FILE_ARG]);
	  return FILE_ERR;
	}
      // for(i = 0; argv[3][i] != '\0'; i++);
      /* if ( i != SHA_DIGEST_LENGTH * 2) { */
      /*   printf("Password must be encripted 160-bits long (sha1sum).\n"); */
      /*   //return PWD_LEN_ERR; */
      /*   return 0; */
      /* } */
  }

  /* at this point args seems to be ok */
  args.port = atoi(argv[PORT_ARG]);  
  args.image = file;
  fseek(file, 0L, SEEK_END);
  args.filesize = ftell(file);
  rewind(file);
  if (args.filesize > powl(2, 32))
    {
      print_output("File size to large.");
      return INVALID_FILE;
    }
  
  printf("Password: ");
  fflush(stdout);
  args.pwdlen = read(fileno(stdin), args.pwd, MAX_PWD_SIZE) - 1;
  printf("%d\n", args.pwdlen);
  return 1;
}


/**
 *
 */
ssize_t
my_getpass (void *pwd)
{
  struct termios old, new;
  int nread;
     
  /* Turn echoing off and fail if we can't. */
  if (tcgetattr (fileno (stdin), &old) != 0)
    return -1;
  new = old;
  new.c_lflag &= ~ECHO;
  if (tcsetattr (fileno (stdin), TCSAFLUSH, &new) != 0)
    return -1;
     
  /* Read the password. */
  nread = read(fileno(stdin), pwd, MAX_PWD_SIZE);

  //nread = getline (lineptr, n, stream);
     
  /* Restore terminal. */
  (void) tcsetattr (fileno (stdin), TCSAFLUSH, &old);
     
  return nread;
}


/**
 * int new_client_thread(tdata_t *thread, int clfd, struct sockaddr_in clientaddr)
 * ------------------------------------------------------------------------
 *
 * It allocates memory on 'thread' to hold a client structure. Then, init
 * all data to build a client node with an ssl connection.
 *
 * Returns 1 if successful and 0 if not.
 */
int new_client_thread(tdata_t *thread, int clfd, struct sockaddr_in clientaddr) {
  thread->client = (client_t *) malloc(sizeof(client_t));

  if ( thread->client == NULL ) return 0;

  thread->client->clidx   = thread->tnum;
  thread->client->socket.s_socket = clfd;
  thread->client->address = clientaddr;
  thread->client->name    = NULL;
  return 1;
}


/**
 *
 */
void data_init_pipes(tdata_t *thread) {
  int pipes[2];
  //TODO: Error checking
  pipe(pipes);
  thread->readpipe  = pipes[0];
  thread->writepipe = pipes[1];
}


#ifdef CDK_AWARE
/**
 *
 */
void help () {
  char *mesg[25];

  /* Create the help message. */
  mesg[0] = "<C></B/29>Help";
  mesg[1] = "";
  mesg[2] = "</B/24>When in the command line.";
  mesg[3] = "<B=threads     > Displays the list of active threads.";
  mesg[4] = "<B=info {tnum} > Shows <tnum> thread information.";
  mesg[5] = "<B=msglist     > Displays the message queue (addresses).";
  mesg[6] = "<B=help        > Shows this help.";
  mesg[7] = "<B=Tab         > Activates the scrolling window.";
  mesg[8] = "<B=help        > Displays this help window.";
  mesg[9] = "";
  mesg[10] = "</B/24>When in the scrolling window.";
  mesg[11] = "<B=l or L    > Loads a file into the window.";
  mesg[12] = "<B=s or S    > Saves the contents of the window to a file.";
  mesg[13] = "<B=Up Arrow  > Scrolls up one line.";
  mesg[14] = "<B=Down Arrow> Scrolls down one line.";
  mesg[15] = "<B=Page Up   > Scrolls back one page.";
  mesg[16] = "<B=Page Down > Scrolls forward one page.";
  mesg[17] = "<B=Tab/Escape> Returns to the command line.";
  mesg[18] = "";
  mesg[19] = "<C> (</B/24>Refer to the scrolling window online manual for more help<!B!24>.)";
  pthread_mutex_lock(&console->mutex);
  popupLabel (ScreenOf(console->entry), mesg, 20);
  pthread_mutex_unlock(&console->mutex);
}


/**
 *
 */
void nocmd() {
  char *msg[8];
  msg[0] = "<C></B/29>Commad";
  msg[1] = "";
  msg[2] = "</B>command not found. Type </24>help<!24> to get a list of commands";

  pthread_mutex_lock(&console->mutex);
  popupLabel (ScreenOf(console->entry), msg, 3);
  pthread_mutex_unlock(&console->mutex);
}
#endif

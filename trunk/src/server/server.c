/*
 *   Copyright (c) 2010 Sebastián Treu
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
#include <openssl/sha.h>
#include <ncurses.h>

#include "console.h"
#include "server.h"
#include "types.h"
#include "constants.h"
#include "common.h"
#include "tellapic.h"

MUTEX_TYPE         *mutex_buf; //this is initialized by THREAD__setup();
MUTEX_TYPE         clcmutex = PTHREAD_MUTEX_INITIALIZER;
int                clccount = 0; //shared
uint32_t           ccbitset = 0; //connected clients bit set
MUTEX_TYPE         ccbitsetmutex = PTHREAD_MUTEX_INITIALIZER;
list_t             *msgqueue;
MUTEX_TYPE         queuemutex = PTHREAD_MUTEX_INITIALIZER;
int                CONTINUE = 1; //this NEEDS to be global as the handler interrupt manages program execution
args_t             args;

console_t          *console;

/*************** JUST FOR TESTING *************/
MUTEX_TYPE         cmmutex = PTHREAD_MUTEX_INITIALIZER;
MUTEX_TYPE         camutex = PTHREAD_MUTEX_INITIALIZER;
unsigned long      cm = 0;
unsigned long      ca = 0;
/**********************************************/

//TEMPORAL
void fetch_stream(tdata_t *thread);
void forward_stream(tdata_t *thread);
void queue_message(mitem_t *item, tdata_t *thread);
void unqueue_message(mitem_t *item);
int find_free_thread(tdata_t thread_data[]);
void *console_thread(void *a);
void print_output(char *msg);
void process_command(char *cmd, tdata_t *thread_data);
void help();
void nocmd();
void send_pwdok(client_t *client);

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
  if (args_check(argc, argv) != 1)                                         /* check program arguments and fill args_t structure in args */
    exit(1);

  args.svfd = start_server(atoi(argv[1]), &serveraddr);                    /* Prepare the server for listen */
  for(i = 0; i < MAX_CLIENTS; i++)                                         /* Initialize main data structures and global variables */
    data_init_thread(&thread_data[i], i);
  pthread_attr_init(&joinattr);                                            /* initialize and create a thread joinable attribute */
  pthread_attr_setdetachstate(&joinattr, PTHREAD_CREATE_JOINABLE); 
  msgqueue = list_make_empty(msgqueue);
  thread_data[SV_THREAD].tstate = THREAD_STATE_ACT;                        /* set server thread state as active */
  setnonblock(args.svfd);
  console = console_create("<C>Server output\0", "</B/24> >>> \0");
  if (console == NULL)
    print_output("Could not start console\n");

  fd_set readset;
  fd_set copyset;
  int fdmax = MAX(args.svfd, console->infd);
  FD_ZERO(&readset);
  FD_SET(args.svfd, &readset);
  FD_SET(console->infd, &readset);

  while(CONTINUE) {
    copyset = readset;
    int notfound = 1;
    int addrlen  = sizeof(clientaddr);
      
    /* TODO: add some kind of verbose level to the command line */
    //write(console->outfd, "<server>[NFO]:\tWaiting for incoming connections", 256);
    print_output("[NFO]:\tWaiting for incoming connections");

    rv = pselect(fdmax + 1, &copyset, NULL, NULL, NULL, &sig_action.sa_mask);

    if (rv < 0) {
      //write(console->outfd, "<server>[ERR]:\tSelect error:", 256);
      print_output("</K/16>[ERR]:\tSelect error.<!K!16>");
      CONTINUE = 0;
    }
    else {

      if (FD_ISSET(args.svfd, &copyset)) {
	if ((clfd = accept(args.svfd, (struct sockaddr *)&clientaddr, &addrlen)) == -1) {
	  print_output("</B>[WRN]:<!B>\tConnection refused.");
	} else {
	  //ca++;  //JUST FOR TESTING PURPOSES
	  char buff[256];
	  sprintf(buff, "[NFO]:\tConnection attempt from %s.", inet_ntoa(clientaddr.sin_addr));
	  print_output(buff);

	  int tnum = find_free_thread(thread_data);

	  if (tnum != -1) {
	    tnum--;
	    if (new_client_thread(&thread_data[tnum], clfd, clientaddr)) {
	      rv = pthread_create(&thread_data[tnum].tid, &joinattr, manage_client, &thread_data[tnum]);
	      if (THREAD_get_error(rv, WARN, tnum)) {
		close(thread_data[tnum].client->fd);
		free(thread_data[tnum].client);
		set_tstate(&thread_data[tnum], THREAD_STATE_FREE);
	      }
	    } else {
	      set_tstate(&thread_data[tnum], THREAD_STATE_FREE);
	      close(clfd);
	      print_output("</B>[WRN]:<!B>\tClient node allocation falied!");
	    }
	  } else {
	    print_output("</B>[WRN]:<!B>\tServer reached full capacity.");
	    close(clfd);
	  }
	}
      }

      if (console_isenabled(console) && FD_ISSET(console->infd, &copyset)) { 
	read(console->infd, &cMsgBuf, cMsgBufLen);
	if (strcmp(cMsgBuf, "q") == 0 || strcmp(cMsgBuf, "quit") == 0) {
	  FD_CLR(console->infd, &readset);
	  console_destroy(console);
	  CONTINUE = 0;
	  close(args.svfd);
	} else {
	  process_command(cMsgBuf, thread_data);
	}
	memset(cMsgBuf, 0, 256);
      }
    }
  }
  char buff[256];
  memset(buff, 0, 256);
  sprintf(buff, "[NFO]:\tServer is going down...");
  print_output(buff);

  pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL); /* set the cancel state of this thread */
  pthread_setcanceltype(PTHREAD_CANCEL_ENABLE, NULL); /* set the cancel state of this thread */

  for( i = 0; i < MAX_CLIENTS; i++) {
    if ( thread_data[i].tstate != THREAD_STATE_NEW) {
      //wprintw(mainscr, "<server>[NFO]:\tSending cancel signal to thread with pid %ld and number %d\n", thread_data[i].tid, thread_data[i].tnum);
      pthread_cancel(thread_data[i].tid);
      pthread_detach(thread_data[i].tid);
    }
  }

  pthread_attr_destroy(&joinattr);

  if (console != NULL && console_isenabled(console))
    console_destroy(console);

  print_output("[NFO]:\tCurses unloaded.");

  /****************************************************/
  /********** TESTING PURPOSES ************************/
  /****************************************************/
  /*
  wprintw(mainscr, "\nserver exits correctly with a total of:\n");
  wrefresh(mainscr);
  wprintw(mainscr, "\n\tConnection attemps: %ld\n",ca);
  wrefresh(mainscr);
  wprintw(mainscr, "\tConnections made: %ld\n", cm);
  wrefresh(mainscr);

  */
  
  list_node_t *node = NULL;
  for (node = list_get_head(msgqueue); node != NULL; node = list_get_next(node)) {
    unqueue_message(node->item);
  }
  list_free(msgqueue);
  free(msgqueue);
  free(args.pwd);
  fclose(args.image);
  print_output("[NFO]:\tServer exits normally");
  pthread_exit(NULL);
}



/**
 *
 */
void process_command(char *cmd, tdata_t *thread_data) {
  char *args[20];
  char buff[256];
  int n;
  int i = 0, j = 0;
  memset(args, 0, 20);
  memset(buff, 0, 256);

  // snippet stolen from internet to avoid using strtok()
  while (*cmd != '\0') {
    args[i] = malloc(sizeof(char) * 20);
    int items_read = sscanf(cmd, "%31[^ ]%n", args[i], &n);
    i++;
    if (items_read == 1)
      cmd += n; /* advance the pointer by the number of characters read */
    if ( *cmd != ' ' ) {
      break; /* didn't find an expected delimiter, done? */
    }
    ++cmd; /* skip the delimiter */
  }

  // Command is tokenized in args array.
  if(strcmp(args[0], "help") == 0 && args[1] == NULL) {
    help();
    free(args[0]);

  } else if (strcmp(args[0], "threads") == 0 && args[1] == NULL) {
    int i = 0;
    pthread_mutex_lock(&ccbitsetmutex);
    for(i; i < MAX_CLIENTS; i++) {
      if (ccbitset & (1 << i)) {
	sprintf(buff, "command output: </B>Thread </24>%d<!24> is alive<!B>", i);
	print_output(buff);
      }
    }
    pthread_mutex_unlock(&ccbitsetmutex);
    sprintf(buff, "command output: </B>You can type </24>info <thread-number><!24> to get a more detailed information.");
    print_output(buff);
    free(args[0]);

  } else if (strcmp(args[0], "msglist") == 0) {
    list_node_t *node = NULL;
    int i = 0;
    pthread_mutex_lock(&queuemutex);
    for(node = list_get_head(msgqueue); node != NULL; node = list_get_next(node)) {
      sprintf(buff, "command output: </B>MessageList[%d] -> %p<!B>", i++, list_get_item(node));
      print_output(buff);
    }
    pthread_mutex_unlock(&queuemutex);

    if (i == 0) {
      memset(buff, 0, 256);
      sprintf(buff, "command output: </B>MessageList is empty!<!B>");
      print_output(buff);
    }
    free(args[0]);

  } else if (strcmp(args[0], "info") == 0) {
    if (args[1] != NULL) {
      int tnum = strtol(args[1], NULL, 10);
      if (tnum < MAX_CLIENTS && tnum >= 0) {
	sprintf(buff, "command output: </B>Thread </24>%d<!24> status: </24>%s<!24>", tnum, tstatus[thread_data[tnum].tstate]);
	print_output(buff);
	pthread_mutex_lock(&ccbitsetmutex);
	if (ccbitset & (1 << tnum)) {
	  char str[INET_ADDRSTRLEN];
	  inet_ntop(AF_INET, &(thread_data[tnum].client->address.sin_addr), str, INET_ADDRSTRLEN);
	  sprintf(buff, "command output: </B>Thread </24>%d<!24> socket number: </24>%d<!24>", tnum, thread_data[tnum].client->fd);
	  print_output(buff);
	  sprintf(buff, "command output: </B>Thread </24>%d<!24> client name: </24>%s<!24>", tnum, thread_data[tnum].client->name);
	  print_output(buff);
	  sprintf(buff, "command output: </B>Thread </24>%d<!24> internet address: </24>%s<!24>", tnum, str);
	  print_output(buff);
	} else {
	  sprintf(buff, "command output: </B>Thread </24>%d<!24> is not connected to any client.", tnum);
	  print_output(buff);
	}
	pthread_mutex_unlock(&ccbitsetmutex);
	free(args[1]);
	free(args[0]);
      }
    }

  } else if (strcmp(args[0], "close") == 0) {
    free(args[0]);

  } else if (strcmp(args[0], "examine") == 0) {
    free(args[0]);

  } else {
    for(j=0; j < i; j++)
      free(args[j]);
    nocmd();
  }
}


void print_output(char *msg) {
  if (console != NULL && console_isenabled(console)) {
    write(console->scrollwindow->outfd, msg, 256);
  } else {
    printf("TEST %s\n", msg);
  }
}

/**
 *
 */
int find_free_thread(tdata_t thread_data[]) {
  int notfound = 1;
  int i;
  char buff[256];
  for(i = 0; i < MAX_CLIENTS && notfound; i++) {
    /* sprintf(buff, "[NFO]:\tsearching place on %d and locking", i); */
    /* print_output(buff); */
    /* memset(buff, '\0', 256); */
    pthread_mutex_lock(&thread_data[i].stmutex);
    switch(thread_data[i].tstate) {
    case THREAD_STATE_NEW:
      /* sprintf(buff, "[NFO]:\tthread %d was never used", i); */
      /* print_output(buff); */
      /* memset(buff, '\0', 256); */
      thread_data[i].tstate = THREAD_STATE_INIT;
      notfound = 0;
      break;

    case THREAD_STATE_FREE:
      /* sprintf(buff, "[NFO]:\tthread %d was used.", i); */
      /* print_output(buff); */
      /* memset(buff, '\0', 256); */
      pthread_join(thread_data[i].tid, NULL);
      thread_data[i].tstate = THREAD_STATE_INIT;
      notfound = 0;
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
void *manage_client(void *arg) {


  char      address[INET_ADDRSTRLEN];          /* this will hold the IP address                   */
  tdata_t   *thread = (tdata_t *) arg;         /* a thread data structure                         */
  int       fdmax   = thread->client->fd + 1;  /* max file descriptor number to be used by select */
  fd_set    readfdset;                         /* this will hold client->fd                       */
  fd_set    fdsetcopy;                         /* this is a required copy                         */



  pthread_cleanup_push(manage_client_cleanup, thread);                                /* register cleanup function for this thread */
  set_tstate(thread, THREAD_STATE_INIT);                                              /* set the thread state to INIT */
  inet_ntop(AF_INET, &(thread->client->address.sin_addr), address, INET_ADDRSTRLEN);  /* copy the client IPV6 address */
  


  if (DEBUG) {
    char      buff[256];
    sprintf(buff, "[NFO]:\tConnection attempt from: </B/24>%s<!B!24>. Asigned socket number </B/24>%d<!B!24> on thread number %d",address,  thread->client->fd, thread->tnum);
    print_output(buff);
  }



  if(authclient(thread->client) == 0) {
    char buff[256];
    sprintf(buff, "</B/16>[ERR]:\tCould not auth with client. Closing link on thread number %d.<!B!16>", thread->tnum);
    print_output(buff);
    pthread_exit(NULL);
  }


  /* TODO: send to connected clients this client id and name */
  mitem_t *msg = malloc(sizeof(mitem_t));
  if (msg == NULL) {
    // abort
  }
  msg->stream   = tellapic_build_ctle(CTL_SV_CLADD, thread->client->fd, thread->client->namelen, thread->client->name);
  msg->tothread = -1; //TODO: #define or something here...
  msg->delivers = 0;
  pthread_mutex_init(&msg->mutex, NULL);
  queue_message(msg, thread);


  FD_ZERO(&readfdset);                     /* initialize file descriptor set */
  FD_SET(thread->client->fd, &readfdset);  /* add client socket to the set   */
  FD_SET(thread->readpipe, &readfdset);    /* add the pipe-event to the set  */


  if (DEBUG) {
    char buff[256];
    sprintf(buff, "[NFO]:\tClient connected from </B/24>%s<!B!24> to thread number </B/24>%d<!B!24> on socket number </B/24>%d<!B!24>\n", address, thread->tnum, thread->client->fd);
    print_output(buff);
  }



  pthread_mutex_lock(&ccbitsetmutex);                  /* Lock the shared resource   */
  ccbitset |= (1 << thread->tnum);                     /* Set the bit flag indicating this client thread number connection */
  pthread_mutex_unlock(&ccbitsetmutex);                /* Unlock the shared resource */
  set_tstate(thread, THREAD_STATE_ACT);                /* set the thread state to INIT */
  pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL); /* set the cancel state of this thread */



  if (DEBUG) {
    char buff[256];
    sprintf(buff, "[NFO]:\tMain attendance loop start on thread %d", thread->tnum);
    print_output(buff);
  }



  while(thread->tstate != THREAD_STATE_END) {
    fdsetcopy = readfdset;   /* Maintain a copy of the file descriptor set */

    
    if (DEBUG) {
      char buff[256];
      sprintf(buff, "[NFO]:\tEntering select() on thread %d", thread->tnum);
      print_output(buff);
    }


    int rv = select(fdmax, &fdsetcopy, NULL, NULL, NULL);


    if (rv < 0) {
      //printw("rv: %d errno: %d\n", rv, errno);
      //refresh();
    } else {

      if (DEBUG) {
	char buff[256];
	sprintf(buff, "[NFO]:\tEXIT Select on thread %d", thread->tnum);
	print_output(buff);
      }


      // Check if select() has set the socket ready for reading
      if (FD_ISSET(thread->client->fd, &fdsetcopy))
	fetch_stream(thread);


      // Check if select() has set the pipe ready for reading. If so, forward to this 
      // client whatever is in the message queue.
      if (FD_ISSET(thread->readpipe, &fdsetcopy))
	forward_stream(thread);
    }
  } 

  pthread_mutex_lock(&ccbitsetmutex);    /* Lock the shared resource   */
  ccbitset &= ~(1 << thread->tnum);      /* Set the bit flag without this client flag */
  pthread_mutex_unlock(&ccbitsetmutex);  /* Unlock the shared resource */
  pthread_cleanup_pop(1);                /* pop the cleanup function */

  if (DEBUG) {
    char buff[256];
    sprintf(buff, "[NFO]:\tThread %d dying...", thread->tnum);    /* this thread has no more reason to live */
    print_output(buff);
  }

  pthread_exit(NULL);
}



/**
 *
 */
int
authclient(client_t *client) 
{
  int      newpwdlen = 0;
  char     *pwd  = NULL;
  int      pwdok = 0;
  int      try   = 0;
  
  // The server is the first to talk. It sends the client id. (fd)
  int rc = tellapic_send_ctl(client->fd, client->fd, CTL_SV_ID);
  if (rc <= 0)
    return pwdok;
  
  while(!pwdok && try < MAX_PWD_TRIES) 
    {
      //stream_t stream = tellapic_read_stream_b(client->fd);
      //if (stream.header.cbyte == CTL_CL_PWD && stream.data.control.idfrom == client->fd) {

      pwd = tellapic_read_pwd(client->fd, pwd, &newpwdlen);
      if (pwd != NULL)
	{
	  // Compare password sent here
	  pwdok = (strncmp(args.pwd, hexastr2binary(pwd), newpwdlen) == 0);
  
	  if (pwdok) 
	    {
	  
	      //send_pwdok(client);                                    
	      tellapic_send_ctl(client->fd, client->fd, CTL_SV_PWDOK);                                	 /* Send CTL_SV_PWDOK to client */
	      stream_t stream = tellapic_read_stream_b(client->fd); 	                                 /* Read client response        */

	      if (stream.header.cbyte == CTL_CL_NAME && stream.data.control.idfrom == client->fd) {     
	  
		// TODO: check that name is not already used!
		client->name = malloc(stream.header.ssize - HEADER_SIZE + 1);	                             /* Allocate memory for client name  */
		client->namelen = stream.header.ssize - HEADER_SIZE;
		memcpy(client->name, stream.data.control.info, stream.header.ssize - HEADER_SIZE);      /* Copy the client name             */
		client->name[stream.header.ssize - HEADER_SIZE] = '\0';                                      /* Add a trailing '\0' just in case */

	    } else return 0;       /* If it isn't a CTL_CL_NAME packet is an invalid sequence */

	  } else try++;

	} else try = MAX_PWD_TRIES;  /* it's not a valid sequence so, disconnect the client */
      
    }

  return pwdok;
}


/**
 *
 */
void forward_stream(tdata_t *thread) {
  mitem_t   *message    = NULL;
  void      *msgaddress = NULL;
  int       result      = 0;

  read(thread->readpipe, &msgaddress, sizeof(msgaddress));            /* Read the message address and clear the pipe */
  message = (mitem_t *) msgaddress;                                   /* Cast to mitem_t*  */
  result  = tellapic_send(thread->client->fd, &message->stream);      /* Forward data to this client */

  if (result == message->stream.header.ssize) {

    pthread_mutex_lock(&message->mutex);
    message->delivers--;

    if (message->delivers == 0)
      unqueue_message(message);

    pthread_mutex_unlock(&message->mutex);

    if (DEBUG) {
      char buff[256];
      sprintf(buff, "[NFO]:\tForwarding ok on thread %d", thread->tnum);
      print_output(buff);
    }

  }
}


/**
 *
 */
void fetch_stream(tdata_t *thread) {
  mitem_t      *item = NULL;
  stream_t     stream;

  stream = tellapic_read_stream_b(thread->client->fd);

  if (stream.header.cbyte != CTL_FAIL) {

    item = malloc(sizeof(mitem_t));
    if (item == NULL)
      return;

    pthread_mutex_init(&item->mutex, NULL);
    item->delivers = 0;
    item->stream   = stream;
    item->tothread = -1;       /* Set -1 by default so we simplified the switch */

    switch(stream.header.cbyte) {

    case CTL_CL_PMSG:
      item->tothread = (1 << stream.data.chat.type.private.idto);
    case CTL_CL_BMSG:
    case CTL_CL_FIG:
      queue_message(item, thread);
      break;

    case CTL_CL_DRW:
      break;

    case CTL_CL_FILEASK:
      //send_file(thread->client->fd); as CTL_CL_DISC but with item->tothread = (1 << stream.data.type.control.idfrom);
      break;


    case CTL_CL_DISC:
      item->stream.header.endian = 0;
      item->stream.header.cbyte = CTL_SV_CLRM;
      item->stream.header.ssize = HEADER_SIZE + 1;
      item->stream.data.control.idfrom = thread->client->fd;
      queue_message(item, thread);
      thread->tstate = THREAD_STATE_END;
      break;

    default:
      // it should be a valid header. Who check this? tellapic.so? we?
      pthread_mutex_destroy(&item->mutex);
      free(item);
      break;
    }
  } else {
    //printw("<thread %d>[ERR]:\tStream read FAIL! (%d)\n", thread->tnum, stream.header.cbyte);
    //refresh();
  }
}


/**
 *
 */
void send_clientname(client_t *client) {

}


/**
 *
 *
int send_clientid(int id) {
  stream_t idstream;

  idstream.header.endian = 0;
  idstream.header.cbyte  = CTL_SV_ID;
  idstream.header.ssize  = HEADER_SIZE + 1;
  idstream.data.type.control.idfrom = id;

  int bytessent = tellapic_send_data(id, &idstream);

  if (DEBUG) {
    char buff[256];
    sprintf(buff, "[NFO]:\tThread sending id %d. Bytes sent: %d.", id, bytessent);
    print_output(buff);
  }
  return (bytessent == idstream.header.ssize);
}
*/

/**
 * helper function
 */
void queue_message(mitem_t *msg, tdata_t *thread) {
  int alone = 0;

  pthread_mutex_lock(&ccbitsetmutex);
  alone = (ccbitset | (1 << thread->tnum)) == (1 << thread->tnum);   // Only queue the item if we (the thread) are not alone.
  pthread_mutex_unlock(&ccbitsetmutex);

  if (alone) {
    // Destroy the message if we are alone in the server and leave.
    pthread_mutex_destroy(&msg->mutex);
    free(msg);
    return;

  } else {
    // Queue the message and signal other threads
    pthread_mutex_lock(&queuemutex);
    list_add_last_item(msgqueue, msg);
    pthread_mutex_unlock(&queuemutex);
  }

  int    i;
  int    current  = thread->tnum;    // We are going to use local data to access the whole tdata_t structure using pointer arithmetics.
  void   *address = msg;

  // Who told you that C won't accept negative indexes such as a[-2]?
  for(i = 0 - current; i < MAX_CLIENTS - current; i++) {
    // Lock the connected clients bit flag. 
    pthread_mutex_lock(&ccbitsetmutex);

    // If thread[i] is connected with a client and this message is private, ensure that is for him. 
    // If it isn't for him, then do nothing. Else, if it isn't private, send it anyway.
    if (ccbitset & (1 << thread[i].tnum) && (msg->tothread == -1 || msg->tothread == thread[i].tnum) && current != thread[i].tnum) {

      // msg is sent to N threads. When msg->delivers reachs 0, means that all threads have processed the message msg. If
      // a thread is somehow blocked forever, this msg will not ever be processed. But, when the server is shutted down, it
      // will removed from the queue in the resource free section. If a thread disconnects a client before the msg address
      // arrives at his pipe, the thread cleanup procedure SHOULD decrement msg->delivers field.
      pthread_mutex_lock(&msg->mutex);
      msg->delivers++;
      pthread_mutex_unlock(&msg->mutex);

      if (DEBUG) {
	char   buf[256];
	sprintf(buf, "Sending msg address </B>%p<!B> to thread </B>%d<!B>", address, thread[i].tnum);
	print_output(buf);
      }
      
      // We are sending a pointer address to the thread i. So, this queue behaves as an array with direct access to it.
      // The thread i will read the pipe, will cast the void* to mitem_t* and it will deliver the message to the client
      // that is connected to.
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
  free(msg);
  free(node);
}


/**
 *
 */
void manage_client_cleanup(void *arg) {
  tdata_t *thread = (tdata_t *) arg;

  if (thread->client->name != NULL)
    free(thread->client->name);

  close(thread->client->fd);
  free(thread->client);

  /* set this state to free */
  set_tstate(thread, THREAD_STATE_FREE);
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
int start_server(int port, struct sockaddr_in *addr) {   
  int sd = 0;
  int val = 1;
  if ((sd = socket( AF_INET, SOCK_STREAM, 0 )) == -1) {
    perror("Server Initialization error: cannot create socket().\n");
    exit(1);
  }
  
  if (setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &val, sizeof(int)) == -1) {
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
  if (bind(sd, (struct sockaddr *)addr, sizeof(*addr)) != 0) {
    perror("Server Initialization error: can't bind() port.\n");
    close(sd);
    abort();
  }

  if (listen(sd, 10) != 0) {
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
 *
 *
int check_pwd(void *arg) {
  // arg is a byte_t pointer and should be verified before with STREAM_is_stream()
  static int    pwdf = 0;
  const byte_t  *pwd = STREAM_get_pwd((byte_t *) arg);

  if (memcmp(args.pwd, pwd, STREAM_PWD_SIZE)) {
      pwdf++;
      if ( pwdf == MAX_PWD_TRIES )
	return SEQ_CANT_CONTINUE;
      else
	return SEQ_NEEDS_RETRY;
  } else
    return SEQ_OK;
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
 * Handle singals
 */
void signal_handler(int sig) {
  switch(sig) {
  case SIGINT:
    if (console != NULL && !console_isenabled(console))
      console_enable(console);
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
int args_check(int argc, char *argv[]) {
  FILE      *file = NULL;
  int       i     = 0;

  /* Check if the program was called correctly */
  if (argc != 4) {
      printf("Usage:\n ./server <port> <path-to-file> <sha1sum-password>\n");
      return 0;
  }
  else {
    /* Port number must be higher thanb 1024 */
    if (atoi(argv[1]) < 1024) {
      printf("Invalid port number. Must be higher than 1024\n");
      //return INVALID_PORT;
      return 0;
    }
    if ((file = fopen(argv[2], "r")) == NULL) {
      printf("Failed to open file: %s\n",argv[2]);
      //return FILE_ERR;
      return 0;
    }
    for(i = 0; argv[3][i] != '\0'; i++);
    if ( i != SHA_DIGEST_LENGTH * 2) {
      printf("Password must be encripted 160-bits long (sha1sum).\n");
      //return PWD_LEN_ERR;
      return 0;
    }
  }

  /* at this point args are ok */
  args.port = atoi(argv[1]);  
  args.pwd = hexastr2binary(argv[3]);
  args.image = file;

  return 1;
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
  thread->client->fd      = clfd;
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

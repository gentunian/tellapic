#ifndef _CONSOLE_H_
#define _CONSOLE_H_

#include <cdk/cdk.h>

#define MAX_HISTORY     2048

typedef enum { CONSOLE_DISABLED, CONSOLE_ENABLED } cstate_t;

typedef struct scrollwindow {
  CDKSWINDOW  *swindow;
  char        *title;
  int         infd;
  int         outfd;

} swindow_t;


typedef struct contype {
  CDKSCREEN        *screen;
  CDKENTRY         *entry;
  WINDOW           *mwindow;
  swindow_t        *scrollwindow;
  cstate_t         state;
  char             *cmd[MAX_HISTORY];
  int              cmdlen;
  char             *prompt;
  int              promptattr;
  int              promptlen;
  int              histcount;
  int              infd;
  int              outfd;
  pthread_mutex_t  mutex;

} console_t;



console_t *console_create(char *title, char *prompt);
int console_disable(console_t *console);
int console_enable(console_t *console);
int console_isenabled(console_t *console);

#endif
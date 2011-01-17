/**
 *   Copyright (c) 2010 Sebastián Treu.
 *
 *   This file is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 */


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



console_t *
console_create(char *title, char *prompt);

int
console_disable(console_t *console);

int
console_enable(console_t *console);

int
console_isenabled(console_t *console);

#endif

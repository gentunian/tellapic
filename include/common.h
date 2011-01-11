/**
 *   Copyright (c) 2010 Sebastián Treu.
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


#ifndef _COMMON_H_
#define _COMMON_H_

#define MMAX(a, b) a > b? a:b;

int                   setnonblock(int fd);
unsigned char         *hexastr2binary(unsigned char *sha1hexadigest);
int                   htoi(unsigned char hexdigit);


#endif

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

#include <stdlib.h>
#include "list.h"


int list_get_length(list_t *listPtr)
{
  return listPtr->count;
}


list_t *list_make_empty(list_t *listPtr)
{
  listPtr = (list_t *) malloc(sizeof(list_t));
  
  listPtr->count  = 0;
  listPtr->head   = NULL;
  listPtr->tail   = NULL;
  
  return listPtr;
}


/* Not recommended for use as does not free the memory used */
void list_clear(list_t  *listPtr) 
{
  listPtr->head  = listPtr->tail = 0;

}

/* use with caution. 'tmpnode' is generic, you should first free */
/* the 'tmpnode' contents, and then free the list                */
void list_free(list_t *listPtr)
{
  int         i = 0;
  list_node_t *tmpnode = NULL;
  
  for(i = listPtr->count; i > 0; i = listPtr->count) {
    tmpnode = list_remove_first(listPtr);
    free(tmpnode);
  }
  //free(listPtr);
}


/*not done*/
list_node_t *list_freed_to_array(list_t *listPtr)
{
  list_node_t *array = (list_node_t *) malloc(sizeof(list_node_t) * listPtr->count);
  list_node_t *nodePtr;
  for( nodePtr  = list_get_head(listPtr); 
       nodePtr != NULL;
       nodePtr  = list_get_next(nodePtr))
    {
      if ( list_get_prev(nodePtr) != NULL )
	  free(list_get_prev(nodePtr));
    }
  free(listPtr);
  return array;
}


bool list_is_member(list_t *listPtr, list_node_t *nodePtr) 
{
  list_node_t *cur = listPtr->head;
  while (cur != 0) 
    {
      if (cur == nodePtr)
	return true;
      cur = cur->next;
    }
  return false;
}


list_node_t *list_get_head(list_t *listPtr) 
{
  return listPtr->head;
}


void *list_get_first_item(list_t *listPtr)
{
  return (listPtr->head)->item;
}


list_node_t *list_get_tail(list_t *listPtr) 
{
  return listPtr->tail;
}


void *list_get_last_item(list_t *listPtr)
{
  return (listPtr->tail)->item;
}


void *list_get_item(list_node_t *nodePtr)
{
  return nodePtr->item;
}


list_node_t *list_get_next(list_node_t *nodePtr) 
{
  return nodePtr->next;
}


void list_set_next(list_node_t *nodePtr, list_node_t *nextnode) 
{
  nodePtr->next = nextnode;
}


list_node_t *list_get_prev(list_node_t *nodePtr) 
{
  return nodePtr->prev;
}


void list_set_prev(list_node_t *nodePtr, list_node_t *nextnode)
{
  nodePtr->prev = nextnode;
}


void list_add_first(list_t *listPtr, list_node_t *nodePtr)
{
  nodePtr->prev = 0;
  if (listPtr->head == 0) 
    {
      listPtr->head = listPtr->tail = nodePtr;
      nodePtr->next = 0;				
    } 
  else 
    {					
      listPtr->head->prev = nodePtr;		
      nodePtr->next = listPtr->head;		
      listPtr->head = nodePtr;			
  }
  listPtr->count++;
}						


void list_add_first_item(list_t *listPtr, void *item)
{
  list_node_t *nodePtr = (list_node_t *) malloc(sizeof(list_node_t));
  nodePtr->prev = 0;
  if (listPtr->head == 0) 
    {
      listPtr->head = listPtr->tail = nodePtr;
      nodePtr->next = 0;				
    } 
  else 
    {					
      listPtr->head->prev = nodePtr;		
      nodePtr->next = listPtr->head;		
      listPtr->head = nodePtr;			
  }
  nodePtr->item = item;
  listPtr->count++;
}						


void list_add_last_item(list_t *listPtr, void *item)
{
  list_node_t *nodePtr = (list_node_t *) malloc(sizeof(list_node_t));
  nodePtr->next = 0;
  if (listPtr->tail == 0) 
    {
      listPtr->head = listPtr->tail = nodePtr;
      nodePtr->prev = 0;		
    }
  else
    {	
      listPtr->tail->next = nodePtr;
      nodePtr->prev = listPtr->tail;	
      listPtr->tail = nodePtr;		
    }
  nodePtr->item = item;
  listPtr->count++;
}	 


void list_add_last(list_t *listPtr, list_node_t *nodePtr)
{
  nodePtr->next = 0;
  if (listPtr->tail == 0) 
    {
      listPtr->head = listPtr->tail = nodePtr;
      nodePtr->prev = 0;		
    }
  else
    {	
      listPtr->tail->next = nodePtr;
      nodePtr->prev = listPtr->tail;	
      listPtr->tail = nodePtr;		
    }
  listPtr->count++;
}	 


void list_append(list_t *listToModify, list_t *listToAppend)
{
  if (listToAppend->head != 0) 
    {	
      if (listToModify->head == 0) 
	{
	  listToModify->head = listToAppend->head;
	  listToModify->tail = listToAppend->tail;
	} 
      else 
	{
	  listToAppend->head->prev = listToModify->tail;
	  listToModify->tail->next = listToAppend->head;
	  listToModify->tail = listToAppend->tail;
	}
      listToModify->count += listToAppend->count;
    }
  listToAppend->head = listToAppend->tail = 0;	
}
				
				
list_node_t *list_remove_first(list_t *listPtr) 
{		
  list_node_t *nodePtr = listPtr->head;

  listPtr->head = listPtr->head->next;
  if (listPtr->head == 0)
    listPtr->tail = 0;								
  else
    listPtr->head->prev = 0;
  listPtr->count--;
  
  nodePtr->prev = NULL;
  nodePtr->next = NULL;

  return nodePtr;
}		


/* use with caution. It doesn't free the node */										
void list_remove(list_t *listPtr, list_node_t *nodePtr) 
{	
  if (nodePtr->prev != 0)
    nodePtr->prev->next = nodePtr->next;
  else
    listPtr->head = nodePtr->next;
  if (nodePtr->next != 0)
    nodePtr->next->prev = nodePtr->prev;
  else
    listPtr->tail = nodePtr->prev;
  listPtr->count--;

  nodePtr->prev = NULL;
  nodePtr->next = NULL;
}


bool list_is_empty(list_t *listPtr) 
{
  return listPtr->head == 0;
}


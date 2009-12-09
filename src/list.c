#include <stdlib.h>
#include "list.h"

list_t *list_make_empty(list_t *listPtr)
{
  listPtr = (list_t *) malloc(sizeof(list_t));

  listPtr->count = 0;
  listPtr->head = NULL;
  listPtr->tail = NULL;
  
  return listPtr;
}

/* Not recommended for use as does not free the memory used */
void list_clear(list_t  *listPtr) 
{
  listPtr->head = listPtr->tail = 0;
}

void list_free(list_t *listPtr)
{

}

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

void list_set_next(list_node_t *nodePtr, list_node_t *value) 
{
  nodePtr->next = value;
}

list_node_t *list_get_prev(list_node_t *nodePtr) 
{
  return nodePtr->prev;
}

void list_set_prev(list_node_t *nodePtr, list_node_t *value)
{
  nodePtr->prev = value;
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
  list_node_t *nodePtr;
  nodePtr = listPtr->head;
  listPtr->head = listPtr->head->next;
  if (listPtr->head == 0)
    listPtr->tail = 0;								
  else
    listPtr->head->prev = 0;
  listPtr->count--;
  return nodePtr;
}		
										
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
}

bool list_is_empty(list_t *listPtr) 
{
  return listPtr->head == 0;
}


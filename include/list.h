#ifndef _LIST_H_
#define _LIST_H_

#include <stdbool.h>

typedef struct list_node
{
  struct list_node *prev, *next;
  void *item;

} list_node_t;

typedef struct g_list 
{
  list_node_t *head, *tail;
  int count;
} list_t;


/* initialization */
list_t *    list_make_empty(list_t *listPtr);
void        list_clear(list_t  *listPtr);
void        list_free(list_t *listPtr);

/* querying */
bool        list_is_member(list_t *listPtr, list_node_t *nodePtr);
bool        list_is_empty(list_t *listPtr);

/* get methods by item */
void *      list_get_first_item(list_t *listPtr);
void *      list_get_last_item(list_t *listPtr);
void *      list_get_item(list_node_t *nodePtr);

/* get methods by node */
list_node_t *list_get_tail(list_t *listPtr); 
list_node_t *list_get_head(list_t *listPtr);
list_node_t *list_get_next(list_node_t *nodePtr);
list_node_t *list_get_prev(list_node_t *nodePtr);

/* add methods by node */
void        list_set_next(list_node_t *nodePtr, list_node_t *nextnode);
void        list_set_prev(list_node_t *nodePtr, list_node_t *nextnode);
void        list_add_first(list_t *listPtr, list_node_t *nodePtr);
void        list_add_last(list_t *listPtr, list_node_t *nodePtr);

/* add methods by item */
void        list_add_last_item(list_t *listPtr, void *item);
void        list_add_first_item(list_t *listPtr, void *item);

/* add methods by list */
void        list_append(list_t *listPtr, list_t *listToAppend);


/* remove method like queue */
list_node_t *list_remove_first(list_t *listPtr);

/* remove method by node */
void        list_remove(list_t *listPtr, list_node_t *nodePtr);

/* remove method by item */
list_node_t *list_remove_item(list_t *listPtr, void *item);

#endif

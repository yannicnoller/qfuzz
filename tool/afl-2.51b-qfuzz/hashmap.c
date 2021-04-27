#include <stdio.h>

// BEGIN YN: handling of hash maps in C

#include "uthash.h"
#include "utarray.h"

/* Represents an entry in the hash map <int, set<int>>. */
struct hash_map_simple_entry {

  /* key */
  int id;

  /* value */
  double value;

  /* internal hash handler */
  UT_hash_handle hh; /* makes this structure hashable */

};

/* Compare function for integers. */
int intsort(const void *a, const void *b) {
  int _a = *(const int *)a;
  int _b = *(const int *)b;
  return (_a < _b) ? -1 : (_a > _b);
}

/* Searches for entry in hash map with given id. */
struct hash_map_simple_entry *find_entry_in_hashmap(int id, struct hash_map_simple_entry **hash_map) {
  struct hash_map_simple_entry *entry;
  HASH_FIND_INT(*hash_map, &id, entry);
  return entry;
}

/* Checks if key-value-combination is new for this hash map. */
int is_new_or_better(int id, double value, struct hash_map_simple_entry **hash_map) {
  struct hash_map_simple_entry *entry;
  entry = find_entry_in_hashmap(id, hash_map);
  if (entry == NULL) {
    return 1;
  } else {
    return entry->value < value;
  }
}

int add_if_new_or_update_if_better(int id, double new_value, struct hash_map_simple_entry **hash_map) {
  struct hash_map_simple_entry *entry;

  /* Check if id is already existent. */
  entry = find_entry_in_hashmap(id, hash_map);
  if (entry == NULL) {

    /* If not create it. */
    entry = (struct hash_map_simple_entry *) malloc(sizeof *entry);

    /* Add content. */
    entry->id = id;
    entry->value = new_value;

    /* Add everything to the map structure. */
    HASH_ADD_INT(*hash_map, id, entry);

    return 1;

  } else {

    /* Check if new value is better */
    if (entry->value < new_value) {
      entry->value = new_value;
      return 1;
    } else {
      return 0;
    }

  }
}

void print_map(struct hash_map_simple_entry **hash_map) {
  struct hash_map_simple_entry *entry;
  for(entry=*hash_map; entry != NULL; entry=(struct hash_map_simple_entry*)(entry->hh.next)) {
    printf("(%d,%lf), ", entry->id, entry->value);
  }
}

char* map_to_string(struct hash_map_simple_entry **hash_map) {
  char* map_string = "";
  char* entry_value = malloc(17);
  struct hash_map_simple_entry *entry;
  for(entry=*hash_map; entry != NULL; entry=(struct hash_map_simple_entry*)(entry->hh.next)) {
    if (entry->hh.next != NULL) {
      snprintf(entry_value, 17, "%d:%.2lf, ", entry->id, entry->value);
    } else {
      snprintf(entry_value, 17, "%d:%.2lf", entry->id, entry->value);
    }
    char* tmp = (char *) malloc(1 + strlen(map_string) + strlen(entry_value));
    strcpy(tmp, map_string);
    strcat(tmp, entry_value);
    map_string = (char *) malloc(1 + strlen(tmp));
    strcpy(map_string, tmp);
    free(tmp);
  }
  free(entry_value);
  return map_string;
}

void delete_all(struct hash_map_simple_entry **hash_map) {
  struct hash_map_simple_entry *current_entry, *tmp;

  HASH_ITER(hh, *hash_map, current_entry, tmp) {

    /* delete it (users advances to next) */
    HASH_DEL(*hash_map, current_entry);

    /* free it */
    free(current_entry);

  }
}

// END YN

// Just for testing
/*int main(int argc, char** argv) {


  int v1_out_1 = 2;
  int v2_out_1 = 3;
  int v2_out_2 = 3;
  int v2_out_3 = 1;

  int res = add_if_new(v1_out_1, v2_out_1, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", v1_out_1, v2_out_1, res);
  print_map(&output_diff_map);

  res = add_if_new(v1_out_1, v2_out_1, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", v1_out_1, v2_out_1, res);
  print_map(&output_diff_map);

  res = add_if_new(v1_out_1, v2_out_2, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", v1_out_1, v2_out_1, res);
  print_map(&output_diff_map);

  res = add_if_new(v1_out_1, v2_out_3, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", v1_out_1, v2_out_3, res);
  print_map(&output_diff_map);

  res = add_if_new(2, 3, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", 2, 3, res);
  print_map(&output_diff_map);

  res = add_if_new(3, 1, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", 3, 1, res);
  print_map(&output_diff_map);

  res = add_if_new(3, 1, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", 3, 1, res);
  print_map(&output_diff_map);


  printf("Cleaning..\n");
  delete_all(&output_diff_map);
  printf("Done.\n");
}*/

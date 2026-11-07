#include "mymalloc.h"
#include <stdio.h>
#define MEMLENGTH 4096
static union {
char bytes[MEMLENGTH];
double not_used;
} heap;
struct metadata{
    int size;
    int free;
    };
static int initialized=0;
void detectleak(void);
void *mymalloc(size_t size, char *file,int line){
    //no malloc has been called senario
    if (initialized==0){
        //create pounter with size entire heap as space and set initialization to true
        atexit(detectleak);
        struct metadata* heappointer=(struct metadata*)heap.bytes;
        heappointer->size=MEMLENGTH;
        heappointer->free=1;
        initialized=1;
    }
    //reject mallocs with no valid size
    if(size==0||size>MEMLENGTH-sizeof(struct metadata)){
        fprintf(stderr,"malloc: Unable to allocate %zu bytes (%s:%d)\n",size,file,line);
        return NULL;
    }
        struct metadata*heappointer=(struct metadata*)heap.bytes;
        int alignedsize=(size + 7) & ~7;
        //traverse metadatas in search of free chunk of space that is large enough
        int traversedbytes=0;
        while (heappointer->free==0 || heappointer->size<(int)sizeof(struct metadata)+alignedsize){
            traversedbytes+=heappointer->size;
            //check if the 
            if (traversedbytes>=MEMLENGTH){
                fprintf(stderr,"malloc: Unable to allocate %zu bytes (%s:%d)\n",size,file,line);
                return NULL;
            }
            //set heaappointer to start at the beginning of available space
            heappointer=(struct metadata*)(heap.bytes+traversedbytes);
        }
    //set heappointer to the size needed and unfree record the size it contains in the case it was a midchunk pointer so it will update properly
        size_t originalsize=heappointer->size;
    //need to consider possiblity remaing space is too small to put a metadata so extra space will be given as well and no splitting
        if (originalsize<sizeof(struct metadata)+8+sizeof(struct metadata)+alignedsize){
            heappointer->size=originalsize;
            heappointer->free=0;
        }
        else{
            heappointer->size=sizeof(struct metadata)+alignedsize;
            heappointer->free=0;
            //set new pointer as the remaining space that is still left
            struct metadata* newpointer=(struct metadata*)(heap.bytes+traversedbytes+heappointer->size);
            //(heap.bytes+traversedbytes+heappointer->size) needs seperate before casting so additon works properly
            newpointer->size=originalsize-heappointer->size;
            newpointer->free=1;
        }
        return (char*)heappointer+sizeof(struct metadata);
}
void myfree(void *ptr, char *file, int line){
    if (initialized==0){
        //create pounter with size entire heap as space and set initialization to true
        atexit(detectleak);
        struct metadata* heappointer=(struct metadata*)heap.bytes;
        heappointer->size=MEMLENGTH;
        heappointer->free=1;
        initialized=1;
    }
    //no value was given ends function
    if(ptr==NULL){
        return;
    }
    //ptr currently points to the start of the chunk move to the start of the metadata and declare the metadata
    struct metadata* startptr=(struct metadata*)((char*)ptr-sizeof(struct metadata));
    //traverse list to make sure the given pointer was not in a bad spot and check the chunks to see if adjacent chunks are also free
    struct metadata* headpointer=(struct metadata*)(heap.bytes);
    struct metadata* previous=NULL;
    int traversedbytes=0;
    while(headpointer!=startptr){
        traversedbytes+=headpointer->size;
        //traversing never found the given pointer so its invalid;
        if (traversedbytes>=MEMLENGTH){
            fprintf(stderr,"free: Inappropriate pointer (%s:%d)\n",file,line);
            exit(2);
        }
        //mark headpointer as the previous and move headpointer by one so if it finds the chunk it will keep it for coalesting
        previous=headpointer;
        headpointer=(struct metadata*)(heap.bytes+traversedbytes);
    }
    //already freed
    if(headpointer->free==1){
        fprintf(stderr,"free: Inappropriate pointer (%s:%d)\n",file,line);
        exit(2);
    }
    //coalest
    if(traversedbytes+headpointer->size<MEMLENGTH){
        //there will be another chunk if conditon met
        struct metadata* next=(struct metadata*)((char*)headpointer+headpointer->size);
        if(next->free==1){
            headpointer->size+=next->size;
        }
    }
    headpointer->free=1;
    //coalest
    if(previous!=NULL && previous->free==1){
        previous->size+=headpointer->size;
        headpointer=previous;
    }
}
void detectleak(void){
    struct metadata* heappointer=(struct metadata*)heap.bytes;
    int traversedbytes=0;
    int leakedchunks=0;
    size_t leakedbytes=0;
    while(traversedbytes<MEMLENGTH){
        if (heappointer->free==0){
            leakedchunks+=1;
            leakedbytes+=heappointer->size-sizeof(struct metadata);
        }
        traversedbytes+=heappointer->size;
        heappointer=(struct metadata*)(heap.bytes+traversedbytes);
    }
    if(leakedchunks>0){
        fprintf(stderr,"mymalloc: %zu bytes leaked in %d objects.\n",leakedbytes,leakedchunks);
    }
}
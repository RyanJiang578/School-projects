#include "mymalloc.h"
#include <stdio.h>
#include <stdlib.h>
int main(int argc, char *argv[]){
    if (argc<2){
        return 1;
    }
    int senario=atoi(argv[1]);
    //oversized
    if (senario==1){
        char* ptr=malloc(5000);
        free(ptr);
    }
    //free with bad pointer
    if (senario==2){
        int x;
        free(&x);
    }
    //double freeing
    if (senario==3){
        char* ptr=malloc(100);
        free(ptr);
        free(ptr);
    }
    //misaligned freeing
    if (senario==4){
        char* ptr=malloc(200);
        free(ptr+1);
    }
    //memory leak
    if(senario==5){
        char* ptr=malloc(200);
    }
    //malloc of no size
    if(senario==6){
        char* ptr=malloc(0);
        free(ptr);
    }
    //free with null pointer
    if(senario==7){
        free(NULL);
    }
}
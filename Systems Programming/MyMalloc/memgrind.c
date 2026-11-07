#include "mymalloc.h"
#include <sys/time.h>
#include <stdio.h>
void task1(void);
void task2(void);
void task3(void);
void task4(void);
void task5(void);
int main(void){
    struct timeval start;
    struct timeval end;
    gettimeofday(&start,NULL);
    for (int i=0;i<50;i++){
        task1();
        task2();
        task3();
        task4();
        task5();
    }
    gettimeofday(&end,NULL);
    double time=((end.tv_sec-start.tv_sec)*1000000+(end.tv_usec-start.tv_usec))/50;
    printf("average time to complete tasks: %f microseconds\n",time);
    return 0;
}
//malloc in increasing order then free in decreasing order
void task1(void){
    char* ptr1=malloc(8);
    char* ptr2=malloc(16);
    char* ptr3=malloc(32);
    char* ptr4=malloc(64);
    char* ptr5=malloc(128);
    char* ptr6=malloc(512);
    char* ptr7=malloc(1024);
    free(ptr7);
    free(ptr6);
    free(ptr5);
    free(ptr4);
    free(ptr3);
    free(ptr2);
    free(ptr1);
}
//store 120 small chunks and free them
void task2(void){
    char* arr[120];
    for(int i=0;i<120;i++){
        arr[i]=malloc(8);
    }
    for(int i=0;i<120;i++){
        free(arr[i]);
    }
}
//free in a random order
void task3(void){
    int allocations=0;
    int pointer=0;
    char* arr[120];
    while (allocations<120){
        int result=rand()%2;
        //malloc
        if (result==0){
            arr[pointer]=malloc(1);
            pointer+=1;
            allocations+=1;
        }
        //free
        else if (result==1){
            if(pointer>0){
                int random=rand()%pointer;
                free(arr[random]);
                arr[random]=arr[pointer-1];
                pointer-=1;
            }
        }
    }
    while(pointer>0){
        free(arr[pointer-1]);
        pointer-=1;
    }
}
//make 8 chunks sized 512 including metadata size free from middle all chunk should coalested into one big chunk after
void task4(void){
    char* ptr1=malloc(504);
    char* ptr2=malloc(504);
    char* ptr3=malloc(504);
    char* ptr4=malloc(504);
    char* ptr5=malloc(504);
    char* ptr6=malloc(504);
    char* ptr7=malloc(504);
    char* ptr8=malloc(504);
    free(ptr4);
    free(ptr5);
    free(ptr3);
    free(ptr6);
    free(ptr2);
    free(ptr7);
    free(ptr1);
    free(ptr8);
    char* bigptr=malloc(4088);
    free(bigptr);
}
//create 5 chunks free chunk 2 and 4 and place smaller pieces in their place then free everything
void task5(void){
    char* ptr1=malloc(800);
    char* ptr2=malloc(800);
    char* ptr3=malloc(800);
    char* ptr4=malloc(800);
    char* ptr5=malloc(800);
    free(ptr2);
    free(ptr4);
    char* newptr2=malloc(600);
    char* newptr4=malloc(600);
    free(ptr1);
    free(newptr2);
    free(ptr3);
    free(newptr4);
    free(ptr5);
}
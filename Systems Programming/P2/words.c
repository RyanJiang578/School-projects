#include <string.h>
#include <stdio.h>
#include <dirent.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>   
#include <stdlib.h>

//default suffix
char* suffix=".txt";
int i=1;
int capacity=16;
int wordstored=0;
struct word{
    char* word;
    int count;
};
struct word* resize(struct word* array);
struct word* search(struct word* array, char* word);
struct word* readfile(char* name, struct word* array);
struct word* traverse(char* path, struct word* array);
int compare(const void* a, const void* b);
int main(int argc, char** argv){
    if (argc==1){
        fprintf(stderr,"not enough arguements");
        return 1;
    }
    if (strcmp((argv[1]),"-s")==0){
        // ./words + -s + .c + document
        if(argc<4){
            fprintf(stderr,"not enough arguements");
            return 1;
        }
        i+=1;
        suffix=argv[i];
        i+=1;
    }
    else if (strncmp(argv[1],"-s",2)==0){
        // ./words + -s.c + document
        if(argc<3){
            fprintf(stderr,"not enough arguements");
            return 1;
        }
        suffix=argv[i]+2;
        i+=1;
    }
    struct word* array=malloc(capacity * sizeof(struct word));;
    for(;i<argc;i++){
            struct stat data;
    int r = stat(argv[i], &data);
    if(r<0){
        fprintf(stderr,"cannot open directory");
        return -1;
    }
    //file
    if (S_ISREG(data.st_mode)){
        array=readfile(argv[i],array);
    } 
    else if (S_ISDIR(data.st_mode)) {
        array=traverse(argv[i],array);
    } 
    else {
        return -1;
    }
    }
    qsort(array, wordstored, sizeof(struct word), compare);
    for(int i=0;i<wordstored;i++){
        char count[32];
        int n=snprintf(count,32,"%d",array[i].count);
        write(STDOUT_FILENO,array[i].word,strlen(array[i].word));
        write(STDOUT_FILENO," ",1);
        write(STDOUT_FILENO,count,n);
        write(STDOUT_FILENO,"\n",1);
        free(array[i].word);
    }
    free(array);
    return 0;
}
    struct word* resize(struct word* array){
        struct word* newarray=malloc(2*capacity*(sizeof(struct word)));
        memcpy(newarray,array,sizeof(struct word)*capacity);
        capacity*=2;
        free(array);
        return newarray;
    }
    struct word* search(struct word* array,char* word){
        for(int i=0;i<wordstored;i++){
            if (strcmp(array[i].word,word)==0){
                array[i].count+=1;
                return array;
            }
        }
        struct word newword;
        newword.count=1;
        newword.word=malloc(strlen(word)+1);
        strcpy(newword.word,word);
        array[wordstored]=newword;
        wordstored+=1;
        if (wordstored==capacity){
            return (resize(array));
        }
        return array;
    }
    struct word* readfile(char* name,struct word* array){
        int bufcap=16;
        int fd=open(name,O_RDONLY);
        if(fd==-1){
            fprintf(stderr,"error");
            return array;
        }
        int offset=0;
        int readbytes=0;
        char* buffer=malloc(sizeof(char)*bufcap);
        readbytes=read(fd,buffer+offset,bufcap-offset);
        while(readbytes>0){
            if(readbytes<0){
                fprintf(stderr,"error reading file\n");
            }
            int total=readbytes+offset;
            int start=-1;
            int end=-1;
            for(int i=0;i<total;i++){
                char c=buffer[i];
                if(c=='-'){
                    if(i>0&&i<total-1&&start!=-1){
                        char prev=buffer[i-1];
                        char next=buffer[i+1];
                        if(!(next>='a'&&next<='z'||next>='A'&&next<='Z')||!(prev>='a'&&prev<='z'||prev>='A'&&prev<='Z')){
                            end=i;
                            //search
                            int len=(end-start);
                            char* word=malloc(len+1);
                            word[len]='\0';
                            strncpy(word,buffer+start,len);
                            array=search(array,word);
                            free(word);
                            start=-1;
                            end=-1;
                        }
                    }
                }
                else if(c=='\''){
                    if (start==-1){
                        start=i;
                    }
                }
                else if(c>='a'&&c<='z'||c>='A'&&c<='Z'){
                    if (start<0){
                        start=i;
                    }
                }
                else{
                    if(start!=-1){
                        end=i;
                        int len=(end-start);
                        char* word=malloc(len+1);
                        word[len]='\0';
                        strncpy(word,buffer+start,len);
                        array=search(array,word);
                        free(word);
                        //run search
                        start=-1;
                        end=-1;
                    }
                }
            }
            if (start!=-1){
                    offset=0;
                    for(int i=start;i<total;i++){
                        buffer[offset]=buffer[i];
                        offset+=1;
                    }
                    if (offset==bufcap){
                        buffer=realloc(buffer,bufcap*=2);
                    }
                }
                else{
                    offset=0;
                }

            readbytes=read(fd,buffer+offset,bufcap-offset);
            if (readbytes==0&&offset!=0){
                if (buffer[offset-1]=='-'){
                    offset-=1;
                }
                char* word=malloc(offset+1);
                word[offset]='\0';
                strncpy(word,buffer,offset);
                array=search(array,word);
                free(word);
            }
        }
        free(buffer);
        close(fd);
        return array;
    }   
    struct word* traverse(char* path,struct word*array){
        DIR* dirp =opendir(path);
        if(!dirp){
            fprintf(stderr,"cannot open directory");
            return array;
        }
        struct dirent* current;
        char name[1024];
        while((current=readdir(dirp))!=NULL){
            if(current->d_name[0]!='.'){
                int bytes=snprintf(name,sizeof(name),"%s/%s",path,current->d_name);
            if (bytes>0&&bytes<sizeof(name)){
                struct stat data;
                int r = stat(name, &data);
                if(r<0){
                fprintf(stderr,"cannot open directory");
                }
                else if(S_ISDIR(data.st_mode)){
                    array=traverse(name,array);
                }
                else if (S_ISREG(data.st_mode)){
                if(strlen(current->d_name)>=strlen(suffix)&&strcmp(current->d_name+(strlen(current->d_name)-strlen(suffix)),suffix)==0){
                    array=readfile(name,array);
                    }
                }
            }
        }
    }
    closedir(dirp);
    return array;
}
int compare(const void* a, const void* b){
    const struct word* word1 = a;
    const struct word* word2 = b;

    if(word1->count > word2->count){
        return -1;
    }
    if(word1->count < word2->count){
        return 1;
    }
    return strcmp(word1->word,word2->word);
}
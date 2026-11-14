#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <fcntl.h>
#include <dirent.h>

void parse(char* buffer,char* args[]){
    int count=0;
    char* token=strtok(buffer," \t\r");
    while(token!=NULL&&count<99){
        args[count]=token;
        count+=1;
        token=strtok(NULL," \t\r");
    }
    args[count]=NULL;
}


int expandwildcards(char* tokens[],char* args[],char* allocated[],int* allocatedcount){
    int count=0;
    *allocatedcount=0;
    for(int i=0;tokens[i]!=NULL;i++){
        char* star=strchr(tokens[i],'*');
        int matches=0;
        if(star!=NULL&&strchr(star+1,'*')==NULL){
            char directory[4096];
            char* slash=strrchr(tokens[i],'/');
            char* pattern=tokens[i];
            if(slash!=NULL){
                int length=slash-tokens[i];
                if(length==0){
                    strcpy(directory,"/");
                }
                else if(length<(int)sizeof(directory)){
                    memcpy(directory,tokens[i],length);
                    directory[length]='\0';
                }
                else{
                    return 1;
                }
                pattern=slash+1;
            }
            else{
                strcpy(directory,".");
            }
            char* asterisk=strchr(pattern,'*');
            if(asterisk!=NULL){
                int prefix=asterisk-pattern;
                char* suffix=asterisk+1;
                int suffixlen=strlen(suffix);
                DIR* dir=opendir(directory);
                if(dir!=NULL){
                    struct dirent* entry;
                    int start=count;
                    while((entry=readdir(dir))!=NULL){
                        char* name=entry->d_name;
                        int namelen=strlen(name);
                        if(strcmp(name,".")!=0&&strcmp(name,"..")!=0&&
                           !(prefix==0&&name[0]=='.')&&
                           namelen>=prefix+suffixlen&&
                           strncmp(name,pattern,prefix)==0&&
                           strcmp(name+namelen-suffixlen,suffix)==0){
                            if(count>=4095){
                                closedir(dir);
                                return 1;
                            }
                            int length=strlen(tokens[i])-strlen(pattern)+namelen+1;
                            char* match=malloc(length);
                            if(match==NULL){
                                closedir(dir);
                                return 1;
                            }
                            if(slash!=NULL){
                                int dirlen=slash-tokens[i]+1;
                                memcpy(match,tokens[i],dirlen);
                                strcpy(match+dirlen,name);
                            }
                            else{
                                strcpy(match,name);
                            }
                            args[count]=match;
                            allocated[*allocatedcount]=match;
                            *allocatedcount+=1;
                            count+=1;
                            matches+=1;
                        }
                    }
                    closedir(dir);
                    for(int j=start;j<count;j++){
                        for(int k=j+1;k<count;k++){
                            if(strcmp(args[j],args[k])>0){
                                char* temp=args[j];
                                args[j]=args[k];
                                args[k]=temp;
                            }
                        }
                    }
                }
            }
        }
        if(matches==0){
            if(count>=4095){
                return 1;
            }
            args[count]=tokens[i];
            count+=1;
        }
    }
    args[count]=NULL;
    return 0;
}

int isbuiltin(char* name){
    return strcmp(name,"pwd")==0||strcmp(name,"cd")==0||strcmp(name,"which")==0||strcmp(name,"exit")==0;
}

int builtin(char* args[]){
    int status=0;
    if(strcmp(args[0],"exit")==0){
        return 0;
    }
    else if(strcmp(args[0],"pwd")==0){
        if(args[1]!=NULL){
            fprintf(stderr,"pwd: too many arguments\n");
            status=1;
        }
        else{
            char cwd[4096];
            if(getcwd(cwd,sizeof(cwd))!=NULL){
                printf("%s\n",cwd);
            }
            else{
                perror("pwd");
                status=1;
            }
        }
    }
    else if(strcmp(args[0],"cd")==0){
        if(args[2]!=NULL){
            fprintf(stderr,"cd: too many arguments\n");
            status=1;
        }
        else{
            char* directory=args[1];
            if(directory==NULL){
                directory=getenv("HOME");
            }
            if(chdir(directory)==-1){
                perror("cd");
                status=1;
            }
        }
    }
    else if(strcmp(args[0],"which")==0){
        char* list[]={"/usr/local/bin","/usr/bin","/bin"};
        if(args[1]==NULL){
            status=1;
        }
        for(int i=1;args[i]!=NULL;i++){
            int found=0;
            if(strchr(args[i],'/')!=NULL){
                if(access(args[i],X_OK)==0){
                    printf("%s\n",args[i]);
                    found=1;
                }
            }
            else{
                char path[1024];
                for(int j=0;j<3&&found==0;j++){
                    int size=snprintf(path,sizeof(path),"%s/%s",list[j],args[i]);
                    if(size>=0&&size<(int)sizeof(path)&&access(path,X_OK)==0){
                        printf("%s\n",path);
                        found=1;
                    }
                }
            }
            if(found==0){
                status=1;
            }
        }
    }
    return status;
}

int findpath(char* name,char* path,int size){
    char* list[]={"/usr/local/bin","/usr/bin","/bin"};
    int found=0;
    if(strchr(name,'/')!=NULL){
        int length=snprintf(path,size,"%s",name);
        if(length>=0&&length<size&&access(path,X_OK)==0){
            found=1;
        }
    }
    else{
        for(int i=0;i<3&&found==0;i++){
            int length=snprintf(path,size,"%s/%s",list[i],name);
            if(length>=0&&length<size&&access(path,X_OK)==0){
                found=1;
            }
        }
    }
    return found;
}

int redirect(char* input,char* output,int append){
    if(input!=NULL){
        int fd=open(input,O_RDONLY);
        if(fd==-1){
            perror("input redirection");
            return 1;
        }
        if(dup2(fd,STDIN_FILENO)==-1){
            perror("dup2");
            close(fd);
            return 1;
        }
        close(fd);
    }
    if(output!=NULL){
        int fd;
        if(append==1){
            fd=open(output,O_WRONLY|O_CREAT|O_APPEND,0640);
        }
        else{
            fd=open(output,O_WRONLY|O_CREAT|O_TRUNC,0640);
        }
        if(fd==-1){
            perror("output redirection");
            return 1;
        }
        if(dup2(fd,STDOUT_FILENO)==-1){
            perror("dup2");
            close(fd);
            return 1;
        }
        close(fd);
    }
    return 0;
}

void reportstatus(int status,int interactive){
    if(interactive==1){
        if(WIFEXITED(status)){
            if(WEXITSTATUS(status)!=0){
                printf("Exited with status %d\n",WEXITSTATUS(status));
            }
        }
        else if(WIFSIGNALED(status)){
            printf("Terminated by signal %s\n",strsignal(WTERMSIG(status)));
        }
    }
}

int main(int argc,char* argv[]){
    char* tokens[100];
    char* args[4096];
    char* allocated[4096];
    char* buffer=malloc(1024);
    if(buffer==NULL){
        perror("malloc");
        return 1;
    }
    int interactive=0;
    if(argc==1&&isatty(STDIN_FILENO)){
        interactive=1;
    }
    int inputfd=STDIN_FILENO;
    if(argc==2){
        inputfd=open(argv[1],O_RDONLY);
        if(inputfd==-1){
            perror("open");
            free(buffer);
            return 1;
        }
    }
    else if(argc>2){
        fprintf(stderr,"Too many arguments\n");
        free(buffer);
        return 1;
    }
    if(interactive==1){
        write(STDOUT_FILENO,"Welcome to my Shell!\n",21);
    }
    int bufsize=1024;
    int running=1;
    while(running==1){
        int bufindex=0;
        int endfile=0;
        if(interactive==1){
            char cwd[4096];
            char* home=getenv("HOME");
            if(getcwd(cwd,sizeof(cwd))!=NULL){
                if(home!=NULL&&strncmp(cwd,home,strlen(home))==0&&(cwd[strlen(home)]=='/'||cwd[strlen(home)]=='\0')){
                    printf("~%s$ ",cwd+strlen(home));
                }
                else{
                    printf("%s$ ",cwd);
                }
                fflush(stdout);
            }
        }
        int reading=1;
        while(reading==1){
            if(bufindex==bufsize-1){
                int newsize=bufsize*2;
                char* temp=realloc(buffer,newsize);
                if(temp==NULL){
                    perror("realloc");
                    free(buffer);
                    if(inputfd!=STDIN_FILENO) close(inputfd);
                    return 1;
                }
                buffer=temp;
                bufsize=newsize;
            }
            ssize_t bytes=read(inputfd,&buffer[bufindex],1);
            if(bytes==1){
                if(buffer[bufindex]=='\n'){
                    reading=0;
                }
                else{
                    bufindex+=1;
                }
            }
            else{
                if(bytes==-1){
                    perror("read");
                }
                endfile=1;
                running=0;
                reading=0;
            }
        }
        buffer[bufindex]='\0';
        char* comment=strchr(buffer,'#');
        if(comment!=NULL){
            *comment='\0';
        }
        parse(buffer,tokens);
        int allocatedcount=0;
        if(expandwildcards(tokens,args,allocated,&allocatedcount)!=0){
            fprintf(stderr,"Wildcard expansion failed\n");
            for(int i=0;i<allocatedcount;i++){
                free(allocated[i]);
            }
            if(endfile==1){
                running=0;
            }
            continue;
        }
        char* output=NULL;
        char* input=NULL;
        char** commands[100];
        int commandcount=1;
        int count=0;
        int error=0;
        int append=0;
        for(int i=0;args[i]!=NULL;i++){
            if(strcmp(args[i],">")==0||strcmp(args[i],">>")==0||strcmp(args[i],"<")==0){
                int type=0;
                if(strcmp(args[i],"<")==0){
                    type=1;
                }
                if(args[i+1]==NULL||strcmp(args[i+1],">")==0||strcmp(args[i+1],">>")==0||strcmp(args[i+1],"<")==0||strcmp(args[i+1],"|")==0){
                    error=1;
                }
                else{
                    if(type==1){
                        input=args[i+1];
                    }
                    else{
                        output=args[i+1];
                        append=strcmp(args[i],">>")==0;
                    }
                    i+=1;
                }
            }
            else{
                args[count]=args[i];
                count+=1;
            }
        }
        args[count]=NULL;
        commands[0]=args;
        for(int i=0;i<count;i++){
            if(strcmp(args[i],"|")==0){
                if(i==0||i==count-1||args[i-1]==NULL||strcmp(args[i+1],"|")==0){
                    error=1;
                }
                args[i]=NULL;
                if(error==0){
                    commands[commandcount]=&args[i+1];
                    commandcount+=1;
                }
            }
        }
        if(error==1){
            fprintf(stderr,"Invalid command syntax\n");
            if(interactive==1){
                printf("Exited with status 1\n");
            }
        }
        else if(commandcount>1){
            int prevread=-1;
            pid_t pids[100];
            int started=0;
            int failed=0;
            int exitpipe=0;
            for(int i=0;i<commandcount;i++){
                if(strcmp(commands[i][0],"exit")==0){
                    exitpipe=1;
                }
            }
            fflush(stdout);
            for(int i=0;i<commandcount&&failed==0;i++){
                int fd[2];
                int haspipe=0;
                if(i<commandcount-1){
                    if(pipe(fd)==-1){
                        perror("pipe");
                        failed=1;
                    }
                    else{
                        haspipe=1;
                    }
                }
                if(failed==0){
                    pid_t pid=fork();
                    if(pid==-1){
                        perror("fork");
                        failed=1;
                        if(haspipe==1){
                            close(fd[0]);
                            close(fd[1]);
                        }
                    }
                    else if(pid==0){
                        if(prevread!=-1){
                            if(dup2(prevread,STDIN_FILENO)==-1){
                                perror("dup2");
                                _exit(1);
                            }
                        }
                        else{
                            int nullfd=open("/dev/null",O_RDONLY);
                            if(nullfd==-1){
                                perror("open");
                                _exit(1);
                            }
                            if(dup2(nullfd,STDIN_FILENO)==-1){
                                perror("dup2");
                                close(nullfd);
                                _exit(1);
                            }
                            close(nullfd);
                        }
                        if(haspipe==1){
                            if(dup2(fd[1],STDOUT_FILENO)==-1){
                                perror("dup2");
                                _exit(1);
                            }
                        }
                        if(prevread!=-1) close(prevread);
                        if(haspipe==1){
                            close(fd[0]);
                            close(fd[1]);
                        }
                        if(isbuiltin(commands[i][0])==1){
                            int status=builtin(commands[i]);
                            fflush(stdout);
                            _exit(status);
                        }
                        char path[1024];
                        if(findpath(commands[i][0],path,sizeof(path))==0){
                            fprintf(stderr,"%s: command not found\n",commands[i][0]);
                            _exit(1);
                        }
                        execv(path,commands[i]);
                        perror("execv");
                        _exit(1);
                    }
                    else{
                        pids[started]=pid;
                        started+=1;
                        if(prevread!=-1) close(prevread);
                        if(haspipe==1){
                            close(fd[1]);
                            prevread=fd[0];
                        }
                        else{
                            prevread=-1;
                        }
                    }
                }
            }
            if(prevread!=-1) close(prevread);
            for(int i=0;i<started;i++){
                int status;
                if(waitpid(pids[i],&status,0)==-1){
                    perror("waitpid");
                }
                else if(i==commandcount-1&&started==commandcount){
                    reportstatus(status,interactive);
                }
            }
            if(exitpipe==1&&failed==0){
                running=0;
            }
        }
        else if(args[0]!=NULL){
            if(isbuiltin(args[0])==1){
                int savedin=-1;
                int savedout=-1;
                int status=0;
                fflush(stdout);
                savedin=dup(STDIN_FILENO);
                savedout=dup(STDOUT_FILENO);
                if(savedin==-1||savedout==-1){
                    perror("dup");
                    status=1;
                }
                else if(redirect(input,output,append)==0){
                    status=builtin(args);
                    if(strcmp(args[0],"exit")==0){
                        running=0;
                    }
                }
                else{
                    status=1;
                }
                fflush(stdout);
                if(savedin!=-1){
                    if(dup2(savedin,STDIN_FILENO)==-1){
                        perror("restore stdin");
                        status=1;
                    }
                    close(savedin);
                }
                if(savedout!=-1){
                    if(dup2(savedout,STDOUT_FILENO)==-1){
                        perror("restore stdout");
                        status=1;
                    }
                    close(savedout);
                }
                if(interactive==1&&status!=0){
                    printf("Exited with status %d\n",status);
                }
            }
            else{
                char path[1024];
                if(findpath(args[0],path,sizeof(path))==0){
                    fprintf(stderr,"%s: command not found\n",args[0]);
                    if(interactive==1){
                        printf("Exited with status 1\n");
                    }
                }
                else{
                    fflush(stdout);
                    pid_t pid=fork();
                    if(pid==-1){
                        perror("fork");
                    }
                    else if(pid==0){
                        if(input==NULL&&(interactive==0||output!=NULL)){
                            int nullfd=open("/dev/null",O_RDONLY);
                            if(nullfd==-1){
                                perror("open");
                                _exit(1);
                            }
                            if(dup2(nullfd,STDIN_FILENO)==-1){
                                perror("dup2");
                                close(nullfd);
                                _exit(1);
                            }
                            close(nullfd);
                        }
                        if(redirect(input,output,append)!=0){
                            _exit(1);
                        }
                        execv(path,args);
                        perror("execv");
                        _exit(1);
                    }
                    else{
                        int status;
                        if(waitpid(pid,&status,0)==-1){
                            perror("waitpid");
                        }
                        else{
                            reportstatus(status,interactive);
                        }
                    }
                }
            }
        }
        for(int i=0;i<allocatedcount;i++){
            free(allocated[i]);
        }
        if(endfile==1){
            running=0;
        }
    }
    if(interactive==1){
        write(STDOUT_FILENO,"Exiting my Shell.\n",18);
    }
    if(inputfd!=STDIN_FILENO){
        close(inputfd);
    }
    free(buffer);
    return 0;
}

CC = gcc
CFLAGS = -std=c99 -Wall -Wextra
TARGET = mysh

all: $(TARGET)

$(TARGET): mysh.c
	$(CC) $(CFLAGS) -o $(TARGET) mysh.c

clean:
	rm -f $(TARGET)
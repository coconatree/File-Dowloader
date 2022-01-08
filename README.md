# Academic Integrity

This account does not take responsibility for any sorts of plagiarism about the repositories contents. And discourages any action that goes againts the rules of academic integrity.

# Note

```
To a false assumtion of mine this assigment does not work properly.
It doesn't have any problems running. (Mostly)
But it has a logic erro in it's design and doesn't retrieve all the files it needs to.
```

```
 All of the HTTP layer request are made using only the java socket API. Rest of the protocol implementation is hard-coded inside the application.   
```

# HTTP-GET-IMPLEMENTATION

This repository contains an programming assigment from a computer networks course.
It is a command line aplication. 
It retrieves a list of paths to visit from a given index URL and iterates over that list to send GET HEAD request to each one.
Afterwards it compares the size retrieved from the GET HEAD request. 
Depending on the resulting condition, it sends a GET RANGE request to retrieve a part of the file.
In the and it saves the dowloaded files to the project's folder and informs the user by printing information.

# How to use it

- Compiling
  - javac FileDowlader.java
  
- Test Cases
  - www.cs.bilkent.edu.tr/~cs421/fall21/project1/index1.txt
  - www.cs.bilkent.edu.tr/~cs421/fall21/project1/index2.txt

- Running
  - java FileDowloader <One of the test cases> <lowerbound - int>-<upperbound - int> 
  - java FileDowloader www.cs.bilkent.edu.tr/~cs421/fall21/project1/index2.txt 100-1000

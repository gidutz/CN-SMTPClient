This program is an implementation of Lab2 in Computer Networks IDC 2013/4

The program starts running by loading the config.ini file located in the same directory, The main class (starter class) is ServerRun.

The structure of the program is as follows.

=== ServerRun.java ===
Loads the settings from config.ini. validates that all the required values are filled and runs Runner


=== Runner.java ===
This is a Runnable java file. when run, it constructs a new ServerSocket and listens on the specified port.
It also starts 3 other threads: ReminderHandler, TaskHandler, PollsHandler, those shall be explained later.
when a connection from a client is established, it constructs a ClientHandler object. 
The number of connections is limited by a semaphore.

=== ClientHandler.java ===
This file implements runnable. It is used to handle a single HTTP request from a single client.
an instance of this class should accept a socket, hence it only exists when the connection is alive.
The socket is then parsed by a HttpParser object and a response is echoed to the Socket by the HttpResponder.
One significant operation of this class is request redirection. 
After parsing the request, The client handler makes sure that the Client is logged in (Cookie with username was sent)
if not logged in, the request is redirected to the login screen (index.html) unless one of the following conditions if fulfilled:
1. The client asks for a resource (image, css, js)
2. The client surfs to poll_reply.html
3. The client surfs fo task_reply.html

=== HttpParser.java ===
This Class accepts a client socket and tries to parse an HTTP request from its stream. 
It should extract the headers, variables, requested url and generate a response code.

=== HttpResponser.java ===
HttpResponder echoes the response to the client in HTTP format. it reads the required file and generates appropriate headers.

=== Email.java ====
This is an abstract class that describes an email object. it has the expected fields of an email.
All E-mail types of this lab (Poll,Task, Reminder) extend this class to their specific properties.
The main fields of this object are "completed", "sent" and "expired"
-completed - is true for Polls and Tasks when they are marked as completed
-expired - is true for all email instances only when the due date is past to the current system time
-sent - is true for all email instances only when is was properly handled (Poll - summery sent to the owner)

=== Poll.java, Task.java, Reminder.java ===
The 3 java files define objects that are used to represent the corresponding email

=== PollArray.java  === 
This simple class is a helper object that saves the results of a poll 

=== EmailHandler.java ===
This abstract class represents an object that implements Runnable. 
An instanced object of this class holds an EmailArrayList that holds a specific data type of Email.
Every 30 seconds the thread performs a check on the list to find email that need to be handled. 
The handeling of each email is abstract and implemented according to the Email type.


=== TasksHandler.java, PollsHandler.java, RemindersHandler.java ===
These are all instanced of EmailHandler. they implement the handle() method according to the instructions


=== SQLiteDataBaseHelper.java ===
This file is an abstraction of SQLite database management. 
It provides CRUD capabilities to the user of this class, for Email data types.


=== SMTPSessoion.java ===
This Class is used to create a connection with the SMTP server and send an Email. it implements the SMTP protocol.

=== SMTPException.java ===
This file extends Exception.java. It is thrown when an error occurs during the SMTP session when trying to send an Email.

=== ChatMessage.java, ChatQueue.java ===
These files are used to implement the chat room. The queue saves the last 30 chat messages. 
A chat message holds the message user and the message content.






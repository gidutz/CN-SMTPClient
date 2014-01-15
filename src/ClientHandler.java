import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/**
 * A thread to handle a single client Accepts the input, parses and outputs a
 * response *
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;

    /* --! connection time out for the socket -- */
    private static final int CONNECTION_TIMEOUT = 30 * 1000;
    private final String CRLF = "\r\n";
    volatile EmailArrayList tasks;
    volatile EmailArrayList reminders;
    volatile EmailArrayList polls;
    private int responseCode;

    public ClientHandler(Socket clientSocket, EmailArrayList tasks, EmailArrayList reminders, EmailArrayList polls) {

        this.clientSocket = clientSocket;

        this.tasks = tasks;
        this.reminders = reminders;
        this.polls = polls;
    }

    /**
     * Starts handling the client request
     */
    @Override
    public void run() {
        System.out.println(clientSocket);
        try {
            clientSocket.setSoTimeout(CONNECTION_TIMEOUT);
            HttpParser request = new HttpParser(clientSocket.getInputStream());

            logRequest(request);
            /*
             * if the user logs in, then generate cookie
			 */
            String path = redirect(request);
            responseCode = 200;

            HttpResponder responder = new HttpResponder(path, responseCode,
                    clientSocket.getOutputStream());
            // make HTTP response
            responder.echoResponse();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ServerRun.connectionLimiter.release();
        System.out.println("Connection eliminated with " + clientSocket);
    }

    private void logRequest(HttpParser parser) {
        System.out.println("method=" + parser.getMethod());
        System.out.println("url=" + parser.getRequestURL());
        System.out.println("headers = " + parser.getHeaders());
        System.out.println("vars = " + parser.getParams());
        System.out.println(parser.getHeaders().toString());
    }

    private String redirect(HttpParser request) {
        String path = request.getRequestURL();
        if (path.startsWith("/")) {
            path = path.substring(1);

        }
        // change absolute path to relative path
        if (path.startsWith("http://" + request.getHeader("host"))) {
            path = path.substring(("http://" + request.getHeader("host"))
                    .length() + 1);
        }

        path = ServerRun.root + path;
        File page = new File(path);
        if (page.isDirectory()) {
            path = path + ServerRun.defaultPage;
        }


        /*
        *This block handles requests, if the request contains a cookie the request is handled
        * if the request doesn't contain a cookie the request is redirected to the login page.
         */
        if (request.getHeader("Cookie") == null) {
            if (!path.contains("/images/") && !path.contains("/css/")) {
                path = ServerRun.root + ServerRun.defaultPage;

            } else if (path.endsWith("task_reply.html")) {
                path = completeTask(Integer.parseInt(request.getParam("id")));


            } else if (path.endsWith("poll_reply.html")) {

                path = pollReply(Integer.parseInt(request.getParam("id")), Integer.parseInt(request.getParam("ans")));

            }
            page = new File(path);

        } else { //There is a cookie with a user name
            String[] cookie = request.getHeader("Cookie").split("=");
            String user = cookie[1];
            String id = null;
            if (path.endsWith("index.html")) {
                path = ServerRun.root + ServerRun.mainPage;
            } else if (path.endsWith("tasks.html")) {
                if ((id = request.getParam("id")) != null) {
                    deleteTask(id);
                    path = generateTasksPage(user);
                } else {
                    path = generateTasksPage(user);
                }
            } else if (path.endsWith("reminders.html")) {
                if ((id = request.getParam("id")) != null) {
                    deleteReminder(id);
                    path = generateRemindersPage(user);

                } else {
                    path = generateRemindersPage(user);
                }
            } else if (path.endsWith("polls.html")) {
                if ((id = request.getParam("id")) != null) {
                    deletePoll(id);
                    path = generatePollsPage(user);
                } else {
                    path = generatePollsPage(user);
                }
            } else if (path.endsWith("submit_reminder.html")) {
                Reminder reminder = null;
                try {
                    String owner = user;
                    String recipient = user;
                    Calendar creationDate = Calendar.getInstance();
                    Calendar dueDate = Calendar.getInstance();
                    Date date = (Email.DATE_FORMAT).parse(request.getParam("due_date") + " " + request.getParam("due_time"));
                    dueDate.setTime(date);
                    String title = request.getParam("title");
                    String data = request.getParam("data");
                    reminder = new Reminder(owner, creationDate, dueDate, recipient, title, data, false);
                    if (request.getParam("id").equalsIgnoreCase("new")) {
                        this.reminders.add(reminder);
                    } else {
                        this.reminders.update(reminder);

                    }
                    path = generateRemindersPage(user);

                } catch (ParseException e) {
                    System.err.println("cannot parse date correctly");

                }


            } else if (path.endsWith("submit_task.html")) {
                Task task = null;
                try {
                    String owner = user;
                    String recipient = request.getParam("recipients");
                    Calendar creationDate = Calendar.getInstance();

                    Calendar dueDate = Calendar.getInstance();
                    Date date = (Email.DATE_FORMAT).parse(request.getParam("due_date") + " " + request.getParam("due_time"));
                    dueDate.setTime(date);
                    String title = request.getParam("title");
                    String data = request.getParam("data");
                    task = new Task(owner, creationDate, dueDate, recipient, title, data, false, false);

                    //Send the Task Immediately!
                    SMTPSession session = new SMTPSession(ServerRun.SMTP_SEVER, ServerRun.SMTP_PORT, ServerRun.AUTHENTICATE);
                    session.sendMessage(task);
                    if (request.getParam("id").equalsIgnoreCase("new")) {
                        this.tasks.add(task);
                    } else {
                        this.tasks.update(task);

                    }

                    path = generateTasksPage(user);

                } catch (Exception e) {
                    System.err.println("Error creating Task");
                    System.err.println(e);
                    path = ServerRun.root + "/submit_reminder.html";

                }


            }//Trying to submit a poll
            else if (path.endsWith("submit_poll.html")) {
                Poll poll = null;
                try {
                    String owner = user;
                    String rec = request.getParam("recipients").trim();
                    String[] recipients = rec.split(CRLF);
                    rec = null;
                    String opts = request.getParam("options").trim();

                    String[] options = opts.split(CRLF);
                    opts = null;
                    Calendar creationDate = Calendar.getInstance();

                    String title = request.getParam("title");
                    String data = request.getParam("data");
                    PollArray results = new PollArray(options.length);
                    poll = new Poll(owner, creationDate, creationDate, recipients, title, data, false, results, options, false);

                    //Send the Poll Immediately!
                    SMTPSession session = new SMTPSession(ServerRun.SMTP_SEVER, ServerRun.SMTP_PORT, ServerRun.AUTHENTICATE);
                    session.sendMessage(poll);

                    this.polls.add(poll);//always adds polls (not only when id=new)

                    path = generatePollsPage(user);

                } catch (Exception e) {
                    System.err.println("Error creating poll");
                    e.printStackTrace();
                    path = ServerRun.root + "/submit_polls.html";

                }

            } else if (path.endsWith("submit_chat_message.html")) {
                try {
                    String text = request.getParam("message");
                    ChatMessage message = new ChatMessage(user, text);
                    SQLiteDBHelper db = new SQLiteDBHelper();
                    db.addChatMessage(message);
                    path = generateChat(db.getChatQueue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (path.endsWith("task_reply.html")) {
                path = completeTask(Integer.parseInt(request.getParam("id")));

            } else if (path.endsWith("poll_reply.html")) {
                path = pollReply(Integer.parseInt(request.getParam("id")), Integer.parseInt(request.getParam("ans")));
            }


        }
        /*
         *checks if the client is trying to surf outside the root directory :/
         */
        page = new

                File(path);

        File serverDir = new File(ServerRun.root);
        if (!page.getPath().startsWith(serverDir.getAbsolutePath()))

        {
            return ServerRun.root + ServerRun.$403page;
        }

        if (!(new File(path)).exists())

        {
            path = ServerRun.root + ServerRun.$404page;

        }

        return path;
    }

    private String generateChat(ChatQueue queue) {
        String path = null;
        try {
            path = ServerRun.root + "chat_messages.html";
            File page = new File(path);
            page.getParentFile().mkdirs();
            page.createNewFile();
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            writer.println(queue.echoMessagesHTML());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return ServerRun.root + "chat.html";
    }

    /**
     * @param user
     * @return
     */
    private String generateRemindersPage(String user) {
        String path = null;
        try {
            path = ServerRun.root + "/" + user + "/reminders.html";
            File page = new File(path);
            page.getParentFile().mkdirs();
            page.createNewFile();
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            String headline = "<table border=\"0\"><tr><td><b>#</td><td><b>reminder " +
                    "title</td><td><b>Creation time</td>" +
                    "<td><b>Due time</td> <td><b>Status</td>" +
                    "<td><a href=\"reminder_editor.html?id=new\">New</a></td>" +
                    "</tr>";

            writer.println(headline);
            headline = null;
            for (Object obj : reminders) {
                Reminder email = (Reminder) obj;

                if (email.getOwner().equals(user)) {
                    StringBuilder sb = new StringBuilder();

                    sb.append("<tr><td>");
                    sb.append(email.getId() + "</td>");
                    sb.append("<td>" + email.getTitle() + "</td>");
                    Date date = email.getCreation_date().getTime();
                    sb.append("<td>" + Email.DATE_FORMAT.format(date) + "</td>");
                    date = email.getDue_date().getTime();
                    sb.append("<td>" + Email.DATE_FORMAT.format(date) + "</td>");
                    sb.append("<td>" + (email.completed ? "completed" : "in progress") + "</td>");

                    if (!email.isComplete()) {
                        sb.append("<td><a href=\"reminders.html?id=");
                        sb.append(email.getId());
                        sb.append("\">Delete</a></td>");
                    }

                    sb.append("</tr>");
                    writer.println(sb.toString());
                }

            }
            writer.println("</table>");
            writer.close();
        } catch (IOException e) {
            System.err.println("cannot create polls file");
            e.printStackTrace();

        }
        return path;

    }

    /**
     * Generates a page with the polls according to the format
     *
     * @param user
     * @return
     */
    private String generatePollsPage(String user) {
        String path = null;
        try {
            path = ServerRun.root + "/" + user + "/polls.html";
            File page = new File(path);
            page.getParentFile().mkdirs();
            page.createNewFile();
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            String headline = "<table border=\"0\"><tr><td><b>#</td><td><b>poll " +
                    "title</td><td><b>Creation time</td>" +
                    "<td><b>Status</td>" +
                    "<td><b>results</td>" +
                    "<td><a href=\"poll_editor.html?id=new\">New</a></td>" +
                    "</tr>";

            writer.println(headline);
            headline = null;
            for (Object obj : polls) {
                Poll email = (Poll) obj;
                if (email.getOwner().equals(user)) {
                    StringBuilder sb = new StringBuilder();

                    sb.append("<tr><td>");
                    sb.append(email.getId() + "</td>");
                    sb.append("<td>" + email.getTitle() + "</td>");
                    Date date = email.getCreation_date().getTime();
                    sb.append("<td>" + Email.DATE_FORMAT.format(date) + "</td>");
                    sb.append("<td>" + (email.completed ? "completed" : "in progress") + "</td>");
                    sb.append("<td>");
                    sb.append("<table>");
                    for (int i = 0; i < email.getOptionsCount(); i++) {
                        sb.append("<tr>" + email.getOption(i));
                        sb.append("=" + email.getVotesForOption(i));
                        sb.append(" </tr>");

                    }
                    sb.append("</table>");

                    sb.append("</td>");

                    sb.append("<td><a href=\"polls.html?id=");
                    sb.append(email.getId());
                    sb.append("\">Delete</a></td>");
                    sb.append("</tr>");
                    writer.println(sb.toString());
                }

            }
            writer.println("</table>");
            writer.close();
        } catch (IOException e) {
            System.err.println("cannot create polls file");
            e.printStackTrace();

        }
        return path;

    }

    /**
     * @param user
     * @return
     */
    private String generateTasksPage(String user) {
        //TODO: make sure only uncompleted tasks may be edited

        String path = null;
        try {


            path = ServerRun.root + "/" + user + "/tasks.html";
            File page = new File(path);
            page.getParentFile().mkdirs();
            page.createNewFile();
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            String headline = "<table border=\"0\"><tr><td><b>#</td><td><b>task " +
                    "title</td><td><b>Creation time</td>" +
                    "<td><b>Due time</td> <td><b>Status</td>" +
                    "<td><a href=\"task_editor.html?id=new\">New</a></td>" +
                    "</tr>";

            writer.println(headline);
            headline = null;
            for (Object obj : tasks) {
                Task email = (Task) obj;
                if (email.getOwner().equals(user)) {
                    StringBuilder sb = new StringBuilder();

                    sb.append("<tr><td>");
                    sb.append(email.getId() + "</td>");
                    sb.append("<td>" + email.getTitle() + "</td>");
                    Date date = email.getCreation_date().getTime();
                    sb.append("<td>" + Email.DATE_FORMAT.format(date) + "</td>");
                    date = email.getDue_date().getTime();
                    sb.append("<td>" + Email.DATE_FORMAT.format(date) + "</td>");
                    String status;
                    if (email.isComplete()) {
                        status = "completed";
                    } else if (email.isExpired()) {
                        status = "time is due";
                    } else {
                        status = "in progress...";
                    }
                    sb.append("<td>" + status + "</td>");
                    sb.append("<td><a href=\"tasks.html?id=");
                    sb.append(email.getId());
                    sb.append("\">Delete</a></td>");
                    sb.append("</tr>");
                    writer.println(sb.toString());
                }

            }
            writer.println("</table>");
            writer.close();
        } catch (IOException e) {
            System.err.println("cannot create polls file");
            e.printStackTrace();

        }
        return path;
    }


    private void deleteTask(String id) {
        tasks.remove(Integer.parseInt(id));

    }

    private void deleteReminder(String id) {
        reminders.remove(Integer.parseInt(id));

    }

    private void deletePoll(String id) {
        polls.remove(Integer.parseInt(id));

    }

    public String pollReply(int id, int option) {
        Poll poll = (Poll) polls.getId(id);
        String path = null;
        if (poll.isComplete()) {
            path = ServerRun.root + "poll_completed.html";
        } else {
            poll.addVote(option);
            polls.update(poll);
            path = ServerRun.root + "poll_reply.html";
            //String owner, Calendar creation_date, Calendar due_date,
            // String recipient, String title, String data,boolean completed
            String data = "Option " + poll.getOption(option);
            String title = poll.getTitle() + " new vote recieved!";
            Reminder reminder = new Reminder(poll.getOwner(), Calendar.getInstance(),
                    Calendar.getInstance(), poll.owner, title, data, false);
            path = ServerRun.root + "poll_reply.html";
        }


        return path;


    }

    /**
     * handles task reply
     * @param id the id of the task to be announced as complete
     * @return
     */
    private String completeTask(int id) {
        try {

            Task task = (Task) tasks.getId(id);
            if (!task.isExpired()) {
                task.setCompleted(true);
                tasks.update(task);
            } else {
               return ServerRun.root+"task_over_due.html";
            }

        } catch (NumberFormatException e) {
            System.err.println("Cannot mark task as complete");
        } catch (Exception e) {
            System.err.println("Probably the task was not found");

        }
        return ServerRun.root + "task_reply.html";
    }

}
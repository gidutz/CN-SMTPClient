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

    EmailArrayList tasks;
    EmailArrayList reminders;
    EmailArrayList polls;

    public ClientHandler(Socket clientSocket, EmailArrayList tasks, EmailArrayList reminders, EmailArrayList polls) {

        this.clientSocket = clientSocket;

        this.tasks = tasks;
        this.reminders = reminders;
        this.polls = polls;
    }

    /**
     * Starts handling the client
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
            int responseCode = 200;
//            if (!request.getRequestURL().equalsIgnoreCase(path)) {
//                responseCode = 301;
//
//            }
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
            page = new File(path);
        }


        if (request.getHeader("Cookie") == null) {
            if (!path.contains("/images/") && !path.contains("/css/")) {
                path = ServerRun.root + ServerRun.defaultPage;

            } else if (path.endsWith("task_reply.html")) {
                try {
                    int id = Integer.parseInt(request.getParam("id"));
                    Task task = (Task) tasks.getId(id);
                    task.setCompleted(true);
                } catch (NumberFormatException e) {
                    System.err.println("Cannot mark task as complete");
                } catch (Exception e) {
                    System.err.println("Porobably the task was not found");

                }


            } else if (path.endsWith("poll_reply.html")) {
                //TODO:handle poll reply
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
                    deleteEmail(id);

                } else {
                    path = generateTasksPage(user);
                }
            } else if (path.endsWith("remainders.html")) {
                if ((id = request.getParam("id")) != null) {
                    deleteEmail(id);

                } else {
                    path = generateReminders(user);
                }
            } else if (path.endsWith("polls.html")) {
                if ((id = request.getParam("id")) != null) {
                    deleteEmail(id);

                } else {
                    path = generatePollsPage(user);
                }
            } else if (path.endsWith("submit_reminder.html")) {
                Reminder reminder = null;
                try {
                    String owner = user;
                    String[] recipients = request.getParam("recipients").split(";");
                    Calendar creationDate = Calendar.getInstance();
                    Calendar dueDate = Calendar.getInstance();
                    Date date = (Email.DATE_FORMAT).parse(request.getParam("due_date") + " " + request.getParam("due_time"));
                    dueDate.setTime(date);
                    String title = request.getParam("title");
                    String data = request.getParam("data");
                    reminder = new Reminder(owner, creationDate, dueDate, recipients, title, data,false);
                    if (request.getParam("id").equalsIgnoreCase("new")) {
                        this.reminders.add(reminder);
                    } else {
                        this.reminders.remove(Integer.parseInt(request.getParam("id")));
                        this.reminders.add(reminder);

                    }
                    path = generateReminders(user);

                } catch (ParseException e) {
                    System.err.println("cannot parse date correctly");

                }


            } else if (path.endsWith("submit_task.html")) {
                Task task = null;
                try {
                    String owner = user;
                    String[] recipients = request.getParam("recipients").split(";");
                    Calendar creationDate = Calendar.getInstance();
                    Calendar dueDate = Calendar.getInstance();
                    Date date = (Email.DATE_FORMAT).parse(request.getParam("due_date") + " " + request.getParam("due_time"));
                    dueDate.setTime(date);
                    String title = request.getParam("title");
                    String data = request.getParam("data");
                    task = new Task(owner, creationDate, dueDate, recipients[0], title, data, false);
                    if (request.getParam("id").equalsIgnoreCase("new")) {
                        this.tasks.add(task);
                    } else {
                        this.tasks.remove(Integer.parseInt(request.getParam("id")));
                        this.tasks.add(task);

                    }
                    path = generateReminders(user);

                } catch (ParseException e) {
                    System.err.println("cannot parse date correctly");

                }

            } else if (path.endsWith("submit_poll.html")) {
                if (request.getParam("id").equalsIgnoreCase("new")) {
                    //TODO: create new reminder and redirect to the list;
                } else {
                    //TODO: update the reminder whose id = getParam("id")
                }
            }


        }

        if (page.getAbsoluteFile().isAbsolute()) {
            // TODO:check if the server root path is the start of

            return path;
        } else if (!(new File(path)).exists()) {
            path = path + ServerRun.$404page;
        }

        return path;
    }

    private String generateReminders(String user) {
        String path = null;
        try {
            path = ServerRun.root + "/" + user + "/polls.html";
            File page = new File(path);
            page.getParentFile().mkdirs();
            page.createNewFile();
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            String headline = "<table border=\"0\"><tr><td><b>#</td><td><b>reminder " +
                    "title</td><td><b>Creation time</td>" +
                    "<td><b>Due time</td> <td><b>Status</td>" +
                    "<td><a href=\"remainder_editor.html?id=new\">New</a></td>" +
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
                    sb.append("<td><a href=\"tasks.html?id=del\">Delete</a></td>");
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
            String headline = "<table border=\"0\"><tr><td><b>#</td><td><b>task " +
                    "title</td><td><b>Creation time</td>" +
                    "<td><b>Due time</td> <td><b>Status</td>" +
                    "<td><a href=\"task_editor.html?id=new\">New</a></td>" +
                    "</tr>";

            writer.println(headline);
            headline = null;
            for (Object obj : polls) {
                Poll email = (Poll) obj;
                if (email.getOwner().equals(user)) {
                    StringBuilder sb = new StringBuilder();

                    sb.append("<tr><td>");
                    sb.append(email.getId() + "<td>");
                    sb.append("<td>" + email.getTitle() + "<td>");
                    Date date = email.getCreation_date().getTime();
                    sb.append("<td>" + Email.DATE_FORMAT.format(date) + "<td>");
                    date = email.getDue_date().getTime();
                    sb.append("<td>" + Email.DATE_FORMAT.format(date) + "<td>");
                    sb.append("<td>" + (email.completed ? "completed" : "in progress") + "<td>");
                    sb.append("<td><a href=\"tasks.html?id=del\">Delete</a></td>");
                    sb.append("<tr>");
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


    private String generateTasksPage(String user) {
        String path = null;
        try {
            path = ServerRun.root + "/" + user + "/tasks.html";
            File page = new File(path);
            page.getParentFile().mkdirs();
            page.createNewFile();
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            writer.println("The first line");
            writer.println("The second line");
            writer.close();
        } catch (IOException e) {
            System.err.println("cannot create tasks file");
            e.printStackTrace();

        }
        return path;
    }


    private void deleteEmail(String id) {

        //Delete the poll and
    }

}
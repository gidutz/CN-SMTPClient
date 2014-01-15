import java.sql.*;
import java.util.*;
import java.util.Date;

public class SQLiteDBHelper {
    private final String FIELD_ID = "id";
    private final String FIELD_OWNER = "owner";
    private final String FIELD_RECP = "recps";
    private final String FIELD_CREATION = "createion_date";
    private final String FIELD_DUE = "due_date";
    private final String FIELD_TITLE = "title";
    private final String FIELD_DATA = "data";
    private final String FIELD_COMPLETED = "completed";
    private final String FIELD_SENT = "sent";
    private final String FIELD_ANSWERS = "ans";
    private final String FIELD_POLL_OPTS = "opll_options";

    private final String CHAT_USER = "user";
    private final String CHAT_MESSAGE = "message";
    private  final String TABLE_REMS = "reminders";
    private final  String TABLE_TASKS = "tasks";
    private final  String TABLE_POLLS = "polls";

    public static Object lock = new Object();


    Connection c;

    public SQLiteDBHelper() {
    }

    public synchronized void openDatabase(String databaseName) {

            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:" + databaseName);
                System.out.println("Opened database successfully");

                HashMap<String, String> fields = new HashMap<String, String>();

                fields.put(FIELD_ID, "TEXT");
                fields.put(FIELD_OWNER, "TEXT");
                fields.put(FIELD_RECP, "TEXT");
                fields.put(FIELD_CREATION, "TEXT");
                fields.put(FIELD_DUE, "TEXT");
                fields.put(FIELD_TITLE, "TEXT");
                fields.put(FIELD_DATA, "TEXT");
                fields.put(FIELD_SENT, "TEXT");
                fields.put(FIELD_COMPLETED, "TEXT");
                fields.put(FIELD_ANSWERS, "TEXT");
                fields.put(FIELD_POLL_OPTS, "TEXT");
                c.close();

                createTable(TABLE_REMS, fields);
                createTable(TABLE_TASKS, fields);
                createTable(TABLE_POLLS, fields);

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
            System.out.println("Table created successfully");


    }

    /**
     * create the three tables the i need
     *
     * @param tableName
     * @param fields
     */
    public synchronized void createTable(String tableName, HashMap<String, String> fields) {
        String databaseName = null;
        if (tableName.equalsIgnoreCase(TABLE_TASKS)){
            databaseName = ServerRun.TASK_DB;
        }else if (tableName.equalsIgnoreCase(TABLE_REMS)){
            databaseName = ServerRun.REM_DB;

        }else if (tableName.equalsIgnoreCase(TABLE_POLLS)){
            databaseName = ServerRun.POLL_DB;

        }


            Statement stmt = null;
            try {
                c = DriverManager.getConnection("jdbc:sqlite:" + databaseName);

                stmt = c.createStatement();
                StringBuilder sql = new StringBuilder();
                sql.append("CREATE TABLE IF NOT EXISTS " + tableName);
                sql.append("(");
                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    sql.append(entry.getKey() + " " + entry.getValue() + ", ");
                }
                sql.deleteCharAt(sql.lastIndexOf(","));
                sql.append(");");

                stmt.executeUpdate(sql.toString());
                stmt.close();
                c.close();
                System.out.println("Table Created!");
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }



    }

    /**
     * Adds the email according to its type to the correct table return 1 upon
     * success or 0 if fails
     *
     * @param email
     * @return
     */
    public synchronized int add(Email email) {

            String table = null;
            String databaseName = null;
            if (email instanceof Task) {
                table = TABLE_TASKS;
                databaseName = ServerRun.TASK_DB;
            } else if (email instanceof Reminder) {
                table = TABLE_REMS;
                databaseName = ServerRun.REM_DB;
            } else if (email instanceof Poll) {
                table = TABLE_POLLS;
                databaseName = ServerRun.POLL_DB;

            } else {
                return 0;
            }

            try {
                c = DriverManager.getConnection("jdbc:sqlite:" + databaseName);

                Statement stmt = c.createStatement();
                StringBuilder sql = new StringBuilder();
                sql.append("INSERT INTO " + table + "(");
                sql.append(FIELD_ID + ",");
                sql.append(FIELD_OWNER + ",");
                sql.append(FIELD_RECP + ",");
                sql.append(FIELD_CREATION + ",");
                sql.append(FIELD_DUE + ",");
                sql.append(FIELD_TITLE + ",");
                sql.append(FIELD_COMPLETED + ",");
                sql.append(FIELD_SENT + ",");
                if (table.equals(TABLE_POLLS)) {
                    sql.append(FIELD_ANSWERS + ",");
                    sql.append(FIELD_POLL_OPTS + ",");

                }
                sql.append(FIELD_DATA + ")");
                sql.append("VALUES (");
                sql.append("\"" + email.getId() + "\",");
                sql.append("\"" + email.getOwner() + "\",");
                sql.append("\"" + email.getRecipientsString() + "\",");
                Date date = email.getCreation_date().getTime();
                sql.append("\"" + Email.DATE_FORMAT.format(date) + "\",");
                date = email.getDue_date().getTime();
                sql.append("\"" + Email.DATE_FORMAT.format(date) + "\",");
                sql.append("\"" + email.getTitle() + "\",");
                sql.append("\"" + email.isComplete() + "\",");
                sql.append("\"" + email.wasSent() + "\",");
                if (table.equals(TABLE_POLLS)) {
                    Poll poll = (Poll) email;
                    sql.append("\"" + poll.getPollResults() + "\",");
                    sql.append("\"" + poll.getOptions() + "\",");

                }
                sql.append("\"" + email.getData() + "\")");

                sql.append(";");

                stmt.executeUpdate(sql.toString());
                System.out.println("Record Inserted!");

                stmt.close();
                c.close();
            } catch (SQLException e) {
                return 0;
            }

            return 1;


    }

    /**
     * Deletes an Email from the table returns 1 upon success or 0 if fails
     *
     * @param email
     * @return
     */
    public synchronized int remove(Email email) {



            String table = null;
            String databaseName = null;
            if (email instanceof Task) {
                table = TABLE_TASKS;
                databaseName = ServerRun.TASK_DB;
            } else if (email instanceof Reminder) {
                table = TABLE_REMS;
                databaseName = ServerRun.REM_DB;
            } else if (email instanceof Poll) {
                table = TABLE_POLLS;
                databaseName = ServerRun.POLL_DB;

            } else {
                return 0;
            }

            try {
                c = DriverManager.getConnection("jdbc:sqlite:" + databaseName);

                Statement stmt = c.createStatement();
                StringBuilder sql = new StringBuilder();
                sql.append("DELETE FROM " + table + " WHERE id=\"");

                sql.append(email.getId());
                sql.append("\";");

                stmt.executeUpdate(sql.toString());
                System.out.println("Record Inserted!");

                stmt.close();
                c.close();
            } catch (SQLException e) {

                return 0;
            }

            return 1;

    }

    public synchronized int updateEmail(Email email) {
            String table = null;
            String databaseName = null;
            if (email instanceof Task) {
                table = TABLE_TASKS;
                databaseName = ServerRun.TASK_DB;
            } else if (email instanceof Reminder) {
                table = TABLE_REMS;
                databaseName = ServerRun.REM_DB;
            } else if (email instanceof Poll) {
                table = TABLE_POLLS;
                databaseName = ServerRun.POLL_DB;

            } else {
                return 0;
            }

            try {
                c = DriverManager.getConnection("jdbc:sqlite:" + databaseName);

                Statement stmt = c.createStatement();
                StringBuilder sql = new StringBuilder();
                sql.append("UPDATE " + table + " SET ");
                sql.append(FIELD_OWNER + "=");
                sql.append("\"" + email.getOwner() + "\",");

                sql.append(FIELD_RECP + "=");
                sql.append("\"" + email.getRecipientsString() + "\",");

                sql.append(FIELD_CREATION + "=");
                Date date = email.getCreation_date().getTime();
                sql.append("\"" + Email.DATE_FORMAT.format(date) + "\",");

                sql.append(FIELD_DUE + "=");
                date = email.getDue_date().getTime();
                sql.append("\"" + Email.DATE_FORMAT.format(date) + "\",");

                sql.append(FIELD_TITLE + "=");
                sql.append("\"" + email.getTitle() + "\",");

                sql.append(FIELD_COMPLETED + "=");
                sql.append("\"" + email.isComplete() + "\",");

                sql.append(FIELD_SENT + "=");
                sql.append("\"" + email.wasSent() + "\",");

                if (table.equals(TABLE_POLLS)) {
                    Poll poll = (Poll) email;
                    sql.append(FIELD_ANSWERS + "=");
                    sql.append("\"" + poll.getPollResults() + "\",");
                }


                sql.append(FIELD_DATA + "= ");
                sql.append("\"" + email.getData() + "\" ");
                sql.append("WHERE id = " + email.getId());

                sql.append(";");

                stmt.executeUpdate(sql.toString());
                System.out.println("Record Updated!");

                stmt.close();
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }

            return 1;

    }

    /**
     * Returns an array of all the Tasks
     *
     * @param tasksList@return
     */
    public synchronized EmailArrayList<Task> getAllTasks(EmailArrayList<Task> tasksList) {


            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:" + ServerRun.TASK_DB);
                c.setAutoCommit(false);
                //System.out.println("Loading tasks... (this may take a while)");

                stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM tasks;");
                while (rs.next()) {
                    try {
                        int id = Integer.parseInt(rs.getString(FIELD_ID));
                        String owner = rs.getString(FIELD_OWNER);
                        String[] recipients = rs.getString(FIELD_RECP).split(";");
                        Calendar creationDate = Calendar.getInstance();
                        Date date = (Email.DATE_FORMAT).parse(rs.getString(FIELD_CREATION));
                        creationDate.setTime(date);
                        Calendar dueDate = Calendar.getInstance();
                        date = (Email.DATE_FORMAT).parse(rs.getString(FIELD_DUE));
                        dueDate.setTime(date);
                        String title = rs.getString(FIELD_TITLE);
                        String data = rs.getString(FIELD_DATA);
                        boolean completed = rs.getString(FIELD_COMPLETED).equalsIgnoreCase("true");
                        boolean sent = rs.getString(FIELD_SENT).equalsIgnoreCase("true");
                        Task task = new Task(owner, creationDate, dueDate, recipients[0], title, data, completed, sent);
                        task.setId(id);
                        tasksList.loadFromDisk(task);
                    } catch (Exception e) {
                        System.err.println("cannot add task");
                    }


                }
                rs.close();
                stmt.close();
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // System.out.println("Tasks loaded successfully");

            return tasksList;

    }


    /**
     * Returns an array of all the reminders
     *
     * @param reminderList
     * @return
     */
    public synchronized EmailArrayList<Reminder> getAllReminders(EmailArrayList<Reminder> reminderList) {
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:" + ServerRun.REM_DB);
                c.setAutoCommit(false);
                //System.out.println("Loading reminders... (this may take a while)");

                stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM reminders;");
                Reminder reminder;
                while (rs.next()) {
                    try {
                        int id = Integer.parseInt(rs.getString(FIELD_ID));
                        String owner = rs.getString(FIELD_OWNER);
                        Calendar creationDate = Calendar.getInstance();
                        Date date = (Email.DATE_FORMAT).parse(rs.getString(FIELD_CREATION));
                        creationDate.setTime(date);
                        Calendar dueDate = Calendar.getInstance();
                        date = (Email.DATE_FORMAT).parse(rs.getString(FIELD_DUE));
                        dueDate.setTime(date);
                        String title = rs.getString(FIELD_TITLE);
                        String data = rs.getString(FIELD_DATA);
                        boolean completed = rs.getString(FIELD_COMPLETED).equalsIgnoreCase("true");
                        reminder = new Reminder(owner, creationDate, dueDate,
                                owner, title,
                                data, completed);
                        reminder.setId(id);
                        reminderList.loadFromDisk(reminder);
                    } catch (Exception e) {
                        System.err.println("cannot add reminder");
                    }


                }
                rs.close();
                stmt.close();
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return reminderList;

    }

    /**
     * Returns an array of all the polls
     *
     * @param pollList@return
     */
    public synchronized EmailArrayList<Poll> getAllPolls(EmailArrayList<Poll> pollList) {


            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:" + ServerRun.POLL_DB);
                c.setAutoCommit(false);

                stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM polls;");
                while (rs.next()) {
                    try {
                        int id = Integer.parseInt(rs.getString(FIELD_ID));
                        String owner = rs.getString(FIELD_OWNER);
                        String str = rs.getString(FIELD_RECP);
                        String[] recipients = str.split(";");
                        Calendar creationDate = Calendar.getInstance();
                        Date date = (Email.DATE_FORMAT).parse(rs.getString(FIELD_CREATION));
                        creationDate.setTime(date);
                        Calendar dueDate = Calendar.getInstance();
                        date = (Email.DATE_FORMAT).parse(rs.getString(FIELD_DUE));
                        dueDate.setTime(date);
                        String title = rs.getString(FIELD_TITLE);
                        String data = rs.getString(FIELD_DATA);
                        boolean completed = rs.getString(FIELD_COMPLETED).equalsIgnoreCase("true");
                        boolean sent = rs.getString(FIELD_SENT).equalsIgnoreCase("true");
                        PollArray pollArray = PollArray.parsePollArray(rs.getString(FIELD_ANSWERS));
                        String[] options = rs.getString(FIELD_POLL_OPTS).split(";");
                        Poll poll = new Poll(owner, creationDate, dueDate, recipients, title, data, completed, pollArray, options, sent);
                        poll.setId(id);
                        pollList.loadFromDisk(poll);
                    } catch (Exception e) {
                        System.err.println("cannot add poll");
                    }


                }
                rs.close();
                stmt.close();
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return pollList;

    }

    public synchronized void initializeChatTable() {

            try {
                c = DriverManager.getConnection("jdbc:sqlite:" + ServerRun.CHAT_DB);
                Statement stmt = c.createStatement();
                String update = "CREATE TABLE IF NOT EXISTS chats (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + CHAT_MESSAGE + " TEXT, " + CHAT_USER + " TEXT);";
                stmt.executeUpdate(update);

                stmt.close();
                c.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

    }

    public synchronized  int addChatMessage(ChatMessage message) {


        try {
            c = DriverManager.getConnection("jdbc:sqlite:" + ServerRun.CHAT_DB);

            Statement stmt = c.createStatement();

            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO chats (");
            sql.append(CHAT_MESSAGE + ",");
            sql.append(CHAT_USER + ") ");
            sql.append("VALUES (");
            sql.append("\"" + message.getMessage() + "\",");
            sql.append("\"" + message.getUser() + "\")");

            sql.append(";");

            stmt.executeUpdate(sql.toString());

            stmt.close();
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

        return 1;

    }

    public synchronized  ChatQueue getChatQueue() {
        ChatQueue queue = new ChatQueue();
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"+ServerRun.CHAT_DB);
            c.setAutoCommit(false);
            //System.out.println("Loading reminders... (this may take a while)");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM chats;");
            ChatMessage message;
            while (rs.next()) {
                try {
                    String user = rs.getString(CHAT_USER);
                    String text = rs.getString(CHAT_MESSAGE);
                    message = new ChatMessage(user, text);
                    queue.addMessage(message);

                } catch (Exception e) {
                    System.err.println("cannot add reminder");
                }


            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
        }
        //System.out.println("Loaded reminders successfully");

        return queue;


    }
}

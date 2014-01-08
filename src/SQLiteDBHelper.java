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
    private final String FIELD_ANSWER = "ans_";
    private final String FIELD_ANSWER_1 = "ans_1";
    private final String FIELD_ANSWER_2 = "ans_2";
    private final String FIELD_ANSWER_3 = "ans_3";
    private final String FIELD_ANSWER_4 = "ans_4";
    private final String FIELD_ANSWER_5 = "ans_5";
    private final String FIELD_ANSWER_6 = "ans_6";
    private final String FIELD_ANSWER_7 = "ans_7";
    private final String FIELD_ANSWER_8 = "ans_8";
    private final String FIELD_ANSWER_9 = "ans_9";
    private final String FIELD_ANSWER_10 = "ans_10";
    Connection c;

    public void openDatabase(String dbPath, String dbName) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            System.out.println("Opened database successfully");

            HashMap<String, String> fields = new HashMap<String, String>();

            fields.put(FIELD_ID, "TEXT");
            fields.put(FIELD_OWNER, "TEXT");
            fields.put(FIELD_RECP, "TEXT");
            fields.put(FIELD_CREATION, "TEXT");
            fields.put(FIELD_DUE, "TEXT");
            fields.put(FIELD_TITLE, "TEXT");
            fields.put(FIELD_DATA, "TEXT");
            fields.put(FIELD_COMPLETED, "TEXT");
            fields.put(FIELD_ANSWER_1, "TEXT");
            fields.put(FIELD_ANSWER_2, "TEXT");
            fields.put(FIELD_ANSWER_3, "TEXT");
            fields.put(FIELD_ANSWER_4, "TEXT");
            fields.put(FIELD_ANSWER_5, "TEXT");
            fields.put(FIELD_ANSWER_6, "TEXT");
            fields.put(FIELD_ANSWER_7, "TEXT");
            fields.put(FIELD_ANSWER_8, "TEXT");
            fields.put(FIELD_ANSWER_9, "TEXT");
            fields.put(FIELD_ANSWER_10, "TEXT");

            createTable("reminders", fields);
            createTable("tasks", fields);
            createTable("polls", fields);

            c.close();
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
    public void createTable(String tableName, HashMap<String, String> fields) {

        Statement stmt = null;
        try {
            c = DriverManager.getConnection("jdbc:sqlite:test.db");

            stmt = c.createStatement();
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS " + tableName);
            sql.append("(");
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                sql.append(entry.getKey() + " " + entry.getValue());
            }

            sql.append(")");

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
    public int add(Email email) {
        String table = null;
        if (email instanceof Task) {
            table = "tasks";
        } else if (email instanceof Reminder) {
            table = "reminders";
        } else if (email instanceof Poll) {
            table = "polls";
        } else {
            return 0;
        }

        try {
            c = DriverManager.getConnection("jdbc:sqlite:test.db");

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
            sql.append(FIELD_DATA + ")");
            sql.append("VALUES (");
            sql.append(email.getId() + ",");
            sql.append(email.getOwner() + ",");
            sql.append(email.getRecipientsString() + ",");
            Date date = email.getCreation_date().getTime();
            sql.append(Email.DATE_FORMAT.format(date) + ",");
            date = email.getDue_date().getTime();
            sql.append(Email.DATE_FORMAT.format(date) + ",");
            sql.append(email.getTitle() + ",");
            sql.append(email.isComplete() + ",");
            sql.append(email.getData() + ")");

            sql.append(";");

            stmt.executeUpdate(sql.toString());
            System.out.println("Record Inserted!");

            stmt.close();
            c.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
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
    public int remove(Email email) {
        String table = null;
        if (email instanceof Task) {
            table = "tasks";
        } else if (email instanceof Reminder) {
            table = "reminders";
        } else if (email instanceof Poll) {
            table = "polls";
        }

        try {
            c = DriverManager.getConnection("jdbc:sqlite:test.db");

            Statement stmt = c.createStatement();
            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM " + table + "WHERE id=");

            sql.append(email.getId());
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
     * Returns an array of all the Tasks
     *
     * @param
     * @return
     */
    public EmailArrayList<Task> getAllTasks() {
        EmailArrayList<Task> tasks = new EmailArrayList<Task>(this);
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tasks;");
            while (rs.next()) {
                try {
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
                    Task task = new Task(owner, creationDate, dueDate, recipients[0], title, data, completed);
                    tasks.add(task);
                } catch (Exception e) {
                    System.err.println("cannot add task");
                }


            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
        }
        System.out.println("Operation done successfully");

        return tasks;
    }

    /**
     * Returns an array of all the reminders
     *
     * @return
     */
    public EmailArrayList<Reminder> getAllReminders() {
        EmailArrayList<Reminder> reminders = new EmailArrayList<Reminder>(this);
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tasks;");
            while (rs.next()) {
                try {
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

                    Reminder reminder = new Reminder(owner, creationDate, dueDate, recipients, title, data);
                    reminders.add(reminder);
                } catch (Exception e) {
                    System.err.println("cannot add reminder");
                }


            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
        }
        System.out.println("Operation done successfully");

        return reminders;
    }

    /**
     * Returns an array of all the polls
     *
     * @param
     * @return
     */
    public EmailArrayList<Poll> getAllPolls() {
        EmailArrayList<Poll> polls = new EmailArrayList<Poll>(this);

        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM tasks;");
            while (rs.next()) {
                try {
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

                    int[] results = new int[10];
                    for (int i = 1; i <= 10; i++) {
                        int result = Integer.parseInt(rs.getString(FIELD_ANSWER + i));
                        results[i] = result;
                    }

                    Poll poll = new Poll(owner, creationDate, dueDate, recipients, title, data, completed, results);
                    polls.add(poll);
                } catch (Exception e) {
                    System.err.println("cannot add poll");
                }


            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
        }
        System.out.println("Operation done successfully");


        return polls;
    }
}

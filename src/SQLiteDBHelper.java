import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class SQLiteDBHelper {
	Connection c;

	public void openDaatabase(String dbPath, String dbName) {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "CCREATE TABLE IF NOT EXISTS COMPANY "
					+ "(ID INT PRIMARY KEY     NOT NULL,"
					+ " NAME           TEXT    NOT NULL, "
					+ " AGE            INT     NOT NULL, "
					+ " ADDRESS        CHAR(50), " + " SALARY         REAL)";
			stmt.executeUpdate(sql);
			stmt.close();
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
		if (c != null) {
			Statement stmt = null;
			try {

				stmt = c.createStatement();
				StringBuilder sql = new StringBuilder();
				sql.append("CREATE TABLE IF NOT EXISTS" + tableName);
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
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
				System.exit(0);
			}
		}
	}

	public void createTables() {
		if (c != null) {
			Statement stmt = null;
			try {

				stmt = c.createStatement();
				String sql = "CREATE TABLE reminders "
						+ "(ID INT PRIMARY KEY     NOT NULL,"
						+ " recp      TEXT    NOT NULL, "
						+ " date      TEXT    NOT NULL, " + " title     TEXT, "
						+ " timestap  REAL)";
				stmt.executeUpdate(sql);

				stmt.executeUpdate(sql);
				stmt.close();
				c.close();
				System.out.println("Table Created!");
			} catch (Exception e) {
				System.err.println(e.getClass().getName() + ": "
						+ e.getMessage());
				System.exit(0);
			}
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
		if (email instanceof Task) {
			// TODO: add to task database/table?
			return 1;
		} else if (email instanceof Reminder) {
			// TODO: add to Reminder database/table
			return 1;
		} else if (email instanceof Poll) {
			// TODO: add to Polls database/table
			return 1;
		}
		return 0;
	}

	/**
	 * Deletes an Email from the table returns 1 upon success or 0 if fails
	 * 
	 * @param email
	 * @return
	 */
	public int remove(Email email) {
		if (email instanceof Task) {
			// TODO: remove from task database/table?
			return 1;
		} else if (email instanceof Reminder) {
			// TODO: remove from Reminder database/table
			return 1;
		} else if (email instanceof Poll) {
			// TODO: remove from Polls database/table
			return 1;
		}
		return 0;
	}

	/**
	 * Returns an array of all the Tasks
	 * 
	 * @param email
	 * @return
	 */
	public EmailArrayList<Task> getAllTasks() {
		EmailArrayList<Task> reminders = new EmailArrayList<Task>(this);

		return reminders;
	}

	/**
	 * Returns an array of all the reminders
	 * 
	 * @param email
	 * @return
	 */
	public EmailArrayList<Reminder> getAllReminders() {
		EmailArrayList<Reminder> reminders = new EmailArrayList<Reminder>(this);
		
		return reminders;
	}

	/**
	 * Returns an array of all the polls
	 * 
	 * @param email
	 * @return
	 */
	public EmailArrayList<Poll> getAllPolls() {
		EmailArrayList<Poll> reminders = new EmailArrayList<Poll>(this);

		return reminders;
	}
}

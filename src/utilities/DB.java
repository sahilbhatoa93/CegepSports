package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author SAHIL BHATOA
 *
 * 
 */
public class DB {

	private String connectionURL;
	private String userName;
	private String password;

	public DB() {

	}

	public DB(String connectionURL, String userName, String password) {

		this.connectionURL = "jdbc:oracle:thin:@" + connectionURL + ":1521:XE";
		this.userName = userName;
		this.password = password;
	}

	public Connection createConnection() throws ClassNotFoundException, SQLException {
		Connection con = null;

		Class.forName("oracle.jdbc.OracleDriver");
		con = (Connection) DriverManager.getConnection(connectionURL, userName, password);
		return con;
	}

	public void closeConnection(Connection connection) throws ClassNotFoundException, SQLException {

		connection.close();

	}

	public ResultSet fireQuery(Connection connection, String query) throws ClassNotFoundException, SQLException {
		ResultSet rs = null;
		PreparedStatement preparedStatement;
		preparedStatement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_UPDATABLE);
		rs = preparedStatement.executeQuery();
		return rs;
	}

	public String fireInsertQuery(Connection connection, String query, Boolean blobBoolean,
			InputStream imageInputStream, Boolean UpdateBoolean) {
		String result = "";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(query);
			if (blobBoolean) {
				if (UpdateBoolean) {
					if (!(imageInputStream == null))
						preparedStatement.setBlob(1, imageInputStream);
				} else if (!(imageInputStream == null))
					preparedStatement.setBinaryStream(1, imageInputStream, imageInputStream.available());
				else
					preparedStatement.setBinaryStream(1, null);
			}
			int rowsInserted = preparedStatement.executeUpdate();

			if (rowsInserted >= 1)
				result = "Success";
			else
				result = "Failed";
			// preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			result = e.getMessage().toString().replaceFirst("\n", "");
		} catch (IOException e) {
			System.out.println("No Image");
		}
		return result;
	}

	public String getTimeStamp() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = new Date();
		return dateFormat.format(date);
	}

	public long getDateTimeStampInUTCFormat() {
		return System.currentTimeMillis();
	}

	public long getDateFromString(String dateInString)  {
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
		String timeZone = "";
		long dateinMilliSec = 0L;
		if (dateInString != null) {
			for (int i = 0; i < dateInString.length(); i++) {
				char charAt2 = dateInString.charAt(i);
				if (Character.isLetter(charAt2)) {
					timeZone = dateInString.substring(i, dateInString.length());
					break;
				}
			}
			formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
			Date date = null;
			try {
				date = formatter.parse(dateInString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					date = formatter.parse(dateInString);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
				dateinMilliSec = date.getTime();
			
		
		}
		return dateinMilliSec;
	}

	public String formatErrorCode(String dbErrorCode) {
		if (dbErrorCode.contains("IO Error"))
			return dbErrorCode.replaceFirst("IO Error: The Network Adapter could not establish the connection", "")
					.concat("DIZ-0001: Please Try Again Later");
		else if (dbErrorCode.contains("Listener refused the connection with the following error:"))
			return dbErrorCode.replaceFirst(
					"Listener refused the connection with the following error:\nORA-12519, TNS:no appropriate service handler found\n",
					"").concat("DIZ-0002: Please Try Again Later");
		else
			return dbErrorCode.replaceFirst("ORA", "DIZ").replaceAll(" .+$", "").replace("\n", "")
					.concat(" Please Try Again Later");
	}

	public void createNotification(String tableName,String rowID) {
		System.out.println(tableName);
		System.out.println(rowID);
		String notificationContent="";
		String notificationType="";
		String studentEmail="";
		try {
			Connection connection = createConnection();
		if (tableName.equalsIgnoreCase("cegepsports.member"))
		{
			ResultSet rs2 = fireQuery(connection,
					"select member.status as status,student.email as email,teams.name as teamName from member "
					+ "inner join student on student.STUDENTID=member.STUDENT_ID inner join teams on teams.ID=member.TEAM_ID where rowID='"+rowID+"'");
			while (rs2.next())
			{
				if (rs2.getString("status").equals("1"))
				{
					notificationContent="Request Accepted to Join Team "+rs2.getString("teamName");
				}
				else
				{
					notificationContent="Request Denied to Join Team "+rs2.getString("teamName");
				}
				notificationType="REQUEST";
			}
		}
		else if (tableName.equalsIgnoreCase("cegepsports.teamtournament"))
		{
			
		}
		writeNotification(notificationContent, notificationType, studentEmail);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void writeNotification(String notificationContent,String notificationType,String studentEmail) {
		System.out.println(notificationContent);
		System.out.println(notificationType);
		System.out.println(studentEmail);
		try {
			Connection connection = createConnection();
			String operationResult = null;
				operationResult = fireInsertQuery(connection, "insert into notifications values (null,'"+notificationContent+"'"
						+ ",(TIMESTAMP '1970-01-01 00:00:00' AT TIME ZONE 'UTC' + "
							+ " numtodsinterval("+getDateTimeStampInUTCFormat()+"/1000,'second')) AT time zone tz_offset('utc')"
									+ ",'"+notificationType+"','"+studentEmail+"')", false, null, false);
			if (operationResult.equals("Success")) {
				
			} else {
				
			}
			closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}

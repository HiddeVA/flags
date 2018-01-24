package main_package;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class DBConnection
{
	private static String servername = "localhost";
	private static Connection connection;
	
	private DBConnection()
	{
	}
	
	public static Connection getInstance()
	{
		if (connection == null)
		{
			connection = getConnection();
			return connection;
		}
		else return connection;
	}
	
	public static void closeConnection()
	{
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (SQLException e) 
			{
				connection = null;
			}
		}
	}
	
	public static boolean isConnnected()
	{
		return connection != null;
	}
	
	public static String[] getServerInfo()
	{
		return new String[] {
				servername
		};
	}
	
	private static Connection getConnection()
	{
		Connection conn = null;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + servername + "/flags";
			String user = "flagmanager";
			String pw =	"VY2Zx4660whjkJcq";
			conn = DriverManager.getConnection(url, user, pw);
		}
		catch (SQLException e)
		{
			System.out.println(e.getMessage());
			System.out.println("Please check if your server is active");
			Logger.logToFile("Failed connection attempt to " + servername + " at " + LocalDateTime.now());
		}
		catch (ClassNotFoundException cnfe)
		{
			System.out.println("Could not find mysql driver. Please check if you exported correctly");
		}
		return conn;
	}
}

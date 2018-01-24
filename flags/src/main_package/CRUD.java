package main_package;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CRUD
{
	Connection connection;
	
	public CRUD(Connection connection)
	{
		this.connection = connection;
	}
	
	public String selectOne(String[] tablenames, String field, String clause)
	{
		String result;
		try 
		{//in case of empty result
			result = selectOne(tablenames, new String[] {field}, clause)[0];
			return result;
		}
		catch (IndexOutOfBoundsException iobe)
		{
			return "";
		}
	}
	
	public String[] selectOne(String[] tablenames, String[] fields, String clause)
	{
		if (clause == null || clause.equals(""))
		{
			throw new IllegalArgumentException("WHERE-clause can not be empty when selecting one record."
					+ "Use selectMulti / selectAll or add a clause");
		}
		String query = buildStatement(tablenames, fields) + " " + clause;
		try
		{
			Statement stmt = connection.createStatement();
			stmt.execute(query);
			
			ResultSet r = stmt.getResultSet();
			if (r.next())
			{
				String[] result = new String[fields.length];
				for (int i = 0; i < fields.length; i++)
				{
					result[i] = r.getString(fields[i]);
				}
				return result;
			}
			else
			{
				Logger.logToConsole("No results found for " + query);
				return new String[0];
			}
		}
		catch (SQLException sqle)
		{
			Logger.logToConsole("Error trying to run query " + query, sqle.getMessage());
			return new String[0];
		}
	}
	
	public List<String> selectMulti(String[] tablenames, String field, String clause)
	{
		//Selects one field and all rows that satisfy clause (clause can be null or empty)
		if (field == null || field.equals(""))
		{
			throw new IllegalArgumentException("Field cannot be empty. "
					+ "Use selectAll instead or create separate queries");
		}
		List<String> result = new ArrayList<>();
		String query = buildStatement(tablenames, field) + " " + (clause != null ? clause : "");
		try
		{
			Statement stmt = connection.createStatement();
			stmt.execute(query);
			
			ResultSet r = stmt.getResultSet();
			while (r.next())
			{
				result.add(r.getString(1));
			}
			return result;
		}
		catch (SQLException e)
		{
			Logger.logToConsole("Error trying to run query " + query, e.getMessage());
			return result; //should be an empty arraylist in case of exception
		}
	}
	
	public List<String[]> selectAll(String[] tablenames, String clause)
	{
		String tablename = tablenames[0];
		String condition = "";
		for (int i = 1; i < tablenames.length; i++)
		{
			tablename += ", " + tablenames[i];
			condition += i > 1 ? " AND" : " WHERE";
			condition += " " + tablenames[i] + ".id = " + tablenames[i -1] + "." + tablenames[i] + "_id";
		}
		String query = "SELECT " + tablenames[0] + ".* FROM " + tablename + condition + " " + clause;
		
		List<String[]> result = new ArrayList<String[]>();
		try 
		{
			Statement statement = connection.createStatement();
			statement.execute("SELECT count(*) FROM information_schema.columns WHERE table_name = '" + tablenames[0] + "'");
			//This works because all tablenames are unique across the database. If they're not unique, then you need to add the tableschema as well
			ResultSet r = statement.getResultSet();
			int columncount = r.next() ? r.getInt(1) : 0;
			
			statement.execute(query);
			r = statement.getResultSet();
			while (r.next())
			{
				String[] tmp = new String[columncount];
				for (int i = 0; i < columncount; i++)
				{
					tmp[i] = r.getString(i + 1);
				}
				result.add(tmp);
			}
			return result;
		}
		catch (SQLException e)
		{
			Logger.logToConsole("Error trying to run query: " + query, e.getMessage());
			return result;
		}
	}
	
	public int insertAndReturnKey(String tablename, String[] values)
	{
		String query = null;
		String valueLabel = values[0];
		for (int i = 1; i < values.length; i++)
		{
			valueLabel += "', '" + values[i];
		}
		try
		{
			Statement statement = connection.createStatement();
			query = "INSERT INTO " + tablename + " VALUES ('" + valueLabel + "')";
			statement.execute(query, Statement.RETURN_GENERATED_KEYS);
			ResultSet r = statement.getGeneratedKeys();
			if (r.next())
			{
				return r.getInt(1);
			}
			else
			{
				throw new SQLException("Unable to fetch key for " + tablename + "_id");
			}
		}
		catch (SQLException e) 
		{
			Logger.logToConsole("Error running query: " + query, e.getMessage());
			return 0;
		}
	}
	
	public boolean insert(String tablename, String[] values)
	{
		if (values.length == 0) throw new IllegalArgumentException("Attempt to insert empty record");
		String valueLabel = values[0];
		for (int i = 1; i < values.length; i++)
		{
			valueLabel += "', '" + values[i];
		}
		String query = "INSERT INTO " + tablename + " VALUES ('" + valueLabel + "')";
		Statement statement;
		try
		{
			statement = connection.createStatement();
			statement.execute(query);
			return true;
		}
		catch (SQLException e) 
		{
			Logger.logToConsole("Error running query: " + query, e.getMessage());
			return false;
		}
	}
	
	public boolean insertWithKey(String tablename, String[] values, int key)
	{
		if (key == 0) return false; //Shouldn't this throw an exception?
		if (values.length == 0) throw new IllegalArgumentException("Attempt to insert empty record");
		String valueLabel = values[0];
		for (int i = 1; i < values.length; i++)
		{
			valueLabel += "', '" + values[i];
		}
		String query = "INSERT INTO " + tablename + " VALUES (" + key + ", '" + valueLabel + "')";
		Statement statement;
		try 
		{
			statement = connection.createStatement();
			statement.execute(query);
			return true;
		}
		catch (SQLException e)
		{
			Logger.logToConsole("Error running query: " + query);
			e.getMessage();
			return false;
		}
	}
	
	public boolean insertMulti(String tablename, List<String[]> values, int key)
	{
		if (key == 0 || values.size() == 0) return false;
		String prepare = "";
		for (int i = 0; i < values.get(0).length; i++)
		{
			prepare += ", ?";
		}
		String query = "INSERT INTO " + tablename + " VALUES (" + key + prepare + ")";
		try 
		{
			PreparedStatement prStmt = connection.prepareStatement(query);
			for (String[] v : values)
			{
				for (int i = 0; i < v.length; i++)
				{
					prStmt.setString(i + 1, v[i]);
				}
				prStmt.execute();
			}
		}
		catch (SQLException e)
		{
			Logger.logToConsole("Error running query: " + query);
			e.getMessage();
		}
		return false;
	}
	
	public boolean insertMultiInOrder(String tablename, List<String[]> values, int key)
	{
		if (key == 0 || values.size() == 0) return false;
		String prepare = ", ?";
		for (int i = 0; i < values.get(0).length; i++)
		{
			prepare += ", ?";
		}
		String query = "INSERT INTO " + tablename + " VALUES (" + key + prepare + ")";
		try 
		{
			PreparedStatement prStmt = connection.prepareStatement(query);
			int vieworder = 1;
			for (String[] v : values)
			{
				for (int i = 0; i < v.length; i++)
				{
					prStmt.setString(i + 1, v[i]);
				}
				prStmt.setInt(v.length + 1, vieworder);
				prStmt.execute();
				vieworder++;
			}
		}
		catch (SQLException e)
		{
			Logger.logToConsole("Error running query: " + query);
			e.getMessage();
		}
		return false;
	}
	
	private String buildStatement(String[] tablenames, String...fields)
	{
		if (tablenames.length == 0) throw new IllegalArgumentException("No tablename given");
		String tablename = tablenames[0];
		String field = fields.length > 0 ? fields[0] : "*";
		for (int i = 1; i < fields.length; i++)
		{
			field += ", " + fields[i];
		}
		String condition = "";
		for (int i = 1; i < tablenames.length; i++)
		{
			tablename += ", " + tablenames[i];
			condition += i > 1 ? " AND" : " WHERE";
			condition += " " + tablenames[i] + ".id = " + tablenames[i - 1] + "." + tablenames[i] + "_id";
		}
		return "SELECT " + field + " FROM " + tablename + condition;
	}
}

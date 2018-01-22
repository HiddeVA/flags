package flags;

import java.util.Arrays;
import javafx.scene.paint.Color;

public abstract class SimpleFlag extends Flag
{
	protected int rows;
	protected Color[] rowColours;
	protected int activeRow = -1;
	
	public SimpleFlag()
	{
		super();
	}
	
	public SimpleFlag(int rows)
	{
		this();
		this.rows = rows;
		this.rowColours = new Color[rows];
		Arrays.fill(rowColours, Color.WHITE);
	}
	
	public SimpleFlag(Color...colours)
	{
		this();
		if (colours.length > 0) 
		{
			this.rows = colours.length;
			this.rowColours = colours;
		}
		else 
		{
			this.rows = 1;
			rowColours = new Color[] {Color.WHITE};
		}
	}
	
	public void setSymbolColour(Color clr)
	{
		if (activeRow < 0) 
		{
			super.setSymbolColour(clr);
		}
		else 
		{
			rowColours[activeRow] = clr;
			this.draw();
			activeRow = -1;
		}
	}
	
	public void setColor(int row, Color colour) throws IllegalArgumentException
	{
		if (row > this.rows || row <= -this.rows) throw new IllegalArgumentException("Invalid row number. Flag only contains " + rows + " rows.");
		if (row < this.rows)
		{
			rowColours[row] = colour;
		}
		else if (row == this.rows)
		{
			rowColours = Arrays.copyOf(rowColours, rows + 1);
			rowColours[rows] = colour;
			rows++;
		}
		else throw new IllegalArgumentException("Could not determine rownumber");
	}
	
	public void undoLastChange()
	{
		if (lastChange == ChangeType.RECOLOUR_RECTANGLE)
		{
			rowColours[activeRow] = lastColour;
		}
		else
		{
			super.undoLastChange();
		}
	}
	
	public Color[] getColors()
	{
		return this.rowColours;
	}
	
	public abstract void draw();
	
	public abstract String getSVGData();
}

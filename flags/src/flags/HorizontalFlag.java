package flags;

import java.util.List;
import javafx.scene.paint.Color;

public class HorizontalFlag extends SimpleFlag
{	
	public HorizontalFlag(int rows)
	{
		super(rows);
	}
	
	public HorizontalFlag(Color...colours)
	{
		super(colours);
	}
	
	public HorizontalFlag(List<Color> colours)
	{
		super(colours.toArray(new Color[colours.size()]));
	}
	
	public boolean checkForSymbol(double x, double y)
	{
		if (!super.checkForSymbol(x, y))
		{
			activeRow = (int) (y / this.getHeight() * rows);
			lastColour = rowColours[activeRow];
			objectColorPicker.setValue(rowColours[activeRow]);
			lastChange = ChangeType.RECOLOUR_RECTANGLE;
		}
		return false;
	}
	
	public void draw()
	{
		double rowHeight = this.getHeight() / this.rows;
		int i = 0;
		while (i < rows) 
		{
			gc.setFill(rowColours[i]);
			gc.fillRect(0, i++ * rowHeight, this.getWidth(), i * rowHeight);
		}
		for (FlagSymbol s : symbols)
		{
			s.draw();
		}
	}
	
	public String getSVGData()
	{
		StringBuilder SVGData = new StringBuilder();
		double rowsize = getHeight() / rows;
		for (int i = 0; i < rowColours.length; i++)
		{
			SVGData.append("<rect x=\"0\" y=\"" + i * rowsize +"\" width=\"" + getWidth() + "\" height=\"" + rowsize + "\" style=\"fill:rgb(" + getRGB(rowColours[i]) + ");\"></rect>");
		}
		for (FlagSymbol s : symbols)
		{
			SVGData.append(s.getSVGPath());
		}
		return SVGData.toString();
	}
	
	protected void snapToPosition(double x, double y)
	{
		if (Math.abs(y - getHeight() / 2) < getHeight() / 20)
		{
			activeSymbol.yPos = getHeight() / 2;			
		}
		else
		{
			activeSymbol.yPos = y;
		}
		activeSymbol.xPos = x;
	}
}

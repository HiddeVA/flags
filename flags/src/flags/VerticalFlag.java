package flags;

import java.util.List;
import javafx.scene.paint.Color;

public class VerticalFlag extends SimpleFlag
{	
	public VerticalFlag(int rows)
	{
		super(rows);
	}
	
	public VerticalFlag(Color...colours)
	{
		super(colours);
	}
	
	public VerticalFlag(List<Color> colours)
	{
		//once set, rownumber is not supposed to change
		super(colours.toArray(new Color[colours.size()]));
	}
	
	public boolean checkForSymbol(double x, double y)
	{
		if (!super.checkForSymbol(x, y)) 
		{
			activeRow = (int) (x / this.getWidth() * rows);
			lastColour = rowColours[activeRow];
			objectColorPicker.setValue(rowColours[activeRow]);
			lastChange = ChangeType.RECOLOUR_RECTANGLE;
		}
		return false;			
	}
	
	public void draw()
	{
		double rowWidth = this.getWidth() / this.rows;
		int i = 0;
		while (i < rows) {
			gc.setFill(rowColours[i]);
			gc.fillRect(i++ * rowWidth, 0, i * rowWidth, this.getHeight());
		}
		for (FlagSymbol s : symbols)
		{
			s.draw();
		}
	}
	
	public String getSVGData()
	{
		StringBuilder SVGData = new StringBuilder();
		double rowsize = getWidth() / rows;
		for (int i = 0; i < rowColours.length; i++)
		{
			SVGData.append("<rect  x=\"" + i * rowsize +"\" y=\"0\" width=\"" + rowsize + "\" height=\"" + getHeight() + "\" style=\"fill:rgb(" + getRGB(rowColours[i]) + ");\"></rect>");
		}
		for (FlagSymbol s : symbols)
		{
			SVGData.append(s.getSVGPath());
		}
		return SVGData.toString();
	}
}

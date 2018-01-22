package flags;

import java.util.List;
import java.util.stream.DoubleStream;
import javafx.scene.paint.Color;

public class DiagonalFlag extends SimpleFlag
{
	private double[] rowSizes;
	private double gradient = 1;
	private boolean startAtMiddle = true;
	
	public DiagonalFlag(int rows)
	{
		super(rows);
	}
	
	public DiagonalFlag(int rows, Color clr)
	{
		this(rows);
		this.backgroundColour = clr;
	}
	
	public DiagonalFlag(List<Color> colours)
	{
		super(colours.toArray(new Color[colours.size()]));
	}
	
	public DiagonalFlag(Color clr, List<Color> colours)
	{
		this(colours);
		this.backgroundColour = clr;
	}
	
	public double getOrientation()
	{
		return this.gradient;
	}
	
	public double[] getRowSizes()
	{
		return this.rowSizes;
	}
	
	public void resizeFlag(double scaleFactor)
	{
		for (int i = 0; i < rowSizes.length; i++)
		{
			rowSizes[i] *= scaleFactor;
		}
		super.resizeFlag(scaleFactor);
	}
	
	public void setDiagonalOrientation(double orientation)
	{
		this.gradient = orientation; //orientation can be zero. That would make a horizontal flag
		//This has a shortcoming. In order to get vertical lines, orientation has to be infinite.
		//Can be fixed by setting the orientation to some very high value
	}
	
	public void setRowSizes(List<Double> rows, boolean startAtMiddle)
	{
		if (rows.size() != rowColours.length) throw new IllegalArgumentException("Invalid array length. Array needs to be of size " + rowColours.length);
		rowSizes = new double[rows.size()];
		for (int i = 0; i < rowSizes.length; i++)
		{
			rowSizes[i] = rows.get(i);
		}
		if (DoubleStream.of(rowSizes).sum() > Math.hypot(this.getHeight(), this.getWidth()))
		{
			throw new IndexOutOfBoundsException("Rows are too large for the flag");
		}
		this.startAtMiddle = startAtMiddle;
	}
	
	protected void snapToPosition(double x, double y)
	{
		//This has a very small, but very frustrating deviation. And I don't know why
		double angle = Math.atan(getHeight() / getWidth()) * gradient;
		double z1 = getHeight() / 2 + Math.tan(angle) * getWidth() / 2;
		double z2 = y + x * Math.tan(angle);
		double distance = (z1 - z2) * Math.cos(angle);
		if (Math.abs(distance) < getWidth() / 20)
		{
			activeSymbol.yPos = y + Math.cos(angle) * distance;
			activeSymbol.xPos = x + Math.sin(angle) * distance;
		}
		else
		{
			activeSymbol.xPos = x;
			activeSymbol.yPos = y;
		}
	}
	
	public void draw()
	{
		gc.setFill(backgroundColour);
		gc.fillRect(0, 0, getWidth(), getHeight());
		
		//This flag is not drawn with rectangles or polygons. Its easier to simply draw lines with a certain thickness
		if (startAtMiddle)
		{
			double totalWidth = DoubleStream.of(rowSizes).sum();
			double angle = Math.atan(getHeight() / getWidth()) * gradient;
			//The angle the lines make with respect to the horizontal axis. It depends on the sizes of the flag and the standard gradient.
			//In this way, a gradient of 1 always goes from bottom-left to top-right
			
			if (Math.abs(gradient) <= 1) //With low gradients, lines are drawn right to left. 
			//We use absolute value, because negative gradients are perfectly fine
			{
				//startpoint is a point on a vertical line. Its starting value depends on the total width of the diagonal lines
				//The total width is not measured on the vertical axis, though, so we need to convert
				//That is why we multiply by a factor of 1/cos(angle)
				//The startpoint is relative to the middle of the diagonal lines and to the middle of the flag, so we divide by 2
				double startpoint = (getHeight() * (1 - gradient) - totalWidth / Math.cos(angle)) / 2;
				for (int i = 0; i < rowColours.length; i++)
				{
					//mind you that the initial startpoint is the top of the line. For proper drawing we need the middle of the line
					//After drawing, we move the startpoint to the bottom of the line, which is the top of the next line
					startpoint += rowSizes[i] / (2 * Math.cos(angle));
					gc.setStroke(rowColours[i]);
					gc.setLineWidth(rowSizes[i]);
					gc.strokeLine(getWidth(), startpoint, 0, getHeight() * gradient + startpoint);
					startpoint += rowSizes[i] / (2 * Math.cos(angle));
				}
			}
			else //with high gradients, lines are drawn top to bottom instead
			{
				//This goes in much the same way as the low-gradient variant, but it uses sin instead of cos.
				//This is because we are now measuring the angle with respect to the vertical axis instead of the horizontal axis
				double startpoint = getWidth() / 2 * (1 + 1 / gradient) - totalWidth / (2 * Math.sin(angle));
				for (int i = 0; i < rowColours.length; i++)
				{
					startpoint += rowSizes[i] / (2 * Math.sin(angle));
					gc.setStroke(rowColours[i]);
					gc.setLineWidth(rowSizes[i]);
					gc.strokeLine(startpoint, 0, startpoint - getWidth() / gradient, getHeight());
					startpoint += rowSizes[i] / (2 * Math.sin(angle));
				}
			}
			
		}
		else
		{
			System.out.println("Not yet implemented. Please select true for startAtMiddle");
			System.out.println("Implementing the start-at-middle was difficult enough already");
		}
		for(FlagSymbol f : symbols)
		{
			f.draw();
		}
	}
	
	public void rotateSymbol()
	{
		if (activeSymbol != null)
		{
			super.rotateSymbol();
		}
		else if (activeRow >= 0)
		{
			this.gradient += 1;
			this.draw();
		}
	}
	
	public String getSVGData()
	{
		throw new RuntimeException("Not implemented yet. Going to be difficult");
	}
}

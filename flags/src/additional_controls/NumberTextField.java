package additional_controls;

import javafx.scene.control.TextField;

public class NumberTextField extends TextField
{//Solution copy-pasted from (with some modifications)
//https://stackoverflow.com/questions/7555564/what-is-the-recommended-way-to-make-a-numeric-textfield-in-javafx
	public NumberTextField(int text)
	{
		//this is a separate constructor so that numbers don't automatically show as 1.0
		super(String.valueOf(text));
	}
	
	public NumberTextField(double text)
	{
		super(String.valueOf(text));
	}
	
	@Override public void replaceText(int start, int end, String text)
	{
		if (validate(text)) super.replaceText(start, end, text);
        if (this.getText().equals("")) this.setText("0"); //to guarantee that it can be parsed
	}
	@Override public void replaceSelection(String text)
	{
        if (validate(text)) super.replaceSelection(text);
        if (this.getText().equals("")) this.setText("0");
	}
	
	public double getNumericText()
	{
		try
		{
			return Double.parseDouble(this.getText());
		}
		catch (NumberFormatException nfe) 
		{
			return 0;
		}
	}
	
	private boolean validate(String text) 
	{
		return text.matches("[0-9]*");
    }
}
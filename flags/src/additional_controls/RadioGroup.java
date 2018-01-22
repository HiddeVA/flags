package additional_controls;

import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class RadioGroup extends VBox
{
	private ToggleGroup tg;
	
	public RadioGroup(RadioButton...buttons)
	{
		super(buttons);
		tg = new ToggleGroup();
		tg.getToggles().addAll(buttons);
		if (buttons.length > 0)
		{
			tg.selectToggle(buttons[0]);
		}
	}
	
	public String getSelectedToggle()
	{
		return ((RadioButton) tg.getSelectedToggle()).getText();
	}
}

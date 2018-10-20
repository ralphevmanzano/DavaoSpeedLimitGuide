package com.dslg.app.dialog;

public class CustomDialogObject
{
	private boolean twoButtons;
	private boolean isButtonOneRed;
	private boolean isButtonTwoRed;
	private String  title;
	private String  buttonOneLabel;
	private String  buttonTwoLabel;
	private String  message;
	
	public CustomDialogObject(boolean twoButtons, String title, String message, String buttonOneLabel,
			String buttonTwoLabel, boolean isButtonOneRed, boolean isButtonTwoRed)
	{
		this.twoButtons = twoButtons;
		if (this.twoButtons)
		{
			this.isButtonOneRed = true;
		}
		else
		{
			this.isButtonOneRed = false;
		}
		this.isButtonTwoRed = isButtonTwoRed;
		this.title = title;
		this.message = message;
		this.buttonOneLabel = buttonOneLabel;
		this.buttonTwoLabel = buttonTwoLabel;
		
	}
	
	public boolean isTwoButtons()
	{
		return twoButtons;
	}
	
	public void setTwoButtons(boolean twoButtons)
	{
		this.twoButtons = twoButtons;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public void setMessage(String message)
	{
		this.message = message;
	}
	
	public String getButtonOneLabel()
	{
		return buttonOneLabel;
	}
	
	public void setButtonOneLabel(String buttonOneLabel)
	{
		this.buttonOneLabel = buttonOneLabel;
	}
	
	public String getButtonTwoLabel()
	{
		return buttonTwoLabel;
	}
	
	public void setButtonTwoLabel(String buttonTwoLabel)
	{
		this.buttonTwoLabel = buttonTwoLabel;
	}
	
	public boolean isButtonOneRed()
	{
		return isButtonOneRed;
	}
	
	public void setButtonOneRed(boolean buttonOneRed)
	{
		isButtonOneRed = buttonOneRed;
	}
	
	public boolean isButtonTwoRed()
	{
		return isButtonTwoRed;
	}
	
	public void setButtonTwoRed(boolean buttonTwoRed)
	{
		isButtonTwoRed = buttonTwoRed;
	}
}
package application;

import com.jfoenix.controls.JFXTextField;

public class JFXNumberTextField extends JFXTextField {
	
	private int maxChars = 0;
	
	public void setMaxChars(int num) {
		maxChars = num;
	}
	
	
	public void replaceText(int start, int end, String text) {
        if (text.matches("[0-9]*")) {
          super.replaceText(start, end, text);
        }
        verify();
    }

    public void replaceSelection(String text) {
        if (text.matches("[0-9]*")) {
          super.replaceSelection(text);
        }
        verify();
    }
    
    private void verify() {
        if (maxChars>0 && getText().length() > maxChars) {
            setText(getText().substring(0, maxChars));
        }
    }
}

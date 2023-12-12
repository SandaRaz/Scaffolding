package gui.surdef;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class InputText extends JTextField implements FocusListener {
    Color textColor;
    String defaultText;

    public Color getTextColor() {
        return textColor;
    }
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public String getDefaultText() {
        return defaultText;
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }

    public InputText(String text, String defaultText,Color textColor){
        super(text);
        this.setDefaultText(defaultText);
        this.setTextColor(textColor);

        Font font = new Font("Roboto", Font.PLAIN,14);
        this.setFont(font);
        this.setForeground(this.getTextColor());

        this.addFocusListener(this);
    }

    @Override
    protected void paintComponent(Graphics g){
        if(!isOpaque() && getBorder() instanceof RoundedCornerBorder){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(getBackground());
            setForeground(getTextColor());
            g2.fill(((RoundedCornerBorder) getBorder()).getBorderShape(0, 0, getWidth() - 1, getHeight() - 1));
            g2.dispose();
        }
        super.paintComponent(g);
    }
    @Override
    public void updateUI(){
        super.updateUI();
        setOpaque(false);
        setBorder(new RoundedCornerBorder());
    }

    @Override
    public void focusGained(FocusEvent e) {
        if(this.getText().equals(getDefaultText())){
            this.setTextColor(Color.BLACK);
            this.setText("");
        }
    }
    @Override
    public void focusLost(FocusEvent e) {
        if(this.getText().isEmpty() || this.getText().isBlank()){
            this.setTextColor(Color.LIGHT_GRAY);
            this.setText(getDefaultText());
        }
    }
}

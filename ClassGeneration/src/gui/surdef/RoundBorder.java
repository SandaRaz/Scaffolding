package gui.surdef;

import javax.swing.border.Border;
import java.awt.*;

public class RoundBorder implements Border {
    private int radius;
    private Color color;

    public RoundBorder(int radius, Color color) {
        this.radius = radius;
        this.color = color;
    }

    @Override
    public void paintBorder(Component c,Graphics g,int x,int y, int width, int height){
        g.setColor(color);
        g.drawRoundRect(x,y,width-1,height-1,radius,radius);
    }

    @Override
    public Insets getBorderInsets(Component c){
        return new Insets(radius,radius,radius,radius);
    }

    @Override
    public boolean isBorderOpaque(){
        return true;
    }
}

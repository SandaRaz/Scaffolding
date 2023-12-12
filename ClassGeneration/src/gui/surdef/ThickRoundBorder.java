package gui.surdef;

import javax.swing.border.Border;
import java.awt.*;

public class ThickRoundBorder implements Border {
    private int radius;
    private Color color;
    private int thickness;

    public ThickRoundBorder(int radius, Color color, int thickness) {
        this.radius = radius;
        this.color = color;
        this.thickness = thickness;
    }

    @Override
    public void paintBorder(Component c,Graphics g,int x,int y, int width, int height){
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(color);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int borderWidth = 2 * (radius + thickness);
        g2d.drawRoundRect(x + thickness, y + thickness, width - 1 - 2 * thickness, height - 1 - 2 * thickness, radius, radius);
        //g.drawRoundRect(x,y,width-1,height-1,radius,radius);

        g2d.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c){
        int borderWidth = 2 * (radius + thickness);
        return new Insets(borderWidth,borderWidth,borderWidth,borderWidth);
    }

    @Override
    public boolean isBorderOpaque(){
        return false;
    }
}

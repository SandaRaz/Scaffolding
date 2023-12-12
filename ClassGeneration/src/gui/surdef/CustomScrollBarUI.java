package gui.surdef;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class CustomScrollBarUI extends BasicScrollBarUI implements MouseListener {
    private final Dimension d = new Dimension();
    private boolean isHovered = false;
    private Color hoverColor = Color.GRAY;
    private Color defaultColor = Color.LIGHT_GRAY;

    @Override
    protected JButton createDecreaseButton(int orientation){
        return new JButton(){
            @Override
            public Dimension getPreferredSize(){
                return d;
            }
        };
    }
    @Override
    protected JButton createIncreaseButton(int orientation){
        return new JButton(){
            @Override
            public Dimension getPreferredSize(){
                return d;
            }
        };
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c,Rectangle trackBounds){
        g.setColor(new Color(250,250,250));
        g.fillRect(trackBounds.x,trackBounds.y,trackBounds.width,trackBounds.height);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c,Rectangle thumbBounds){
        c.addMouseListener(this);
        if(thumbBounds.isEmpty() || !scrollbar.isEnabled()){
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(isHovered ? hoverColor : defaultColor);
        g2d.fillRoundRect(thumbBounds.x,thumbBounds.y,thumbBounds.width,thumbBounds.height,12,12);
        g2d.setColor(defaultColor);
        g2d.drawRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width,thumbBounds.height,12,12);
        g2d.dispose();
    }

    @Override
    protected void setThumbBounds(int x,int y,int width,int height){
        super.setThumbBounds(x,y,width,height);
        scrollbar.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        isHovered = true;
        scrollbar.repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        isHovered = false;
        scrollbar.repaint();
    }
}

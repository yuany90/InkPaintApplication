package org.yourorghere;


/**
 *
 * @author yuanlu
 */
import com.sun.opengl.util.Animator;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import com.sun.opengl.util.Animator;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.media.opengl.GLCanvas;
import javax.swing.JLabel;
/**
 *
 * @author Tim Boudreau
 */
public class PaintCanvas extends JLabel {
    private int brushDiameter, speed;
//    private final MouseL mouseListener = new MouseL();
    private BufferedImage backingImage = null;
    private final BrushSizeView brushView = new BrushSizeView();
    private Color color;
    private final JPanel panel = new JPanel();
    private GLCanvas glCanvas = new GLCanvas();
    GLRender render = new GLRender();
    final Animator animator;
    public PaintCanvas() {
        setFocusable(true);
        add(panel);
        add(glCanvas);
        glCanvas.addGLEventListener(render);
        glCanvas.addMouseListener(render);
	glCanvas.addMouseMotionListener(render);
        glCanvas.setSize(512,512);
//        glCanvas.setPreferredSize(new Dimension(512, 512));
        System.out.println(glCanvas.getSize());
        animator = new Animator(glCanvas);
        animator.start();
    }

    public void setBrushDiameter(int val) {
        this.brushDiameter = val;
        render.brushDiameter = val;
        brushView.repaint();
    }

    public int getBrushDiameter() {
        return brushDiameter;
    }
    public void setSpeed(int val) {
        this.speed = val;
        render.speed = val;
    }

    public int getSpeed() {
        return speed;
    }
    
    public void setColor(Color c) {
        this.color = c;
        render.color = c;
        brushView.repaint();
    }

    public Color getColor() {
        return color;
    }

    public void clear() {
        render.clear = true;
//        repaint();
    }
    public void fill() {
        render.fill = true;
    }

    JComponent getBrushSizeView() {
        return brushView;
    }

//    public BufferedImage getImage() {
//        int width = Math.min(getWidth(), 1600);
//        int height = Math.min(getHeight(), 1200);
//        if (backingImage == null || backingImage.getWidth() != width || backingImage.getHeight() != height) {
//            int newWidth = backingImage == null ? width : Math.max(width, backingImage.getWidth());
//            int newHeight = backingImage == null ? height : Math.max(height, backingImage.getHeight());
//            if (newHeight > height && newWidth > width && backingImage != null) {
//                return backingImage;
//            }
//            BufferedImage old = backingImage;
//            backingImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB_PRE);
//            Graphics2D g = backingImage.createGraphics();
//            g.setColor(Color.WHITE);
//            g.fillRect(0, 0, width, height);
//            if (old != null) {
//                g.drawRenderedImage(old,
//                        AffineTransform.getTranslateInstance(0, 0));
//            }
//            g.dispose();
//            setPreferredSize(new Dimension (newWidth, newHeight));
//        }
//        return backingImage;
//    }

    private class BrushSizeView extends JComponent {
        @Override
        public void paint(Graphics g) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Point p = new Point(getWidth() / 2, getHeight() / 2);
            int half = getBrushDiameter() / 2;
            int diam = getBrushDiameter();
            g.setColor(getColor());
            g.fillOval(p.x - half, p.y - half, diam, diam);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension (24, 24);
        }
    }

}

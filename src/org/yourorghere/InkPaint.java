package org.yourorghere;

/**
 *
 * @author yuanlu
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JFrame;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public final class InkPaint extends JFrame implements ActionListener, ChangeListener {
    private final PaintCanvas canvas = new PaintCanvas(); //The component the user draws on
    private final Image inkPaintIcon = new ImageIcon(getClass().getResource("/icon.png")).getImage();
    private final JMenuBar menubar = new JMenuBar();
    private final JMenu file = new JMenu("File");
    private final JMenuItem save = new JMenuItem("save");
    private final JMenuItem exit = new JMenuItem("exit");
    private final JMenu edit = new JMenu("Edit");
    private final JMenuItem clear = new JMenuItem("Clear");
    private final JMenuItem fill = new JMenuItem("Fill");
    private final JComponent preview = canvas.getBrushSizeView(); //A component in the toolbar that shows the paintbrush size
    private final JToolBar toolbar = new JToolBar(); //The toolbar
    private final JColorChooser color = new JColorChooser(); //Our color chooser component from the ColorChooser library
    private final JFileChooser chooser = new JFileChooser();
    private final JLabel label = new JLabel(
            "Color"); //A label for the color chooser
    private final JLabel brushSizeLabel = new JLabel(
            "Size"); //A label for the brush size slider
    private final JLabel brushFlowLabel = new JLabel(
            "Flow"); //A label for the brush size slider
    private final JSlider brushSizeSlider = new JSlider(1, 24); //A slider to set the brush size
//    private InstanceContent content = new InstanceContent(); //The bag of stuff we add/remove the Saver from, and store the last-used file in
    private final JSlider speedSlider = new JSlider(0, 127); //A slider to set the brush size
//    private InstanceContent content = new InstanceContent(); //The bag of stuff we add/remove the Saver from, and store the last-used file in
    private Color frontColor = Color.lightGray;
    private JPanel titlePanel = new JPanel();
    public InkPaint() {
        setIconImage(inkPaintIcon);
        setTitle("Ink Paint");
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        //Configure our components, attach listeners
//        color.addActionListener(this);
        save.addActionListener(this);
        exit.addActionListener(this);
        clear.addActionListener(this);
        fill.addActionListener(this);
        setJMenuBar(menubar);
        menubar.add(file);
        file.add(save);
        file.add(exit);
        menubar.add(edit);
        edit.add(clear);
        edit.add(fill);
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        //Configure the toolbar
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 7, 7));
        toolbar.add(titlePanel);
        titlePanel.add(label);
        titlePanel.setBackground(frontColor);
        canvas.setColor(frontColor);
        canvas.setBrushDiameter(10);
        brushSizeSlider.setPreferredSize(new Dimension(80,10));
        brushSizeSlider.setValue(canvas.getBrushDiameter());
        brushSizeSlider.addChangeListener(this);
        canvas.setSpeed(10);
        speedSlider.setPreferredSize(new Dimension(80,10));
        speedSlider.setValue(canvas.getSpeed());
        speedSlider.addChangeListener(this);
        
        titlePanel.addMouseListener(
            new MouseAdapter(){
                public void mouseClicked(MouseEvent e){
                    Color selectedColor = JColorChooser.showDialog(InkPaint.this, "Pick a Color"
                , Color.WHITE);
                    if (selectedColor != null){
                        frontColor = selectedColor;
                        titlePanel.setBackground(frontColor);
                        canvas.setColor(frontColor);
                    }
                }
             }   
        );
        toolbar.add(brushSizeLabel);
        toolbar.add(brushSizeSlider);
        toolbar.add(preview);
        toolbar.add(brushFlowLabel);
        toolbar.add(speedSlider);
        
        
    }


    public void stateChanged(ChangeEvent e) {
        if(e.getSource().equals(brushSizeSlider)){
            canvas.setBrushDiameter(brushSizeSlider.getValue());
        } else if(e.getSource().equals(speedSlider)){
            canvas.setSpeed(speedSlider.getValue());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(clear)) {
            canvas.clear();
        } else if(e.getSource().equals(save)){
          int option = chooser.showSaveDialog(InkPaint.this);
          if(option == JFileChooser.APPROVE_OPTION){  
            if(chooser.getSelectedFile()!=null){  
            File f = chooser.getSelectedFile(); 
            System.out.println(f.getName());
            if (f != null) {
                if (!f.getAbsolutePath().endsWith(".jpg")) {
                    f = new File(f.getAbsolutePath() + ".jpg");
                }
                try {
                    if (!f.exists()) {
                        if (!f.createNewFile()) {
                            System.err.println("Cannot create file:" + f.getName());
                            throw new IOException();
                        }
                    } else {
                        System.err.println("Rewrite file:" + f.getName());
                    }
                        ImageIO.write(canvas.render.getImage(), "jpg", f);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
           }
         }
        } else if(e.getSource().equals(exit)){
             canvas.animator.stop();
             System.exit(0);
        } else if(e.getSource().equals(fill)){
             canvas.fill();
        }
    }

    public static void main(String[] args) {
        final InkPaint inkPaint = new InkPaint();
        inkPaint.setSize(520, 600);
        inkPaint.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Run this on another thread than the AWT event queue to
                // make sure the call to Animator.stop() completes before
                // exiting
                new Thread(new Runnable() {
                    public void run() {
                        inkPaint.canvas.animator.stop();
                        System.exit(0);
                    }
                }).start();
            }
        });
        // Center frame
        inkPaint.setLocationRelativeTo(null);
        inkPaint.setVisible(true);
    }

}
 

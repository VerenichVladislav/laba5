package laba5;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;



public class MainFrame extends JFrame {

    private final int HEIGHT = 800;
    private final int WIDTH = 800;

    private boolean fileLoaded = false;

    private GraphicsDisplay display = new GraphicsDisplay();

    private JCheckBoxMenuItem showAxisMenuItem;
    private JCheckBoxMenuItem showMarkersMenuItem;
    private JCheckBoxMenuItem showGridMenuItem;

    private JMenuItem resetGraphicsMenuItem;
    private JMenuItem shapeRotateAntiClockItem;
    private JMenuItem saveToBinMenuItem;

    private JFileChooser fileChooser = null;

    public MainFrame()  {
        super("Построение графиков функций на основе подготовленных файлов");

        setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - WIDTH)/2, (kit.getScreenSize().height - HEIGHT)/2);
        //setExtendedState(MAXIMIZED_BOTH);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        Action openGraphicsAction = new AbstractAction("Open file") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    openGraphics(fileChooser.getSelectedFile());

            }
        };
        fileMenu.add(openGraphicsAction);

        Action saveToBinAction = new AbstractAction("Save to .bin file") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileChooser == null){
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    display.saveToBinFile(fileChooser.getSelectedFile());

            }
        };
        saveToBinMenuItem = fileMenu.add(saveToBinAction);

        JMenu graphicsMenu = new JMenu("Graphic");
        menuBar.add(graphicsMenu);


        Action showAxisAction = new AbstractAction("Show axis") {
            @Override
            public void actionPerformed(ActionEvent e) {
                display.setShowAxis(showAxisMenuItem.isSelected());
            }
        };
        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
        graphicsMenu.add(showAxisMenuItem);
        showAxisMenuItem.setSelected(true);


        Action showGridAction = new AbstractAction("Show grid") {
            @Override
            public void actionPerformed(ActionEvent e) {
                display.setShowGrid(showGridMenuItem.isSelected());
            }
        };
        showGridMenuItem = new JCheckBoxMenuItem(showGridAction);
        graphicsMenu.add(showGridMenuItem);
        showGridMenuItem.setSelected(true);


        Action showMarkersAction = new AbstractAction("Show markers") {
            @Override
            public void actionPerformed(ActionEvent e) {
                display.setShowMarkers(showMarkersMenuItem.isSelected());
            }
        };
        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
        graphicsMenu.add(showMarkersMenuItem);
        showMarkersMenuItem.setSelected(true);


        Action rotatesShapeAntiClockAction = new AbstractAction("Show rotate") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(display.isAntiClockRotate())
                {
                    display.setClockRotate(false);
                    display.setAntiClockRotate(false);
                }
                else
                    display.setAntiClockRotate(true);
            }
        };
        shapeRotateAntiClockItem = new JCheckBoxMenuItem(rotatesShapeAntiClockAction);
        graphicsMenu.add(shapeRotateAntiClockItem);
        shapeRotateAntiClockItem.setEnabled(false);
        graphicsMenu.addSeparator();


        Action resetGraphicsAction = new AbstractAction("Reset") {
            @Override
            public void actionPerformed(ActionEvent event) {
                MainFrame.this.display.reset();
            }
        };
        resetGraphicsMenuItem = new JMenuItem(resetGraphicsAction);
        graphicsMenu.add(resetGraphicsMenuItem);
        resetGraphicsMenuItem.setEnabled(false);


        graphicsMenu.addMenuListener(new GraphicsMenuListener());
        getContentPane().add(display, BorderLayout.CENTER);
    }

    protected void openGraphics(File selectedFile) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
            Double[][] graphicsData  = new Double[in.available()/(Double.SIZE/8)/2][];
            int i = 0;
            while (in.available() > 0) {
                Double x = in.readDouble();
                Double y = in.readDouble();
                graphicsData[i++] = new Double[] { x, y };
            }
            if (graphicsData != null && graphicsData.length > 0) {
                fileLoaded = true;
                resetGraphicsMenuItem.setEnabled(true);
                display.showGraphics(graphicsData);
            }
            in.close();
        }
        catch (FileNotFoundException e){
            JOptionPane.showMessageDialog(MainFrame.this, "Selected file not found", "Data load error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        catch (IOException ex){
            JOptionPane.showMessageDialog(MainFrame.this, "Points read from file error", "Data load error", JOptionPane.WARNING_MESSAGE);
            return;
        }
    }

    private class GraphicsMenuListener implements MenuListener {

        @Override
        public void menuCanceled(MenuEvent arg0) {

        }

        @Override
        public void menuDeselected(MenuEvent arg0) {

        }

        @Override
        public void menuSelected(MenuEvent arg0) {
            showAxisMenuItem.setEnabled(fileLoaded);
            showMarkersMenuItem.setEnabled(fileLoaded);
            showGridMenuItem.setEnabled(fileLoaded);
            shapeRotateAntiClockItem.setEnabled(fileLoaded);
            saveToBinMenuItem.setEnabled(fileLoaded);
        }
    }
    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }
}
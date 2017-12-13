package laba5;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.*;

public class GraphicsDisplay extends JPanel {

    private Double[][] graphicsData;
    private Double[][] originalData;

    private int selectedMarker = -1;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private double scaleX;
    private double scaleY;

    private double[][] zoom_point = new double[2][2];
    private ArrayList<double[][]> undoHistory = new ArrayList(5);
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean showGrid = true;
    private boolean clockRotate = false;
    private boolean antiClockRotate = false;

    private Font axisFont;
    private Font labelsFont;

    private BasicStroke axisStroke;
    private BasicStroke graphicsStroke;
    private BasicStroke markerStroke;
    private BasicStroke gridStroke;
    private BasicStroke selectionStroke;
    private static DecimalFormat formatter=(DecimalFormat)NumberFormat.getInstance();

    private boolean scaleMode = false;
    private boolean changeMode = false;
    private double[] originalPoint = new double[2];
    private Rectangle2D.Double selectionRect = new Rectangle2D.Double();

    public GraphicsDisplay ()	{
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[] {4, 1, 1, 1, 2, 1, 1, 1, 4}, 0.0f);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 5.0f, null, 0.0f);
        selectionStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[] { 10, 10 }, 0.0F);
        gridStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 5.0f, null, 2.0f);
        axisFont = new Font("Serif", Font.BOLD, 36);
        labelsFont = new java.awt.Font("Serif",0,15);
        addMouseMotionListener(new MouseMotionHandler());
        addMouseListener(new MouseHandler());
    }

    public void showGraphics(Double[][] graphicsData)	{
        this.graphicsData = graphicsData;
        this.originalData = graphicsData;

        minX = (graphicsData[0])[0];
        maxX = (graphicsData[graphicsData.length - 1])[0];
        minY = (graphicsData[0])[1];
        maxY = minY;

        for (int i = 1; i < graphicsData.length; i++) {
            if ((graphicsData[i])[1] < minY) {
                minY = (graphicsData[i])[1];
            }
            if ((graphicsData[i])[1] > maxY) {
                maxY = (graphicsData[i])[1];
            }
        }

        zoom(minX, maxY, maxX, minY);

    }

    public void zoom(double x1,double y1,double x2,double y2)	{
        this.zoom_point[0][0]=x1;
        this.zoom_point[0][1]=y1;
        this.zoom_point[1][0]=x2;
        this.zoom_point[1][1]=y2;
        this.repaint();
    }
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - zoom_point[0][0];
        double deltaY = zoom_point[0][1] - y;
        return new Point2D.Double(deltaX*scaleX, deltaY*scaleY);
    }

    protected double[] translatePointToXY(int x, int y)
    {
        return new double[] { this.zoom_point[0][0] + x / this.scaleX, this.zoom_point[0][1] - y / this.scaleY };
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }

    protected void paintGrid (Graphics2D canvas) {
        canvas.setStroke(gridStroke);
        canvas.setColor(Color.GRAY);

        double pos = zoom_point[0][0];;
        double step = (zoom_point[1][0] - zoom_point[0][0])/10;

        while (pos < zoom_point[1][0]){
            canvas.draw(new Line2D.Double(xyToPoint(pos, zoom_point[0][1]), xyToPoint(pos, zoom_point[1][1])));
            pos += step;
        }
        canvas.draw(new Line2D.Double(xyToPoint(zoom_point[1][0],zoom_point[0][1]), xyToPoint(zoom_point[1][0],zoom_point[1][1])));

        pos = zoom_point[1][1];
        step = (zoom_point[0][1] - zoom_point[1][1]) / 10;
        while (pos < zoom_point[0][1]){
            canvas.draw(new Line2D.Double(xyToPoint(zoom_point[0][0], pos), xyToPoint(zoom_point[1][0], pos)));
            pos=pos + step;
        }
        canvas.draw(new Line2D.Double(xyToPoint(zoom_point[0][0],zoom_point[0][1]), xyToPoint(zoom_point[1][0],zoom_point[0][1])));
    }

    protected void paintGraphics (Graphics2D canvas) {
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.BLUE);

        Double currentX = null;
        Double currentY = null;
        for (Double[] point : this.graphicsData)
        {
            if ((point[0].doubleValue() >= this.zoom_point[0][0]) && (point[1].doubleValue() <= this.zoom_point[0][1]) &&
                    (point[0].doubleValue() <= this.zoom_point[1][0]) && (point[1].doubleValue() >= this.zoom_point[1][1]))
            {
                if ((currentX != null) && (currentY != null)) {
                    canvas.draw(new Line2D.Double(xyToPoint(currentX.doubleValue(), currentY.doubleValue()),
                            xyToPoint(point[0].doubleValue(), point[1].doubleValue())));
                }
                currentX = point[0];
                currentY = point[1];
            }
        }
    }

    protected void paintAxis(Graphics2D canvas){

        canvas.setStroke(this.axisStroke);
        canvas.setColor(java.awt.Color.BLACK);
        canvas.setFont(this.axisFont);
        FontRenderContext context=canvas.getFontRenderContext();


        if (!(zoom_point[0][0] > 0 || zoom_point[1][0] < 0)){
            canvas.draw(new Line2D.Double(xyToPoint(0, zoom_point[0][1]),
                    xyToPoint(0, zoom_point[1][1])));
            canvas.draw(new Line2D.Double(xyToPoint(-(zoom_point[1][0] - zoom_point[0][0]) * 0.0025,
                    zoom_point[0][1] - (zoom_point[0][1] - zoom_point[1][1]) * 0.015),xyToPoint(0,zoom_point[0][1])));
            canvas.draw(new Line2D.Double(xyToPoint((zoom_point[1][0] - zoom_point[0][0]) * 0.0025,
                    zoom_point[0][1] - (zoom_point[0][1] - zoom_point[1][1]) * 0.015),
                    xyToPoint(0, zoom_point[0][1])));
            Rectangle2D bounds = axisFont.getStringBounds("y",context);
            Point2D.Double labelPos = xyToPoint(0.0, zoom_point[0][1]);
            canvas.drawString("y",(float)labelPos.x + 10,(float)(labelPos.y + bounds.getHeight() / 2));
        }
        if (!(zoom_point[1][1] > 0.0D || zoom_point[0][1] < 0.0D)){
            canvas.draw(new Line2D.Double(xyToPoint(zoom_point[0][0],0),
                    xyToPoint(zoom_point[1][0],0)));
            canvas.draw(new Line2D.Double(xyToPoint(zoom_point[1][0] - (zoom_point[1][0] - zoom_point[0][0]) * 0,
                    (zoom_point[0][1] - zoom_point[1][1]) * 0.005), xyToPoint(zoom_point[1][0], 0)));
            canvas.draw(new Line2D.Double(xyToPoint(zoom_point[1][0] - (zoom_point[1][0] - zoom_point[0][0]) * 0.01,
                    -(zoom_point[0][1] - zoom_point[1][1]) * 0.005), xyToPoint(zoom_point[1][0], 0)));
            Rectangle2D bounds = axisFont.getStringBounds("x",context);
            Point2D.Double labelPos = xyToPoint(this.zoom_point[1][0],0.0D);
            canvas.drawString("x",(float)(labelPos.x - bounds.getWidth() - 10),(float)(labelPos.y - bounds.getHeight() / 2));
        }
    }

    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(this.markerStroke);
        canvas.setColor(Color.RED);
        canvas.setPaint(Color.RED);

        for (Double[] point : graphicsData) {
            if (isAllEven(point[1])) canvas.setPaint(Color.GREEN);
            else canvas.setPaint(Color.RED);
            Point2D.Double center = xyToPoint(point[0], point[1]);
            canvas.draw(new Line2D.Double(shiftPoint(center, -5, 0), shiftPoint(center, 5, 0)));
            canvas.draw(new Line2D.Double(shiftPoint(center, 0, -5), shiftPoint(center, 0, 5)));

            canvas.draw(new Line2D.Double(shiftPoint(center, -2, -5), shiftPoint(center, 2, -5)));
            canvas.draw(new Line2D.Double(shiftPoint(center, -2, 5), shiftPoint(center, 2, 5)));
            canvas.draw(new Line2D.Double(shiftPoint(center, -5, 2), shiftPoint(center, -5, -2)));
            canvas.draw(new Line2D.Double(shiftPoint(center, 5, 2), shiftPoint(center, 5, -2)));
        }
    }

    private boolean isAllEven(double x){
        int n = Math.abs((int)x);
        if(n == 0) return true;
        while(n >= 1){
            if((n%10)%2 != 0) return false;
            n/=10;
        }
        return true;
    }


    private void paintLabels(Graphics2D canvas){
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.labelsFont);
        FontRenderContext context=canvas.getFontRenderContext();
        double labelYPos;
        double labelXPos;
        if (!(zoom_point[1][1] >= 0 || zoom_point[0][1] <= 0))
            labelYPos = 0;
        else labelYPos = zoom_point[1][1];
        if (!(zoom_point[0][0] >= 0 || zoom_point[1][0] <= 0.0D))
            labelXPos=0;
        else labelXPos = zoom_point[0][0];
        double pos = zoom_point[0][0];
        double step = (zoom_point[1][0] - zoom_point[0][0]) / 10;
        while (pos < zoom_point[1][0]){
            java.awt.geom.Point2D.Double point = xyToPoint(pos,labelYPos);
            String label = formatter.format(pos);
            Rectangle2D bounds = labelsFont.getStringBounds(label,context);
            canvas.drawString(label, (float)(point.getX() + 5), (float)(point.getY() - bounds.getHeight()));
            pos=pos + step;
        }
        pos = zoom_point[1][1];
        step = (zoom_point[0][1] - zoom_point[1][1]) / 10.0D;
        while (pos < zoom_point[0][1]){
            Point2D.Double point = xyToPoint(labelXPos,pos);
            String label=formatter.format(pos);
            Rectangle2D bounds = labelsFont.getStringBounds(label,context);
            canvas.drawString(label,(float)(point.getX() + 5),(float)(point.getY() - bounds.getHeight()));
            pos=pos + step;
        }
        if (selectedMarker >= 0)
        {
            Point2D.Double point = xyToPoint((graphicsData[selectedMarker])[0],
                    (graphicsData[selectedMarker])[1]);
            String label = "X=" + formatter.format((graphicsData[selectedMarker])[0]) +
                    ", Y=" + formatter.format((graphicsData[selectedMarker])[1]);
            Rectangle2D bounds = labelsFont.getStringBounds(label, context);
            canvas.setColor(Color.BLACK);
            canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        scaleX=this.getSize().getWidth() / (this.zoom_point[1][0] - this.zoom_point[0][0]);
        scaleY=this.getSize().getHeight() / (this.zoom_point[0][1] - this.zoom_point[1][1]);
        if ((this.graphicsData == null) || (this.graphicsData.length == 0)) return;


        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Font oldFont = canvas.getFont();
        Paint oldPaint = canvas.getPaint();
        if (clockRotate) {
            AffineTransform at = AffineTransform.getRotateInstance(Math.PI/2, getSize().getWidth()/2, getSize().getHeight()/2);
            at.concatenate(new AffineTransform(getSize().getHeight()/getSize().getWidth(), 0.0, 0.0, getSize().getWidth()/getSize().getHeight(),
                    (getSize().getWidth()-getSize().getHeight())/2, (getSize().getHeight()-getSize().getWidth())/2));
            canvas.setTransform(at);

        }
        if (antiClockRotate) {
            AffineTransform at = AffineTransform.getRotateInstance(-Math.PI/2, getSize().getWidth()/2, getSize().getHeight()/2);
            at.concatenate(new AffineTransform(getSize().getHeight()/getSize().getWidth(), 0.0, 0.0, getSize().getWidth()/getSize().getHeight(),
                    (getSize().getWidth()-getSize().getHeight())/2, (getSize().getHeight()-getSize().getWidth())/2));
            canvas.setTransform(at);


        }
        if(showGrid)
            paintGrid(canvas);
        if (showAxis){
            paintAxis(canvas);
            paintLabels(canvas);
        }
        paintGraphics(canvas);
        if (showMarkers)
            paintMarkers(canvas);

        paintSelection(canvas);
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);

    }

    private void paintSelection(Graphics2D canvas) {
        if (!scaleMode) return;
        canvas.setStroke(selectionStroke);
        canvas.setColor(Color.BLACK);
        canvas.draw(selectionRect);
    }


    public void setClockRotate(boolean clockRotate) {
        this.clockRotate = clockRotate;
        repaint();
    }


    public void setAntiClockRotate(boolean antiClockRotate) {
        this.antiClockRotate = antiClockRotate;
        repaint();
    }

    public void setShowGrid(boolean showGrid){
        this.showGrid = showGrid;
        repaint();
    }


    public boolean isAntiClockRotate() {
        return antiClockRotate;
    }


    public void reset() {
        showGraphics(this.originalData);
    }


    protected int findSelectedPoint(int x, int y)
    {
        if (graphicsData == null) return -1;
        int pos = 0;
        for (Double[] point : graphicsData) {
            Point2D.Double screenPoint = xyToPoint(point[0], point[1]);
            double distance = (screenPoint.getX() - x) * (screenPoint.getX() - x) + (screenPoint.getY() - y) * (screenPoint.getY() - y);
            if (distance < 100) return pos;
            pos++;
        }	    return -1;
    }

    public void saveToBinFile(File selectedFile)	{
        try{
            DataOutputStream out = new DataOutputStream(new FileOutputStream(selectedFile));
            for (Double[] point : graphicsData){
                out.writeDouble(point[0]);
                out.writeDouble(point[1]);
            }
            out.close();

        }
        catch (FileNotFoundException e){ /* :) */ }
        catch (IOException e){ /* :) */ }

    }


    //Îòñëåæèâàíèå ñîñòîÿíèÿ
    public class MouseHandler extends MouseAdapter {
        public MouseHandler() {
        }
        public void mouseClicked(MouseEvent ev) {
            //Âîññòàíîâëåíèå íà øàã íàçàä
            //ïðàâîé êíîïêîé ìûøè
            if (ev.getButton() == 3) {
                if (undoHistory.size() > 0)
                {
                    zoom_point = (undoHistory.get(undoHistory.size() - 1));

                    undoHistory.remove(undoHistory.size() - 1);
                } else {
                    zoom(minX, maxY, maxX, minY);
                }
                repaint();
            }
        }

        public void mousePressed(MouseEvent ev) {
            //Åñëè çàæàëè
            //Ðÿäîì ñ ìàðêåðîì-èçìåíÿåì åãî
            //åñëè â ïð-âå, âûäåëÿåì ïðÿìîóãîëüíèêîì
            if (ev.getButton() != 1) return;
            selectedMarker = findSelectedPoint(ev.getX(), ev.getY());
            originalPoint = translatePointToXY(ev.getX(), ev.getY());
            if (selectedMarker >= 0) {
                changeMode = true;
                setCursor(Cursor.getPredefinedCursor(8));
            }
            else {
                scaleMode = true;
                setCursor(Cursor.getPredefinedCursor(5));
                selectionRect.setFrame(ev.getX(), ev.getY(), 1.0D, 1.0D);
            }
        }

        public void mouseReleased(MouseEvent ev) {
            if (ev.getButton() != 1) return;

            //Êîãäà îòïóñêàåì ìûøü, ïðîâåðÿåì:
            //Åñëè èçìåíÿëè ìàñøòàá-
            //ñîõðàíÿåì òî÷êó è çóìèì
            setCursor(Cursor.getPredefinedCursor(0));
            if (changeMode) {
                changeMode = false;
            } else {
                scaleMode = false;
                double[] finalPoint = translatePointToXY(ev.getX(), ev.getY());
                undoHistory.add(zoom_point);
                zoom_point = new double[2][2];
                zoom(originalPoint[0], originalPoint[1], finalPoint[0], finalPoint[1]);
                repaint();
            }
        }
    }

    //Îòñëåæèâàíèå êîîðäèíàò
    public class MouseMotionHandler implements MouseMotionListener {

        public void mouseDragged(MouseEvent ev) {
            //Äâèãàåì ìàðêåð
            if (changeMode) {
                double[] currentPoint = translatePointToXY(ev.getX(), ev.getY());
                double newY = (graphicsData[selectedMarker])[1] +
                        (currentPoint[1] - (graphicsData[selectedMarker])[1]);
                if (newY > zoom_point[0][1]) {
                    newY = zoom_point[0][1];
                }
                if (newY < zoom_point[1][1]) {
                    newY = zoom_point[1][1];
                }
                (graphicsData[selectedMarker])[1] = newY;
                repaint();
            }
            //èëè ïðÿìîóãîëüíèê
            else {
                double width = ev.getX() - selectionRect.getX();
                if (width < 5.0D) {
                    width = 5.0D;
                }
                double height = ev.getY() - selectionRect.getY();
                if (height < 5.0D) {
                    height = 5.0D;
                }
                selectionRect.setFrame(selectionRect.getX(), selectionRect.getY(), width, height);
                repaint();
            }
        }


        public void mouseMoved(MouseEvent ev) {
            selectedMarker = findSelectedPoint(ev.getX(), ev.getY());
            if (selectedMarker >= 0)
                setCursor(Cursor.getPredefinedCursor(8));
            else {
                setCursor(Cursor.getPredefinedCursor(0));
            }
            repaint();
        }

    }

}
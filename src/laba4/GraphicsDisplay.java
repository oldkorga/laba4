package laba4;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import laba4.GraphicsDisplay;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
	private Double[][] graphicsData;
	private Double[][] altGraphicsData;
	private float[] dash = { 3, 1, 3, 1, 3, 1, 1, 1, 1, 1, 1, 1 };
    private double[][] viewport = new double[2][2];
    private double scaleX;
    private double scaleY;

    
    private List<double[][]> zoomHistory = new ArrayList<>();
    private Rectangle2D.Double selectionRect = new Rectangle2D.Double(); // Прямоугольник выделенной области
    private Point2D.Double selectionStart = new Point2D.Double();   // Начальная точка выделения
    private boolean isSelecting = false; 
    
    
	private boolean showAxis = true;
	private boolean showMarkers = true;
	private boolean showAltGraphic = false;
	private boolean isRotated = false;
	private BasicStroke graphicsStroke;
	private BasicStroke altGraphicsStroke;
	private BasicStroke axisStroke;
	private BasicStroke markerStroke;
	private Font axisFont;
	private boolean isDraggingAltGraphicsData = false; // Указывает, из какого массива перетаскивается точка
    private Point2D.Double draggedPoint = null;
    private int draggedIndex = -1;

    
    public GraphicsDisplay() {
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f);
        altGraphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        axisFont = new Font("Serif", Font.BOLD, 36);

        
        
        
        addMouseListener(new MouseAdapter() {
        	
        	 public void mouseClicked(MouseEvent ev) {
                 if (ev.getButton() == 3) {
                	 if (!zoomHistory.isEmpty()) {
        	             double[][] lastViewport = zoomHistory.remove(zoomHistory.size() - 1);
        	             viewport[0][0] = lastViewport[0][0];
        	             viewport[0][1] = lastViewport[0][1];
        	             viewport[1][0] = lastViewport[1][0];
        	             viewport[1][1] = lastViewport[1][1];
        	             repaint();
        	         }
        	     }

             }
        	
        	
        	 @Override
        	 public void mousePressed(MouseEvent e) {
        		 double[] mousePos = translatePointToXY(e.getX(), e.getY());
        	     Point2D.Double clickedPoint = new Point2D.Double();
        	     clickedPoint.setLocation(mousePos[0], mousePos[1]);

        	     if (e.getButton() == MouseEvent.BUTTON1) {
        	         // Проверяем, была ли нажата точка на графике
        	         if (graphicsData != null) {
        	             for (int i = 0; i < graphicsData.length; i++) {
        	                 Point2D.Double markerCenter = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
        	                 if (markerCenter.distance(e.getPoint()) < 7) {
        	                     draggedPoint = clickedPoint;
        	                     draggedIndex = i;
        	                     isDraggingAltGraphicsData = false;
        	                     return;
        	                 }
        	             }
        	         }

        	         if (altGraphicsData != null) {
        	             for (int i = 0; i < altGraphicsData.length; i++) {
        	                 Point2D.Double markerCenter = xyToPoint(altGraphicsData[i][0], altGraphicsData[i][1]);
        	                 if (markerCenter.distance(e.getPoint()) < 7) {
        	                     draggedPoint = clickedPoint;
        	                     draggedIndex = i;
        	                     isDraggingAltGraphicsData = true;
        	                     return;
        	                 }
        	             }
        	         }
        	         if(draggedIndex == -1) {
        	        	 isSelecting = true;	
        	             selectionStart = clickedPoint;
        	             setCursor(Cursor.getPredefinedCursor(5));
        	             GraphicsDisplay.this.selectionRect.setFrame(e.getX(), e.getY(), 1.0D, 1.0D);
        	         }
        	         
        	         
        	     }
        	 }


        	 @Override
        	 public void mouseReleased(MouseEvent e) {
        	     if (e.getButton() == 1) {
        	    	 if(draggedIndex == -1) {
        	         setCursor(Cursor.getPredefinedCursor(0));
        	         isSelecting = false;
            		 double[] mousePos = translatePointToXY(e.getX(), e.getY());
        	         Point2D.Double selectionEnd = new Point2D.Double();
        	         selectionEnd.setLocation(mousePos[0], mousePos[1]);

        	         double[][] currentViewport = new double[][] {
        	                 {viewport[0][0], viewport[0][1]},
        	                 {viewport[1][0], viewport[1][1]}
        	         };

        	         zoomHistory.add(currentViewport);
        	         
        	         zoomToRegion(selectionStart.x, selectionStart.y, selectionEnd.x, selectionEnd.y);
        	         repaint();

        	         selectionRect = new Rectangle2D.Double();
        	     }
        	     else {
        	    	 draggedPoint = null;
            	     draggedIndex = -1;
            	     isDraggingAltGraphicsData = false;
        	     }
        	 }
        	 }
        	 


        });

        addMouseMotionListener(new MouseMotionAdapter() {
        	@Override
        	public void mouseDragged(MouseEvent e) {
        	    if (draggedPoint != null && draggedIndex != -1) {
        	        // Обновляем координаты перетаскиваемой точки
        	        double newY = translatePointToXY(e.getX(), e.getY())[1];
        	        
        	        if (newY > GraphicsDisplay.this.viewport[0][1]) {
                        newY = GraphicsDisplay.this.viewport[0][1];
                    }

                    if (newY < GraphicsDisplay.this.viewport[1][1]) {
                        newY = GraphicsDisplay.this.viewport[1][1];
                    }
                    System.out.println(graphicsData[draggedIndex][1]);
        	        System.out.println(newY);
        	        if (isDraggingAltGraphicsData) {
        	            altGraphicsData[draggedIndex][1] = newY;
        	        } else {
        	            graphicsData[draggedIndex][1] = newY;
        	        }
        	        repaint();
        	    } else if (isSelecting) {
        	        // Обновляем рамку выделения
        	    	double width = e.getX() - selectionRect.getX();
                    if (width < 5) width = 5;

                    double height = e.getY() - selectionRect.getY();
                    if (height < 5) height = 5;
                    
                    selectionRect.setFrame(selectionRect.getX(), selectionRect.getY(), width, height);
                    repaint();
        	     
        	    }
        	}

        	    
            


            @Override
            public void mouseMoved(MouseEvent e) {
                if (showMarkers && graphicsData != null) {
                    for (Double[] point : graphicsData) {
                        Point2D.Double markerPoint = xyToPoint(point[0], point[1]);
                        if (markerPoint.distance(e.getPoint()) < 15) {
                            setToolTipText(String.format("X: %.2f, Y: %.2f", point[0], point[1]));
                            return;
                        }
                    }
                    
                    setToolTipText(null);
                }
                
                if (showMarkers && altGraphicsData != null) {
                    for (Double[] point : altGraphicsData) {
                        Point2D.Double markerPoint = xyToPoint(point[0], point[1]);
                        if (markerPoint.distance(e.getPoint()) < 15) {
                            setToolTipText(String.format("X: %.2f, Y: %.2f", point[0], point[1]));
                            return;
                        }
                    }
                    setToolTipText(null);

                }
                
            }
        });
    }

    public void saveGraphicsData(String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (Double[] point : graphicsData) {
                writer.write(point[0] + "," + point[1] + "\n");
            }
            for (Double[] point : altGraphicsData) {
                writer.write(point[0] + "," + point[1] + "\n");
            }
        }
    }

    private void zoomToRegion(double x1, double y1, double x2, double y2) {
    	this.viewport[0][0] = x1;
        this.viewport[0][1] = y1;
        this.viewport[1][0] = x2;
        this.viewport[1][1] = y2;
        this.repaint();
    }
	
	public void showGraphics(Double[][] graphicsData) {
		this.graphicsData = graphicsData;
		getScale();
		repaint();
	}

	public void showAltGraphics(Double[][] altGraphicsData) {
		this.altGraphicsData = altGraphicsData;
		this.showAltGraphic = true;
		getScale();
		repaint();
	}

	public void setShowAxis(boolean showAxis) {
		this.showAxis = showAxis;
		repaint();
	}

	public void setIsRotated(boolean isRotated) {
		this.isRotated = isRotated;
		repaint();
	}

	public void setShowAltGraphic(boolean showAltGraphic) {
		this.showAltGraphic = showAltGraphic;
		getScale();
		repaint();
	}
	
	public void setShowMarkers(boolean showMarkers) {
		this.showMarkers = showMarkers;
		repaint();
	}

	
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        
        if (graphicsData == null || graphicsData.length == 0)
            return;
        
        scaleX = this.getSize().getWidth() / (viewport[1][0] - viewport[0][0]);
        scaleY = this.getSize().getHeight() / (viewport[0][1] - viewport[1][1]);

        Graphics2D canvas = (Graphics2D) g;
        if (isRotated) {
        	scaleX = this.getSize().getHeight() / (viewport[1][0] - viewport[0][0]);
        	scaleY = this.getSize().getWidth() / (viewport[0][1] - viewport[1][1]);
            AffineTransform tform = canvas.getTransform();
            tform.quadrantRotate(-1, (viewport[0][1] - viewport[1][1]) / 2 * scaleY, (viewport[1][0] - viewport[0][0]) / 2 * scaleX);
            tform.translate((viewport[0][1] - viewport[1][1]) / 2 * scaleY - (viewport[1][0] - viewport[0][0]) / 2 * scaleX, (viewport[1][0] - viewport[0][0]) / 2 * scaleX - (viewport[0][1] - viewport[1][1]) / 2 * scaleY);
            canvas.setTransform(tform);
        }

        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
        if (showAxis)
            paintAxis(canvas);
        paintGraphics(canvas);
        if (showMarkers)
            paintMarkers(canvas);
        paintSelection(canvas);
        // Отображаем координаты маркера, на который наведена мышь

        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
        
        
        
        
    }
	protected void paintSelection(Graphics2D canvas) {
		if (isSelecting) {
            canvas.setColor(new Color(0, 0, 255, 150)); // Прозрачный синий цвет
            canvas.setStroke(new BasicStroke(4));
            canvas.draw(selectionRect);
        }
	}

	protected void getScale() {
		Double[] mmdata = getMinMax(graphicsData);
		
		viewport[0][0]=mmdata[0];
		viewport[0][1]=mmdata[3];
		viewport[1][0]=mmdata[1];
		viewport[1][1]=mmdata[2];

		if (this.showAltGraphic) {
			Double[] altmmdata = getMinMax(altGraphicsData);
			viewport[0][0] = Math.min(viewport[0][0], altmmdata[0]);
			viewport[1][0] = Math.max(viewport[1][0], altmmdata[1]);
			viewport[1][1] = Math.min(viewport[1][1], altmmdata[2]);
			viewport[0][1] = Math.max(viewport[0][1], altmmdata[3]);
		}

	}

	protected Double[] getMinMax(Double[][] graphicsData) {
		Double[] minmaxxy = { 0.0, 0.0, 0.0, 0.0 };
		minmaxxy[0] = graphicsData[0][0];
		minmaxxy[1] = graphicsData[graphicsData.length - 1][0];
		minmaxxy[2] = graphicsData[0][1];
		minmaxxy[3] = minmaxxy[2];

		for (int i = 1; i < graphicsData.length; i++) {
			if (graphicsData[i][1] < minmaxxy[2]) {
				minmaxxy[2] = graphicsData[i][1];
			}
			if (graphicsData[i][1] > minmaxxy[3]) {
				minmaxxy[3] = graphicsData[i][1];
			}
		}

		return minmaxxy;
	}

	protected void paintGraphics(Graphics2D canvas) {
		canvas.setStroke(graphicsStroke);
		canvas.setColor(Color.RED);
		GeneralPath graphics = new GeneralPath();
		for (int i = 0; i < graphicsData.length; i++) {
			Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
			if (i > 0) {
				graphics.lineTo(point.getX(), point.getY());
			} else {
				graphics.moveTo(point.getX(), point.getY());
			}
		}
		canvas.draw(graphics);

		if (showAltGraphic) {
			canvas.setStroke(altGraphicsStroke);
			canvas.setColor(Color.GREEN);
			GeneralPath altGraphics = new GeneralPath();
			for (int i = 0; i < altGraphicsData.length; i++) {
				Point2D.Double point = xyToPoint(altGraphicsData[i][0], altGraphicsData[i][1]);
				if (i > 0) {
					altGraphics.lineTo(point.getX(), point.getY());
				} else {
					altGraphics.moveTo(point.getX(), point.getY());
				}
			}
			canvas.draw(altGraphics);
		}
	}

	protected Point2D.Double shiftPoint(Point2D.Double point, double shiftX, double shiftY) {
        return new Point2D.Double(point.getX() + shiftX, point.getY() + shiftY);
    }
	
	protected void paintMarkers(Graphics2D canvas) {
		canvas.setStroke(markerStroke);
		paintMarkersFromData(graphicsData, canvas);
		if (showAltGraphic) {
			paintMarkersFromData(altGraphicsData, canvas);
		}
	}

	protected double[] translatePointToXY(int x, int y) {
        return new double[]{this.viewport[0][0] + (double)x / this.scaleX, this.viewport[0][1] - (double)y / this.scaleY};
    }
	
	protected void paintMarkersFromData(Double[][] graphicsData, Graphics2D canvas) {
        for (Double[] point : graphicsData) {
            canvas.setColor(Color.RED);
            if (sumLessThan10(point[1]))
                canvas.setColor(Color.BLUE);
            Ellipse2D.Double marker = new Ellipse2D.Double();
            Point2D.Double center = xyToPoint(point[0], point[1]);
            Point2D.Double corner = shiftPoint(center, 3, 3);
            Line2D.Double line1 = new Line2D.Double();
            Line2D.Double line2 = new Line2D.Double();
            marker.setFrameFromCenter(center, corner);
            line1.setLine(shiftPoint(center, 3, 0), shiftPoint(center, -3, 0));
            line2.setLine(shiftPoint(center, 0, 3), shiftPoint(center, 0, -3));
            canvas.draw(marker);
            canvas.draw(line1);
            canvas.draw(line2);
        }
    }

	protected boolean sumLessThan10(double y) {
		int sum = 0;
		int Y = (int) (y);
		if (Y < 0)
			Y = -Y;
		while (Y != 0) {
			sum += Y % 10;
			Y /= 10;
		}
		return sum < 10;
	}

	protected void paintAxis(Graphics2D canvas) {
		canvas.setStroke(axisStroke);
		canvas.setColor(Color.BLACK);
		canvas.setPaint(Color.BLACK);
		canvas.setFont(axisFont);
		if ((viewport[0][0] <= 0.0 && viewport[1][0] >= 0.0) || (viewport[1][1] <= 0.0 && viewport[0][1] >= 0.0 && isRotated)) {
			drawY(canvas);
		}
		if ((viewport[1][1] <= 0.0 && viewport[0][1] >= 0.0) || (viewport[0][0] <= 0.0 && viewport[1][0] >= 0.0 && isRotated)) {
			drawX(canvas);
		}
	}

	protected void drawY(Graphics2D canvas) {
	    FontRenderContext context = canvas.getFontRenderContext();

	    // Рисуем ось Y
	    Point2D.Double lineStart = xyToPoint(0, viewport[1][1]);
	    Point2D.Double lineEnd = xyToPoint(0, viewport[0][1]);
	    canvas.draw(new Line2D.Double(lineStart, lineEnd));

	    // Рисуем стрелку
	    GeneralPath arrow = new GeneralPath();
	    arrow.moveTo(lineEnd.getX(), lineEnd.getY());
	    arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
	    arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
	    arrow.closePath();
	    canvas.draw(arrow);
	    canvas.fill(arrow);

	    // Подпись для оси Y
	    Rectangle2D bounds = axisFont.getStringBounds("y", context);
	    Point2D.Double labelPos = lineEnd;
	    canvas.drawString("y", (float) labelPos.getX() + 10, (float) (labelPos.getY() - bounds.getY()));

	    // Рисуем метку для y = 1
	    if(viewport[0][1] > 1) {
	    Point2D.Double markPosition = xyToPoint(0, 1); // Координаты точки y = 1
	    canvas.draw(new Line2D.Double(
	            markPosition.getX() - 5, markPosition.getY(), // Начало черты
	            markPosition.getX() + 5, markPosition.getY()  // Конец черты
	    ));

	    // Уменьшаем шрифт для подписи "1"
	    Font smallFont = axisFont.deriveFont(axisFont.getSize() * 0.75f); // Уменьшаем размер шрифта
	    canvas.setFont(smallFont);
	    Rectangle2D markBounds = smallFont.getStringBounds("1", context);
	    canvas.drawString("1",
	            (float) (markPosition.getX() + 5 + markBounds.getWidth() / 2),
	            (float) (markPosition.getY() + markBounds.getHeight() / 2));

	    // Восстанавливаем оригинальный шрифт
	    canvas.setFont(axisFont);
	    }
	}

		
	

	protected void drawX(Graphics2D canvas) {
	    FontRenderContext context = canvas.getFontRenderContext();

	    // Рисуем ось X
	    Point2D.Double lineStart = xyToPoint(viewport[0][0], 0);
	    Point2D.Double lineEnd = xyToPoint(viewport[1][0], 0);
	    canvas.draw(new Line2D.Double(lineEnd, lineStart));

	    // Рисуем стрелку
	    GeneralPath arrow = new GeneralPath();
	    arrow.moveTo(lineEnd.getX(), lineEnd.getY());
	    arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
	    arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
	    arrow.closePath();
	    canvas.draw(arrow);
	    canvas.fill(arrow);

	    // Подпись для оси X
	    Rectangle2D bounds = axisFont.getStringBounds("x", context);
	    Point2D.Double labelPos = lineEnd;
	    canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10),
	            (float) (labelPos.getY() + bounds.getY()));

	    // Рисуем метку для x = 1
	    if(viewport[1][0] > 1) {
	    Point2D.Double markPosition = xyToPoint(1, 0); // Координаты точки x = 1
	    canvas.draw(new Line2D.Double(
	            markPosition.getX(), markPosition.getY() - 5, // Начало черты
	            markPosition.getX(), markPosition.getY() + 5  // Конец черты
	    ));

	    // Уменьшаем шрифт для подписи "1"
	    Font smallFont = axisFont.deriveFont(axisFont.getSize() * 0.75f); // Уменьшаем размер шрифта
	    canvas.setFont(smallFont);
	    Rectangle2D markBounds = smallFont.getStringBounds("1", context);
	    canvas.drawString("1", 
	            (float) (markPosition.getX() - markBounds.getWidth() / 2), 
	            (float) (markPosition.getY() + 5 + markBounds.getHeight()));

	    // Восстанавливаем оригинальный шрифт
	    canvas.setFont(axisFont);
	    }
	}


	

	protected Point2D.Double xyToPoint(double x, double y) {
		double deltaX = x - viewport[0][0];
		double deltaY = viewport[0][1] - y;
		return new Point2D.Double(deltaX * scaleX, deltaY * scaleY);
	}

	
	 
}
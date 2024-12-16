package laba4;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
// Список координат точек для построения графика
	private Double[][] graphicsData;
	private Double[][] graphicsData1;
// Флаговые переменные, задающие правила отображения графика
	private boolean showAxis = true;
	private boolean showSecondGraphic = false;
	private boolean showMarkers = true;
// Границы диапазона пространства, подлежащего отображению
	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	private double startX ;
	private double startY ;
	private boolean selecting = true;
	private double  currentX ;
	private double currentY;
	private double[] zoomStack;
// Используемый масштаб отображения
	private double scale;
// Различные стили черчения линий
	private BasicStroke graphicsStroke;
	private BasicStroke axisStroke;
	private BasicStroke markerStroke;
// Различные шрифты отображения надписей
	private Font axisFont;

	public GraphicsDisplay() {

		setBackground(Color.WHITE);

		graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
// Перо для рисования осей координат
		axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Перо для рисования контуров маркеров
		markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Шрифт для подписей осей координат
		axisFont = new Font("Serif", Font.BOLD, 36);
	}
// Данный метод вызывается из обработчика элемента меню "Открыть файл с

// главного окна приложения в случае успешной загрузки данных
	public void showGraphics(Double[][] graphicsData) {
// Сохранить массив точек во внутреннем поле класса
		this.graphicsData = graphicsData;
// Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
		repaint();
	}

	
	// Метод для вычисления суммы цифр целой части числа
	private int sumOfDigits(int number) {
	    int sum = 0;
	    number = Math.abs(number); // Берем абсолютное значение числа
	    while (number > 0) {
	        sum += number % 10; // Получаем последнюю цифру числа и добавляем ее к сумме
	        number /= 10; // Убираем последнюю цифру
	    }
	    return sum;
	}
	
// Методы-модификаторы для изменения параметров отображения графика
// Изменение любого параметра приводит к перерисовке области
	public void setShowAxis(boolean showAxis) {
		this.showAxis = showAxis;
		repaint();
	}
	
	public void setShowSecondGraphic(Double[][] graphicsData1) {
		this.graphicsData1 = graphicsData1;
		repaint();
	}

	public void setShowMarkers(boolean showMarkers) {
		this.showMarkers = showMarkers;
		repaint();
	}

	public void mousePressed(MouseEvent e) {
	    if (SwingUtilities.isLeftMouseButton(e)) {
	        startX = e.getX();
	        startY = e.getY();
	        selecting = true;
	    }
	}

	
	public void mouseDragged(MouseEvent e) {
	    if (selecting) {
	        currentX = e.getX();
	        currentY = e.getY();
	        repaint(); // Перерисовка для отображения рамки
	    }
	}
	
	public void mouseReleased(MouseEvent e) {
	    if (SwingUtilities.isLeftMouseButton(e)) {
	        selecting = false;
	        zoomToSelection(startX, startY, currentX, currentY);
	    }
	}
	public void mouseClicked(MouseEvent e) {
	    if (SwingUtilities.isRightMouseButton(e)) {
	        if (!zoomStack.isEmpty()) {
	            restorePreviousZoom();
	        }
	    }
	}
	
	public void zoomToSelection(double startX,double startY,double currentX,double currentY){
		
	}
	
	public void restorePreviousZoom() {
		
		
	}
// Метод отображения всего компонента, содержащего график
	public void paintComponent(Graphics g) {
		/*
		 * Шаг 1 - Вызвать метод предка для заливки области цветом заднего фона Эта
		 * функциональность - единственное, что осталось в наследство от paintComponent
		 * класса JPanel
		 */
		super.paintComponent(g);
// Шаг 2 - Если данные графика не загружены (при показе компонента при запуске программы) - ничего не делать
		if (graphicsData == null || graphicsData.length == 0)
			return;
// Шаг 3 - Определить минимальное и максимальное значения для координат X и Y
// Это необходимо для определения области пространства, подлежащей отображению
// Еѐ верхний левый угол это (minX, maxY) - правый нижний это (maxX, minY)
		minX = graphicsData[0][0];
		maxX = graphicsData[graphicsData.length - 1][0];
		minY = graphicsData[0][1];
		maxY = minY;
// Найти минимальное и максимальное значение функции
		for (int i = 1; i < graphicsData.length; i++) {
			if (graphicsData[i][1] < minY) {
				minY = graphicsData[i][1];
			}
			if (graphicsData[i][1] > maxY) {
				maxY = graphicsData[i][1];
			}
		}

		double scaleX = getSize().getWidth() / (maxX - minX);
		double scaleY = getSize().getHeight() / (maxY - minY);
// Шаг 5 - Чтобы изображение было неискажѐнным - масштаб должен быть одинаков
// Выбираем за основу минимальный
		scale = Math.min(scaleX, scaleY);
// Шаг 6 - корректировка границ отображаемой области согласно выбранному масштабу
		if (scale == scaleX) {
			/*
			 * Если за основу был взят масштаб по оси X, значит по оси Y делений меньше,
			 * т.е. подлежащий визуализации диапазон по Y будет меньше высоты окна. Значит
			 * необходимо добавить делений, сделаем это так: 1) Вычислим, сколько делений
			 * влезет по Y при выбранном масштабе - getSize().getHeight()/scale 2) Вычтем из
			 * этого сколько делений требовалось изначально 3) Набросим по половине
			 * недостающего расстояния на maxY и minY
			 */
			double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
			maxY += yIncrement;
			minY -= yIncrement;
		}
		if (scale == scaleY) {
// Если за основу был взят масштаб по оси Y, действовать по аналогии
			double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
			maxX += xIncrement;
			minX -= xIncrement;
		}
// Шаг 7 - Сохранить текущие настройки холста
		Graphics2D canvas = (Graphics2D) g;
		Stroke oldStroke = canvas.getStroke();
		Color oldColor = canvas.getColor();
		Paint oldPaint = canvas.getPaint();
		Font oldFont = canvas.getFont();
// Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
// Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет затираться последующим
// Первыми (если нужно) отрисовываются оси координат.
		if (showAxis)
			paintAxis(canvas);
// Затем отображается сам график
		paintGraphics(canvas);
// Затем (если нужно) отображаются маркеры точек, по которым строился график.
		if (showMarkers)
			paintMarkers(canvas);
// Шаг 9 - Восстановить старые настройки холста
		canvas.setFont(oldFont);
		canvas.setPaint(oldPaint);
		canvas.setColor(oldColor);
		canvas.setStroke(oldStroke);
	}

// Отрисовка графика по прочитанным координатам
	protected void paintGraphics(Graphics2D canvas) {
		// Установить стиль линии (чередование 3 полосок и 3 точек)
		float[] dashPattern = { 10, 5, 10, 5, 10, 5, 4, 5, 4, 5, 4, 5 };
		/*
		 * Объяснение dashPattern: - 10: длина полоски - 5: пробел между полосками - 15:
		 * длинный пробел между полосками и точками - 2: длина точки - 5: пробел между
		 * точками - 15: длинный пробел между точками и следующими полосками
		 */

		canvas.setStroke(new BasicStroke(3, // Толщина линии
				BasicStroke.CAP_BUTT, // Концы линии без закруглений
				BasicStroke.JOIN_MITER, // Соединения линий без закруглений
				10, // Толщина соединений
				dashPattern, // Паттерн штрихов
				0 // Начальное смещение
		));

		// Установить цвет линии
		canvas.setColor(Color.RED);

		// Создать путь для графика
		GeneralPath graphics = new GeneralPath();
		for (int i = 0; i < graphicsData.length; i++) {
			// Преобразовать значения (x, y) в точку на экране
			Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
			if (i > 0) {
				// Не первая итерация цикла - вести линию в точку point
				graphics.lineTo(point.getX(), point.getY());
			} else {
				// Первая итерация цикла - установить начало пути в точку point
				graphics.moveTo(point.getX(), point.getY());
			}
		}

		// Нарисовать график
		canvas.draw(graphics);
	}
	
	

// Отображение маркеров точек, по которым рисовался график
	protected void paintMarkers(Graphics2D canvas) {
// Шаг 1 - Установить специальное перо для черчения контуров маркеров
		canvas.setStroke(markerStroke);

// Выбрать красный цвета для контуров маркеров
		canvas.setColor(Color.RED);

// Выбрать красный цвет для закрашивания маркеров внутри
		canvas.setPaint(Color.RED);

// Шаг 2 - Организовать цикл по всем точкам графика
		for (Double[] point : graphicsData) {
			  
			        // Преобразуем значение точки в целое число (целая часть)
			        int intPart = (int) Math.floor(point[1]); // Берем целую часть числа

			        // Проверяем условие: сумма цифр целой части должна быть меньше 10
			        if (sumOfDigits(intPart) < 10) {
			            // Если условие выполняется, рисуем маркер с другим цветом
			            canvas.setColor(Color.GREEN); // Например, зеленым для подходящих точек
			        } else {
			            // Для остальных точек оставляем красный цвет
			            canvas.setColor(Color.RED);
			        }
			// Инициализировать эллипс как объект для представления маркера
			Ellipse2D.Double marker = new Ellipse2D.Double();

// Центр - в точке (x,y)
			Point2D.Double center = xyToPoint(point[0], point[1]);

// Угол прямоугольника - отстоит на расстоянии (3,3)
			Point2D.Double corner = shiftPoint(center, 11, 11);

// Задать эллипс по центру и диагонали
			marker.setFrameFromCenter(center, corner);

			canvas.draw(marker); // Начертить контур маркера

			canvas.setStroke(new BasicStroke(2));
// Горизонтальная линия через центр
			canvas.draw(new Line2D.Double(center.getX() - 11, center.getY(), center.getX() + 11, center.getY()));
// Вертикальная линия через центр
			canvas.draw(new Line2D.Double(center.getX(), center.getY() - 11, center.getX(), center.getY() + 11));

		}
	}

// Метод, обеспечивающий отображение осей координат
	protected void paintAxis(Graphics2D canvas) {
// Установить особое начертание для осей
		canvas.setStroke(axisStroke);
// Оси рисуются чѐрным цветом
		canvas.setColor(Color.BLACK);
// Стрелки заливаются чѐрным цветом
		canvas.setPaint(Color.BLACK);
// Подписи к координатным осям делаются специальным шрифтом
		canvas.setFont(axisFont);
// Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
		FontRenderContext context = canvas.getFontRenderContext();
// Определить, должна ли быть видна ось Y на графике
		if (minX <= 0.0 && maxX >= 0.0) {
// Она должна быть видна, если левая граница показываемой области (minX) <= 0.0,
// а правая (maxX) >= 0.0
// Сама ось - это линия между точками (0, maxY) и (0, minY)
			canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
// Стрелка оси Y
			GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на верхний конец оси Y
			Point2D.Double lineEnd = xyToPoint(0, maxY);
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести левый "скат" стрелки в точку с относительными координатами (5,20)
			arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
// Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
			arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
// Замкнуть треугольник стрелки
			arrow.closePath();
			canvas.draw(arrow); // Нарисовать стрелку
			canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси Y
// Определить, сколько места понадобится для надписи "y"
			Rectangle2D bounds = axisFont.getStringBounds("y", context);
			Point2D.Double labelPos = xyToPoint(0, maxY);
// Вывести надпись в точке с вычисленными координатами
			canvas.drawString("y", (float) labelPos.getX() + 10, (float) (labelPos.getY() - bounds.getY()));
		}
// Определить, должна ли быть видна ось X на графике
		if (minY <= 0.0 && maxY >= 0.0) {
// Она должна быть видна, если верхняя граница показываемой области (maxX) >= 0.0,
// а нижняя (minY) <= 0.0
			canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));
// Стрелка оси X
			GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на правый конец оси X
			Point2D.Double lineEnd = xyToPoint(maxX, 0);
			arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести верхний "скат" стрелки в точку с относительными координатами (-20,-5)
			arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
// Вести левую часть стрелки в точку с относительными координатами (0, 10)
			arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
// Замкнуть треугольник стрелки
			arrow.closePath();
			canvas.draw(arrow); // Нарисовать стрелку
			canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси X
// Определить, сколько места понадобится для надписи "x"
			Rectangle2D bounds = axisFont.getStringBounds("x", context);
			Point2D.Double labelPos = xyToPoint(maxX, 0);
// Вывести надпись в точке с вычисленными координатами
			canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10),
					(float) (labelPos.getY() + bounds.getY()));
		}
	}

	/*
	 * Метод-помощник, осуществляющий преобразование координат. Оно необходимо, т.к.
	 * верхнему левому углу холста с координатами (0.0, 0.0) соответствует точка
	 * графика с координатами (minX, maxY), где minX - это самое "левое" значение X,
	 * а maxY - самое "верхнее" значение Y.
	 */
	protected Point2D.Double xyToPoint(double x, double y) {
// Вычисляем смещение X от самой левой точки (minX)
		double deltaX = x - minX;
// Вычисляем смещение Y от точки верхней точки (maxY)
		double deltaY = maxY - y;
		return new Point2D.Double(deltaX * scale, deltaY * scale);
	}

	/*
	 * Метод-помощник, возвращающий экземпляр класса Point2D.Double смещѐнный по
	 * отношению к исходному на deltaX, deltaY К сожалению, стандартного метода,
	 * выполняющего такую задачу, нет.
	 */
	protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
// Инициализировать новый экземпляр точки
		Point2D.Double dest = new Point2D.Double();
// Задать еѐ координаты как координаты существующей точки + заданные смещения
		dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
		return dest;
	}
}

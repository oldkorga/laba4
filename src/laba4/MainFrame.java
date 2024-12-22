package laba4;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

@SuppressWarnings("serial")

public class MainFrame extends JFrame {
// Начальные размеры окна приложения
	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
// Объект диалогового окна для выбора файлов
	private JFileChooser fileChooser = null;
	
// Пункты меню
	private JCheckBoxMenuItem showAxisMenuItem;
	private JCheckBoxMenuItem showMarkersMenuItem;
	private JCheckBoxMenuItem showAltGraphicMenuItem;
	private JCheckBoxMenuItem isRotatedMenuItem;
	
// Компонент-отображатель графика 
	private GraphicsDisplay display = new GraphicsDisplay();
// Флаг, указывающий на загруженность данных графика
	private boolean fileLoaded = false;
	private boolean altFileLoaded = false;

	public MainFrame() {
// Вызов конструктора предка Frame
		super("Построение графиков функций на основе заранее подготовленных файлов");

		setSize(WIDTH, HEIGHT);
		Toolkit kit = Toolkit.getDefaultToolkit();
// Отцентрировать окно приложения на экране
		setLocation((kit.getScreenSize().width - WIDTH) / 2, (kit.getScreenSize().height - HEIGHT) / 2);
		setExtendedState(MAXIMIZED_BOTH);
// Создать и установить полосу меню
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
// Добавить пункт меню "Файл"
		JMenu fileMenu = new JMenu("Файл");
		menuBar.add(fileMenu);
// Создать действие по открытию файла
		Action openGraphicsAction = new AbstractAction("Открыть файл с графиком") {
			public void actionPerformed(ActionEvent event) {
				if (fileChooser == null) {
					fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(new File("."));
				}
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
					openGraphics(fileChooser.getSelectedFile(), false);
			}
		};
		

		fileMenu.add(openGraphicsAction);	
		
		Action openAltGraphicsAction = new AbstractAction("Открыть файл со вторым графиком") {
			public void actionPerformed(ActionEvent event) {
				if (fileChooser == null) {
					fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(new File("."));
				}
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
					openGraphics(fileChooser.getSelectedFile(), true);
			}
		};
		fileMenu.add(openAltGraphicsAction);
		
// Создать пункт меню "График"
		JMenu graphicsMenu = new JMenu("График");
		menuBar.add(graphicsMenu);

		Action showAxisAction = new AbstractAction("Показывать оси координат") {
			public void actionPerformed(ActionEvent event) {
				display.setShowAxis(showAxisMenuItem.isSelected());
			}
		};
		showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
		graphicsMenu.add(showAxisMenuItem);
		showAxisMenuItem.setSelected(true);
			
		
// Повторить действия для элемента "Показывать маркеры точек"
		Action showMarkersAction = new AbstractAction("Показывать маркеры точек") {
			public void actionPerformed(ActionEvent event) {
// по аналогии с showAxisMenuItem
				display.setShowMarkers(showMarkersMenuItem.isSelected());
			}
		};
		showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
		graphicsMenu.add(showMarkersMenuItem);
		showMarkersMenuItem.setSelected(true);
		graphicsMenu.addMenuListener(new GraphicsMenuListener());
		Action showAltGraphicAction = new AbstractAction("Показывать второй график") {
			public void actionPerformed(ActionEvent event) {
				display.setShowAltGraphic(showAltGraphicMenuItem.isSelected());
			}
		};
		showAltGraphicMenuItem = new JCheckBoxMenuItem(showAltGraphicAction);
		graphicsMenu.add(showAltGraphicMenuItem);
		showAltGraphicMenuItem.setSelected(true);
		graphicsMenu.addMenuListener(new GraphicsMenuListener());
		Action isRotatedAction = new AbstractAction("Повернуть график") {
			public void actionPerformed(ActionEvent event) {
				display.setIsRotated(isRotatedMenuItem.isSelected());
			}
		};
		isRotatedMenuItem = new JCheckBoxMenuItem(isRotatedAction);
		graphicsMenu.add(isRotatedMenuItem);
		isRotatedMenuItem.setSelected(false);
		graphicsMenu.addMenuListener(new GraphicsMenuListener());
		getContentPane().add(display, BorderLayout.CENTER);
	}

// Считывание данных графика из существующего файла
	protected void openGraphics(File selectedFile, boolean alt) {
		try {
// Шаг 1 - Открыть поток чтения данных, связанный с входным файловым потоком
			DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
			
			Double[][] graphicsData = new Double[in.available() / (Double.SIZE / 8) / 2][];
// Шаг 3 - Цикл чтения данных (пока в потоке есть данные)
			int i = 0;
			while (in.available() > 0) {
				Double x = in.readDouble();
				Double y = in.readDouble();
				graphicsData[i++] = new Double[] { x, y };
			}
			if (graphicsData != null && graphicsData.length > 0) {
				fileLoaded = true;
				if(!alt)
				display.showGraphics(graphicsData);
				else {
					altFileLoaded = true;
					display.showAltGraphics(graphicsData);
				}
			}

			in.close();
		} catch (FileNotFoundException ex) {
// В случае исключительной ситуации типа "Файл не найден" показать сообщение об ошибке
			JOptionPane.showMessageDialog(MainFrame.this, "Указанный файл не найден", "Ошибка загрузки данных",
					JOptionPane.WARNING_MESSAGE);
			return;
		} catch (IOException ex) {
// В случае ошибки ввода из файлового потока показать сообщение об ошибке
			JOptionPane.showMessageDialog(MainFrame.this, "Ошибка чтения координат точек из файла",
					"Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
			return;
		}
	}

	public static void main(String[] args) {
// Создать и показать экземпляр главного окна приложения
		MainFrame frame = new MainFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

// Класс-слушатель событий, связанных с отображением меню
	private class GraphicsMenuListener implements MenuListener {
// Обработчик, вызываемый перед показом меню
		public void menuSelected(MenuEvent e) {
// Доступность или недоступность элементов меню "График" определяется загруженностью данных
			showAxisMenuItem.setEnabled(fileLoaded);
			showMarkersMenuItem.setEnabled(fileLoaded);
			showAltGraphicMenuItem.setEnabled(altFileLoaded);
		}

// Обработчик, вызываемый после того, как меню исчезло с экрана
		public void menuDeselected(MenuEvent e) {
		}

// Обработчик, вызываемый в случае отмены выбора пункта меню (очень редкая ситуация)
		public void menuCanceled(MenuEvent e) {
		}
	}
}
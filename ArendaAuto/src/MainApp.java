import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainApp extends JFrame {
	private JPanel navigationPanel;
	private JPanel mainPanel;
	private JButton clientsButton;
	private JButton carsButton;
	private JButton rentalsButton;
	private JButton paymentsButton;


	private void showClientsPanel() {
		mainPanel.removeAll();
		mainPanel.add(new ClientsPanel(), BorderLayout.CENTER);
		mainPanel.revalidate();
		mainPanel.repaint();
	}

	private void showCarsPanel() {
		mainPanel.removeAll();
		mainPanel.add(new CarsPanel(), BorderLayout.CENTER);
		mainPanel.revalidate();
		mainPanel.repaint();
	}

	private void showRentalsPanel() {
		mainPanel.removeAll();
		mainPanel.add(new RentalsPanel(), BorderLayout.CENTER);
		mainPanel.revalidate();
		mainPanel.repaint();
	}

	private void showPaymentsPanel() {
		mainPanel.removeAll();
		mainPanel.add(new PaymentsPanel(), BorderLayout.CENTER);
		mainPanel.revalidate();
		mainPanel.repaint();
	}

	public MainApp() {
		setTitle("Car Rental Management System");
		setSize(600, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);




		// Создаем панель навигации
		navigationPanel = new JPanel();
		navigationPanel.setLayout(new GridLayout(4, 1));
		clientsButton = new JButton("Клиенты");
		carsButton = new JButton("Автомобили");
		rentalsButton = new JButton("Аренды");
		paymentsButton = new JButton("Платежи");



		carsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showCarsPanel();
			}
		});

		clientsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showClientsPanel();
			}
		});

		rentalsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showRentalsPanel();
			}
		});

		paymentsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showPaymentsPanel();
			}
		});



		// Добавляем слушателей для кнопок навигации
		clientsButton.addActionListener(new NavigationButtonListener());
		carsButton.addActionListener(new NavigationButtonListener());
		rentalsButton.addActionListener(new NavigationButtonListener());
		paymentsButton.addActionListener(new NavigationButtonListener());

		navigationPanel.add(clientsButton);
		navigationPanel.add(carsButton);
		navigationPanel.add(rentalsButton);
		navigationPanel.add(paymentsButton);

		// Создаем информационную область
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(new JLabel("Рабочая зона! Пожалуйста выберете нужную вкладку!"), BorderLayout.CENTER);

		// Добавляем панель навигации и информационную область в главное окно
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(navigationPanel, BorderLayout.WEST);
		getContentPane().add(mainPanel, BorderLayout.CENTER);

	}

	// Слушатель для кнопок навигации
	private class NavigationButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			String buttonText = button.getText();
			// Здесь можно добавить логику для переключения между различными разделами приложения
			// Например, отображение соответствующего списка или формы
			mainPanel.removeAll();
			mainPanel.add(new JLabel("Вы выбрали раздел: " + buttonText), BorderLayout.CENTER);
			mainPanel.revalidate();
			mainPanel.repaint();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			MainApp mainApp = new MainApp();
			mainApp.setVisible(true);
		});
	}
}
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PaymentsPanel extends JPanel {
	private JTable paymentsTable;
	private DefaultTableModel tableModel;
	private JComboBox<String> rentalComboBox;
	private JTextField dateField;
	private JTextField amountField;
	private JComboBox<String> paymentMethodComboBox;
	private JTextArea detailsTextArea;
	private Connection connection;

	public PaymentsPanel() {
		setLayout(new BorderLayout());
		try {
			connection = DatabaseConnection.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Ошибка подключения к базе данных", "Ошибка", JOptionPane.ERROR_MESSAGE);
		}

		// Создание таблицы для отображения списка платежей
		String[] columnNames = {"ID аренды", "Дата", "Сумма", "Способ оплаты", "Детали"};
		tableModel = new DefaultTableModel(columnNames, 0);
		paymentsTable = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(paymentsTable);

		// Добавление таблицы в панель
		add(scrollPane, BorderLayout.CENTER);

		// Панель для формы добавления платежа
		JPanel addPaymentPanel = new JPanel(new GridLayout(6, 2));
		addPaymentPanel.setBorder(BorderFactory.createTitledBorder("Добавить платеж"));

		JLabel rentalLabel = new JLabel("Аренда:");
		rentalComboBox = new JComboBox<>();
		loadRentalsIntoComboBox();
		JLabel dateLabel = new JLabel("Дата:");
		dateField = new JTextField();
		JLabel amountLabel = new JLabel("Сумма:");
		amountField = new JTextField();
		JLabel paymentMethodLabel = new JLabel("Способ оплаты:");
		paymentMethodComboBox = new JComboBox<>(new String[]{"Наличные", "Карта", "Перевод"});
		JLabel detailsLabel = new JLabel("Детали:");
		detailsTextArea = new JTextArea(3, 20);
		JScrollPane detailsScrollPane = new JScrollPane(detailsTextArea);
		JButton addButton = new JButton("Добавить");
		addButton.addActionListener(new AddButtonListener());

		addPaymentPanel.add(rentalLabel);
		addPaymentPanel.add(rentalComboBox);
		addPaymentPanel.add(dateLabel);
		addPaymentPanel.add(dateField);
		addPaymentPanel.add(amountLabel);
		addPaymentPanel.add(amountField);
		addPaymentPanel.add(paymentMethodLabel);
		addPaymentPanel.add(paymentMethodComboBox);
		addPaymentPanel.add(detailsLabel);
		addPaymentPanel.add(detailsScrollPane);
		addPaymentPanel.add(new JLabel());
		addPaymentPanel.add(addButton);

		add(addPaymentPanel, BorderLayout.NORTH);

		// Загрузка списка платежей из базы данных
		loadPaymentsFromDatabase();
	}

	// Метод для загрузки списка аренд в выпадающий список
	private void loadRentalsIntoComboBox() {
		String sql = "SELECT RentalID FROM Rentals";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				int rentalID = resultSet.getInt("RentalID");
				rentalComboBox.addItem(String.valueOf(rentalID));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Метод для загрузки списка платежей из базы данных
	private void loadPaymentsFromDatabase() {
		String sql = "SELECT RentalID, PaymentDate, Amount, PaymentMethod, Details FROM Payments";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				int rentalID = resultSet.getInt("RentalID");
				String date = resultSet.getString("PaymentDate");
				double amount = resultSet.getDouble("Amount");
				String paymentMethod = resultSet.getString("PaymentMethod");
				String details = resultSet.getString("Details");
				addPaymentToTable(rentalID, date, amount, paymentMethod, details);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Метод для добавления нового платежа в базу данных и в таблицу
	private void addPaymentToTable(int rentalID, String date, double amount, String paymentMethod, String details) {
		String[] rowData = {String.valueOf(rentalID), date, String.valueOf(amount), paymentMethod, details};
		tableModel.addRow(rowData);
	}

	// Слушатель для кнопки "Добавить"
	private class AddButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int rentalID = Integer.parseInt((String) rentalComboBox.getSelectedItem());
			String date = dateField.getText();
			double amount = 0;
			try {
				amount = Double.parseDouble(amountField.getText());
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(PaymentsPanel.this, "Введите корректное значение суммы", "Ошибка", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String paymentMethod = (String) paymentMethodComboBox.getSelectedItem();
			String details = detailsTextArea.getText();
			if (!date.isEmpty() && amount > 0 && !details.isEmpty()) {
				addPaymentToTable(rentalID, date, amount, paymentMethod, details);
				insertPaymentIntoDatabase(rentalID, date, amount, paymentMethod, details);
				clearInputFields();
			} else {
				JOptionPane.showMessageDialog(PaymentsPanel.this, "Пожалуйста, заполните все поля корректно", "Ошибка", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// Метод для вставки нового платежа в базу данных
	private void insertPaymentIntoDatabase(int rentalID, String date, double amount, String paymentMethod, String details) {
		String sql = "INSERT INTO Payments (RentalID, PaymentDate, Amount, PaymentMethod, Details) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, rentalID);
			statement.setString(2, date);
			statement.setDouble(3, amount);
			statement.setString(4, paymentMethod);
			statement.setString(5, details);
			statement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	// Метод для очистки полей ввода формы добавления платежа
	private void clearInputFields() {
		dateField.setText("");
		amountField.setText("");
		detailsTextArea.setText("");
	}
}

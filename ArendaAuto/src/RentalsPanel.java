import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RentalsPanel extends JPanel {
	private JTable rentalsTable;
	private DefaultTableModel tableModel;
	private JComboBox<String> clientComboBox;
	private JComboBox<String> carComboBox;
	private JTextField startDateField;
	private JTextField endDateField;
	private JTextField costField;
	private JCheckBox completedCheckBox;
	private JButton deleteButton;
	private Connection connection;

	public RentalsPanel() {
		setLayout(new BorderLayout());
		try {
			connection = DatabaseConnection.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Ошибка подключения к базе данных", "Ошибка", JOptionPane.ERROR_MESSAGE);
		}

		// Создание таблицы для отображения списка арендованных автомобилей
		String[] columnNames = {"Клиент", "Автомобиль", "Дата начала", "Дата окончания", "Стоимость", "Статус"};
		tableModel = new DefaultTableModel(columnNames, 0);
		rentalsTable = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(rentalsTable);

		// Добавление таблицы в панель
		add(scrollPane, BorderLayout.CENTER);

		// Панель для формы добавления аренды
		JPanel addRentPanel = new JPanel(new GridLayout(7, 2));
		addRentPanel.setBorder(BorderFactory.createTitledBorder("Добавить аренду"));

		JLabel clientLabel = new JLabel("Клиент:");
		clientComboBox = new JComboBox<>();
		JLabel carLabel = new JLabel("Автомобиль:");
		carComboBox = new JComboBox<>();
		JLabel startDateLabel = new JLabel("Дата начала:");
		startDateField = new JTextField();
		JLabel endDateLabel = new JLabel("Дата окончания:");
		endDateField = new JTextField();
		JLabel costLabel = new JLabel("Стоимость:");
		costField = new JTextField();
		JLabel completedLabel = new JLabel("Завершено:");
		completedCheckBox = new JCheckBox();
		JButton addButton = new JButton("Добавить");
		addButton.addActionListener(new AddButtonListener());

		addRentPanel.add(clientLabel);
		addRentPanel.add(clientComboBox);
		addRentPanel.add(carLabel);
		addRentPanel.add(carComboBox);
		addRentPanel.add(startDateLabel);
		addRentPanel.add(startDateField);
		addRentPanel.add(endDateLabel);
		addRentPanel.add(endDateField);
		addRentPanel.add(costLabel);
		addRentPanel.add(costField);
		addRentPanel.add(completedLabel);
		addRentPanel.add(completedCheckBox);
		addRentPanel.add(new JLabel());
		addRentPanel.add(addButton);

		add(addRentPanel, BorderLayout.NORTH);

		// Добавление кнопки "Удалить" и привязка слушателя к ней
		deleteButton = new JButton("Удалить");
		deleteButton.addActionListener(new DeleteButtonListener());
		add(deleteButton, BorderLayout.SOUTH);

		// Загрузка клиентов и автомобилей из базы данных
		loadClientsAndCarsFromDatabase();
		// Загрузка аренд из базы данных
		loadRentalsFromDatabase();
	}

	// Метод для загрузки списка клиентов и автомобилей из базы данных
	private void loadClientsAndCarsFromDatabase() {
		// Загрузка клиентов и автомобилей
		String clientsSql = "SELECT FirstName, LastName FROM Clients";
		String carsSql = "SELECT Brand, Model FROM Cars";
		try (PreparedStatement clientStatement = connection.prepareStatement(clientsSql);
			 PreparedStatement carStatement = connection.prepareStatement(carsSql)) {
			ResultSet clientResultSet = clientStatement.executeQuery();
			ResultSet carResultSet = carStatement.executeQuery();

			while (clientResultSet.next()) {
				String firstName = clientResultSet.getString("FirstName");
				String lastName = clientResultSet.getString("LastName");
				clientComboBox.addItem(firstName + " " + lastName);
			}

			while (carResultSet.next()) {
				String brand = carResultSet.getString("Brand");
				String model = carResultSet.getString("Model");
				carComboBox.addItem(brand + " " + model);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Метод для добавления новой аренды в таблицу
	private void addRentalToTable(int clientID, String car, String startDate, String endDate, double cost, String status) {
		String[] rowData = {getClientName(clientID), car, startDate, endDate, String.valueOf(cost), status};
		tableModel.addRow(rowData);
	}

	// Метод для загрузки списка аренд из базы данных
	private void loadRentalsFromDatabase() {
		String sql = "SELECT r.ClientID, r.CarID, r.StartDate, r.EndDate, r.TotalCost, r.Status FROM Rentals r";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				int clientID = resultSet.getInt("ClientID");
				int carID = resultSet.getInt("CarID");
				String startDate = resultSet.getString("StartDate");
				String endDate = resultSet.getString("EndDate");
				double totalCost = resultSet.getDouble("TotalCost");
				String status = resultSet.getString("Status");
				addRentalToTable(clientID, getCarInfo(carID), startDate, endDate, totalCost, status);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Метод для получения имени клиента по его ID
	private String getClientName(int clientID) {
		String clientName = "";
		String sql = "SELECT FirstName, LastName FROM Clients WHERE ClientID = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, clientID);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				String firstName = resultSet.getString("FirstName");
				String lastName = resultSet.getString("LastName");
				clientName = firstName + " " + lastName;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return clientName;
	}

	// Метод для получения информации об автомобиле по его ID
	private String getCarInfo(int carID) {
		String carInfo = "";
		String sql = "SELECT Brand, Model FROM Cars WHERE CarID = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, carID);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				String brand = resultSet.getString("Brand");
				String model = resultSet.getString("Model");
				carInfo = brand + " " + model;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return carInfo;
	}

	// Слушатель для кнопки "Добавить"
	private class AddButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String client = (String) clientComboBox.getSelectedItem();
			String car = (String) carComboBox.getSelectedItem();
			String startDate = startDateField.getText();
			String endDate = endDateField.getText();
			double cost = Double.parseDouble(costField.getText());
			boolean completed = completedCheckBox.isSelected();
			if (!client.isEmpty() && !car.isEmpty() && !startDate.isEmpty() && !endDate.isEmpty()) {
				int clientID = getClientIDByName(client);
				int carID = getCarIDByName(car);
				addRentalToTable(clientID, car, startDate, endDate, cost, completed ? "Да" : "Нет");
				insertRentalIntoDatabase(clientID, carID, startDate, endDate, cost, completed);
				clearInputFields();
			} else {
				JOptionPane.showMessageDialog(RentalsPanel.this, "Пожалуйста, заполните все поля корректно", "Ошибка", JOptionPane.ERROR_MESSAGE);
			}
		}
	}


	// Слушатель для кнопки "Удалить"
	// Слушатель для кнопки "Удалить"
	private class DeleteButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedRow = rentalsTable.getSelectedRow();
			if (selectedRow != -1) {
				int confirm = JOptionPane.showConfirmDialog(RentalsPanel.this, "Вы уверены, что хотите удалить эту запись?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					int rentalID = getRentalID(selectedRow);
					if (rentalID != -1) {
						deleteRentalFromDatabase(rentalID);
						tableModel.removeRow(selectedRow);
					} else {
						JOptionPane.showMessageDialog(RentalsPanel.this, "Ошибка удаления записи из базы данных", "Ошибка", JOptionPane.ERROR_MESSAGE);
					}
				}
			} else {
				JOptionPane.showMessageDialog(RentalsPanel.this, "Пожалуйста, выберите запись для удаления", "Ошибка", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// Метод для получения ID аренды по индексу строки в таблице
	private int getRentalID(int rowIndex) {
		int rentalID = -1;
		String sql = "SELECT RentalID FROM Rentals LIMIT ?, 1";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, rowIndex);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				rentalID = resultSet.getInt("RentalID");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rentalID;
	}

	// Метод для удаления записи аренды из базы данных по ее ID
	private void deleteRentalFromDatabase(int rentalID) {
		String sql = "DELETE FROM Rentals WHERE RentalID = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, rentalID);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	// Метод для получения ID клиента по его имени
	private int getClientIDByName(String clientName) {
		int clientID = -1;
		String[] names = clientName.split(" ");
		String firstName = names[0];
		String lastName = names[1];
		String sql = "SELECT ClientID FROM Clients WHERE FirstName = ? AND LastName = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, firstName);
			statement.setString(2, lastName);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				clientID = resultSet.getInt("ClientID");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return clientID;
	}

	// Метод для получения ID автомобиля по его имени
	private int getCarIDByName(String carName) {
		int carID = -1;
		String[] names = carName.split(" ");
		String brand = names[0];
		String model = names[1];
		String sql = "SELECT CarID FROM Cars WHERE Brand = ? AND Model = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, brand);
			statement.setString(2, model);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				carID = resultSet.getInt("CarID");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return carID;
	}

	// Метод для вставки новой аренды в базу данных
	private void insertRentalIntoDatabase(int clientID, int carID, String startDate, String endDate, double cost, boolean completed) {
		String sql = "INSERT INTO Rentals (ClientID, CarID, StartDate, EndDate, TotalCost, Status) VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, clientID);
			statement.setInt(2, carID);
			statement.setString(3, startDate);
			statement.setString(4, endDate);
			statement.setDouble(5, cost);
			statement.setBoolean(6, completed);
			statement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	// Метод для очистки полей ввода формы добавления аренды
	private void clearInputFields() {
		startDateField.setText("");
		endDateField.setText("");
		costField.setText("");
		completedCheckBox.setSelected(false);
	}
}

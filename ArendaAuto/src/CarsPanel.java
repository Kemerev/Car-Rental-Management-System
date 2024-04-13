import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CarsPanel extends JPanel {
	private JTable carsTable;
	private DefaultTableModel tableModel;
	private JTextField brandField;
	private JTextField modelField;
	private JTextField yearField;
	private JTextField rentalCostField;
	private JTextField availabilityField;
	private Connection connection;

	public CarsPanel() {
		setLayout(new BorderLayout());
		try {
			connection = DatabaseConnection.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Ошибка подключения к базе данных", "Ошибка", JOptionPane.ERROR_MESSAGE);
		}

		// Создание таблицы для отображения списка автомобилей
		String[] columnNames = {"Марка", "Модель", "Год выпуска", "Стоимость аренды", "Доступность"};
		tableModel = new DefaultTableModel(columnNames, 0);
		carsTable = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(carsTable);

		// Добавление таблицы в панель
		add(scrollPane, BorderLayout.CENTER);

		// Панель для формы добавления автомобиля
		JPanel addCarPanel = new JPanel(new GridLayout(6, 2));
		addCarPanel.setBorder(BorderFactory.createTitledBorder("Добавить автомобиль"));

		JLabel brandLabel = new JLabel("Марка:");
		brandField = new JTextField();
		JLabel modelLabel = new JLabel("Модель:");
		modelField = new JTextField();
		JLabel yearLabel = new JLabel("Год выпуска:");
		yearField = new JTextField();
		JLabel rentalCostLabel = new JLabel("Стоимость аренды:");
		rentalCostField = new JTextField();
		JLabel availabilityLabel = new JLabel("Доступность:");
		availabilityField = new JTextField();
		JButton addButton = new JButton("Добавить");
		addButton.addActionListener(new AddButtonListener());

		addCarPanel.add(brandLabel);
		addCarPanel.add(brandField);
		addCarPanel.add(modelLabel);
		addCarPanel.add(modelField);
		addCarPanel.add(yearLabel);
		addCarPanel.add(yearField);
		addCarPanel.add(rentalCostLabel);
		addCarPanel.add(rentalCostField);
		addCarPanel.add(availabilityLabel);
		addCarPanel.add(availabilityField);
		addCarPanel.add(new JLabel());
		addCarPanel.add(addButton);

		add(addCarPanel, BorderLayout.NORTH);

		// Кнопка для удаления выбранного автомобиля
		JButton deleteButton = new JButton("Удалить автомобиль");
		deleteButton.addActionListener(new DeleteButtonListener());
		add(deleteButton, BorderLayout.SOUTH);

		// Загрузка автомобилей из базы данных
		loadCarsFromDatabase();
	}

	// Метод для загрузки списка автомобилей из базы данных
	private void loadCarsFromDatabase() {
		String sql = "SELECT Brand, Model, Year, RentalCostPerDay, Availability FROM Cars";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String brand = resultSet.getString("Brand");
				String model = resultSet.getString("Model");
				int year = resultSet.getInt("Year");
				double rentalCostPerDay = resultSet.getDouble("RentalCostPerDay");
				int availability = resultSet.getInt("Availability");
				addCarToTable(brand, model, year, rentalCostPerDay, availability);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Метод для добавления нового автомобиля в базу данных и в таблицу
	private void addCarToTable(String brand, String model, int year, double rentalCostPerDay, int availability) {
		String[] rowData = {brand, model, String.valueOf(year), String.valueOf(rentalCostPerDay), String.valueOf(availability)};
		tableModel.addRow(rowData);
	}

	// Метод для удаления автомобиля из базы данных и из таблицы
	private void deleteCarFromTable(int row) {
		String brand = (String) tableModel.getValueAt(row, 0);
		String model = (String) tableModel.getValueAt(row, 1);
		int year = Integer.parseInt((String) tableModel.getValueAt(row, 2));
		String sql = "DELETE FROM Cars WHERE Brand = ? AND Model = ? AND Year = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, brand);
			statement.setString(2, model);
			statement.setInt(3, year);
			statement.executeUpdate();
			tableModel.removeRow(row);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Слушатель для кнопки "Добавить"
	private class AddButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String brand = brandField.getText();
			String model = modelField.getText();
			int year = Integer.parseInt(yearField.getText());
			double rentalCostPerDay = Double.parseDouble(rentalCostField.getText());
			int availability = Integer.parseInt(availabilityField.getText());
			if (!brand.isEmpty() && !model.isEmpty() && year > 0 && rentalCostPerDay > 0 && (availability == 0 || availability == 1)) {
				addCarToTable(brand, model, year, rentalCostPerDay, availability);
				insertCarIntoDatabase(brand, model, year, rentalCostPerDay, availability);
				clearInputFields();
			} else {
				JOptionPane.showMessageDialog(CarsPanel.this, "Пожалуйста, заполните все поля корректно", "Ошибка", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// Метод для вставки нового автомобиля в базу данных
	private void insertCarIntoDatabase(String brand, String model, int year, double rentalCostPerDay, int availability) {
		String sql = "INSERT INTO Cars (Brand, Model, Year, RentalCostPerDay, Availability) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, brand);
			statement.setString(2, model);
			statement.setInt(3, year);
			statement.setDouble(4, rentalCostPerDay);
			statement.setInt(5, availability);
			statement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	// Метод для очистки полей ввода формы добавления автомобиля
	private void clearInputFields() {
		brandField.setText("");
		modelField.setText("");
		yearField.setText("");
		rentalCostField.setText("");
		availabilityField.setText("");
	}

	// Слушатель для кнопки "Удалить автомобиль"
	private class DeleteButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedRow = carsTable.getSelectedRow();
			if (selectedRow != -1) {
				deleteCarFromTable(selectedRow);
			} else {
				JOptionPane.showMessageDialog(CarsPanel.this, "Пожалуйста, выберите автомобиль для удаления", "Ошибка", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}

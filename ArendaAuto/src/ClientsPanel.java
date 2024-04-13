import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientsPanel extends JPanel {
	private JTable clientsTable;
	private DefaultTableModel tableModel;
	private JTextField firstNameField;
	private JTextField lastNameField;
	private JTextField phoneNumberField;
	private Connection connection;

	public ClientsPanel() {
		setLayout(new BorderLayout());
		try {
			connection = DatabaseConnection.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Ошибка подключения к базе данных", "Ошибка", JOptionPane.ERROR_MESSAGE);
		}

		// Создание таблицы для отображения списка клиентов
		String[] columnNames = {"Имя", "Фамилия", "Номер телефона"};
		tableModel = new DefaultTableModel(columnNames, 0);
		clientsTable = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(clientsTable);

		// Добавление таблицы в панель
		add(scrollPane, BorderLayout.CENTER);

		// Панель для формы добавления клиента
		JPanel addClientPanel = new JPanel(new GridLayout(4, 2));
		addClientPanel.setBorder(BorderFactory.createTitledBorder("Добавить клиента"));

		JLabel firstNameLabel = new JLabel("Имя:");
		firstNameField = new JTextField();
		JLabel lastNameLabel = new JLabel("Фамилия:");
		lastNameField = new JTextField();
		JLabel phoneNumberLabel = new JLabel("Номер телефона:");
		phoneNumberField = new JTextField();
		JButton addButton = new JButton("Добавить");
		addButton.addActionListener(new AddButtonListener());

		addClientPanel.add(firstNameLabel);
		addClientPanel.add(firstNameField);
		addClientPanel.add(lastNameLabel);
		addClientPanel.add(lastNameField);
		addClientPanel.add(phoneNumberLabel);
		addClientPanel.add(phoneNumberField);
		addClientPanel.add(new JLabel());
		addClientPanel.add(addButton);

		add(addClientPanel, BorderLayout.NORTH);

		// Кнопка для удаления выбранного клиента
		JButton deleteButton = new JButton("Удалить клиента");
		deleteButton.addActionListener(new DeleteButtonListener());
		add(deleteButton, BorderLayout.SOUTH);

		// Загрузка клиентов из базы данных
		loadClientsFromDatabase();
	}

	// Метод для загрузки списка клиентов из базы данных
	private void loadClientsFromDatabase() {
		String sql = "SELECT FirstName, LastName, PhoneNumber FROM Clients";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String firstName = resultSet.getString("FirstName");
				String lastName = resultSet.getString("LastName");
				String phoneNumber = resultSet.getString("PhoneNumber");
				addClientToTable(firstName, lastName, phoneNumber);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Метод для добавления нового клиента в базу данных и в таблицу
	private void addClientToTable(String firstName, String lastName, String phoneNumber) {
		String[] rowData = {firstName, lastName, phoneNumber};
		tableModel.addRow(rowData);
	}

	// Метод для удаления клиента из базы данных и из таблицы
	private void deleteClientFromTable(int row) {
		String firstName = (String) tableModel.getValueAt(row, 0);
		String lastName = (String) tableModel.getValueAt(row, 1);
		String phoneNumber = (String) tableModel.getValueAt(row, 2);
		String sql = "DELETE FROM Clients WHERE FirstName = ? AND LastName = ? AND PhoneNumber = ?";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, firstName);
			statement.setString(2, lastName);
			statement.setString(3, phoneNumber);
			statement.executeUpdate();
			tableModel.removeRow(row);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Метод для очистки полей ввода формы добавления клиента
	private void clearInputFields() {
		firstNameField.setText("");
		lastNameField.setText("");
		phoneNumberField.setText("");
	}

	// Слушатель для кнопки "Добавить"
	private class AddButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String firstName = firstNameField.getText();
			String lastName = lastNameField.getText();
			String phoneNumber = phoneNumberField.getText();
			if (!firstName.isEmpty() && !lastName.isEmpty() && !phoneNumber.isEmpty()) {
				addClientToTable(firstName, lastName, phoneNumber);
				insertClientIntoDatabase(firstName, lastName, phoneNumber);
				clearInputFields();
			} else {
				JOptionPane.showMessageDialog(ClientsPanel.this, "Пожалуйста, заполните все поля", "Ошибка", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// Метод для вставки нового клиента в базу данных
	private void insertClientIntoDatabase(String firstName, String lastName, String phoneNumber) {
		String sql = "INSERT INTO Clients (FirstName, LastName, PhoneNumber) VALUES (?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, firstName);
			statement.setString(2, lastName);
			statement.setString(3, phoneNumber);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Слушатель для кнопки "Удалить клиента"
	private class DeleteButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedRow = clientsTable.getSelectedRow();
			if (selectedRow != -1) {
				deleteClientFromTable(selectedRow);
			} else {
				JOptionPane.showMessageDialog(ClientsPanel.this, "Пожалуйста, выберите клиента для удаления", "Ошибка", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}

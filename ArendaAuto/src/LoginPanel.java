import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginPanel extends JPanel {
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JButton loginButton;
	private Connection connection;

	public LoginPanel() {
		setLayout(new BorderLayout());

		JPanel panel = new JPanel(new GridLayout(3, 2));

		JLabel usernameLabel = new JLabel("Username:");
		JLabel passwordLabel = new JLabel("Password:");

		usernameField = new JTextField();
		passwordField = new JPasswordField();

		loginButton = new JButton("Login");
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				login();
			}
		});

		panel.add(usernameLabel);
		panel.add(usernameField);
		panel.add(passwordLabel);
		panel.add(passwordField);
		panel.add(new JLabel());
		panel.add(loginButton);

		add(panel, BorderLayout.CENTER);
	}

	private void login() {
		String username = usernameField.getText();
		String password = String.valueOf(passwordField.getPassword());

		try {
			connection = DatabaseConnection.getConnection();
			String sql = "SELECT * FROM Users WHERE Username=? AND Password=?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, username);
			statement.setString(2, password);
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				JOptionPane.showMessageDialog(this, "Login successful", "Success", JOptionPane.INFORMATION_MESSAGE);
				openMainApp(); // Открываем главное окно
				SwingUtilities.getWindowAncestor(this).dispose(); // Закрываем окно авторизации
			} else {
				JOptionPane.showMessageDialog(this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
			}

			resultSet.close();
			statement.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error connecting to database", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void openMainApp() {
		MainApp mainApp = new MainApp();
		mainApp.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Login");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(new LoginPanel());
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}

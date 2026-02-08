import javax.swing.*;
import java.awt.*;

public class AFSMainFrame extends JFrame {
    private DataManager dataManager;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public AFSMainFrame() {
        dataManager = DataManager.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Assessment Feedback System - APU");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Assessment Feedback System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Login Form
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loginButton = new JButton("Login");
        JButton exitButton = new JButton("Exit");

        loginButton.addActionListener(e -> handleLogin());
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Enter key login
        passwordField.addActionListener(e -> handleLogin());

        // Set focus to username field
        usernameField.requestFocusInWindow();
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter username and password",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = dataManager.authenticate(username, password);
        if (user != null) {
            this.dispose();
            openDashboard(user);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid credentials. Please contact administrator if you need an account.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            usernameField.requestFocus();
        }
    }

    private void openDashboard(User user) {
        if (user instanceof AdminStaff) {
            new AdminDashboard((AdminStaff) user).setVisible(true);
        } else if (user instanceof AcademicLeader) {
            new AcademicLeaderDashboard((AcademicLeader) user).setVisible(true);
        } else if (user instanceof Lecturer) {
            new LecturerDashboard((Lecturer) user).setVisible(true);
        } else if (user instanceof Student) {
            new StudentDashboard((Student) user).setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AFSMainFrame().setVisible(true);
        });
    }
}
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

class AdminDashboard extends JFrame {
    private AdminStaff admin;
    private DataManager dataManager;
    private JTabbedPane tabbedPane;

    public AdminDashboard(AdminStaff admin) {
        this.admin = admin;
        this.dataManager = DataManager.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Admin Dashboard - " + admin.getFullName());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("User Management", createUserManagementPanel());
        tabbedPane.addTab("Field Management", createFieldManagementPanel());
        tabbedPane.addTab("Assign Lecturers", createAssignmentPanel());
        tabbedPane.addTab("Grading System", createGradingPanel());
        tabbedPane.addTab("Class Management", createClassManagementPanel());
        tabbedPane.addTab("Profile", createProfilePanel());

        add(tabbedPane);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        menu.add(logoutItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // User list
        String[] columns = {"User ID", "Username", "Full Name", "Role", "Email"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        JTable userTable = new JTable(model);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only allow single selection
        refreshUserTable(model);

        JScrollPane scrollPane = new JScrollPane(userTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add User");
        JButton editButton = new JButton("Edit User");
        JButton deleteButton = new JButton("Delete User");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> showAddUserDialog(model));
        editButton.addActionListener(e -> showEditUserDialog(userTable, model));
        deleteButton.addActionListener(e -> deleteUser(userTable, model));
        refreshButton.addActionListener(e -> refreshUserTable(model));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshUserTable(DefaultTableModel model) {
        model.setRowCount(0); // Clear all rows first

        // Use a Set to track unique user IDs to prevent duplicates
        Set<String> addedUserIds = new HashSet<>();

        for (User user : dataManager.getAllUsers()) {
            // Only add if we haven't seen this user ID before
            if (!addedUserIds.contains(user.getUserId())) {
                model.addRow(new Object[]{
                        user.getUserId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getRole(),
                        user.getEmail()
                });
                addedUserIds.add(user.getUserId());
            }
        }

        System.out.println("User table refreshed. Total unique users: " + addedUserIds.size());
    }

    private void showAddUserDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Add New User", true);
        dialog.setSize(450, 450);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(9, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField = new JTextField();
        JTextField emailField = new JTextField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{
                "ADMIN", "ACADEMIC_LEADER", "LECTURER", "STUDENT"
        });

        JLabel fieldLabel = new JLabel("Field:");
        JComboBox<String> fieldCombo = new JComboBox<>();

        // Populate field combo
        for (Field field : dataManager.getAllFields()) {
            fieldCombo.addItem(field.getFieldId() + " - " + field.getFieldName());
        }

        // Initially hide field selection
        fieldLabel.setVisible(false);
        fieldCombo.setVisible(false);

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Full Name:"));
        panel.add(fullNameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Role:"));
        panel.add(roleCombo);
        panel.add(fieldLabel);
        panel.add(fieldCombo);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        // Show/hide field selection based on role
        roleCombo.addActionListener(e -> {
            String selectedRole = (String) roleCombo.getSelectedItem();
            boolean showField = selectedRole.equals("ACADEMIC_LEADER") || selectedRole.equals("LECTURER");
            fieldLabel.setVisible(showField);
            fieldCombo.setVisible(showField);
            dialog.revalidate();
            dialog.repaint();
        });

        saveButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String role = (String) roleCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!");
                return;
            }

            // Check if username already exists
            for (User existingUser : dataManager.getAllUsers()) {
                if (existingUser.getUsername().equals(username)) {
                    JOptionPane.showMessageDialog(dialog, "Username already exists!");
                    return;
                }
            }

            User newUser = null;
            String userId = "";

            switch (role) {
                case "ADMIN":
                    userId = dataManager.generateNextId("ADM");
                    newUser = new AdminStaff(userId, username, password, fullName, email);
                    break;

                case "ACADEMIC_LEADER":
                    if (fieldCombo.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(dialog, "Please select a field for the Academic Leader!");
                        return;
                    }
                    userId = dataManager.generateNextId("AL");
                    AcademicLeader leader = new AcademicLeader(userId, username, password, fullName, email);

                    // Assign field
                    String leaderFieldId = ((String) fieldCombo.getSelectedItem()).split(" - ")[0];
                    leader.setFieldId(leaderFieldId);

                    // Update field with this leader
                    Field leaderField = dataManager.getFieldById(leaderFieldId);
                    if (leaderField != null) {
                        leaderField.setAcademicLeaderId(userId);
                    }

                    newUser = leader;
                    break;

                case "LECTURER":
                    if (fieldCombo.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(dialog, "Please select a field for the Lecturer!");
                        return;
                    }
                    userId = dataManager.generateNextId("LEC");
                    Lecturer lecturer = new Lecturer(userId, username, password, fullName, email);

                    // Assign field
                    String lecFieldId = ((String) fieldCombo.getSelectedItem()).split(" - ")[0];
                    lecturer.setFieldId(lecFieldId);

                    // Find and assign to academic leader of that field
                    Field lecField = dataManager.getFieldById(lecFieldId);
                    if (lecField != null && lecField.getAcademicLeaderId() != null) {
                        lecturer.setAcademicLeaderId(lecField.getAcademicLeaderId());

                        // Add to leader's managed lecturers
                        for (User u : dataManager.getAllUsers()) {
                            if (u instanceof AcademicLeader && u.getUserId().equals(lecField.getAcademicLeaderId())) {
                                ((AcademicLeader) u).addLecturer(userId);
                                break;
                            }
                        }
                    }

                    newUser = lecturer;
                    break;

                case "STUDENT":
                    userId = dataManager.generateNextId("STU");
                    newUser = new Student(userId, username, password, fullName, email);
                    break;
            }

            dataManager.addUser(newUser);
            refreshUserTable(model);
            if (role.equals("LECTURER") || role.equals("ACADEMIC_LEADER")) {
                refreshAssignmentPanel();
            }

            JOptionPane.showMessageDialog(dialog, "User created successfully!\nUser ID: " + userId);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        panel.add(saveButton);
        panel.add(cancelButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditUserDialog(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit");
            return;
        }

        String userId = (String) table.getValueAt(selectedRow, 0);
        User user = dataManager.getUserById(userId);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "User not found!");
            return;
        }

        JDialog dialog = new JDialog(this, "Edit User", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField fullNameField = new JTextField(user.getFullName());
        JTextField emailField = new JTextField(user.getEmail());
        JPasswordField passwordField = new JPasswordField();

        panel.add(new JLabel("Full Name:"));
        panel.add(fullNameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("New Password (optional):"));
        panel.add(passwordField);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        User finalUser = user;
        saveButton.addActionListener(e -> {
            finalUser.setFullName(fullNameField.getText().trim());
            finalUser.setEmail(emailField.getText().trim());
            String newPassword = new String(passwordField.getPassword());
            if (!newPassword.isEmpty()) {
                finalUser.setPassword(newPassword);
            }
            dataManager.saveAllData();
            refreshUserTable(model);
            JOptionPane.showMessageDialog(dialog, "User updated successfully!");
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        panel.add(saveButton);
        panel.add(cancelButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteUser(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete");
            return;
        }

        String userId = (String) table.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this user?\nUser ID: " + userId,
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.deleteUser(userId);
            refreshUserTable(model);
            JOptionPane.showMessageDialog(this, "User deleted successfully!");
        }
    }

    private JPanel createAssignmentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        JComboBox<String> lecturerCombo = new JComboBox<>();
        JComboBox<String> leaderCombo = new JComboBox<>();

        for (Lecturer lec : dataManager.getAllLecturers()) {
            lecturerCombo.addItem(lec.getUserId() + " - " + lec.getFullName());
        }

        for (User user : dataManager.getAllUsers()) {
            if (user instanceof AcademicLeader) {
                leaderCombo.addItem(user.getUserId() + " - " + user.getFullName());
            }
        }

        formPanel.add(new JLabel("Select Lecturer:"));
        formPanel.add(lecturerCombo);
        formPanel.add(new JLabel("Assign to Academic Leader:"));
        formPanel.add(leaderCombo);

        JButton assignButton = new JButton("Assign");
        assignButton.addActionListener(e -> {
            if (lecturerCombo.getSelectedItem() == null || leaderCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select both lecturer and leader");
                return;
            }

            String lecId = ((String) lecturerCombo.getSelectedItem()).split(" - ")[0];
            String leaderId = ((String) leaderCombo.getSelectedItem()).split(" - ")[0];

            for (Lecturer lec : dataManager.getAllLecturers()) {
                if (lec.getUserId().equals(lecId)) {
                    lec.setAcademicLeaderId(leaderId);
                    break;
                }
            }

            dataManager.saveAllData();
            JOptionPane.showMessageDialog(this, "Lecturer assigned successfully!");
        });

        formPanel.add(assignButton);

        panel.add(formPanel, BorderLayout.NORTH);

        return panel;
    }

    private void refreshAssignmentPanel() {
        int assignmentTabIndex = -1;
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals("Assign Lecturers")) {
                assignmentTabIndex = i;
                break;
            }
        }

        if (assignmentTabIndex != -1) {
            tabbedPane.removeTabAt(assignmentTabIndex);
            tabbedPane.insertTab("Assign Lecturers", null, createAssignmentPanel(), null, assignmentTabIndex);
        }
    }

    private JPanel createGradingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea gradingInfo = new JTextArea();
        gradingInfo.setEditable(false);
        gradingInfo.setFont(new Font("Monospaced", Font.PLAIN, 12));

        StringBuilder info = new StringBuilder("APU Grading System:\n\n");
        info.append(String.format("%-10s %-15s %-20s\n", "Grade", "Range (%)", "Description"));
        info.append("=".repeat(50) + "\n");

        Map<String, GradingSystem.GradeRange> ranges = GradingSystem.getGradeRanges();
        for (GradingSystem.GradeRange range : ranges.values()) {
            info.append(String.format("%-10s %-15s %-20s\n",
                    range.grade,
                    range.minPercentage + "-" + range.maxPercentage,
                    range.description));
        }

        gradingInfo.setText(info.toString());

        JScrollPane scrollPane = new JScrollPane(gradingInfo);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createClassManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Class ID", "Class Name", "Module", "Day", "Time", "Room", "Enrolled/Capacity"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable classTable = new JTable(model);
        refreshClassTable(model);

        JScrollPane scrollPane = new JScrollPane(classTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Create Class");
        JButton editButton = new JButton("Edit Schedule");
        JButton deleteButton = new JButton("Delete Class");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> showCreateClassDialog(model));
        editButton.addActionListener(e -> showEditClassScheduleDialog(classTable, model));
        deleteButton.addActionListener(e -> deleteClass(classTable, model));
        refreshButton.addActionListener(e -> refreshClassTable(model));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshClassTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (ClassGroup cls : dataManager.getAllClasses()) {
            Module module = dataManager.getModuleById(cls.getModuleId());
            String moduleName = module != null ? module.getModuleName() : "N/A";

            String day = cls.getDayOfWeek() != null ? cls.getDayOfWeek() : "Not set";
            String time = cls.getStartTime() != null ? cls.getStartTime() + "-" + cls.getEndTime() : "Not set";
            String room = cls.getRoom() != null ? cls.getRoom() : "Not set";
            String enrollment = cls.getStudentIds().size() + "/" + cls.getCapacity();

            model.addRow(new Object[]{
                    cls.getClassId(),
                    cls.getClassName(),
                    moduleName,
                    day,
                    time,
                    room,
                    enrollment
            });
        }
    }

    private void showCreateClassDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Create New Class", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(9, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField classNameField = new JTextField();
        JComboBox<String> moduleCombo = new JComboBox<>();

        for (Module module : dataManager.getAllModules()) {
            moduleCombo.addItem(module.getModuleId() + " - " + module.getModuleName());
        }

        // Day of week dropdown
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        JComboBox<String> dayCombo = new JComboBox<>(days);

        // Time dropdowns
        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format("%02d:00", i);
        }
        JComboBox<String> startTimeCombo = new JComboBox<>(hours);
        JComboBox<String> endTimeCombo = new JComboBox<>(hours);
        startTimeCombo.setSelectedItem("09:00");
        endTimeCombo.setSelectedItem("11:00");

        JTextField roomField = new JTextField("Room 101");
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(30, 1, 100, 1));

        panel.add(new JLabel("Class Name:"));
        panel.add(classNameField);
        panel.add(new JLabel("Module:"));
        panel.add(moduleCombo);
        panel.add(new JLabel("Day of Week:"));
        panel.add(dayCombo);
        panel.add(new JLabel("Start Time:"));
        panel.add(startTimeCombo);
        panel.add(new JLabel("End Time:"));
        panel.add(endTimeCombo);
        panel.add(new JLabel("Room:"));
        panel.add(roomField);
        panel.add(new JLabel("Capacity:"));
        panel.add(capacitySpinner);

        JButton saveButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String className = classNameField.getText().trim();
            if (className.isEmpty() || moduleCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields!");
                return;
            }

            String moduleId = ((String) moduleCombo.getSelectedItem()).split(" - ")[0];
            String classId = "CLS" + String.format("%03d", dataManager.getAllClasses().size() + 1);
            String day = (String) dayCombo.getSelectedItem();
            String startTime = (String) startTimeCombo.getSelectedItem();
            String endTime = (String) endTimeCombo.getSelectedItem();
            String room = roomField.getText().trim();
            int capacity = (Integer) capacitySpinner.getValue();

            ClassGroup newClass = new ClassGroup(classId, className, moduleId, day, startTime, endTime, room, capacity);
            dataManager.addClass(newClass);
            refreshClassTable(model);

            JOptionPane.showMessageDialog(dialog,
                    "Class created successfully!\n" +
                            "Class ID: " + classId + "\n" +
                            "Schedule: " + day + " " + startTime + "-" + endTime + "\n" +
                            "Room: " + room + "\n" +
                            "Capacity: " + capacity);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        panel.add(saveButton);
        panel.add(cancelButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditClassScheduleDialog(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to edit");
            return;
        }

        String classId = (String) table.getValueAt(selectedRow, 0);
        ClassGroup cls = dataManager.getClassById(classId);

        if (cls == null) return;

        JDialog dialog = new JDialog(this, "Edit Class Schedule", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        JComboBox<String> dayCombo = new JComboBox<>(days);
        if (cls.getDayOfWeek() != null) {
            dayCombo.setSelectedItem(cls.getDayOfWeek());
        }

        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format("%02d:00", i);
        }
        JComboBox<String> startTimeCombo = new JComboBox<>(hours);
        JComboBox<String> endTimeCombo = new JComboBox<>(hours);

        if (cls.getStartTime() != null) {
            startTimeCombo.setSelectedItem(cls.getStartTime());
        }
        if (cls.getEndTime() != null) {
            endTimeCombo.setSelectedItem(cls.getEndTime());
        }

        JTextField roomField = new JTextField(cls.getRoom() != null ? cls.getRoom() : "");
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(cls.getCapacity(), 1, 100, 1));

        panel.add(new JLabel("Class ID:"));
        panel.add(new JLabel(cls.getClassId()));
        panel.add(new JLabel("Day of Week:"));
        panel.add(dayCombo);
        panel.add(new JLabel("Start Time:"));
        panel.add(startTimeCombo);
        panel.add(new JLabel("End Time:"));
        panel.add(endTimeCombo);
        panel.add(new JLabel("Room:"));
        panel.add(roomField);
        panel.add(new JLabel("Capacity:"));
        panel.add(capacitySpinner);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            cls.setDayOfWeek((String) dayCombo.getSelectedItem());
            cls.setStartTime((String) startTimeCombo.getSelectedItem());
            cls.setEndTime((String) endTimeCombo.getSelectedItem());
            cls.setRoom(roomField.getText().trim());
            cls.setCapacity((Integer) capacitySpinner.getValue());

            dataManager.saveAllData();
            refreshClassTable(model);
            JOptionPane.showMessageDialog(dialog, "Class schedule updated successfully!");
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        panel.add(saveButton);
        panel.add(cancelButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteClass(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to delete");
            return;
        }

        String classId = (String) table.getValueAt(selectedRow, 0);
        ClassGroup cls = dataManager.getClassById(classId);

        if (cls != null && !cls.getStudentIds().isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "This class has " + cls.getStudentIds().size() + " enrolled students.\nAre you sure you want to delete it?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this class?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.deleteClass(classId);
            refreshClassTable(model);
            JOptionPane.showMessageDialog(this, "Class deleted successfully!");
        }
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("User ID:"));
        panel.add(new JLabel(admin.getUserId()));
        panel.add(new JLabel("Username:"));
        panel.add(new JLabel(admin.getUsername()));
        panel.add(new JLabel("Full Name:"));
        JTextField nameField = new JTextField(admin.getFullName());
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField(admin.getEmail());
        panel.add(emailField);
        panel.add(new JLabel("Role:"));
        panel.add(new JLabel(admin.getRole()));

        JButton updateButton = new JButton("Update Profile");
        updateButton.addActionListener(e -> {
            admin.setFullName(nameField.getText());
            admin.setEmail(emailField.getText());
            dataManager.saveAllData();
            JOptionPane.showMessageDialog(this, "Profile updated!");
        });

        panel.add(updateButton);

        return panel;
    }

    private JPanel createFieldManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Field ID", "Field Name", "Academic Leader", "Modules Count"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable fieldTable = new JTable(model);
        refreshFieldTable(model);

        JScrollPane scrollPane = new JScrollPane(fieldTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Field");
        JButton editButton = new JButton("Edit Field");
        JButton deleteButton = new JButton("Delete Field");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> showAddFieldDialog(model));
        editButton.addActionListener(e -> showEditFieldDialog(fieldTable, model));
        deleteButton.addActionListener(e -> deleteField(fieldTable, model));
        refreshButton.addActionListener(e -> refreshFieldTable(model));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshFieldTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Field field : dataManager.getAllFields()) {
            String leaderName = "";
            if (field.getAcademicLeaderId() != null) {
                User leader = dataManager.getUserById(field.getAcademicLeaderId());
                if (leader != null) {
                    leaderName = leader.getFullName();
                }
            }
            model.addRow(new Object[]{
                    field.getFieldId(),
                    field.getFieldName(),
                    leaderName,
                    field.getModuleIds().size()
            });
        }
    }

    private void showAddFieldDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Add New Field", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField fieldNameField = new JTextField();

        panel.add(new JLabel("Field Name:"));
        panel.add(fieldNameField);

        JButton saveButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String fieldName = fieldNameField.getText().trim();

            if (fieldName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Field name is required!");
                return;
            }

            String fieldId = "FLD" + String.format("%03d", dataManager.getAllFields().size() + 1);
            Field newField = new Field(fieldId, fieldName);

            dataManager.addField(newField);
            refreshFieldTable(model);
            JOptionPane.showMessageDialog(dialog, "Field created successfully!\nField ID: " + fieldId);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        panel.add(saveButton);
        panel.add(cancelButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditFieldDialog(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a field to edit");
            return;
        }

        String fieldId = (String) table.getValueAt(selectedRow, 0);
        Field field = dataManager.getFieldById(fieldId);

        if (field == null) return;

        JDialog dialog = new JDialog(this, "Edit Field", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField(field.getFieldName());

        panel.add(new JLabel("Field Name:"));
        panel.add(nameField);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String newName = nameField.getText().trim();

            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Field name cannot be empty!");
                return;
            }

            field.setFieldName(newName);
            dataManager.saveAllData();
            refreshFieldTable(model);
            JOptionPane.showMessageDialog(dialog, "Field updated successfully!");
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        panel.add(saveButton);
        panel.add(cancelButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteField(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a field to delete");
            return;
        }

        String fieldId = (String) table.getValueAt(selectedRow, 0);
        Field field = dataManager.getFieldById(fieldId);

        if (field.getAcademicLeaderId() != null || !field.getModuleIds().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete field that has assigned leaders or modules!\nPlease remove them first.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this field?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.deleteField(fieldId);
            refreshFieldTable(model);
            JOptionPane.showMessageDialog(this, "Field deleted successfully!");
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?");
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new AFSMainFrame().setVisible(true);
        }
    }
}
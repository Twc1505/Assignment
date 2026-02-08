import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.io.File;
import java.util.Set;
import java.util.HashSet;

class StudentDashboard extends JFrame {
    private Student student;
    private DataManager dataManager;

    public StudentDashboard(Student student) {
        this.student = student;
        this.dataManager = DataManager.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Student Dashboard - " + student.getFullName());
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Register Classes", createRegistrationPanel());
        tabbedPane.addTab("Submit Assignments", createSubmissionPanel());
        tabbedPane.addTab("My Results", createResultsPanel());
        tabbedPane.addTab("Feedback", createFeedbackViewPanel());
        tabbedPane.addTab("Comments", createCommentsPanel());
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

    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add filter panel at top
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by Module:"));
        JComboBox<String> moduleFilterCombo = new JComboBox<>();
        moduleFilterCombo.addItem("All Modules");
        for (Module module : dataManager.getAllModules()) {
            moduleFilterCombo.addItem(module.getModuleId() + " - " + module.getModuleName());
        }
        filterPanel.add(moduleFilterCombo);

        JButton filterButton = new JButton("Apply Filter");
        filterPanel.add(filterButton);
        panel.add(filterPanel, BorderLayout.NORTH);

        String[] columns = {"Class ID", "Class Name", "Module", "Day", "Time", "Room", "Available Slots", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable classTable = new JTable(model);
        refreshRegistrationTable(model, null);

        // Filter functionality
        filterButton.addActionListener(e -> {
            String selected = (String) moduleFilterCombo.getSelectedItem();
            if (selected.equals("All Modules")) {
                refreshRegistrationTable(model, null);
            } else {
                String moduleId = selected.split(" - ")[0];
                refreshRegistrationTable(model, moduleId);
            }
        });

        JScrollPane scrollPane = new JScrollPane(classTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton registerButton = new JButton("Register for Selected Class");
        JButton dropButton = new JButton("Drop Selected Class");
        JButton viewDetailsButton = new JButton("View Details");
        JButton refreshButton = new JButton("Refresh");

        registerButton.addActionListener(e -> registerForClass(classTable, model));
        dropButton.addActionListener(e -> dropClass(classTable, model));
        viewDetailsButton.addActionListener(e -> showClassDetails(classTable));
        refreshButton.addActionListener(e -> {
            String selected = (String) moduleFilterCombo.getSelectedItem();
            if (selected.equals("All Modules")) {
                refreshRegistrationTable(model, null);
            } else {
                String moduleId = selected.split(" - ")[0];
                refreshRegistrationTable(model, moduleId);
            }
        });

        buttonPanel.add(registerButton);
        buttonPanel.add(dropButton);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshRegistrationTable(DefaultTableModel model, String filterModuleId) {
        model.setRowCount(0);

        for (ClassGroup cls : dataManager.getAllClasses()) {
            // Apply module filter if specified
            if (filterModuleId != null && !cls.getModuleId().equals(filterModuleId)) {
                continue;
            }

            Module module = dataManager.getModuleById(cls.getModuleId());
            String moduleName = module != null ? module.getModuleName() : "N/A";

            String day = cls.getDayOfWeek() != null ? cls.getDayOfWeek() : "TBA";
            String time = cls.getStartTime() != null ? cls.getStartTime() + "-" + cls.getEndTime() : "TBA";
            String room = cls.getRoom() != null ? cls.getRoom() : "TBA";

            String status;
            if (student.getRegisteredClasses().contains(cls.getClassId())) {
                status = "Registered";
            } else if (cls.isFull()) {
                status = "Full";
            } else {
                status = "Available";
            }

            String availableSlots = cls.getAvailableSlots() + "/" + cls.getCapacity();

            model.addRow(new Object[]{
                    cls.getClassId(),
                    cls.getClassName(),
                    moduleName,
                    day,
                    time,
                    room,
                    availableSlots,
                    status
            });
        }
    }

    private void registerForClass(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to register");
            return;
        }

        String classId = (String) table.getValueAt(selectedRow, 0);
        String status = (String) table.getValueAt(selectedRow, 7);

        if (status.equals("Registered")) {
            JOptionPane.showMessageDialog(this, "You are already registered for this class!");
            return;
        }

        if (status.equals("Full")) {
            JOptionPane.showMessageDialog(this, "This class is full. Please select another class or time slot.");
            return;
        }

        ClassGroup cls = dataManager.getClassById(classId);
        if (cls == null) return;

        // Check for time conflicts
        String conflict = checkScheduleConflict(cls);
        if (conflict != null) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Warning: Time conflict detected!\n" + conflict + "\n\nDo you still want to register?",
                    "Schedule Conflict",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Register student
        student.registerClass(classId);
        cls.addStudent(student.getUserId());
        dataManager.saveAllData();

        // Show confirmation with schedule details
        Module module = dataManager.getModuleById(cls.getModuleId());
        JOptionPane.showMessageDialog(this,
                "Successfully registered!\n\n" +
                        "Class: " + cls.getClassName() + "\n" +
                        "Module: " + (module != null ? module.getModuleName() : "N/A") + "\n" +
                        "Schedule: " + cls.getScheduleInfo() + "\n" +
                        "Enrolled: " + cls.getStudentIds().size() + "/" + cls.getCapacity(),
                "Registration Successful",
                JOptionPane.INFORMATION_MESSAGE);

        refreshRegistrationTable(model, null);
    }

    private String checkScheduleConflict(ClassGroup newClass) {
        if (newClass.getDayOfWeek() == null || newClass.getStartTime() == null) {
            return null; // No schedule set yet
        }

        for (String registeredClassId : student.getRegisteredClasses()) {
            ClassGroup regClass = dataManager.getClassById(registeredClassId);
            if (regClass == null || regClass.getDayOfWeek() == null) continue;

            // Check if same day
            if (regClass.getDayOfWeek().equals(newClass.getDayOfWeek())) {
                // Check time overlap
                if (timesOverlap(regClass.getStartTime(), regClass.getEndTime(),
                        newClass.getStartTime(), newClass.getEndTime())) {
                    return "Conflicts with: " + regClass.getClassName() +
                            " (" + regClass.getStartTime() + "-" + regClass.getEndTime() + ")";
                }
            }
        }
        return null;
    }

    private boolean timesOverlap(String start1, String end1, String start2, String end2) {
        try {
            int s1 = Integer.parseInt(start1.replace(":", ""));
            int e1 = Integer.parseInt(end1.replace(":", ""));
            int s2 = Integer.parseInt(start2.replace(":", ""));
            int e2 = Integer.parseInt(end2.replace(":", ""));

            return (s1 < e2 && s2 < e1);
        } catch (Exception e) {
            return false;
        }
    }

    private void dropClass(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to drop");
            return;
        }

        String classId = (String) table.getValueAt(selectedRow, 0);
        String status = (String) table.getValueAt(selectedRow, 7);

        if (!status.equals("Registered")) {
            JOptionPane.showMessageDialog(this, "You are not registered for this class!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to drop this class?",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            student.getRegisteredClasses().remove(classId);

            ClassGroup cls = dataManager.getClassById(classId);
            if (cls != null) {
                cls.removeStudent(student.getUserId());
            }

            dataManager.saveAllData();
            refreshRegistrationTable(model, null);
            JOptionPane.showMessageDialog(this, "Class dropped successfully!");
        }
    }

    private void showClassDetails(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class to view details");
            return;
        }

        String classId = (String) table.getValueAt(selectedRow, 0);
        ClassGroup cls = dataManager.getClassById(classId);

        if (cls == null) return;

        Module module = dataManager.getModuleById(cls.getModuleId());
        String moduleName = module != null ? module.getModuleName() : "N/A";
        String moduleCode = module != null ? module.getModuleCode() : "N/A";

        // Get lecturer name
        String lecturerName = "N/A";
        if (module != null && module.getLecturerId() != null) {
            User lecturer = dataManager.getUserById(module.getLecturerId());
            if (lecturer != null) {
                lecturerName = lecturer.getFullName();
            }
        }

        StringBuilder details = new StringBuilder();
        details.append("CLASS DETAILS\n");
        details.append("=".repeat(40)).append("\n\n");
        details.append("Class ID: ").append(cls.getClassId()).append("\n");
        details.append("Class Name: ").append(cls.getClassName()).append("\n");
        details.append("Module: ").append(moduleName).append(" (").append(moduleCode).append(")\n");
        details.append("Lecturer: ").append(lecturerName).append("\n\n");
        details.append("SCHEDULE\n");
        details.append("-".repeat(40)).append("\n");
        details.append("Day: ").append(cls.getDayOfWeek() != null ? cls.getDayOfWeek() : "TBA").append("\n");
        details.append("Time: ").append(cls.getStartTime() != null ? cls.getStartTime() + " - " + cls.getEndTime() : "TBA").append("\n");
        details.append("Room: ").append(cls.getRoom() != null ? cls.getRoom() : "TBA").append("\n\n");
        details.append("ENROLLMENT\n");
        details.append("-".repeat(40)).append("\n");
        details.append("Capacity: ").append(cls.getCapacity()).append(" students\n");
        details.append("Enrolled: ").append(cls.getStudentIds().size()).append(" students\n");
        details.append("Available: ").append(cls.getAvailableSlots()).append(" slots\n");

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 350));

        JOptionPane.showMessageDialog(this, scrollPane, "Class Details", JOptionPane.INFORMATION_MESSAGE);
    }



    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Assessment", "Module", "Marks Obtained", "Total Marks", "Grade"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable resultTable = new JTable(model);

        List<AssessmentResult> studentResults = dataManager.getResultsByStudent(student.getUserId());
        for (AssessmentResult result : studentResults) {
            Assessment assessment = null;
            for (Assessment a : dataManager.getAllAssessments()) {
                if (a.getAssessmentId().equals(result.getAssessmentId())) {
                    assessment = a;
                    break;
                }
            }

            if (assessment != null) {
                Module module = dataManager.getModuleById(assessment.getModuleId());
                String moduleName = module != null ? module.getModuleName() : "N/A";

                model.addRow(new Object[]{
                        assessment.getAssessmentName(),
                        moduleName,
                        result.getMarksObtained(),
                        assessment.getTotalMarks(),
                        result.getGrade()
                });
            }
        }

        panel.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFeedbackViewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<String> assessmentCombo = new JComboBox<>();
        List<AssessmentResult> studentResults = dataManager.getResultsByStudent(student.getUserId());

        for (AssessmentResult result : studentResults) {
            for (Assessment a : dataManager.getAllAssessments()) {
                if (a.getAssessmentId().equals(result.getAssessmentId())) {
                    assessmentCombo.addItem(a.getAssessmentId() + " - " + a.getAssessmentName());
                    break;
                }
            }
        }

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Select Assessment:"));
        topPanel.add(assessmentCombo);

        panel.add(topPanel, BorderLayout.NORTH);

        JTextArea feedbackArea = new JTextArea();
        feedbackArea.setEditable(false);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);

        panel.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);

        JButton viewButton = new JButton("View Feedback");
        viewButton.addActionListener(e -> {
            if (assessmentCombo.getSelectedItem() == null) return;

            String assessmentId = ((String) assessmentCombo.getSelectedItem()).split(" - ")[0];

            for (AssessmentResult result : studentResults) {
                if (result.getAssessmentId().equals(assessmentId)) {
                    String feedback = result.getFeedback();
                    feedbackArea.setText(feedback != null && !feedback.isEmpty() ? feedback : "No feedback available yet.");
                    break;
                }
            }
        });

        panel.add(viewButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCommentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Info label
        JLabel infoLabel = new JLabel("<html><b>Module & Lecturer Feedback</b><br>" +
                "Your feedback is anonymous and helps improve the quality of teaching.</html>");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(infoLabel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));

        // Module selection panel
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(new JLabel("Select Module to Provide Feedback:"));

        JComboBox<String> moduleCombo = new JComboBox<>();
        // Populate with student's registered modules
        for (String classId : student.getRegisteredClasses()) {
            ClassGroup cls = dataManager.getClassById(classId);
            if (cls != null) {
                Module module = dataManager.getModuleById(cls.getModuleId());
                if (module != null) {
                    String item = module.getModuleId() + " - " + module.getModuleName();
                    // Check if already in combo to avoid duplicates
                    boolean exists = false;
                    for (int i = 0; i < moduleCombo.getItemCount(); i++) {
                        if (moduleCombo.getItemAt(i).startsWith(module.getModuleId())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        moduleCombo.addItem(item);
                    }
                }
            }
        }
        selectionPanel.add(moduleCombo);
        contentPanel.add(selectionPanel, BorderLayout.NORTH);

        // Feedback form panel
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Feedback Form"));

        JPanel ratingsPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        ratingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Rating components
        JLabel contentLabel = new JLabel("Content Quality (1-5):");
        JSpinner contentSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));

        JLabel teachingLabel = new JLabel("Teaching Effectiveness (1-5):");
        JSpinner teachingSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));

        JLabel clarityLabel = new JLabel("Material Clarity (1-5):");
        JSpinner claritySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));

        JLabel responsivenessLabel = new JLabel("Lecturer Responsiveness (1-5):");
        JSpinner responsivenessSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));

        JLabel overallLabel = new JLabel("Overall Satisfaction (1-5):");
        JSpinner overallSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));

        ratingsPanel.add(contentLabel);
        ratingsPanel.add(contentSpinner);
        ratingsPanel.add(teachingLabel);
        ratingsPanel.add(teachingSpinner);
        ratingsPanel.add(clarityLabel);
        ratingsPanel.add(claritySpinner);
        ratingsPanel.add(responsivenessLabel);
        ratingsPanel.add(responsivenessSpinner);
        ratingsPanel.add(overallLabel);
        ratingsPanel.add(overallSpinner);

        formPanel.add(ratingsPanel, BorderLayout.NORTH);

        // Text feedback panel
        JPanel textPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        textPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel strengthsLabel = new JLabel("What are the strengths of this module/lecturer?");
        JTextArea strengthsArea = new JTextArea(3, 40);
        strengthsArea.setLineWrap(true);
        strengthsArea.setWrapStyleWord(true);
        JScrollPane strengthsScroll = new JScrollPane(strengthsArea);

        JLabel improvementsLabel = new JLabel("What could be improved?");
        JTextArea improvementsArea = new JTextArea(3, 40);
        improvementsArea.setLineWrap(true);
        improvementsArea.setWrapStyleWord(true);
        JScrollPane improvementsScroll = new JScrollPane(improvementsArea);

        JLabel commentsLabel = new JLabel("Additional Comments (Optional):");
        JTextArea commentsArea = new JTextArea(3, 40);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        JScrollPane commentsScroll = new JScrollPane(commentsArea);

        textPanel.add(strengthsLabel);
        textPanel.add(strengthsScroll);
        textPanel.add(improvementsLabel);
        textPanel.add(improvementsScroll);
        textPanel.add(commentsLabel);
        textPanel.add(commentsScroll);

        formPanel.add(textPanel, BorderLayout.CENTER);

        contentPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton submitButton = new JButton("Submit Feedback");
        JButton clearButton = new JButton("Clear Form");
        JButton viewMyFeedbackButton = new JButton("View My Submitted Feedback");

        submitButton.addActionListener(e -> {
            if (moduleCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select a module!");
                return;
            }

            String moduleId = ((String) moduleCombo.getSelectedItem()).split(" - ")[0];
            Module module = dataManager.getModuleById(moduleId);

            if (module == null || module.getLecturerId() == null) {
                JOptionPane.showMessageDialog(this, "Module or lecturer information not found!");
                return;
            }

            // Check if already submitted feedback for this module
            for (ModuleFeedback fb : dataManager.getAllModuleFeedbacks()) {
                if (fb.getStudentId().equals(student.getUserId()) &&
                        fb.getModuleId().equals(moduleId)) {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "You have already submitted feedback for this module.\nDo you want to update it?",
                            "Update Feedback",
                            JOptionPane.YES_NO_OPTION);
                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }
                    // Remove old feedback
                    dataManager.deleteModuleFeedback(fb.getFeedbackId());
                    break;
                }
            }

            // Create new feedback
            String feedbackId = "FB" + String.format("%03d", dataManager.getAllModuleFeedbacks().size() + 1);
            ModuleFeedback feedback = new ModuleFeedback(feedbackId, student.getUserId(),
                    moduleId, module.getLecturerId());

            feedback.setContentQualityRating((Integer) contentSpinner.getValue());
            feedback.setTeachingEffectivenessRating((Integer) teachingSpinner.getValue());
            feedback.setMaterialClarityRating((Integer) claritySpinner.getValue());
            feedback.setResponsivenessRating((Integer) responsivenessSpinner.getValue());
            feedback.setOverallRating((Integer) overallSpinner.getValue());
            feedback.setStrengths(strengthsArea.getText().trim());
            feedback.setImprovements(improvementsArea.getText().trim());
            feedback.setAdditionalComments(commentsArea.getText().trim());

            dataManager.addModuleFeedback(feedback);

            JOptionPane.showMessageDialog(this,
                    "Thank you for your feedback!\nYour response has been submitted anonymously.",
                    "Feedback Submitted",
                    JOptionPane.INFORMATION_MESSAGE);

            // Clear form
            contentSpinner.setValue(3);
            teachingSpinner.setValue(3);
            claritySpinner.setValue(3);
            responsivenessSpinner.setValue(3);
            overallSpinner.setValue(3);
            strengthsArea.setText("");
            improvementsArea.setText("");
            commentsArea.setText("");
        });

        clearButton.addActionListener(e -> {
            contentSpinner.setValue(3);
            teachingSpinner.setValue(3);
            claritySpinner.setValue(3);
            responsivenessSpinner.setValue(3);
            overallSpinner.setValue(3);
            strengthsArea.setText("");
            improvementsArea.setText("");
            commentsArea.setText("");
        });

        viewMyFeedbackButton.addActionListener(e -> showMySubmittedFeedback());

        buttonPanel.add(submitButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(viewMyFeedbackButton);

        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Student ID:"));
        panel.add(new JLabel(student.getUserId()));

        panel.add(new JLabel("Full Name:"));
        JTextField nameField = new JTextField(student.getFullName());
        panel.add(nameField);

        panel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField(student.getEmail());
        panel.add(emailField);

        panel.add(new JLabel("Current Password:"));
        JPasswordField currentPasswordField = new JPasswordField();
        panel.add(currentPasswordField);

        panel.add(new JLabel("New Password:"));
        JPasswordField newPasswordField = new JPasswordField();
        panel.add(newPasswordField);

        panel.add(new JLabel("Confirm New Password:"));
        JPasswordField confirmPasswordField = new JPasswordField();
        panel.add(confirmPasswordField);

        JButton updateButton = new JButton("Update Profile");
        updateButton.addActionListener(e -> {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // Update name and email
            student.setFullName(nameField.getText().trim());
            student.setEmail(emailField.getText().trim());

            // Check if user wants to change password
            if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
                // Verify current password
                if (!currentPassword.equals(student.getPassword())) {
                    JOptionPane.showMessageDialog(this,
                            "Current password is incorrect!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check if new password is provided
                if (newPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "New password cannot be empty!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check if passwords match
                if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(this,
                            "New passwords do not match!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check password length
                if (newPassword.length() < 6) {
                    JOptionPane.showMessageDialog(this,
                            "Password must be at least 6 characters long!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Update password
                student.setPassword(newPassword);

                // Clear password fields
                currentPasswordField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");

                dataManager.saveAllData(); // AUTO-SAVE
                JOptionPane.showMessageDialog(this,
                        "Profile and password updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Only name/email updated
                dataManager.saveAllData(); // AUTO-SAVE
                JOptionPane.showMessageDialog(this,
                        "Profile updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        panel.add(updateButton);

        // Add info label
        JLabel infoLabel = new JLabel("<html><i>Leave password fields empty to keep current password</i></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        panel.add(infoLabel);

        return panel;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?");
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new AFSMainFrame().setVisible(true);
        }
    }

    private void showMySubmittedFeedback() {
        JDialog dialog = new JDialog(this, "My Submitted Feedback", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);

        String[] columns = {"Module", "Submitted Date", "Avg Rating", "View Details"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);

        for (ModuleFeedback fb : dataManager.getAllModuleFeedbacks()) {
            if (fb.getStudentId().equals(student.getUserId())) {
                Module module = dataManager.getModuleById(fb.getModuleId());
                String moduleName = module != null ? module.getModuleName() : "N/A";

                model.addRow(new Object[]{
                        moduleName,
                        fb.getSubmissionDate().toString(),
                        String.format("%.1f", fb.getAverageRating()),
                        "Click to view"
                });
            }
        }

        JScrollPane scrollPane = new JScrollPane(table);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // ============================================================
    // SUBMISSION PANEL
    // ============================================================
    private JPanel createSubmissionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel infoLabel = new JLabel("<html><b>Assignment Submission</b><br>" +
                "Submit your assignments here. You can view submission status and deadlines.</html>");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(infoLabel, BorderLayout.NORTH);

        String[] columns = {"Assessment ID", "Assessment Name", "Module", "Type",
                "Total Marks", "Status", "Submission Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable assessmentTable = new JTable(model);

        refreshSubmissionTable(model);

        JScrollPane scrollPane = new JScrollPane(assessmentTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton submitButton = new JButton("Submit Assignment");
        JButton viewSubmissionButton = new JButton("View My Submission");
        JButton refreshButton = new JButton("Refresh");

        submitButton.addActionListener(e -> showSubmissionDialog(assessmentTable, model));
        viewSubmissionButton.addActionListener(e -> viewSubmissionDetails(assessmentTable));
        refreshButton.addActionListener(e -> refreshSubmissionTable(model));

        buttonPanel.add(submitButton);
        buttonPanel.add(viewSubmissionButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshSubmissionTable(DefaultTableModel model) {
        model.setRowCount(0);

        Set<String> studentModules = new HashSet<>();
        for (String classId : student.getRegisteredClasses()) {
            ClassGroup cls = dataManager.getClassById(classId);
            if (cls != null) {
                studentModules.add(cls.getModuleId());
            }
        }

        for (Assessment assessment : dataManager.getAllAssessments()) {
            if (studentModules.contains(assessment.getModuleId())) {
                Module module = dataManager.getModuleById(assessment.getModuleId());
                String moduleName = module != null ? module.getModuleName() : "N/A";

                String status = "NOT SUBMITTED";
                String submissionDate = "-";

                // PRIORITY 1: Check if result exists (graded by lecturer)
                AssessmentResult result = dataManager.getResultByStudentAndAssessment(
                        student.getUserId(), assessment.getAssessmentId()
                );

                if (result != null) {
                    // Has been graded - show as GRADED
                    status = "GRADED";
                    submissionDate = result.getSubmissionDate() != null ?
                            result.getSubmissionDate().toString().substring(0, 16) :
                            "Graded";
                } else {
                    // Check if submission exists (submitted but not graded yet)
                    AssessmentSubmission submission = dataManager.getSubmissionByStudentAndAssessment(
                            student.getUserId(), assessment.getAssessmentId()
                    );

                    if (submission != null) {
                        status = submission.getStatus(); // SUBMITTED or GRADED
                        submissionDate = submission.getSubmissionDate().toString().substring(0, 16);
                    }
                }

                model.addRow(new Object[]{
                        assessment.getAssessmentId(),
                        assessment.getAssessmentName(),
                        moduleName,
                        assessment.getAssessmentType(),
                        assessment.getTotalMarks(),
                        status,
                        submissionDate
                });
            }
        }
    }

    private void showSubmissionDialog(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an assessment to submit!");
            return;
        }

        String assessmentId = (String) table.getValueAt(selectedRow, 0);
        String assessmentName = (String) table.getValueAt(selectedRow, 1);
        String status = (String) table.getValueAt(selectedRow, 5);

        if (status.equals("GRADED")) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "This assessment has already been graded.\nDo you want to resubmit?",
                    "Already Graded",
                    JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;
        }

        JDialog dialog = new JDialog(this, "Submit Assignment - " + assessmentName, true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        infoPanel.add(new JLabel("Assessment:"));
        infoPanel.add(new JLabel(assessmentName));
        infoPanel.add(new JLabel("Assessment ID:"));
        infoPanel.add(new JLabel(assessmentId));
        infoPanel.add(new JLabel("Student:"));
        infoPanel.add(new JLabel(student.getFullName()));

        mainPanel.add(infoPanel, BorderLayout.NORTH);

        JPanel filePanel = new JPanel(new BorderLayout(5, 5));
        filePanel.setBorder(BorderFactory.createTitledBorder("Select File to Submit"));

        JTextField filePathField = new JTextField();
        filePathField.setEditable(false);
        JButton browseButton = new JButton("Browse...");

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(dialog);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        });

        JPanel browsePanel = new JPanel(new BorderLayout(5, 0));
        browsePanel.add(filePathField, BorderLayout.CENTER);
        browsePanel.add(browseButton, BorderLayout.EAST);

        filePanel.add(browsePanel, BorderLayout.NORTH);

        JLabel commentsLabel = new JLabel("Comments (Optional):");
        JTextArea commentsArea = new JTextArea(5, 30);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        JScrollPane commentsScroll = new JScrollPane(commentsArea);

        filePanel.add(commentsLabel, BorderLayout.CENTER);
        filePanel.add(commentsScroll, BorderLayout.SOUTH);

        mainPanel.add(filePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton submitBtn = new JButton("Submit");
        JButton cancelBtn = new JButton("Cancel");

        submitBtn.addActionListener(e -> {
            String filePath = filePathField.getText().trim();

            if (filePath.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please select a file to submit!",
                        "No File Selected",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            File file = new File(filePath);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(dialog,
                        "Selected file does not exist!",
                        "File Not Found",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            AssessmentSubmission existingSubmission =
                    dataManager.getSubmissionByStudentAndAssessment(student.getUserId(), assessmentId);

            if (existingSubmission != null) {
                existingSubmission.setFilePath(filePath);
                existingSubmission.setFileName(file.getName());
                existingSubmission.setStatus("SUBMITTED");
                existingSubmission.setSubmissionComments(commentsArea.getText().trim());
            } else {
                String submissionId = "SUB" + String.format("%03d",
                        dataManager.getAllSubmissions().size() + 1);
                AssessmentSubmission newSubmission = new AssessmentSubmission(
                        submissionId, assessmentId, student.getUserId(), filePath, file.getName()
                );
                newSubmission.setSubmissionComments(commentsArea.getText().trim());
                dataManager.addSubmission(newSubmission);
            }

            dataManager.saveAllData();
            refreshSubmissionTable(model);

            JOptionPane.showMessageDialog(dialog,
                    "Assignment submitted successfully!\n\n" +
                            "File: " + file.getName() + "\n" +
                            "Submitted: " + new java.util.Date(),
                    "Submission Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void viewSubmissionDetails(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an assessment!");
            return;
        }

        String assessmentId = (String) table.getValueAt(selectedRow, 0);
        AssessmentSubmission submission = dataManager.getSubmissionByStudentAndAssessment(
                student.getUserId(), assessmentId
        );

        if (submission == null) {
            JOptionPane.showMessageDialog(this,
                    "No submission found for this assessment.",
                    "Not Submitted",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("SUBMISSION DETAILS\n");
        details.append("=".repeat(50)).append("\n\n");
        details.append("Submission ID: ").append(submission.getSubmissionId()).append("\n");
        details.append("File Name: ").append(submission.getFileName()).append("\n");
        details.append("File Path: ").append(submission.getFilePath()).append("\n");
        details.append("Submission Date: ").append(submission.getSubmissionDate()).append("\n");
        details.append("Status: ").append(submission.getStatus()).append("\n\n");

        if (submission.getSubmissionComments() != null &&
                !submission.getSubmissionComments().isEmpty()) {
            details.append("Your Comments:\n");
            details.append(submission.getSubmissionComments()).append("\n");
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(this, scrollPane,
                "Submission Details", JOptionPane.INFORMATION_MESSAGE);
    }
}

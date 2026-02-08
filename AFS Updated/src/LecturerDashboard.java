import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

class LecturerDashboard extends JFrame {
    private Lecturer lecturer;
    private DataManager dataManager;
    private JLabel notificationLabel;
    private JTabbedPane mainTabbedPane;

    public LecturerDashboard(Lecturer lecturer) {
        this.lecturer = lecturer;
        this.dataManager = DataManager.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Lecturer Dashboard - " + lecturer.getFullName());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Field info and notification panel at top
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Left side - field info
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Field lecField = dataManager.getFieldById(lecturer.getFieldId());
        String fieldName = lecField != null ? lecField.getFieldName() : "No Field Assigned";
        JLabel fieldLabel = new JLabel("Your Field: " + fieldName);
        fieldLabel.setFont(new Font("Arial", Font.BOLD, 12));
        leftPanel.add(fieldLabel);

        // Right side - notification badge (NOW STORED AS CLASS VARIABLE)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        int pendingCount = dataManager.getNotificationCountForLecturer(lecturer.getUserId());

        notificationLabel = new JLabel(); // CREATE AS CLASS VARIABLE
        updateNotificationLabel(); // Call update method
        rightPanel.add(notificationLabel);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);

        mainTabbedPane = new JTabbedPane(); // STORE AS CLASS VARIABLE
        mainTabbedPane.addTab("My Modules", createModulesPanel());
        mainTabbedPane.addTab("Assessments", createAssessmentsPanel());

        // Create pending submissions tab with initial count
        mainTabbedPane.addTab("Pending Submissions (" + pendingCount + ")", createPendingSubmissionsPanel());

        mainTabbedPane.addTab("Enter Marks", createMarksEntryPanel());
        mainTabbedPane.addTab("Feedback", createFeedbackPanel());
        mainTabbedPane.addTab("Profile", createProfilePanel());

        mainPanel.add(mainTabbedPane, BorderLayout.CENTER);
        add(mainPanel);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        menu.add(logoutItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    private JPanel createModulesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Module Code", "Module Name", "Total Classes", "Total Students"};
        DefaultTableModel moduleModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable moduleTable = new JTable(moduleModel); //

        // Refresh module table
        refreshModuleTable(moduleModel);

        JScrollPane moduleScrollPane = new JScrollPane(moduleTable);

        // Class schedule details panel
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Class Schedule Details"));

        String[] classColumns = {"Class ID", "Class Name", "Day", "Time", "Room", "Enrolled", "Capacity"};
        DefaultTableModel classModel = new DefaultTableModel(classColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable classTable = new JTable(classModel);
        JScrollPane classScrollPane = new JScrollPane(classTable);

        JButton viewStudentsButton = new JButton("View Enrolled Students");
        viewStudentsButton.addActionListener(e -> showEnrolledStudents(classTable));

        JPanel classButtonPanel = new JPanel(new FlowLayout());
        classButtonPanel.add(viewStudentsButton);

        bottomPanel.add(classScrollPane, BorderLayout.CENTER);
        bottomPanel.add(classButtonPanel, BorderLayout.SOUTH);

        // Split pane for modules and classes
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, moduleScrollPane, bottomPanel);
        splitPane.setDividerLocation(200);
        panel.add(splitPane, BorderLayout.CENTER);

        // When a module is selected, show its classes
        moduleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = moduleTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String moduleCode = (String) moduleTable.getValueAt(selectedRow, 0);
                    // Find module by code
                    for (Module module : dataManager.getAllModules()) {
                        if (module.getModuleCode().equals(moduleCode) &&
                                module.getLecturerId() != null &&
                                module.getLecturerId().equals(lecturer.getUserId())) {
                            refreshClassesForModule(classModel, module.getModuleId());
                            break;
                        }
                    }
                }
            }
        });

        return panel;
    }

    private void refreshModuleTable(DefaultTableModel model) {
        model.setRowCount(0);

        System.out.println("DEBUG: Refreshing module table for lecturer: " + lecturer.getUserId());
        System.out.println("DEBUG: Lecturer assigned modules: " + lecturer.getAssignedModules().size());

        // Get modules assigned to this lecturer
        for (Module module : dataManager.getAllModules()) {
            if (module.getLecturerId() != null && module.getLecturerId().equals(lecturer.getUserId())) {
                System.out.println("DEBUG: Found module: " + module.getModuleId() + " - " + module.getModuleName());

                List<ClassGroup> classes = dataManager.getClassesByModule(module.getModuleId());
                int totalStudents = 0;
                for (ClassGroup cls : classes) {
                    totalStudents += cls.getStudentIds().size();
                }

                model.addRow(new Object[]{
                        module.getModuleCode(),
                        module.getModuleName(),
                        classes.size(),
                        totalStudents
                });
            }
        }

        System.out.println("DEBUG: Total modules shown: " + model.getRowCount());
    }

    private void refreshClassesForModule(DefaultTableModel model, String moduleId) {
        model.setRowCount(0);
        List<ClassGroup> classes = dataManager.getClassesByModule(moduleId);

        System.out.println("DEBUG: Loading classes for module: " + moduleId);
        System.out.println("DEBUG: Found " + classes.size() + " classes");

        for (ClassGroup cls : classes) {
            String day = cls.getDayOfWeek() != null ? cls.getDayOfWeek() : "Not set";
            String time = cls.getStartTime() != null ? cls.getStartTime() + "-" + cls.getEndTime() : "Not set";
            String room = cls.getRoom() != null ? cls.getRoom() : "Not set";

            model.addRow(new Object[]{
                    cls.getClassId(),
                    cls.getClassName(),
                    day,
                    time,
                    room,
                    cls.getStudentIds().size(),
                    cls.getCapacity()
            });
        }
    }

    private void showEnrolledStudents(JTable classTable) {
        int selectedRow = classTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a class first");
            return;
        }

        String classId = (String) classTable.getValueAt(selectedRow, 0);
        ClassGroup cls = dataManager.getClassById(classId);

        if (cls == null) return;

        // Create dialog to show students
        JDialog dialog = new JDialog(this, "Enrolled Students - " + cls.getClassName(), true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        // Get module info
        Module module = dataManager.getModuleById(cls.getModuleId());
        String moduleName = module != null ? module.getModuleName() : "N/A";

        // Header panel with class info
        JPanel headerPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        headerPanel.add(new JLabel("Class:"));
        headerPanel.add(new JLabel(cls.getClassName()));
        headerPanel.add(new JLabel("Module:"));
        headerPanel.add(new JLabel(moduleName));
        headerPanel.add(new JLabel("Schedule:"));
        headerPanel.add(new JLabel(cls.getScheduleInfo()));
        headerPanel.add(new JLabel("Room:"));
        headerPanel.add(new JLabel(cls.getRoom() != null ? cls.getRoom() : "Not set"));
        headerPanel.add(new JLabel("Enrolled/Capacity:"));
        headerPanel.add(new JLabel(cls.getStudentIds().size() + "/" + cls.getCapacity()));

        // Student table
        String[] columns = {"Student ID", "Full Name", "Email"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable studentTable = new JTable(model);

        // Populate with enrolled students
        for (String studentId : cls.getStudentIds()) {
            User student = dataManager.getUserById(studentId);
            if (student != null) {
                model.addRow(new Object[]{
                        student.getUserId(),
                        student.getFullName(),
                        student.getEmail()
                });
            }
        }

        JScrollPane scrollPane = new JScrollPane(studentTable);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton exportButton = new JButton("Export List");
        JButton closeButton = new JButton("Close");

        exportButton.addActionListener(e -> {
            StringBuilder export = new StringBuilder();
            export.append("CLASS ENROLLMENT LIST\n");
            export.append("=".repeat(60)).append("\n\n");
            export.append("Class: ").append(cls.getClassName()).append("\n");
            export.append("Module: ").append(moduleName).append("\n");
            export.append("Schedule: ").append(cls.getScheduleInfo()).append("\n");
            export.append("Room: ").append(cls.getRoom() != null ? cls.getRoom() : "Not set").append("\n");
            export.append("Total Students: ").append(cls.getStudentIds().size()).append("\n\n");
            export.append(String.format("%-15s %-30s %-30s\n", "Student ID", "Full Name", "Email"));
            export.append("=".repeat(60)).append("\n");

            for (String studentId : cls.getStudentIds()) {
                User student = dataManager.getUserById(studentId);
                if (student != null) {
                    export.append(String.format("%-15s %-30s %-30s\n",
                            student.getUserId(),
                            student.getFullName(),
                            student.getEmail()));
                }
            }

            JTextArea textArea = new JTextArea(export.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            JScrollPane exportScroll = new JScrollPane(textArea);
            exportScroll.setPreferredSize(new Dimension(650, 400));

            JOptionPane.showMessageDialog(dialog, exportScroll, "Export Preview", JOptionPane.INFORMATION_MESSAGE);
        });

        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel createAssessmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Assessment ID", "Name", "Type", "Total Marks", "Weightage %"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable assessmentTable = new JTable(model);
        refreshAssessmentTable(model);

        JScrollPane scrollPane = new JScrollPane(assessmentTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton createButton = new JButton("Create Assessment");
        JButton refreshButton = new JButton("Refresh");

        createButton.addActionListener(e -> showCreateAssessmentDialog(model));
        refreshButton.addActionListener(e -> refreshAssessmentTable(model));

        buttonPanel.add(createButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshAssessmentTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Module module : dataManager.getAllModules()) {
            if (module.getLecturerId() != null && module.getLecturerId().equals(lecturer.getUserId())) {
                List<Assessment> assessments = dataManager.getAssessmentsByModule(module.getModuleId());
                for (Assessment assessment : assessments) {
                    model.addRow(new Object[]{
                            assessment.getAssessmentId(),
                            assessment.getAssessmentName(),
                            assessment.getAssessmentType(),
                            assessment.getTotalMarks(),
                            assessment.getWeightage()
                    });
                }
            }
        }
    }

    private void showCreateAssessmentDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Create Assessment", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JComboBox<String> moduleCombo = new JComboBox<>();
        for (Module module : dataManager.getAllModules()) {
            if (module.getLecturerId() != null && module.getLecturerId().equals(lecturer.getUserId())) {
                moduleCombo.addItem(module.getModuleId() + " - " + module.getModuleName());
            }
        }

        JTextField nameField = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
                "Assignment", "Quiz", "Test", "Exam", "Project", "Presentation"
        });
        JTextField marksField = new JTextField();
        JTextField weightageField = new JTextField();

        panel.add(new JLabel("Module:"));
        panel.add(moduleCombo);
        panel.add(new JLabel("Assessment Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Type:"));
        panel.add(typeCombo);
        panel.add(new JLabel("Total Marks:"));
        panel.add(marksField);
        panel.add(new JLabel("Weightage (%):"));
        panel.add(weightageField);

        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");

        createButton.addActionListener(e -> {
            try {
                if (moduleCombo.getSelectedItem() == null || nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all fields!");
                    return;
                }

                String moduleId = ((String) moduleCombo.getSelectedItem()).split(" - ")[0];
                String name = nameField.getText().trim();
                String type = (String) typeCombo.getSelectedItem();
                double marks = Double.parseDouble(marksField.getText().trim());
                double weightage = Double.parseDouble(weightageField.getText().trim());

                String assessmentId = "ASS" + String.format("%03d", dataManager.getAllAssessments().size() + 1);
                Assessment assessment = new Assessment(assessmentId, name, type, marks, weightage, moduleId);
                dataManager.addAssessment(assessment);
                refreshAssessmentTable(model);

                JOptionPane.showMessageDialog(dialog, "Assessment created successfully!");
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for marks and weightage!");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        panel.add(createButton);
        panel.add(cancelButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JPanel createMarksEntryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout());

        JComboBox<String> assessmentCombo = new JComboBox<>();
        for (Assessment assessment : dataManager.getAllAssessments()) {
            for (Module module : dataManager.getAllModules()) {
                if (module.getModuleId().equals(assessment.getModuleId()) &&
                        module.getLecturerId() != null &&
                        module.getLecturerId().equals(lecturer.getUserId())) {
                    assessmentCombo.addItem(assessment.getAssessmentId() + " - " + assessment.getAssessmentName());
                }
            }
        }

        topPanel.add(new JLabel("Select Assessment:"));
        topPanel.add(assessmentCombo);

        JButton loadButton = new JButton("Load Students");
        topPanel.add(loadButton);

        panel.add(topPanel, BorderLayout.NORTH);

        // Table with marks, grade, and status columns
        String[] columns = {"Student ID", "Student Name", "Marks", "Grade", "Status", "Feedback"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Only marks column is editable in table
            }
        };
        JTable marksTable = new JTable(model);

        // Set column widths
        marksTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Student ID
        marksTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Student Name
        marksTable.getColumnModel().getColumn(2).setPreferredWidth(60);  // Marks
        marksTable.getColumnModel().getColumn(3).setPreferredWidth(50);  // Grade
        marksTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Status
        marksTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Feedback

        loadButton.addActionListener(e -> {
            if (assessmentCombo.getSelectedItem() == null) return;

            String assessmentId = ((String) assessmentCombo.getSelectedItem()).split(" - ")[0];
            Assessment assessment = dataManager.getAssessmentById(assessmentId);

            if (assessment == null) return;

            model.setRowCount(0);
            for (ClassGroup cls : dataManager.getAllClasses()) {
                if (cls.getModuleId().equals(assessment.getModuleId())) {
                    for (String studentId : cls.getStudentIds()) {
                        User student = dataManager.getUserById(studentId);

                        if (student != null) {
                            // Check if marks already exist (INCLUDING from Pending Submissions grading)
                            AssessmentResult existingResult = dataManager.getResultByStudentAndAssessment(studentId, assessmentId);

                            String existingMarks = "";
                            String existingGrade = "";
                            String status = "Not Graded";
                            String feedbackStatus = "No Feedback";

                            if (existingResult != null) {
                                existingMarks = String.valueOf(existingResult.getMarksObtained());
                                existingGrade = existingResult.getGrade() != null ? existingResult.getGrade() : "";
                                status = "Graded";

                                String feedback = existingResult.getFeedback();
                                if (feedback != null && !feedback.trim().isEmpty()) {
                                    feedbackStatus = "Has Feedback";
                                }
                            } else {
                                // Check if submission exists (shows if student submitted but not graded)
                                AssessmentSubmission submission = dataManager.getSubmissionByStudentAndAssessment(studentId, assessmentId);
                                if (submission != null) {
                                    status = "Submitted - Pending";
                                    feedbackStatus = "Waiting for grade";
                                }
                            }

                            model.addRow(new Object[]{studentId, student.getFullName(), existingMarks, existingGrade, status, feedbackStatus});
                        }
                    }
                }
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(panel,
                        "No students found for this assessment.\nMake sure students are enrolled in classes for this module.",
                        "No Students",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JScrollPane scrollPane = new JScrollPane(marksTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton editMarksButton = new JButton("Edit Selected Student's Marks & Feedback");
        JButton bulkSaveButton = new JButton("Quick Save Marks (Bulk)");
        JButton viewFeedbackButton = new JButton("View Feedback");
        JButton refreshButton = new JButton("Refresh");

        // Edit Marks Button - Opens dialog for individual student
        editMarksButton.addActionListener(e -> {
            int selectedRow = marksTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a student first!");
                return;
            }

            if (assessmentCombo.getSelectedItem() == null) return;

            String studentId = (String) marksTable.getValueAt(selectedRow, 0);
            String studentName = (String) marksTable.getValueAt(selectedRow, 1);
            String assessmentId = ((String) assessmentCombo.getSelectedItem()).split(" - ")[0];

            showEditMarksDialog(studentId, studentName, assessmentId, marksTable, selectedRow);
        });

        // Bulk Save Button - For quick marks entry in table
        bulkSaveButton.addActionListener(e -> {
            if (assessmentCombo.getSelectedItem() == null) return;

            String assessmentId = ((String) assessmentCombo.getSelectedItem()).split(" - ")[0];
            Assessment selectedAssessment = dataManager.getAssessmentById(assessmentId);

            if (selectedAssessment == null) return;

            int updatedCount = 0;
            int newCount = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 0; i < marksTable.getRowCount(); i++) {
                String studentId = (String) marksTable.getValueAt(i, 0);
                Object marksObj = marksTable.getValueAt(i, 2);

                if (marksObj != null && !marksObj.toString().trim().isEmpty()) {
                    try {
                        double marks = Double.parseDouble(marksObj.toString().trim());

                        // Validate marks
                        if (marks < 0 || marks > selectedAssessment.getTotalMarks()) {
                            errors.add("Student " + studentId + ": Marks must be between 0 and " + selectedAssessment.getTotalMarks());
                            continue;
                        }

                        // Check if this is an update or new entry
                        AssessmentResult existingResult = dataManager.getResultByStudentAndAssessment(studentId, assessmentId);

                        // Preserve existing feedback if it exists
                        String existingFeedback = "";
                        if (existingResult != null) {
                            existingFeedback = existingResult.getFeedback() != null ? existingResult.getFeedback() : "";
                            updatedCount++;
                        } else {
                            newCount++;
                        }

                        // AUTO-GRADE CALCULATION
                        dataManager.updateOrCreateResultWithAutoGrade(studentId, assessmentId, marks, existingFeedback);

                        // Calculate grade for display
                        double percentage = (marks / selectedAssessment.getTotalMarks()) * 100;
                        String grade = GradingSystem.calculateGrade(percentage);
                        marksTable.setValueAt(grade, i, 3);
                        marksTable.setValueAt("Graded", i, 4);

                    } catch (NumberFormatException ex) {
                        errors.add("Student " + studentId + ": Invalid marks format");
                    }
                }
            }

            // Show results
            StringBuilder message = new StringBuilder();

            if (newCount > 0 || updatedCount > 0) {
                message.append("Marks saved successfully!\n\n");
                if (newCount > 0) {
                    message.append("New results created: ").append(newCount).append("\n");
                }
                if (updatedCount > 0) {
                    message.append("Existing results updated: ").append(updatedCount).append("\n");
                }
            }

            if (!errors.isEmpty()) {
                message.append("\nErrors:\n");
                for (String error : errors) {
                    message.append("- ").append(error).append("\n");
                }
            }

            if (message.length() > 0) {
                JOptionPane.showMessageDialog(panel, message.toString(),
                        errors.isEmpty() ? "Success" : "Completed with Errors",
                        errors.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(panel, "No marks were entered!", "Info", JOptionPane.INFORMATION_MESSAGE);
            }

            // Refresh to show updated status
            loadButton.doClick();
        });

        // View Feedback Button
        viewFeedbackButton.addActionListener(e -> {
            int selectedRow = marksTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a student first!");
                return;
            }

            if (assessmentCombo.getSelectedItem() == null) return;

            String studentId = (String) marksTable.getValueAt(selectedRow, 0);
            String studentName = (String) marksTable.getValueAt(selectedRow, 1);
            String assessmentId = ((String) assessmentCombo.getSelectedItem()).split(" - ")[0];

            AssessmentResult result = dataManager.getResultByStudentAndAssessment(studentId, assessmentId);

            if (result == null) {
                JOptionPane.showMessageDialog(panel, "No results found for this student!");
                return;
            }

            String feedback = result.getFeedback();
            if (feedback == null || feedback.trim().isEmpty()) {
                feedback = "No feedback provided yet.";
            }

            JTextArea textArea = new JTextArea(feedback, 10, 40);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JScrollPane scroll = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(panel, scroll,
                    "Feedback for " + studentName,
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // Refresh Button
        refreshButton.addActionListener(e -> loadButton.doClick());

        buttonPanel.add(editMarksButton);
        buttonPanel.add(bulkSaveButton);
        buttonPanel.add(viewFeedbackButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Edit marks dialog (same as grading from Pending Submissions)
    private void showEditMarksDialog(String studentId, String studentName, String assessmentId, JTable marksTable, int tableRow) {
        // Get assessment details
        Assessment assessment = dataManager.getAssessmentById(assessmentId);
        if (assessment == null) return;

        Module module = dataManager.getModuleById(assessment.getModuleId());
        String moduleName = module != null ? module.getModuleName() : "N/A";

        // Get existing result if any
        AssessmentResult existingResult = dataManager.getResultByStudentAndAssessment(studentId, assessmentId);

        // Create dialog
        JDialog dialog = new JDialog(this, "Edit Marks & Feedback - " + studentName, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Assessment Details"));

        infoPanel.add(new JLabel("Student:"));
        infoPanel.add(new JLabel(studentName + " (" + studentId + ")"));

        infoPanel.add(new JLabel("Module:"));
        infoPanel.add(new JLabel(moduleName));

        infoPanel.add(new JLabel("Assessment:"));
        infoPanel.add(new JLabel(assessment.getAssessmentName()));

        infoPanel.add(new JLabel("Type:"));
        infoPanel.add(new JLabel(assessment.getAssessmentType()));

        // Marks section
        JLabel marksLabel = new JLabel("Marks:");
        JPanel marksPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField marksField = new JTextField(10);
        marksPanel.add(marksField);
        marksPanel.add(new JLabel("/ " + assessment.getTotalMarks()));

        JLabel gradePreviewLabel = new JLabel("Grade: (will be calculated automatically)");
        gradePreviewLabel.setFont(new Font("Arial", Font.BOLD, 12));
        marksPanel.add(gradePreviewLabel);

        infoPanel.add(marksLabel);
        infoPanel.add(marksPanel);

        // Pre-fill existing marks if available
        if (existingResult != null) {
            marksField.setText(String.valueOf(existingResult.getMarksObtained()));
            double percentage = (existingResult.getMarksObtained() / assessment.getTotalMarks()) * 100;
            String grade = GradingSystem.calculateGrade(percentage);
            gradePreviewLabel.setText("Grade: " + grade + " (" + String.format("%.1f%%", percentage) + ")");
            gradePreviewLabel.setForeground(Color.BLUE);
        }

        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Feedback panel
        JPanel feedbackPanel = new JPanel(new BorderLayout(5, 5));
        feedbackPanel.setBorder(BorderFactory.createTitledBorder("Feedback"));

        JLabel feedbackLabel = new JLabel("Provide detailed feedback to help the student improve:");
        feedbackPanel.add(feedbackLabel, BorderLayout.NORTH);

        JTextArea feedbackArea = new JTextArea(10, 40);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setFont(new Font("Arial", Font.PLAIN, 12));

        // Pre-fill existing feedback if available
        if (existingResult != null && existingResult.getFeedback() != null) {
            feedbackArea.setText(existingResult.getFeedback());
        }

        JScrollPane feedbackScroll = new JScrollPane(feedbackArea);
        feedbackPanel.add(feedbackScroll, BorderLayout.CENTER);

        mainPanel.add(feedbackPanel, BorderLayout.CENTER);

        // Declare final for lambda
        final Assessment finalAssessment = assessment;

        // Real-time grade preview
        marksField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                try {
                    double marks = Double.parseDouble(marksField.getText());
                    if (finalAssessment != null) {
                        double percentage = (marks / finalAssessment.getTotalMarks()) * 100;
                        String grade = GradingSystem.calculateGrade(percentage);
                        gradePreviewLabel.setText("Grade: " + grade + " (" + String.format("%.1f%%", percentage) + ")");
                        gradePreviewLabel.setForeground(Color.BLUE);
                    }
                } catch (NumberFormatException ex) {
                    gradePreviewLabel.setText("Grade: (enter valid marks)");
                    gradePreviewLabel.setForeground(Color.GRAY);
                }
            }
        });

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton saveButton = new JButton("Save Marks & Feedback");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String marksText = marksField.getText().trim();
            String feedback = feedbackArea.getText().trim();

            // Validate marks
            if (marksText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter marks for this assessment!",
                        "Marks Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double marks = Double.parseDouble(marksText);

                // Validate marks range
                if (marks < 0 || marks > assessment.getTotalMarks()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Marks must be between 0 and " + assessment.getTotalMarks(),
                            "Invalid Marks",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Calculate grade (auto)
                double percentage = (marks / assessment.getTotalMarks()) * 100;
                String grade = GradingSystem.calculateGrade(percentage);

                // Save result with feedback (AUTO-GRADE)
                dataManager.updateOrCreateResultWithAutoGrade(studentId, assessmentId, marks, feedback);

                // Update table display
                marksTable.setValueAt(marksText, tableRow, 2);
                marksTable.setValueAt(grade, tableRow, 3);
                marksTable.setValueAt("Graded", tableRow, 4);
                marksTable.setValueAt(feedback.isEmpty() ? "No Feedback" : "Has Feedback", tableRow, 5);

                JOptionPane.showMessageDialog(dialog,
                        "Marks and feedback saved successfully!\n\n" +
                                "Marks: " + marks + "/" + assessment.getTotalMarks() + "\n" +
                                "Grade: " + grade + " (auto-calculated)\n" +
                                "Feedback: " + (feedback.isEmpty() ? "None" : feedback.length() + " characters"),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid number for marks!",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void showFeedbackDialog(String studentId, String studentName, String assessmentId, JTable marksTable, int tableRow) {
        // Get assessment details
        Assessment assessment = dataManager.getAssessmentById(assessmentId);
        if (assessment == null) return;

        Module module = dataManager.getModuleById(assessment.getModuleId());
        String moduleName = module != null ? module.getModuleName() : "N/A";

        // Get existing result if any
        AssessmentResult existingResult = dataManager.getResultByStudentAndAssessment(studentId, assessmentId);

        // Create dialog
        JDialog dialog = new JDialog(this, "Provide Feedback - " + studentName, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Assessment Details"));

        infoPanel.add(new JLabel("Student:"));
        infoPanel.add(new JLabel(studentName + " (" + studentId + ")"));

        infoPanel.add(new JLabel("Module:"));
        infoPanel.add(new JLabel(moduleName));

        infoPanel.add(new JLabel("Assessment:"));
        infoPanel.add(new JLabel(assessment.getAssessmentName()));

        infoPanel.add(new JLabel("Type:"));
        infoPanel.add(new JLabel(assessment.getAssessmentType()));

        // Marks section
        JLabel marksLabel = new JLabel("Marks:");
        JPanel marksPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField marksField = new JTextField(10);
        marksPanel.add(marksField);
        marksPanel.add(new JLabel("/ " + assessment.getTotalMarks()));

        infoPanel.add(marksLabel);
        infoPanel.add(marksPanel);

        // Pre-fill existing marks if available
        if (existingResult != null) {
            marksField.setText(String.valueOf(existingResult.getMarksObtained()));
        } else {
            // Get marks from table if entered
            Object tableMarks = marksTable.getValueAt(tableRow, 2);
            if (tableMarks != null && !tableMarks.toString().trim().isEmpty()) {
                marksField.setText(tableMarks.toString());
            }
        }

        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Feedback panel
        JPanel feedbackPanel = new JPanel(new BorderLayout(5, 5));
        feedbackPanel.setBorder(BorderFactory.createTitledBorder("Feedback"));

        JLabel feedbackLabel = new JLabel("Provide detailed feedback to help the student improve:");
        feedbackPanel.add(feedbackLabel, BorderLayout.NORTH);

        JTextArea feedbackArea = new JTextArea(10, 40);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setFont(new Font("Arial", Font.PLAIN, 12));

        // Pre-fill existing feedback if available
        if (existingResult != null && existingResult.getFeedback() != null) {
            feedbackArea.setText(existingResult.getFeedback());
        }

        JScrollPane feedbackScroll = new JScrollPane(feedbackArea);
        feedbackPanel.add(feedbackScroll, BorderLayout.CENTER);

        // Tips panel
        JPanel tipsPanel = new JPanel(new BorderLayout());
        tipsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        JLabel tipsLabel = new JLabel("<html><i>Tips: Be specific, constructive, and actionable. Focus on what was done well and areas for improvement.</i></html>");
        tipsLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        tipsPanel.add(tipsLabel);

        feedbackPanel.add(tipsPanel, BorderLayout.SOUTH);

        mainPanel.add(feedbackPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton saveButton = new JButton("Save Marks & Feedback");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String marksText = marksField.getText().trim();
            String feedback = feedbackArea.getText().trim();

            // Validate marks
            if (marksText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter marks for this assessment!",
                        "Marks Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double marks = Double.parseDouble(marksText);

                // Validate marks range
                if (marks < 0 || marks > assessment.getTotalMarks()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Marks must be between 0 and " + assessment.getTotalMarks(),
                            "Invalid Marks",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Calculate grade
                double percentage = (marks / assessment.getTotalMarks()) * 100;
                String grade = GradingSystem.calculateGrade(percentage);

                // Save result with feedback
                dataManager.updateOrCreateResult(studentId, assessmentId, marks, grade, feedback);

                // Update table
                marksTable.setValueAt(marksText, tableRow, 2);
                marksTable.setValueAt(grade, tableRow, 3);
                marksTable.setValueAt("Graded", tableRow, 4);
                marksTable.setValueAt(feedback.isEmpty() ? "No Feedback" : "Has Feedback", tableRow, 5);

                JOptionPane.showMessageDialog(dialog,
                        "Marks and feedback saved successfully!\n\n" +
                                "Marks: " + marks + "/" + assessment.getTotalMarks() + "\n" +
                                "Grade: " + grade + "\n" +
                                "Feedback: " + (feedback.isEmpty() ? "None" : feedback.length() + " characters"),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid number for marks!",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private JPanel createFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top info panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel infoLabel = new JLabel("<html><b>Student Feedback Summary</b><br>" +
                "View anonymous feedback from students about your modules</html>");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        topPanel.add(infoLabel, BorderLayout.NORTH);

        // Module selection
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(new JLabel("Select Module:"));
        JComboBox<String> moduleCombo = new JComboBox<>();

        for (Module module : dataManager.getAllModules()) {
            if (module.getLecturerId() != null && module.getLecturerId().equals(lecturer.getUserId())) {
                moduleCombo.addItem(module.getModuleId() + " - " + module.getModuleName());
            }
        }
        selectionPanel.add(moduleCombo);

        JButton loadButton = new JButton("Load Feedback");
        selectionPanel.add(loadButton);

        topPanel.add(selectionPanel, BorderLayout.CENTER);
        panel.add(topPanel, BorderLayout.NORTH);

        // Feedback table
        String[] columns = {"Submission Date", "Content", "Teaching", "Clarity", "Responsive", "Overall", "Avg"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable feedbackTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(feedbackTable);

        loadButton.addActionListener(e -> {
            if (moduleCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select a module");
                return;
            }

            String moduleId = ((String) moduleCombo.getSelectedItem()).split(" - ")[0];
            refreshFeedbackTable(model, moduleId);
        });

        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton viewDetailsButton = new JButton("View Detailed Feedback");
        JButton summaryButton = new JButton("View Summary Report");

        viewDetailsButton.addActionListener(e -> {
            int selectedRow = feedbackTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a feedback entry");
                return;
            }

            if (moduleCombo.getSelectedItem() == null) return;
            String moduleId = ((String) moduleCombo.getSelectedItem()).split(" - ")[0];

            List<ModuleFeedback> feedbacks = dataManager.getFeedbacksByModule(moduleId);
            if (selectedRow < feedbacks.size()) {
                showDetailedFeedback(feedbacks.get(selectedRow));
            }
        });

        summaryButton.addActionListener(e -> {
            if (moduleCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select a module");
                return;
            }
            String moduleId = ((String) moduleCombo.getSelectedItem()).split(" - ")[0];
            showFeedbackSummary(moduleId);
        });

        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(summaryButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshFeedbackTable(DefaultTableModel model, String moduleId) {
        model.setRowCount(0);
        List<ModuleFeedback> feedbacks = dataManager.getFeedbacksByModule(moduleId);

        for (ModuleFeedback fb : feedbacks) {
            model.addRow(new Object[]{
                    fb.getSubmissionDate().toString().substring(0, 10),
                    fb.getContentQualityRating(),
                    fb.getTeachingEffectivenessRating(),
                    fb.getMaterialClarityRating(),
                    fb.getResponsivenessRating(),
                    fb.getOverallRating(),
                    String.format("%.1f", fb.getAverageRating())
            });
        }

        if (feedbacks.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No feedback received for this module yet.",
                    "No Feedback",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showDetailedFeedback(ModuleFeedback feedback) {
        JDialog dialog = new JDialog(this, "Detailed Feedback (Anonymous)", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        StringBuilder details = new StringBuilder();
        details.append("ANONYMOUS STUDENT FEEDBACK\n");
        details.append("=".repeat(60)).append("\n\n");
        details.append("Submitted: ").append(feedback.getSubmissionDate()).append("\n\n");

        details.append("RATINGS (1-5 scale)\n");
        details.append("-".repeat(60)).append("\n");
        details.append("Content Quality:         ").append(getRatingStars(feedback.getContentQualityRating())).append("\n");
        details.append("Teaching Effectiveness:  ").append(getRatingStars(feedback.getTeachingEffectivenessRating())).append("\n");
        details.append("Material Clarity:        ").append(getRatingStars(feedback.getMaterialClarityRating())).append("\n");
        details.append("Responsiveness:          ").append(getRatingStars(feedback.getResponsivenessRating())).append("\n");
        details.append("Overall Satisfaction:    ").append(getRatingStars(feedback.getOverallRating())).append("\n");
        details.append("Average Rating:          ").append(String.format("%.1f/5.0", feedback.getAverageRating())).append("\n\n");

        details.append("WRITTEN FEEDBACK\n");
        details.append("-".repeat(60)).append("\n\n");
        details.append("Strengths:\n");
        details.append(feedback.getStrengths() != null && !feedback.getStrengths().isEmpty() ?
                feedback.getStrengths() : "No comments provided").append("\n\n");

        details.append("Areas for Improvement:\n");
        details.append(feedback.getImprovements() != null && !feedback.getImprovements().isEmpty() ?
                feedback.getImprovements() : "No comments provided").append("\n\n");

        if (feedback.getAdditionalComments() != null && !feedback.getAdditionalComments().isEmpty()) {
            details.append("Additional Comments:\n");
            details.append(feedback.getAdditionalComments()).append("\n");
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);

        panel.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showFeedbackSummary(String moduleId) {
        List<ModuleFeedback> feedbacks = dataManager.getFeedbacksByModule(moduleId);

        if (feedbacks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No feedback available for this module.");
            return;
        }

        Module module = dataManager.getModuleById(moduleId);
        String moduleName = module != null ? module.getModuleName() : "Unknown";

        JDialog dialog = new JDialog(this, "Feedback Summary - " + moduleName, true);
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Calculate averages
        double avgContent = 0, avgTeaching = 0, avgClarity = 0, avgResponsiveness = 0, avgOverall = 0;
        for (ModuleFeedback fb : feedbacks) {
            avgContent += fb.getContentQualityRating();
            avgTeaching += fb.getTeachingEffectivenessRating();
            avgClarity += fb.getMaterialClarityRating();
            avgResponsiveness += fb.getResponsivenessRating();
            avgOverall += fb.getOverallRating();
        }

        int count = feedbacks.size();
        avgContent /= count;
        avgTeaching /= count;
        avgClarity /= count;
        avgResponsiveness /= count;
        avgOverall /= count;

        StringBuilder summary = new StringBuilder();
        summary.append("FEEDBACK SUMMARY REPORT\n");
        summary.append("=".repeat(70)).append("\n\n");
        summary.append("Module: ").append(moduleName).append("\n");
        summary.append("Total Feedback Responses: ").append(count).append("\n\n");

        summary.append("AVERAGE RATINGS\n");
        summary.append("-".repeat(70)).append("\n");
        summary.append(String.format("%-30s %s (%.2f/5.0)\n", "Content Quality:",
                getRatingStars((int) Math.round(avgContent)), avgContent));
        summary.append(String.format("%-30s %s (%.2f/5.0)\n", "Teaching Effectiveness:",
                getRatingStars((int) Math.round(avgTeaching)), avgTeaching));
        summary.append(String.format("%-30s %s (%.2f/5.0)\n", "Material Clarity:",
                getRatingStars((int) Math.round(avgClarity)), avgClarity));
        summary.append(String.format("%-30s %s (%.2f/5.0)\n", "Responsiveness:",
                getRatingStars((int) Math.round(avgResponsiveness)), avgResponsiveness));
        summary.append(String.format("%-30s %s (%.2f/5.0)\n", "Overall Satisfaction:",
                getRatingStars((int) Math.round(avgOverall)), avgOverall));

        double totalAvg = (avgContent + avgTeaching + avgClarity + avgResponsiveness + avgOverall) / 5.0;
        summary.append("\n");
        summary.append(String.format("%-30s %s (%.2f/5.0)\n", "OVERALL AVERAGE:",
                getRatingStars((int) Math.round(totalAvg)), totalAvg));

        JTextArea textArea = new JTextArea(summary.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);

        panel.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private String getRatingStars(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < rating ? "★" : "☆");
        }
        return stars.toString() + " (" + rating + ")";
    }


    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("User ID:"));
        panel.add(new JLabel(lecturer.getUserId()));

        panel.add(new JLabel("Full Name:"));
        JTextField nameField = new JTextField(lecturer.getFullName());
        panel.add(nameField);

        panel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField(lecturer.getEmail());
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
            lecturer.setFullName(nameField.getText().trim());
            lecturer.setEmail(emailField.getText().trim());

            // Check if user wants to change password
            if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
                // Verify current password
                if (!currentPassword.equals(lecturer.getPassword())) {
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
                lecturer.setPassword(newPassword);

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
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?");
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new AFSMainFrame().setVisible(true);
        }
    }

    // ============================================================
    // PENDING SUBMISSIONS PANEL
    // ============================================================
    private JPanel createPendingSubmissionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel infoLabel = new JLabel("<html><b>Pending Submissions</b><br>" +
                "Students who have submitted their assignments waiting for your review.</html>");
        panel.add(infoLabel, BorderLayout.NORTH);

        String[] columns = {"Submission ID", "Student", "Assessment", "Module",
                "Submitted Date", "File Name", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable submissionTable = new JTable(model);

        refreshPendingSubmissions(model);

        JScrollPane scrollPane = new JScrollPane(submissionTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton viewButton = new JButton("View Submission Details");
        JButton gradeButton = new JButton("Grade This Submission");
        JButton refreshButton = new JButton("Refresh");

        viewButton.addActionListener(e -> viewSubmissionDetails(submissionTable));
        gradeButton.addActionListener(e -> gradeSubmission(submissionTable, model));
        refreshButton.addActionListener(e -> refreshPendingSubmissions(model));

        buttonPanel.add(viewButton);
        buttonPanel.add(gradeButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshPendingSubmissions(DefaultTableModel model) {
        model.setRowCount(0);
        List<AssessmentSubmission> pending = dataManager.getPendingSubmissionsForLecturer(
                lecturer.getUserId()
        );

        for (AssessmentSubmission sub : pending) {
            User student = dataManager.getUserById(sub.getStudentId());
            Assessment assessment = dataManager.getAssessmentById(sub.getAssessmentId());
            Module module = assessment != null ? dataManager.getModuleById(assessment.getModuleId()) : null;

            model.addRow(new Object[]{
                    sub.getSubmissionId(),
                    student != null ? student.getFullName() : "N/A",
                    assessment != null ? assessment.getAssessmentName() : "N/A",
                    module != null ? module.getModuleName() : "N/A",
                    sub.getSubmissionDate().toString().substring(0, 16),
                    sub.getFileName(),
                    sub.getStatus()
            });
        }
    }

    private void viewSubmissionDetails(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a submission!");
            return;
        }

        String submissionId = (String) table.getValueAt(selectedRow, 0);

        AssessmentSubmission submission = null;
        for (AssessmentSubmission sub : dataManager.getAllSubmissions()) {
            if (sub.getSubmissionId().equals(submissionId)) {
                submission = sub;
                break;
            }
        }

        if (submission == null) return;

        User student = dataManager.getUserById(submission.getStudentId());
        Assessment assessment = dataManager.getAssessmentById(submission.getAssessmentId());

        StringBuilder details = new StringBuilder();
        details.append("SUBMISSION DETAILS\n");
        details.append("=".repeat(60)).append("\n\n");
        details.append("Submission ID: ").append(submission.getSubmissionId()).append("\n");
        details.append("Student: ").append(student != null ? student.getFullName() : "N/A").append("\n");
        details.append("Assessment: ").append(assessment != null ? assessment.getAssessmentName() : "N/A").append("\n");
        details.append("File Name: ").append(submission.getFileName()).append("\n");
        details.append("File Path: ").append(submission.getFilePath()).append("\n");
        details.append("Submission Date: ").append(submission.getSubmissionDate()).append("\n");
        details.append("Status: ").append(submission.getStatus()).append("\n\n");

        if (submission.getSubmissionComments() != null &&
                !submission.getSubmissionComments().isEmpty()) {
            details.append("Student Comments:\n");
            details.append(submission.getSubmissionComments()).append("\n\n");
        }

        details.append("File Location:\n");
        details.append(submission.getFilePath());

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Submission Details",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void gradeSubmission(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a submission to grade!");
            return;
        }

        String submissionId = (String) table.getValueAt(selectedRow, 0);

        // Find submission
        AssessmentSubmission submission = null;
        for (AssessmentSubmission sub : dataManager.getAllSubmissions()) {
            if (sub.getSubmissionId().equals(submissionId)) {
                submission = sub;
                break;
            }
        }

        if (submission == null) return;

        User student = dataManager.getUserById(submission.getStudentId());
        Assessment assessment = dataManager.getAssessmentById(submission.getAssessmentId());
        Module module = assessment != null ? dataManager.getModuleById(assessment.getModuleId()) : null;

        // Create grading dialog
        JDialog dialog = new JDialog(this, "Grade Submission", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Submission Information"));

        infoPanel.add(new JLabel("Student:"));
        infoPanel.add(new JLabel(student != null ? student.getFullName() : "N/A"));
        infoPanel.add(new JLabel("Module:"));
        infoPanel.add(new JLabel(module != null ? module.getModuleName() : "N/A"));
        infoPanel.add(new JLabel("Assessment:"));
        infoPanel.add(new JLabel(assessment != null ? assessment.getAssessmentName() : "N/A"));
        infoPanel.add(new JLabel("Total Marks:"));
        infoPanel.add(new JLabel(assessment != null ? String.valueOf(assessment.getTotalMarks()) : "N/A"));
        infoPanel.add(new JLabel("Submitted:"));
        infoPanel.add(new JLabel(submission.getSubmissionDate().toString().substring(0, 16)));
        infoPanel.add(new JLabel("File:"));
        infoPanel.add(new JLabel(submission.getFileName()));

        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // Grading panel
        JPanel gradingPanel = new JPanel(new BorderLayout(10, 10));
        gradingPanel.setBorder(BorderFactory.createTitledBorder("Enter Marks and Feedback"));

        JPanel marksPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        marksPanel.add(new JLabel("Marks Obtained:"));
        JTextField marksField = new JTextField(10);
        marksPanel.add(marksField);
        marksPanel.add(new JLabel("/ " + (assessment != null ? assessment.getTotalMarks() : "0")));

        // Grade will be AUTO-CALCULATED - show preview
        JLabel gradePreviewLabel = new JLabel("Grade: (will be calculated automatically)");
        gradePreviewLabel.setFont(new Font("Arial", Font.BOLD, 12));
        marksPanel.add(gradePreviewLabel);

        // Declare final variables for lambda access
        final Assessment finalAssessmentForPreview = assessment;

        // Update grade preview as marks are entered
        marksField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                try {
                    double marks = Double.parseDouble(marksField.getText());
                    if (finalAssessmentForPreview != null) {
                        double percentage = (marks / finalAssessmentForPreview.getTotalMarks()) * 100;
                        String grade = GradingSystem.calculateGrade(percentage);
                        gradePreviewLabel.setText("Grade: " + grade + " (" + String.format("%.1f%%", percentage) + ")");
                        gradePreviewLabel.setForeground(Color.BLUE);
                    }
                } catch (NumberFormatException ex) {
                    gradePreviewLabel.setText("Grade: (enter valid marks)");
                    gradePreviewLabel.setForeground(Color.GRAY);
                }
            }
        });

        gradingPanel.add(marksPanel, BorderLayout.NORTH);

        // Feedback area
        JLabel feedbackLabel = new JLabel("Feedback for Student:");
        JTextArea feedbackArea = new JTextArea(8, 40);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        JScrollPane feedbackScroll = new JScrollPane(feedbackArea);

        JPanel feedbackPanel = new JPanel(new BorderLayout(5, 5));
        feedbackPanel.add(feedbackLabel, BorderLayout.NORTH);
        feedbackPanel.add(feedbackScroll, BorderLayout.CENTER);

        gradingPanel.add(feedbackPanel, BorderLayout.CENTER);

        mainPanel.add(gradingPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton submitGradeButton = new JButton("Submit Grade");
        JButton cancelButton = new JButton("Cancel");

        // Declare final variables for lambda access
        final Assessment finalAssessment = assessment;
        final AssessmentSubmission finalSubmission = submission;

        submitGradeButton.addActionListener(e -> {
            String marksText = marksField.getText().trim();
            if (marksText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter marks!");
                return;
            }

            try {
                double marks = Double.parseDouble(marksText);

                if (finalAssessment == null) return;

                if (marks < 0 || marks > finalAssessment.getTotalMarks()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Marks must be between 0 and " + finalAssessment.getTotalMarks(),
                            "Invalid Marks",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String feedback = feedbackArea.getText().trim();

                // AUTO-GRADE CALCULATION - No need to enter grade manually
                dataManager.updateOrCreateResultWithAutoGrade(
                        finalSubmission.getStudentId(),
                        finalSubmission.getAssessmentId(),
                        marks,
                        feedback
                );

                // Calculate for display
                double percentage = (marks / finalAssessment.getTotalMarks()) * 100;
                String grade = GradingSystem.calculateGrade(percentage);

                JOptionPane.showMessageDialog(dialog,
                        "Grade submitted successfully!\n\n" +
                                "Marks: " + marks + "/" + finalAssessment.getTotalMarks() + "\n" +
                                "Percentage: " + String.format("%.2f%%", percentage) + "\n" +
                                "Grade: " + grade + " (auto-calculated)\n" +
                                "Status: GRADED",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                refreshPendingSubmissions(model);
                updateNotificationLabel();
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number for marks!");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitGradeButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void updateNotificationLabel() {
        int pendingCount = dataManager.getNotificationCountForLecturer(lecturer.getUserId());

        if (pendingCount > 0) {
            notificationLabel.setText("🔔 " + pendingCount + " pending submission" +
                    (pendingCount > 1 ? "s" : ""));
            notificationLabel.setFont(new Font("Arial", Font.BOLD, 12));
            notificationLabel.setForeground(Color.RED);
            notificationLabel.setVisible(true);
        } else {
            notificationLabel.setText("✓ No pending submissions");
            notificationLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            notificationLabel.setForeground(new Color(34, 139, 34)); // Green
            notificationLabel.setVisible(true);
        }

        // Update tab title
        if (mainTabbedPane != null) {
            // Find the Pending Submissions tab (index 2)
            mainTabbedPane.setTitleAt(2, "Pending Submissions (" + pendingCount + ")");
        }
    }
}

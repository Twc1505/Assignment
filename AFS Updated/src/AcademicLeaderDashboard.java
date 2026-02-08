import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

class AcademicLeaderDashboard extends JFrame {
    private AcademicLeader leader;
    private DataManager dataManager;
    private JTabbedPane tabbedPane;

    public AcademicLeaderDashboard(AcademicLeader leader) {
        this.leader = leader;
        this.dataManager = DataManager.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Academic Leader Dashboard - " + leader.getFullName());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Module Management", createModuleManagementPanel());
        tabbedPane.addTab("Assign Lecturers", createLecturerAssignmentPanel());
        tabbedPane.addTab("Reports", createReportsPanel());
        tabbedPane.addTab("Profile", createProfilePanel());

        add(tabbedPane);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        menu.add(logoutItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        // Show warning if no field assigned
        if (leader.getFieldId() == null) {
            JOptionPane.showMessageDialog(this,
                    "Warning: You are not assigned to any field yet.\nPlease contact the administrator.",
                    "No Field Assigned",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private JPanel createModuleManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add field info at top
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Field leaderField = dataManager.getFieldById(leader.getFieldId());
        String fieldName = leaderField != null ? leaderField.getFieldName() : "No Field Assigned";
        JLabel fieldLabel = new JLabel("Your Field: " + fieldName);
        fieldLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(fieldLabel);
        panel.add(infoPanel, BorderLayout.NORTH);

        String[] columns = {"Module ID", "Module Code", "Module Name", "Lecturer"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable moduleTable = new JTable(model);
        refreshModuleTable(model);

        JScrollPane scrollPane = new JScrollPane(moduleTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Create Module");
        JButton editButton = new JButton("Edit Module");
        JButton deleteButton = new JButton("Delete Module");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> showCreateModuleDialog(model));
        editButton.addActionListener(e -> showEditModuleDialog(moduleTable, model));
        deleteButton.addActionListener(e -> deleteModule(moduleTable, model));
        refreshButton.addActionListener(e -> refreshModuleTable(model));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshModuleTable(DefaultTableModel model) {
        model.setRowCount(0);
        // Only show modules from leader's field
        List<Module> fieldModules = dataManager.getModulesByField(leader.getFieldId());

        for (Module module : fieldModules) {
            String lecturerName = "";
            if (module.getLecturerId() != null) {
                for (User user : dataManager.getAllUsers()) {
                    if (user.getUserId().equals(module.getLecturerId())) {
                        lecturerName = user.getFullName();
                        break;
                    }
                }
            }
            model.addRow(new Object[]{
                    module.getModuleId(), module.getModuleCode(),
                    module.getModuleName(), lecturerName
            });
        }
    }

    private void showCreateModuleDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Create New Module", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();
        JTextArea descArea = new JTextArea(3, 20);
        JScrollPane descScroll = new JScrollPane(descArea);

        panel.add(new JLabel("Module Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Module Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(descScroll);

        JButton saveButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String desc = descArea.getText().trim();

            if (code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Code and Name are required!");
                return;
            }

            String moduleId = "MOD" + String.format("%03d", dataManager.getAllModules().size() + 1);
            Module newModule = new Module(moduleId, name, code, desc);

            // Assign to leader's field
            newModule.setFieldId(leader.getFieldId());

            // Add module to field
            Field field = dataManager.getFieldById(leader.getFieldId());
            if (field != null) {
                field.addModule(moduleId);
            }

            dataManager.addModule(newModule);
            dataManager.saveAllData();
            refreshModuleTable(model);
            refreshLecturerAssignmentPanel();
            JOptionPane.showMessageDialog(dialog, "Module created successfully!");
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        panel.add(saveButton);
        panel.add(cancelButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditModuleDialog(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a module to edit");
            return;
        }

        String moduleId = (String) table.getValueAt(selectedRow, 0);
        Module module = dataManager.getModuleById(moduleId);

        if (module == null) return;

        JDialog dialog = new JDialog(this, "Edit Module", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField codeField = new JTextField(module.getModuleCode());
        JTextField nameField = new JTextField(module.getModuleName());
        JTextArea descArea = new JTextArea(module.getDescription(), 3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);

        panel.add(new JLabel("Module Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Module Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(descScroll);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String newCode = codeField.getText().trim();
            String newName = nameField.getText().trim();
            String newDesc = descArea.getText().trim();

            if (newCode.isEmpty() || newName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Module Code and Name cannot be empty!");
                return;
            }

            // Update the module
            module.setModuleCode(newCode);
            module.setModuleName(newName);
            module.setDescription(newDesc);

            dataManager.saveAllData();
            refreshModuleTable(model);
            refreshLecturerAssignmentPanel(); // ADD THIS LINE
            JOptionPane.showMessageDialog(dialog, "Module updated successfully!");
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        panel.add(saveButton);
        panel.add(cancelButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteModule(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a module to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this module?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String moduleId = (String) table.getValueAt(selectedRow, 0);
            dataManager.getAllModules().removeIf(m -> m.getModuleId().equals(moduleId));
            dataManager.saveAllData(); // AUTO-SAVE
            refreshModuleTable(model);
            JOptionPane.showMessageDialog(this, "Module deleted!");
        }
    }

    private JPanel createLecturerAssignmentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        JComboBox<String> moduleCombo = new JComboBox<>();
        JComboBox<String> lecturerCombo = new JComboBox<>();

        // Only show modules from leader's field
        List<Module> fieldModules = dataManager.getModulesByField(leader.getFieldId());
        for (Module mod : fieldModules) {
            moduleCombo.addItem(mod.getModuleId() + " - " + mod.getModuleName());
        }

        // Only show lecturers from leader's field
        List<Lecturer> fieldLecturers = dataManager.getLecturersByField(leader.getFieldId());
        for (Lecturer lec : fieldLecturers) {
            lecturerCombo.addItem(lec.getUserId() + " - " + lec.getFullName());
        }

        formPanel.add(new JLabel("Select Module:"));
        formPanel.add(moduleCombo);
        formPanel.add(new JLabel("Assign Lecturer:"));
        formPanel.add(lecturerCombo);

        JButton assignButton = new JButton("Assign");
        assignButton.addActionListener(e -> {
            if (moduleCombo.getSelectedItem() == null || lecturerCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select both module and lecturer");
                return;
            }

            String modId = ((String) moduleCombo.getSelectedItem()).split(" - ")[0];
            String lecId = ((String) lecturerCombo.getSelectedItem()).split(" - ")[0];

            Module module = dataManager.getModuleById(modId);
            if (module != null) {
                module.setLecturerId(lecId);
            }

            for (Lecturer lec : dataManager.getAllLecturers()) {
                if (lec.getUserId().equals(lecId)) {
                    lec.addModule(modId);
                    break;
                }
            }

            dataManager.saveAllData(); // AUTO-SAVE
            JOptionPane.showMessageDialog(this, "Lecturer assigned successfully!");
        });

        formPanel.add(assignButton);
        panel.add(formPanel, BorderLayout.NORTH);

        return panel;
    }

    private void refreshLecturerAssignmentPanel() {
        int assignmentTabIndex = -1;
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals("Assign Lecturers")) {
                assignmentTabIndex = i;
                break;
            }
        }

        if (assignmentTabIndex != -1) {
            tabbedPane.removeTabAt(assignmentTabIndex);
            tabbedPane.insertTab("Assign Lecturers", null, createLecturerAssignmentPanel(), null, assignmentTabIndex);
        }
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Button panel at top
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton modulePerformanceButton = new JButton("Module Performance Report");
        JButton feedbackAnalysisButton = new JButton("Feedback Analysis Report");
        JButton gpaReportButton = new JButton("Module GPA Report");
        JButton comprehensiveButton = new JButton("Comprehensive Report");

        buttonPanel.add(modulePerformanceButton);
        buttonPanel.add(feedbackAnalysisButton);
        buttonPanel.add(gpaReportButton);
        buttonPanel.add(comprehensiveButton);

        panel.add(buttonPanel, BorderLayout.NORTH);

        // Report display area
        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Module Performance Report
        modulePerformanceButton.addActionListener(e -> {
            StringBuilder report = new StringBuilder();
            report.append("=".repeat(80)).append("\n");
            report.append("MODULE PERFORMANCE REPORT\n");
            report.append("Field: ").append(dataManager.getFieldById(leader.getFieldId()).getFieldName()).append("\n");
            report.append("=".repeat(80)).append("\n\n");

            List<Module> fieldModules = dataManager.getModulesByField(leader.getFieldId());

            for (Module module : fieldModules) {
                report.append("Module: ").append(module.getModuleName()).append(" (").append(module.getModuleCode()).append(")\n");

                User lecturer = dataManager.getUserById(module.getLecturerId());
                report.append("Lecturer: ").append(lecturer != null ? lecturer.getFullName() : "Not assigned").append("\n");

                List<Assessment> assessments = dataManager.getAssessmentsByModule(module.getModuleId());
                report.append("Total Assessments: ").append(assessments.size()).append("\n");

                int totalResults = 0;
                for (Assessment assessment : assessments) {
                    totalResults += dataManager.getResultsByAssessment(assessment.getAssessmentId()).size();
                }
                report.append("Total Results Recorded: ").append(totalResults).append("\n");

                List<ClassGroup> classes = dataManager.getClassesByModule(module.getModuleId());
                int totalStudents = 0;
                for (ClassGroup cls : classes) {
                    totalStudents += cls.getStudentIds().size();
                }
                report.append("Total Students Enrolled: ").append(totalStudents).append("\n");

                report.append("-".repeat(80)).append("\n\n");
            }

            reportArea.setText(report.toString());
        });

        // Feedback Analysis Report
        feedbackAnalysisButton.addActionListener(e -> {
            StringBuilder report = new StringBuilder();
            report.append("=".repeat(80)).append("\n");
            report.append("STUDENT FEEDBACK ANALYSIS REPORT\n");
            report.append("Field: ").append(dataManager.getFieldById(leader.getFieldId()).getFieldName()).append("\n");
            report.append("=".repeat(80)).append("\n\n");

            List<Module> fieldModules = dataManager.getModulesByField(leader.getFieldId());

            for (Module module : fieldModules) {
                List<ModuleFeedback> feedbacks = dataManager.getFeedbacksByModule(module.getModuleId());

                if (feedbacks.isEmpty()) {
                    continue; // Skip modules with no feedback
                }

                report.append("Module: ").append(module.getModuleName()).append(" (").append(module.getModuleCode()).append(")\n");

                User lecturer = dataManager.getUserById(module.getLecturerId());
                report.append("Lecturer: ").append(lecturer != null ? lecturer.getFullName() : "Not assigned").append("\n");
                report.append("Feedback Responses: ").append(feedbacks.size()).append("\n\n");

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

                report.append(String.format("  Content Quality:        %.2f/5.0 %s\n", avgContent, getRatingStars((int)Math.round(avgContent))));
                report.append(String.format("  Teaching Effectiveness: %.2f/5.0 %s\n", avgTeaching, getRatingStars((int)Math.round(avgTeaching))));
                report.append(String.format("  Material Clarity:       %.2f/5.0 %s\n", avgClarity, getRatingStars((int)Math.round(avgClarity))));
                report.append(String.format("  Responsiveness:         %.2f/5.0 %s\n", avgResponsiveness, getRatingStars((int)Math.round(avgResponsiveness))));
                report.append(String.format("  Overall Satisfaction:   %.2f/5.0 %s\n", avgOverall, getRatingStars((int)Math.round(avgOverall))));

                double totalAvg = (avgContent + avgTeaching + avgClarity + avgResponsiveness + avgOverall) / 5.0;
                report.append(String.format("  ** AVERAGE SCORE:       %.2f/5.0 **\n", totalAvg));

                // Performance indicator
                String performance;
                if (totalAvg >= 4.5) performance = "EXCELLENT";
                else if (totalAvg >= 4.0) performance = "VERY GOOD";
                else if (totalAvg >= 3.5) performance = "GOOD";
                else if (totalAvg >= 3.0) performance = "SATISFACTORY";
                else performance = "NEEDS IMPROVEMENT";

                report.append("  Performance Level: ").append(performance).append("\n");
                report.append("-".repeat(80)).append("\n\n");
            }

            if (report.toString().split("Module:").length <= 2) {
                report.append("\nNo feedback has been submitted for modules in this field yet.\n");
            }

            reportArea.setText(report.toString());
        });

        // GPA Report
        gpaReportButton.addActionListener(e -> {
            StringBuilder report = new StringBuilder();
            report.append("=".repeat(80)).append("\n");
            report.append("MODULE GPA REPORT\n");
            report.append("Field: ").append(dataManager.getFieldById(leader.getFieldId()).getFieldName()).append("\n");
            report.append("=".repeat(80)).append("\n\n");

            List<Module> fieldModules = dataManager.getModulesByField(leader.getFieldId());

            for (Module module : fieldModules) {
                List<Assessment> assessments = dataManager.getAssessmentsByModule(module.getModuleId());

                if (assessments.isEmpty()) {
                    continue;
                }

                report.append("Module: ").append(module.getModuleName()).append(" (").append(module.getModuleCode()).append(")\n");

                User lecturer = dataManager.getUserById(module.getLecturerId());
                report.append("Lecturer: ").append(lecturer != null ? lecturer.getFullName() : "Not assigned").append("\n");

                // Calculate GPA for this module
                double totalGPA = 0;
                int studentCount = 0;

                // Get all students enrolled in this module
                List<ClassGroup> classes = dataManager.getClassesByModule(module.getModuleId());
                java.util.Set<String> uniqueStudents = new java.util.HashSet<>();
                for (ClassGroup cls : classes) {
                    uniqueStudents.addAll(cls.getStudentIds());
                }

                // Calculate average GPA per student for this module
                for (String studentId : uniqueStudents) {
                    double studentTotalMarks = 0;
                    double studentTotalWeight = 0;

                    for (Assessment assessment : assessments) {
                        for (AssessmentResult result : dataManager.getResultsByAssessment(assessment.getAssessmentId())) {
                            if (result.getStudentId().equals(studentId)) {
                                double percentage = (result.getMarksObtained() / assessment.getTotalMarks()) * 100;
                                double weightedScore = (percentage / 100.0) * assessment.getWeightage();
                                studentTotalMarks += weightedScore;
                                studentTotalWeight += assessment.getWeightage();
                                break;
                            }
                        }
                    }

                    if (studentTotalWeight > 0) {
                        double finalPercentage = (studentTotalMarks / studentTotalWeight) * 100;
                        double gpa = calculateGPA(finalPercentage);
                        totalGPA += gpa;
                        studentCount++;
                    }
                }

                if (studentCount > 0) {
                    double avgGPA = totalGPA / studentCount;
                    report.append(String.format("  Students Assessed: %d\n", studentCount));
                    report.append(String.format("  Average GPA: %.2f/4.0\n", avgGPA));
                    report.append(String.format("  Average Grade: %s\n", getGradeFromGPA(avgGPA)));

                    // Grade distribution
                    report.append("\n  Grade Distribution:\n");
                    int[] gradeCount = new int[8]; // A+, A, B+, B, C+, C, D, F

                    for (String studentId : uniqueStudents) {
                        double studentTotalMarks = 0;
                        double studentTotalWeight = 0;

                        for (Assessment assessment : assessments) {
                            for (AssessmentResult result : dataManager.getResultsByAssessment(assessment.getAssessmentId())) {
                                if (result.getStudentId().equals(studentId)) {
                                    double percentage = (result.getMarksObtained() / assessment.getTotalMarks()) * 100;
                                    double weightedScore = (percentage / 100.0) * assessment.getWeightage();
                                    studentTotalMarks += weightedScore;
                                    studentTotalWeight += assessment.getWeightage();
                                    break;
                                }
                            }
                        }

                        if (studentTotalWeight > 0) {
                            double finalPercentage = (studentTotalMarks / studentTotalWeight) * 100;
                            String grade = GradingSystem.calculateGrade(finalPercentage);

                            switch(grade) {
                                case "A+": gradeCount[0]++; break;
                                case "A": gradeCount[1]++; break;
                                case "B+": gradeCount[2]++; break;
                                case "B": gradeCount[3]++; break;
                                case "C+": gradeCount[4]++; break;
                                case "C": gradeCount[5]++; break;
                                case "D": gradeCount[6]++; break;
                                case "F": gradeCount[7]++; break;
                            }
                        }
                    }

                    String[] grades = {"A+", "A", "B+", "B", "C+", "C", "D", "F"};
                    for (int i = 0; i < grades.length; i++) {
                        if (gradeCount[i] > 0) {
                            report.append(String.format("    %s: %d students (%.1f%%)\n",
                                    grades[i], gradeCount[i], (gradeCount[i] * 100.0 / studentCount)));
                        }
                    }
                } else {
                    report.append("  No assessment results available yet.\n");
                }

                report.append("-".repeat(80)).append("\n\n");
            }

            reportArea.setText(report.toString());
        });

        // Comprehensive Report
        comprehensiveButton.addActionListener(e -> {
            StringBuilder report = new StringBuilder();
            report.append("=".repeat(80)).append("\n");
            report.append("COMPREHENSIVE FIELD REPORT\n");
            report.append("Field: ").append(dataManager.getFieldById(leader.getFieldId()).getFieldName()).append("\n");
            report.append("Academic Leader: ").append(leader.getFullName()).append("\n");
            report.append("Report Generated: ").append(new java.util.Date()).append("\n");
            report.append("=".repeat(80)).append("\n\n");

            List<Module> fieldModules = dataManager.getModulesByField(leader.getFieldId());
            report.append("FIELD OVERVIEW\n");
            report.append("-".repeat(80)).append("\n");
            report.append("Total Modules: ").append(fieldModules.size()).append("\n");

            List<Lecturer> fieldLecturers = dataManager.getLecturersByField(leader.getFieldId());
            report.append("Total Lecturers: ").append(fieldLecturers.size()).append("\n\n");

            // Module-by-module comprehensive report
            for (Module module : fieldModules) {
                report.append("\n").append("=".repeat(80)).append("\n");
                report.append("MODULE: ").append(module.getModuleName()).append(" (").append(module.getModuleCode()).append(")\n");
                report.append("=".repeat(80)).append("\n");

                User lecturer = dataManager.getUserById(module.getLecturerId());
                report.append("Lecturer: ").append(lecturer != null ? lecturer.getFullName() : "Not assigned").append("\n\n");

                // Enrollment data
                List<ClassGroup> classes = dataManager.getClassesByModule(module.getModuleId());
                java.util.Set<String> uniqueStudents = new java.util.HashSet<>();
                for (ClassGroup cls : classes) {
                    uniqueStudents.addAll(cls.getStudentIds());
                }
                report.append("Enrollment: ").append(uniqueStudents.size()).append(" students in ")
                        .append(classes.size()).append(" classes\n\n");

                // Assessment data
                List<Assessment> assessments = dataManager.getAssessmentsByModule(module.getModuleId());
                report.append("Assessments: ").append(assessments.size()).append(" total\n");
                int totalResults = 0;
                for (Assessment assessment : assessments) {
                    totalResults += dataManager.getResultsByAssessment(assessment.getAssessmentId()).size();
                }
                report.append("Results Recorded: ").append(totalResults).append("\n\n");

                // GPA data
                if (!uniqueStudents.isEmpty() && !assessments.isEmpty()) {
                    double totalGPA = 0;
                    int studentCount = 0;

                    for (String studentId : uniqueStudents) {
                        double studentTotalMarks = 0;
                        double studentTotalWeight = 0;

                        for (Assessment assessment : assessments) {
                            for (AssessmentResult result : dataManager.getResultsByAssessment(assessment.getAssessmentId())) {
                                if (result.getStudentId().equals(studentId)) {
                                    double percentage = (result.getMarksObtained() / assessment.getTotalMarks()) * 100;
                                    double weightedScore = (percentage / 100.0) * assessment.getWeightage();
                                    studentTotalMarks += weightedScore;
                                    studentTotalWeight += assessment.getWeightage();
                                    break;
                                }
                            }
                        }

                        if (studentTotalWeight > 0) {
                            double finalPercentage = (studentTotalMarks / studentTotalWeight) * 100;
                            double gpa = calculateGPA(finalPercentage);
                            totalGPA += gpa;
                            studentCount++;
                        }
                    }

                    if (studentCount > 0) {
                        double avgGPA = totalGPA / studentCount;
                        report.append(String.format("Average GPA: %.2f/4.0 (%s)\n\n", avgGPA, getGradeFromGPA(avgGPA)));
                    }
                }

                // Feedback data
                List<ModuleFeedback> feedbacks = dataManager.getFeedbacksByModule(module.getModuleId());
                report.append("Student Feedback: ").append(feedbacks.size()).append(" responses\n");

                if (!feedbacks.isEmpty()) {
                    double totalAvg = 0;
                    for (ModuleFeedback fb : feedbacks) {
                        totalAvg += fb.getAverageRating();
                    }
                    totalAvg /= feedbacks.size();
                    report.append(String.format("Average Feedback Rating: %.2f/5.0 %s\n", totalAvg, getRatingStars((int)Math.round(totalAvg))));
                }

                report.append("\n");
            }

            reportArea.setText(report.toString());
        });

        return panel;
    }

    private double calculateGPA(double percentage) {
        if (percentage >= 90) return 4.0;
        else if (percentage >= 80) return 3.7;
        else if (percentage >= 75) return 3.3;
        else if (percentage >= 70) return 3.0;
        else if (percentage >= 65) return 2.7;
        else if (percentage >= 60) return 2.3;
        else if (percentage >= 50) return 2.0;
        else return 0.0;
    }

    private String getGradeFromGPA(double gpa) {
        if (gpa >= 3.7) return "A";
        else if (gpa >= 3.3) return "B+";
        else if (gpa >= 3.0) return "B";
        else if (gpa >= 2.7) return "C+";
        else if (gpa >= 2.3) return "C";
        else if (gpa >= 2.0) return "D";
        else return "F";
    }

    private String getRatingStars(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < rating ? "★" : "☆");
        }
        return stars.toString();
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("User ID:"));
        panel.add(new JLabel(leader.getUserId()));

        panel.add(new JLabel("Full Name:"));
        JTextField nameField = new JTextField(leader.getFullName());
        panel.add(nameField);

        panel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField(leader.getEmail());
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
            leader.setFullName(nameField.getText().trim());
            leader.setEmail(emailField.getText().trim());

            // Check if user wants to change password
            if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
                // Verify current password
                if (!currentPassword.equals(leader.getPassword())) {
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
                leader.setPassword(newPassword);

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
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to logout?", 
            "Confirm Logout", 
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new AFSMainFrame().setVisible(true);
        }
    }}
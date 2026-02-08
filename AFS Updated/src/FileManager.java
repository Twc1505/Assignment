import java.io.*;
import java.util.*;

class FileManager {
    private static FileManager instance;
    private static final String DATA_DIR = "data/";

    // File names
    private static final String USERS_FILE = "users.txt";
    private static final String MODULES_FILE = "modules.txt";
    private static final String CLASSES_FILE = "classes.txt";
    private static final String ASSESSMENTS_FILE = "assessments.txt";
    private static final String RESULTS_FILE = "results.txt";
    private static final String FIELDS_FILE = "fields.txt";
    private static final String LECTURER_MODULES_FILE = "lecturer_modules.txt";
    private static final String STUDENT_CLASSES_FILE = "student_classes.txt";
    private static final String LEADER_LECTURERS_FILE = "leader_lecturers.txt";
    private static final String MODULE_FEEDBACKS_FILE = "module_feedbacks.txt";
    private static final String SUBMISSIONS_FILE = "submissions.txt"; // NEW

    private FileManager() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
            System.out.println("Created data directory: " + DATA_DIR);
        }
    }

    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }

    // ============================================================
    // SAVE METHODS
    // ============================================================

    public void saveUsers(List<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + USERS_FILE))) {
            for (User user : users) {
                String line = user.getUserId() + "," +
                        user.getUsername() + "," +
                        user.getPassword() + "," +
                        user.getFullName() + "," +
                        user.getEmail() + "," +
                        user.getRole();

                if (user instanceof Lecturer) {
                    Lecturer lec = (Lecturer) user;
                    line += "," + (lec.getAcademicLeaderId() != null ? lec.getAcademicLeaderId() : "");
                    line += "," + (lec.getFieldId() != null ? lec.getFieldId() : "");
                } else if (user instanceof AcademicLeader) {
                    AcademicLeader leader = (AcademicLeader) user;
                    line += ",";
                    line += "," + (leader.getFieldId() != null ? leader.getFieldId() : "");
                }

                writer.println(line);
            }
            System.out.println("Saved " + users.size() + " users to " + USERS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public void saveModules(List<Module> modules) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + MODULES_FILE))) {
            for (Module module : modules) {
                writer.println(module.getModuleId() + "," +
                        module.getModuleName() + "," +
                        module.getModuleCode() + "," +
                        module.getDescription() + "," +
                        (module.getLecturerId() != null ? module.getLecturerId() : "") + "," +
                        (module.getFieldId() != null ? module.getFieldId() : ""));
            }
            System.out.println("Saved " + modules.size() + " modules to " + MODULES_FILE);
        } catch (IOException e) {
            System.err.println("Error saving modules: " + e.getMessage());
        }
    }

    public void saveClasses(List<ClassGroup> classes) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + CLASSES_FILE))) {
            for (ClassGroup cls : classes) {
                String studentIds = String.join(";", cls.getStudentIds());
                writer.println(cls.getClassId() + "," +
                        cls.getClassName() + "," +
                        cls.getModuleId() + "," +
                        studentIds + "," +
                        (cls.getDayOfWeek() != null ? cls.getDayOfWeek() : "") + "," +
                        (cls.getStartTime() != null ? cls.getStartTime() : "") + "," +
                        (cls.getEndTime() != null ? cls.getEndTime() : "") + "," +
                        (cls.getRoom() != null ? cls.getRoom() : "") + "," +
                        cls.getCapacity());
            }
            System.out.println("Saved " + classes.size() + " classes to " + CLASSES_FILE);
        } catch (IOException e) {
            System.err.println("Error saving classes: " + e.getMessage());
        }
    }

    public void saveAssessments(List<Assessment> assessments) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + ASSESSMENTS_FILE))) {
            for (Assessment assessment : assessments) {
                writer.println(assessment.getAssessmentId() + "," +
                        assessment.getAssessmentName() + "," +
                        assessment.getAssessmentType() + "," +
                        assessment.getTotalMarks() + "," +
                        assessment.getWeightage() + "," +
                        assessment.getModuleId());
            }
            System.out.println("Saved " + assessments.size() + " assessments to " + ASSESSMENTS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving assessments: " + e.getMessage());
        }
    }

    public void saveResults(List<AssessmentResult> results) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + RESULTS_FILE))) {
            for (AssessmentResult result : results) {
                String feedback = result.getFeedback() != null ? result.getFeedback().replace(",", "~~COMMA~~") : "";
                writer.println(result.getResultId() + "," +
                        result.getStudentId() + "," +
                        result.getAssessmentId() + "," +
                        result.getMarksObtained() + "," +
                        (result.getGrade() != null ? result.getGrade() : "") + "," +
                        feedback);
            }
            System.out.println("Saved " + results.size() + " results to " + RESULTS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving results: " + e.getMessage());
        }
    }

    public void saveModuleFeedbacks(List<ModuleFeedback> feedbacks) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + MODULE_FEEDBACKS_FILE))) {
            for (ModuleFeedback feedback : feedbacks) {
                writer.println(feedback.toString());
            }
            System.out.println("Saved " + feedbacks.size() + " module feedbacks to " + MODULE_FEEDBACKS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving module feedbacks: " + e.getMessage());
        }
    }

    public void saveFields(List<Field> fields) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + FIELDS_FILE))) {
            for (Field field : fields) {
                writer.println(field.toString());
            }
            System.out.println("Saved " + fields.size() + " fields to " + FIELDS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving fields: " + e.getMessage());
        }
    }

    public void saveLecturerModules(List<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + LECTURER_MODULES_FILE))) {
            for (User user : users) {
                if (user instanceof Lecturer) {
                    Lecturer lec = (Lecturer) user;
                    if (!lec.getAssignedModules().isEmpty()) {
                        String moduleIds = String.join(";", lec.getAssignedModules());
                        writer.println(lec.getUserId() + "," + moduleIds);
                    }
                }
            }
            System.out.println("Saved lecturer-module mappings");
        } catch (IOException e) {
            System.err.println("Error saving lecturer modules: " + e.getMessage());
        }
    }

    public void saveStudentClasses(List<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + STUDENT_CLASSES_FILE))) {
            for (User user : users) {
                if (user instanceof Student) {
                    Student stu = (Student) user;
                    if (!stu.getRegisteredClasses().isEmpty()) {
                        String classIds = String.join(";", stu.getRegisteredClasses());
                        writer.println(stu.getUserId() + "," + classIds);
                    }
                }
            }
            System.out.println("Saved student-class registrations");
        } catch (IOException e) {
            System.err.println("Error saving student classes: " + e.getMessage());
        }
    }

    public void saveLeaderLecturers(List<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + LEADER_LECTURERS_FILE))) {
            for (User user : users) {
                if (user instanceof AcademicLeader) {
                    AcademicLeader leader = (AcademicLeader) user;
                    if (!leader.getManagedLecturers().isEmpty()) {
                        String lecturerIds = String.join(";", leader.getManagedLecturers());
                        writer.println(leader.getUserId() + "," + lecturerIds);
                    }
                }
            }
            System.out.println("Saved leader-lecturer mappings");
        } catch (IOException e) {
            System.err.println("Error saving leader lecturers: " + e.getMessage());
        }
    }

    // ============================================================
    // SAVE SUBMISSIONS
    // ============================================================
    public void saveSubmissions(List<AssessmentSubmission> submissions) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + SUBMISSIONS_FILE))) {
        for (AssessmentSubmission sub : submissions) {
            // Create the exact format expected by loadSubmissions()
            String submissionDate = sub.getSubmissionDate() != null ? 
                sub.getSubmissionDate().toString() : 
                new java.util.Date().toString();
            
            String comments = sub.getSubmissionComments() != null ? 
                sub.getSubmissionComments().replace(",", "~~COMMA~~") : 
                "";
            
            writer.println(
                sub.getSubmissionId() + "," +
                sub.getAssessmentId() + "," +
                sub.getStudentId() + "," +
                sub.getFilePath() + "," +
                sub.getFileName() + "," +
                submissionDate + "," +
                sub.getStatus() + "," +
                comments
            );
            
            // DEBUG: Print what we're saving
            System.out.println("DEBUG - Saving submission: " + sub.getSubmissionId() + 
                             " for student: " + sub.getStudentId());
        }
        System.out.println("✓ Saved " + submissions.size() + " submissions to " + SUBMISSIONS_FILE);
    } catch (IOException e) {
        System.err.println("ERROR saving submissions: " + e.getMessage());
        e.printStackTrace();
    }
}

    // ============================================================
    // LOAD METHODS
    // ============================================================

    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(DATA_DIR + USERS_FILE);

        if (!file.exists()) {
            System.out.println("No users file found. Will create on first save.");
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String userId = parts[0];
                    String username = parts[1];
                    String password = parts[2];
                    String fullName = parts[3];
                    String email = parts[4];
                    String role = parts[5];

                    User user = null;
                    switch (role) {
                        case "ADMIN":
                            user = new AdminStaff(userId, username, password, fullName, email);
                            break;
                        case "ACADEMIC_LEADER":
                            user = new AcademicLeader(userId, username, password, fullName, email);
                            if (parts.length > 7 && !parts[7].isEmpty()) {
                                ((AcademicLeader) user).setFieldId(parts[7]);
                            }
                            break;
                        case "LECTURER":
                            user = new Lecturer(userId, username, password, fullName, email);
                            if (parts.length > 6 && !parts[6].isEmpty()) {
                                ((Lecturer) user).setAcademicLeaderId(parts[6]);
                            }
                            if (parts.length > 7 && !parts[7].isEmpty()) {
                                ((Lecturer) user).setFieldId(parts[7]);
                            }
                            break;
                        case "STUDENT":
                            user = new Student(userId, username, password, fullName, email);
                            break;
                    }

                    if (user != null) {
                        users.add(user);
                    }
                }
            }
            System.out.println("Loaded " + users.size() + " users from " + USERS_FILE);
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }

        return users;
    }

    public List<Module> loadModules() {
        List<Module> modules = new ArrayList<>();
        File file = new File(DATA_DIR + MODULES_FILE);

        if (!file.exists()) {
            System.out.println("No modules file found.");
            return modules;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String moduleId = parts[0];
                    String moduleName = parts[1];
                    String moduleCode = parts[2];
                    String description = parts[3];

                    Module module = new Module(moduleId, moduleName, moduleCode, description);

                    if (parts.length > 4 && !parts[4].isEmpty()) {
                        module.setLecturerId(parts[4]);
                    }

                    if (parts.length > 5 && !parts[5].isEmpty()) {
                        module.setFieldId(parts[5]);
                    }

                    modules.add(module);
                }
            }
            System.out.println("Loaded " + modules.size() + " modules from " + MODULES_FILE);
        } catch (IOException e) {
            System.err.println("Error loading modules: " + e.getMessage());
        }

        return modules;
    }

    public List<ClassGroup> loadClasses() {
        List<ClassGroup> classes = new ArrayList<>();
        File file = new File(DATA_DIR + CLASSES_FILE);

        if (!file.exists()) {
            System.out.println("No classes file found.");
            return classes;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    String classId = parts[0];
                    String className = parts[1];
                    String moduleId = parts[2];

                    ClassGroup cls;

                    if (parts.length >= 9 && !parts[4].isEmpty()) {
                        String dayOfWeek = parts[4];
                        String startTime = parts[5];
                        String endTime = parts[6];
                        String room = parts[7];
                        int capacity = 30;
                        try {
                            capacity = Integer.parseInt(parts[8]);
                        } catch (NumberFormatException e) {
                            capacity = 30;
                        }

                        cls = new ClassGroup(classId, className, moduleId, dayOfWeek, startTime, endTime, room, capacity);
                    } else {
                        cls = new ClassGroup(classId, className, moduleId);
                    }

                    if (parts.length > 3 && !parts[3].isEmpty()) {
                        String[] studentIds = parts[3].split(";");
                        for (String studentId : studentIds) {
                            if (!studentId.trim().isEmpty()) {
                                cls.addStudent(studentId);
                            }
                        }
                    }

                    classes.add(cls);
                }
            }
            System.out.println("Loaded " + classes.size() + " classes from " + CLASSES_FILE);
        } catch (IOException e) {
            System.err.println("Error loading classes: " + e.getMessage());
        }

        return classes;
    }

    public List<Assessment> loadAssessments() {
        List<Assessment> assessments = new ArrayList<>();
        File file = new File(DATA_DIR + ASSESSMENTS_FILE);

        if (!file.exists()) {
            System.out.println("No assessments file found.");
            return assessments;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String assessmentId = parts[0];
                    String name = parts[1];
                    String type = parts[2];
                    double totalMarks = Double.parseDouble(parts[3]);
                    double weightage = Double.parseDouble(parts[4]);
                    String moduleId = parts[5];

                    Assessment assessment = new Assessment(assessmentId, name, type, totalMarks, weightage, moduleId);
                    assessments.add(assessment);
                }
            }
            System.out.println("Loaded " + assessments.size() + " assessments from " + ASSESSMENTS_FILE);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading assessments: " + e.getMessage());
        }

        return assessments;
    }

    public List<AssessmentResult> loadResults() {
        List<AssessmentResult> results = new ArrayList<>();
        File file = new File(DATA_DIR + RESULTS_FILE);

        if (!file.exists()) {
            System.out.println("No results file found.");
            return results;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String resultId = parts[0];
                    String studentId = parts[1];
                    String assessmentId = parts[2];
                    double marks = Double.parseDouble(parts[3]);
                    String grade = parts.length > 4 ? parts[4] : "";
                    String feedback = parts.length > 5 ? parts[5].replace("~~COMMA~~", ",") : "";

                    AssessmentResult result = new AssessmentResult(resultId, studentId, assessmentId, marks, feedback);
                    if (!grade.isEmpty()) {
                        result.setGrade(grade);
                    }
                    results.add(result);
                }
            }
            System.out.println("Loaded " + results.size() + " results from " + RESULTS_FILE);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading results: " + e.getMessage());
        }

        return results;
    }

    public List<ModuleFeedback> loadModuleFeedbacks() {
        List<ModuleFeedback> feedbacks = new ArrayList<>();
        File file = new File(DATA_DIR + MODULE_FEEDBACKS_FILE);

        if (!file.exists()) {
            System.out.println("No module feedbacks file found.");
            return feedbacks;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 13) {
                    String feedbackId = parts[0];
                    String studentId = parts[1];
                    String moduleId = parts[2];
                    String lecturerId = parts[3];

                    ModuleFeedback feedback = new ModuleFeedback(feedbackId, studentId, moduleId, lecturerId);

                    try {
                        feedback.setContentQualityRating(Integer.parseInt(parts[4]));
                        feedback.setTeachingEffectivenessRating(Integer.parseInt(parts[5]));
                        feedback.setMaterialClarityRating(Integer.parseInt(parts[6]));
                        feedback.setResponsivenessRating(Integer.parseInt(parts[7]));
                        feedback.setOverallRating(Integer.parseInt(parts[8]));

                        feedback.setStrengths(parts[9].replace("~~COMMA~~", ","));
                        feedback.setImprovements(parts[10].replace("~~COMMA~~", ","));
                        feedback.setAdditionalComments(parts[11].replace("~~COMMA~~", ","));

                        feedbacks.add(feedback);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing feedback ratings: " + e.getMessage());
                    }
                }
            }
            System.out.println("Loaded " + feedbacks.size() + " module feedbacks from " + MODULE_FEEDBACKS_FILE);
        } catch (IOException e) {
            System.err.println("Error loading module feedbacks: " + e.getMessage());
        }

        return feedbacks;
    }

    public List<Field> loadFields() {
        List<Field> fields = new ArrayList<>();
        File file = new File(DATA_DIR + FIELDS_FILE);

        if (!file.exists()) {
            System.out.println("No fields file found.");
            return fields;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String fieldId = parts[0];
                    String fieldName = parts[1];

                    Field field = new Field(fieldId, fieldName);

                    if (parts.length > 2 && !parts[2].isEmpty()) {
                        field.setAcademicLeaderId(parts[2]);
                    }

                    if (parts.length > 3 && !parts[3].isEmpty()) {
                        String[] moduleIds = parts[3].split(";");
                        for (String moduleId : moduleIds) {
                            if (!moduleId.trim().isEmpty()) {
                                field.addModule(moduleId);
                            }
                        }
                    }

                    fields.add(field);
                }
            }
            System.out.println("Loaded " + fields.size() + " fields from " + FIELDS_FILE);
        } catch (IOException e) {
            System.err.println("Error loading fields: " + e.getMessage());
        }

        return fields;
    }

    public void loadLecturerModules(List<User> users) {
        File file = new File(DATA_DIR + LECTURER_MODULES_FILE);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String lecturerId = parts[0];
                    String[] moduleIds = parts[1].split(";");

                    for (User user : users) {
                        if (user instanceof Lecturer && user.getUserId().equals(lecturerId)) {
                            Lecturer lec = (Lecturer) user;
                            for (String moduleId : moduleIds) {
                                if (!moduleId.trim().isEmpty()) {
                                    lec.addModule(moduleId);
                                }
                            }
                            break;
                        }
                    }
                }
            }
            System.out.println("Loaded lecturer-module mappings");
        } catch (IOException e) {
            System.err.println("Error loading lecturer modules: " + e.getMessage());
        }
    }

    public void loadStudentClasses(List<User> users) {
        File file = new File(DATA_DIR + STUDENT_CLASSES_FILE);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String studentId = parts[0];
                    String[] classIds = parts[1].split(";");

                    for (User user : users) {
                        if (user instanceof Student && user.getUserId().equals(studentId)) {
                            Student stu = (Student) user;
                            for (String classId : classIds) {
                                if (!classId.trim().isEmpty()) {
                                    stu.registerClass(classId);
                                }
                            }
                            break;
                        }
                    }
                }
            }
            System.out.println("Loaded student-class registrations");
        } catch (IOException e) {
            System.err.println("Error loading student classes: " + e.getMessage());
        }
    }

    public void loadLeaderLecturers(List<User> users) {
        File file = new File(DATA_DIR + LEADER_LECTURERS_FILE);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String leaderId = parts[0];
                    String[] lecturerIds = parts[1].split(";");

                    for (User user : users) {
                        if (user instanceof AcademicLeader && user.getUserId().equals(leaderId)) {
                            AcademicLeader leader = (AcademicLeader) user;
                            for (String lecturerId : lecturerIds) {
                                if (!lecturerId.trim().isEmpty()) {
                                    leader.addLecturer(lecturerId);
                                }
                            }
                            break;
                        }
                    }
                }
            }
            System.out.println("Loaded leader-lecturer mappings");
        } catch (IOException e) {
            System.err.println("Error loading leader lecturers: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD SUBMISSIONS
    // ============================================================
    public List<AssessmentSubmission> loadSubmissions() {
        List<AssessmentSubmission> submissions = new ArrayList<>();
        File file = new File(DATA_DIR + SUBMISSIONS_FILE);

        if (!file.exists()) {
            System.out.println("No submissions file found.");
            return submissions;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    String submissionId = parts[0];
                    String assessmentId = parts[1];
                    String studentId = parts[2];
                    String filePath = parts[3];
                    String fileName = parts[4];
                    // parts[5] is submissionDate timestamp
                    String status = parts[6];
                    String comments = parts.length > 7 ? parts[7].replace("~~COMMA~~", ",") : "";

                    AssessmentSubmission sub = new AssessmentSubmission(
                            submissionId, assessmentId, studentId, filePath, fileName
                    );
                    sub.setStatus(status);
                    sub.setSubmissionComments(comments);

                    submissions.add(sub);
                }
            }
            System.out.println("Loaded " + submissions.size() + " submissions from " + SUBMISSIONS_FILE);
        } catch (IOException e) {
            System.err.println("Error loading submissions: " + e.getMessage());
        }

        return submissions;
    }

    // ============================================================
    // SAVE ALL DATA
    // ============================================================
    public void saveAllData(List<User> users, List<Module> modules, List<ClassGroup> classes,
                            List<Assessment> assessments, List<AssessmentResult> results,
                            List<Field> fields, List<ModuleFeedback> moduleFeedbacks,
                            List<AssessmentSubmission> submissions) {  // ADDED PARAMETER
        System.out.println("\n=== SAVING ALL DATA TO FILES ===");
        saveUsers(users);
        saveModules(modules);
        saveClasses(classes);
        saveAssessments(assessments);
        saveResults(results);
        saveFields(fields);
        saveModuleFeedbacks(moduleFeedbacks);
        saveSubmissions(submissions);  // NEW
        saveLecturerModules(users);
        saveStudentClasses(users);
        saveLeaderLecturers(users);
        System.out.println("=== SAVE COMPLETE ===\n");
    }
}
import java.io.File;
import java.util.*;

class DataManager {
    private static DataManager instance;
    private List<User> users;
    private List<Module> modules;
    private List<ClassGroup> classes;
    private List<Assessment> assessments;
    private List<AssessmentResult> results;
    private List<Field> fields;
    private List<ModuleFeedback> moduleFeedbacks;
    private List<AssessmentSubmission> submissions; // NEW
    private User currentUser;
    private FileManager fileManager;

    private DataManager() {
        fileManager = FileManager.getInstance();
        users = new ArrayList<>();
        modules = new ArrayList<>();
        classes = new ArrayList<>();
        assessments = new ArrayList<>();
        results = new ArrayList<>();
        fields = new ArrayList<>();
        moduleFeedbacks = new ArrayList<>();
        submissions = new ArrayList<>(); // NEW
        loadAllData();
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
// Add this to DataManager class
public void debugCheckSubmissions() {
    System.out.println("=== DEBUG: Current Submissions in Memory ===");
    System.out.println("Total submissions: " + submissions.size());
    for (AssessmentSubmission sub : submissions) {
        System.out.println("  - " + sub.getSubmissionId() + 
                         " | Student: " + sub.getStudentId() + 
                         " | Assessment: " + sub.getAssessmentId() +
                         " | Status: " + sub.getStatus());
    }
    
    // Check file
    File file = new File("data/submissions.txt");
    System.out.println("Submissions file exists: " + file.exists());
    System.out.println("Submissions file size: " + (file.exists() ? file.length() : 0) + " bytes");
}
    private void loadAllData() {
        System.out.println("\n=== LOADING DATA FROM FILES ===");

        // Load in correct order - fields must be first
        fields = fileManager.loadFields();
        System.out.println("DEBUG: Loaded " + fields.size() + " fields");
        for (Field f : fields) {
            System.out.println("  - Field: " + f.getFieldId() + " (" + f.getFieldName() + ")");
        }

        users = fileManager.loadUsers();
        System.out.println("DEBUG: Loaded " + users.size() + " users");
        for (User u : users) {
            System.out.println("  - User: " + u.getUserId() + " (" + u.getFullName() + ") - " + u.getRole());
        }

        modules = fileManager.loadModules();
        System.out.println("DEBUG: Loaded " + modules.size() + " modules");
        for (Module m : modules) {
            System.out.println("  - Module: " + m.getModuleId() + " (" + m.getModuleName() + ") - Field: " + m.getFieldId() + ", Lecturer: " + m.getLecturerId());
        }

        classes = fileManager.loadClasses();
        System.out.println("DEBUG: Loaded " + classes.size() + " classes");

        assessments = fileManager.loadAssessments();
        System.out.println("DEBUG: Loaded " + assessments.size() + " assessments");

        results = fileManager.loadResults();
        System.out.println("DEBUG: Loaded " + results.size() + " results");

        moduleFeedbacks = fileManager.loadModuleFeedbacks();
        System.out.println("DEBUG: Loaded " + moduleFeedbacks.size() + " feedbacks");

        submissions = fileManager.loadSubmissions();
        System.out.println("DEBUG: Loaded " + submissions.size() + " submissions");

        // Load relationships AFTER loading users
        fileManager.loadLecturerModules(users);
        fileManager.loadStudentClasses(users);
        fileManager.loadLeaderLecturers(users);

        System.out.println("\n=== CHECKING DATA INTEGRITY ===");

        // Debug: Show field assignments
        System.out.println("\nDEBUG: Field Assignments:");
        for (Field field : fields) {
            System.out.println("  Field " + field.getFieldId() + " (" + field.getFieldName() +
                    ") -> Leader: " + field.getAcademicLeaderId() +
                    ", Modules: " + field.getModuleIds().size());
            for (String moduleId : field.getModuleIds()) {
                System.out.println("    - Module: " + moduleId);
            }
        }

        // Debug: Show lecturer assignments
        System.out.println("\nDEBUG: Lecturer Details:");
        for (User user : users) {
            if (user instanceof Lecturer) {
                Lecturer lec = (Lecturer) user;
                System.out.println("  Lecturer " + lec.getUserId() + " (" + lec.getFullName() +
                        ") -> Field: " + lec.getFieldId() +
                        ", Leader: " + lec.getAcademicLeaderId() +
                        ", Modules: " + lec.getAssignedModules().size());
                for (String modId : lec.getAssignedModules()) {
                    System.out.println("    - Module: " + modId);
                }
            }
        }

        // Debug: Show academic leader info
        System.out.println("\nDEBUG: Academic Leader Details:");
        for (User user : users) {
            if (user instanceof AcademicLeader) {
                AcademicLeader leader = (AcademicLeader) user;
                System.out.println("  Leader " + leader.getUserId() + " (" + leader.getFullName() +
                        ") -> Field: " + leader.getFieldId() +
                        ", Manages lecturers: " + leader.getManagedLecturers().size());
                for (String lecId : leader.getManagedLecturers()) {
                    System.out.println("    - Lecturer: " + lecId);
                }
            }
        }

        // ONLY initialize if NO data exists at all
        if (users.isEmpty() && fields.isEmpty() && modules.isEmpty()) {
            System.out.println("\n!!! NO DATA FOUND - Initializing default data !!!");
            initializeDefaultData();
            saveAllData();
        } else {
            System.out.println("\n=== DATA LOADING COMPLETE ===");
            System.out.println("Total fields: " + fields.size());
            System.out.println("Total users: " + users.size());
            System.out.println("Total modules: " + modules.size());
        }
    }

    public void saveAllData() {
        fileManager.saveAllData(users, modules, classes, assessments, results,
                fields, moduleFeedbacks, submissions);
    }

    // USER MANAGEMENT
    public User authenticate(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                currentUser = user;
                return user;
            }
        }
        return null;
    }

    public void addUser(User user) { users.add(user); saveAllData(); }
    public void deleteUser(String userId) { users.removeIf(u -> u.getUserId().equals(userId)); saveAllData(); }
    public List<User> getAllUsers() { return users; }
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { currentUser = user; }

    public User getUserById(String userId) {
        for (User user : users) {
            if (user.getUserId().equals(userId)) return user;
        }
        return null;
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        for (User user : users) {
            if (user instanceof Student) students.add((Student) user);
        }
        return students;
    }

    public List<Lecturer> getAllLecturers() {
        List<Lecturer> lecturers = new ArrayList<>();
        for (User user : users) {
            if (user instanceof Lecturer) lecturers.add((Lecturer) user);
        }
        return lecturers;
    }

    // MODULE MANAGEMENT
    public void addModule(Module module) { modules.add(module); saveAllData(); }
    public void deleteModule(String moduleId) { modules.removeIf(m -> m.getModuleId().equals(moduleId)); saveAllData(); }
    public List<Module> getAllModules() { return modules; }

    public Module getModuleById(String moduleId) {
        for (Module module : modules) {
            if (module.getModuleId().equals(moduleId)) return module;
        }
        return null;
    }

    public List<Module> getModulesByField(String fieldId) {
        List<Module> fieldModules = new ArrayList<>();
        if (fieldId == null) {
            System.out.println("DEBUG getModulesByField: fieldId is NULL!");
            return fieldModules;
        }

        System.out.println("DEBUG getModulesByField: Looking for modules in field: " + fieldId);

        for (Module module : modules) {
            System.out.println("  - Checking module " + module.getModuleId() +
                    " (Field: " + module.getFieldId() + ")");
            if (fieldId.equals(module.getFieldId())) {
                System.out.println("    → MATCH! Adding to list");
                fieldModules.add(module);
            }
        }

        System.out.println("DEBUG getModulesByField: Found " + fieldModules.size() + " modules");
        return fieldModules;
    }

    // CLASS MANAGEMENT
    public void addClass(ClassGroup classGroup) { classes.add(classGroup); saveAllData(); }
    public void deleteClass(String classId) { classes.removeIf(c -> c.getClassId().equals(classId)); saveAllData(); }
    public List<ClassGroup> getAllClasses() { return classes; }

    public ClassGroup getClassById(String classId) {
        for (ClassGroup cls : classes) {
            if (cls.getClassId().equals(classId)) return cls;
        }
        return null;
    }

    public List<ClassGroup> getClassesByModule(String moduleId) {
        List<ClassGroup> result = new ArrayList<>();
        for (ClassGroup cls : classes) {
            if (cls.getModuleId().equals(moduleId)) result.add(cls);
        }
        return result;
    }

    // ASSESSMENT MANAGEMENT
    public void addAssessment(Assessment assessment) { assessments.add(assessment); saveAllData(); }
    public void deleteAssessment(String assessmentId) { assessments.removeIf(a -> a.getAssessmentId().equals(assessmentId)); saveAllData(); }
    public List<Assessment> getAllAssessments() { return assessments; }

    public Assessment getAssessmentById(String assessmentId) {
        for (Assessment assessment : assessments) {
            if (assessment.getAssessmentId().equals(assessmentId)) return assessment;
        }
        return null;
    }

    public List<Assessment> getAssessmentsByModule(String moduleId) {
        List<Assessment> result = new ArrayList<>();
        for (Assessment assessment : assessments) {
            if (assessment.getModuleId().equals(moduleId)) result.add(assessment);
        }
        return result;
    }

    // FIELD MANAGEMENT
    public void addField(Field field) { fields.add(field); saveAllData(); }
    public void deleteField(String fieldId) { fields.removeIf(f -> f.getFieldId().equals(fieldId)); saveAllData(); }
    public List<Field> getAllFields() { return fields; }

    public Field getFieldById(String fieldId) {
        for (Field field : fields) {
            if (field.getFieldId().equals(fieldId)) return field;
        }
        return null;
    }

    public List<Lecturer> getLecturersByField(String fieldId) {
        List<Lecturer> lecturers = new ArrayList<>();
        if (fieldId == null) {
            System.out.println("DEBUG getLecturersByField: fieldId is NULL!");
            return lecturers;
        }

        System.out.println("DEBUG getLecturersByField: Looking for lecturers in field: " + fieldId);

        for (User user : users) {
            if (user instanceof Lecturer) {
                Lecturer lec = (Lecturer) user;
                System.out.println("  - Checking lecturer " + lec.getUserId() +
                        " (Field: " + lec.getFieldId() + ")");
                if (fieldId.equals(lec.getFieldId())) {
                    System.out.println("    → MATCH! Adding to list");
                    lecturers.add(lec);
                }
            }
        }

        System.out.println("DEBUG getLecturersByField: Found " + lecturers.size() + " lecturers");
        return lecturers;
    }

    // RESULT MANAGEMENT
    public void addResult(AssessmentResult result) { results.add(result); saveAllData(); }
    public void deleteResult(String resultId) { results.removeIf(r -> r.getResultId().equals(resultId)); saveAllData(); }
    public List<AssessmentResult> getAllResults() { return results; }

    public List<AssessmentResult> getResultsByStudent(String studentId) {
        List<AssessmentResult> studentResults = new ArrayList<>();
        for (AssessmentResult result : results) {
            if (result.getStudentId().equals(studentId)) studentResults.add(result);
        }
        return studentResults;
    }

    public List<AssessmentResult> getResultsByAssessment(String assessmentId) {
        List<AssessmentResult> assessmentResults = new ArrayList<>();
        for (AssessmentResult result : results) {
            if (result.getAssessmentId().equals(assessmentId)) assessmentResults.add(result);
        }
        return assessmentResults;
    }

    public AssessmentResult getResultByStudentAndAssessment(String studentId, String assessmentId) {
        for (AssessmentResult result : results) {
            if (result.getStudentId().equals(studentId) && result.getAssessmentId().equals(assessmentId)) {
                return result;
            }
        }
        return null;
    }

    // AUTO-GRADE CALCULATION - new method to auto generate the grade defined by admin
    public void updateOrCreateResultWithAutoGrade(String studentId, String assessmentId, double marks, String feedback) {
        Assessment assessment = getAssessmentById(assessmentId);
        if (assessment == null) return;

        double percentage = (marks / assessment.getTotalMarks()) * 100;
        String grade = GradingSystem.calculateGrade(percentage); // AUTO-CALCULATED

        AssessmentResult existingResult = getResultByStudentAndAssessment(studentId, assessmentId);

        if (existingResult != null) {
            existingResult.setMarksObtained(marks);
            existingResult.setGrade(grade);
            if (feedback != null && !feedback.isEmpty()) existingResult.setFeedback(feedback);
        } else {
            String resultId = "RES" + String.format("%03d", results.size() + 1);
            AssessmentResult newResult = new AssessmentResult(resultId, studentId, assessmentId, marks, feedback != null ? feedback : "");
            newResult.setGrade(grade);
            results.add(newResult);
        }

        AssessmentSubmission submission = getSubmissionByStudentAndAssessment(studentId, assessmentId);
        if (submission != null) submission.setStatus("GRADED");

        saveAllData();
    }

    // OLD METHOD - Keep for compatibility
    public void updateOrCreateResult(String studentId, String assessmentId, double marks, String grade, String feedback) {
        AssessmentResult existingResult = getResultByStudentAndAssessment(studentId, assessmentId);
        if (existingResult != null) {
            existingResult.setMarksObtained(marks);
            existingResult.setGrade(grade);
            if (feedback != null && !feedback.isEmpty()) existingResult.setFeedback(feedback);
        } else {
            String resultId = "RES" + String.format("%03d", results.size() + 1);
            AssessmentResult newResult = new AssessmentResult(resultId, studentId, assessmentId, marks, feedback != null ? feedback : "");
            newResult.setGrade(grade);
            results.add(newResult);
        }
        saveAllData();
    }

    // SUBMISSION MANAGEMENT
// Replace the addSubmission method with this FORCEFUL version:
public void addSubmission(AssessmentSubmission submission) { 
    submissions.add(submission); 
    
    System.out.println("DEBUG: Adding submission - ID: " + submission.getSubmissionId() + 
                      ", Student: " + submission.getStudentId() + 
                      ", Assessment: " + submission.getAssessmentId());
    
    // Force save ALL data, not just submissions
    saveAllData();
    
    // Double-check by printing current count
    System.out.println("DEBUG: Total submissions now: " + submissions.size());
}    public void deleteSubmission(String submissionId) { submissions.removeIf(s -> s.getSubmissionId().equals(submissionId)); saveAllData(); }
    public List<AssessmentSubmission> getAllSubmissions() { return submissions; }

    public List<AssessmentSubmission> getSubmissionsByStudent(String studentId) {
        List<AssessmentSubmission> studentSubmissions = new ArrayList<>();
        for (AssessmentSubmission sub : submissions) {
            if (sub.getStudentId().equals(studentId)) studentSubmissions.add(sub);
        }
        return studentSubmissions;
    }

    public List<AssessmentSubmission> getSubmissionsByAssessment(String assessmentId) {
        List<AssessmentSubmission> assessmentSubmissions = new ArrayList<>();
        for (AssessmentSubmission sub : submissions) {
            if (sub.getAssessmentId().equals(assessmentId)) assessmentSubmissions.add(sub);
        }
        return assessmentSubmissions;
    }

    public AssessmentSubmission getSubmissionByStudentAndAssessment(String studentId, String assessmentId) {
        for (AssessmentSubmission sub : submissions) {
            if (sub.getStudentId().equals(studentId) && sub.getAssessmentId().equals(assessmentId)) {
                return sub;
            }
        }
        return null;
    }

    public List<AssessmentSubmission> getPendingSubmissionsForLecturer(String lecturerId) {
        List<AssessmentSubmission> pending = new ArrayList<>();
        for (AssessmentSubmission sub : submissions) {
            if (sub.getStatus().equals("SUBMITTED")) {
                Assessment assessment = getAssessmentById(sub.getAssessmentId());
                if (assessment != null) {
                    Module module = getModuleById(assessment.getModuleId());
                    if (module != null && lecturerId.equals(module.getLecturerId())) {
                        pending.add(sub);
                    }
                }
            }
        }
        return pending;
    }

    public int countPendingSubmissions(String assessmentId) {
        int count = 0;
        for (AssessmentSubmission sub : submissions) {
            if (sub.getAssessmentId().equals(assessmentId) && sub.getStatus().equals("SUBMITTED")) count++;
        }
        return count;
    }

    public int getNotificationCountForLecturer(String lecturerId) {
        return getPendingSubmissionsForLecturer(lecturerId).size();
    }

    // MODULE FEEDBACK MANAGEMENT
    public void addModuleFeedback(ModuleFeedback feedback) { moduleFeedbacks.add(feedback); saveAllData(); }
    public void deleteModuleFeedback(String feedbackId) { moduleFeedbacks.removeIf(f -> f.getFeedbackId().equals(feedbackId)); saveAllData(); }
    public List<ModuleFeedback> getAllModuleFeedbacks() { return moduleFeedbacks; }

    public List<ModuleFeedback> getFeedbacksByModule(String moduleId) {
        List<ModuleFeedback> result = new ArrayList<>();
        for (ModuleFeedback feedback : moduleFeedbacks) {
            if (feedback.getModuleId().equals(moduleId)) result.add(feedback);
        }
        return result;
    }

    public List<ModuleFeedback> getFeedbacksByLecturer(String lecturerId) {
        List<ModuleFeedback> result = new ArrayList<>();
        for (ModuleFeedback feedback : moduleFeedbacks) {
            if (feedback.getLecturerId().equals(lecturerId)) result.add(feedback);
        }
        return result;
    }

    public List<ModuleFeedback> getFeedbacksByStudent(String studentId) {
        List<ModuleFeedback> result = new ArrayList<>();
        for (ModuleFeedback feedback : moduleFeedbacks) {
            if (feedback.getStudentId().equals(studentId)) result.add(feedback);
        }
        return result;
    }

    public String generateNextId(String prefix) {
        int maxId = 0;
        for (User user : users) {
            if (user.getUserId().startsWith(prefix)) {
                try {
                    int id = Integer.parseInt(user.getUserId().substring(prefix.length()));
                    maxId = Math.max(maxId, id);
                } catch (NumberFormatException e) {}
            }
        }
        return prefix + String.format("%03d", maxId + 1);
    }

    private void createResult(String id, String studentId, String assessmentId, double marks, String grade, String feedback) {
        AssessmentResult result = new AssessmentResult(id, studentId, assessmentId, marks, feedback);
        result.setGrade(grade);
        results.add(result);
    }

    // ============================================================
    // INITIALIZE DEFAULT DATA - FIXED VERSION
    // ============================================================
    private void initializeDefaultData() {
        System.out.println("\n=== INITIALIZING DEFAULT DATA ===");

        // Only initialize if lists are truly empty
        if (!users.isEmpty() || !fields.isEmpty() || !modules.isEmpty()) {
            System.out.println("WARNING: Data already exists. Skipping initialization to prevent data loss.");
            return;
        }

        // Just add to empty lists
        System.out.println("Creating fresh sample data...");

        // ============================================================
        // CREATE FIELDS FIRST
        // ============================================================
        Field field1 = new Field("FLD001", "Computer Science");
        Field field2 = new Field("FLD002", "Business");
        Field field3 = new Field("FLD003", "Engineering");

        fields.add(field1);
        fields.add(field2);
        fields.add(field3);
        System.out.println("✓ Created 3 fields");

        // ============================================================
        // CREATE ADMINS
        // ============================================================
        users.add(new AdminStaff("ADM001", "admin", "admin123", "System Administrator", "admin@apu.edu.my"));
        users.add(new AdminStaff("ADM002", "admin2", "pass123", "Sarah Admin", "sarah.admin@apu.edu.my"));
        System.out.println("✓ Created 2 admins");

        // ============================================================
        // CREATE ACADEMIC LEADERS & LINK TO FIELDS
        // ============================================================
        AcademicLeader leader1 = new AcademicLeader("AL001", "leader1", "pass123", "Dr. John Smith", "john.smith@apu.edu.my");
        leader1.setFieldId("FLD001");
        field1.setAcademicLeaderId("AL001");
        users.add(leader1);

        AcademicLeader leader2 = new AcademicLeader("AL002", "leader2", "pass123", "Dr. Sarah Johnson", "sarah.johnson@apu.edu.my");
        leader2.setFieldId("FLD002");
        field2.setAcademicLeaderId("AL002");
        users.add(leader2);

        AcademicLeader leader3 = new AcademicLeader("AL003", "leader3", "pass123", "Prof. David Lee", "david.lee@apu.edu.my");
        leader3.setFieldId("FLD003");
        field3.setAcademicLeaderId("AL003");
        users.add(leader3);
        System.out.println("✓ Created 3 academic leaders");

        // ============================================================
        // CREATE LECTURERS & LINK TO FIELDS
        // ============================================================

        // Computer Science Lecturers (4)
        Lecturer lec1 = new Lecturer("LEC001", "lecturer1", "pass123", "Prof. Jane Doe", "jane.doe@apu.edu.my");
        lec1.setAcademicLeaderId("AL001");
        lec1.setFieldId("FLD001");
        leader1.addLecturer("LEC001");
        users.add(lec1);

        Lecturer lec2 = new Lecturer("LEC002", "lecturer2", "pass123", "Dr. Michael Chen", "michael.chen@apu.edu.my");
        lec2.setAcademicLeaderId("AL001");
        lec2.setFieldId("FLD001");
        leader1.addLecturer("LEC002");
        users.add(lec2);

        Lecturer lec3 = new Lecturer("LEC003", "lecturer3", "pass123", "Dr. Emily Wong", "emily.wong@apu.edu.my");
        lec3.setAcademicLeaderId("AL001");
        lec3.setFieldId("FLD001");
        leader1.addLecturer("LEC003");
        users.add(lec3);

        Lecturer lec4 = new Lecturer("LEC004", "lecturer4", "pass123", "Dr. Ahmad Razak", "ahmad.razak@apu.edu.my");
        lec4.setAcademicLeaderId("AL001");
        lec4.setFieldId("FLD001");
        leader1.addLecturer("LEC004");
        users.add(lec4);

        // Business Lecturers (4)
        Lecturer lec5 = new Lecturer("LEC005", "lecturer5", "pass123", "Prof. Lisa Kumar", "lisa.kumar@apu.edu.my");
        lec5.setAcademicLeaderId("AL002");
        lec5.setFieldId("FLD002");
        leader2.addLecturer("LEC005");
        users.add(lec5);

        Lecturer lec6 = new Lecturer("LEC006", "lecturer6", "pass123", "Dr. Robert Tan", "robert.tan@apu.edu.my");
        lec6.setAcademicLeaderId("AL002");
        lec6.setFieldId("FLD002");
        leader2.addLecturer("LEC006");
        users.add(lec6);

        Lecturer lec7 = new Lecturer("LEC007", "lecturer7", "pass123", "Prof. Maria Garcia", "maria.garcia@apu.edu.my");
        lec7.setAcademicLeaderId("AL002");
        lec7.setFieldId("FLD002");
        leader2.addLecturer("LEC007");
        users.add(lec7);

        Lecturer lec8 = new Lecturer("LEC008", "lecturer8", "pass123", "Dr. James Lee", "james.lee@apu.edu.my");
        lec8.setAcademicLeaderId("AL002");
        lec8.setFieldId("FLD002");
        leader2.addLecturer("LEC008");
        users.add(lec8);

        // Engineering Lecturers (4)
        Lecturer lec9 = new Lecturer("LEC009", "lecturer9", "pass123", "Prof. David Kumar", "david.kumar@apu.edu.my");
        lec9.setAcademicLeaderId("AL003");
        lec9.setFieldId("FLD003");
        leader3.addLecturer("LEC009");
        users.add(lec9);

        Lecturer lec10 = new Lecturer("LEC010", "lecturer10", "pass123", "Dr. Siti Nurhaliza", "siti.nurhaliza@apu.edu.my");
        lec10.setAcademicLeaderId("AL003");
        lec10.setFieldId("FLD003");
        leader3.addLecturer("LEC010");
        users.add(lec10);

        Lecturer lec11 = new Lecturer("LEC011", "lecturer11", "pass123", "Dr. Wei Zhang", "wei.zhang@apu.edu.my");
        lec11.setAcademicLeaderId("AL003");
        lec11.setFieldId("FLD003");
        leader3.addLecturer("LEC011");
        users.add(lec11);

        Lecturer lec12 = new Lecturer("LEC012", "lecturer12", "pass123", "Prof. Arjun Patel", "arjun.patel@apu.edu.my");
        lec12.setAcademicLeaderId("AL003");
        lec12.setFieldId("FLD003");
        leader3.addLecturer("LEC012");
        users.add(lec12);

        System.out.println("✓ Created 12 lecturers (4 per field)");

        // ============================================================
        // CREATE STUDENTS
        // ============================================================
        Student stu1 = new Student("STU001", "student1", "pass123", "Ali Ahmad", "ali.ahmad@student.apu.edu.my");
        Student stu2 = new Student("STU002", "student2", "pass123", "Nurul Aisyah", "nurul.aisyah@student.apu.edu.my");
        Student stu3 = new Student("STU003", "student3", "pass123", "Kumar Raj", "kumar.raj@student.apu.edu.my");
        Student stu4 = new Student("STU004", "student4", "pass123", "Lee Wei Ming", "lee.weiming@student.apu.edu.my");
        Student stu5 = new Student("STU005", "student5", "pass123", "Sarah Tan", "sarah.tan@student.apu.edu.my");
        Student stu6 = new Student("STU006", "student6", "pass123", "Muhammad Hafiz", "hafiz@student.apu.edu.my");
        Student stu7 = new Student("STU007", "student7", "pass123", "Priya Sharma", "priya@student.apu.edu.my");
        Student stu8 = new Student("STU008", "student8", "pass123", "Chen Wei", "chen@student.apu.edu.my");
        Student stu9 = new Student("STU009", "student9", "pass123", "Fatima Hassan", "fatima@student.apu.edu.my");
        Student stu10 = new Student("STU010", "student10", "pass123", "Raj Kumar", "raj@student.apu.edu.my");

        users.add(stu1);
        users.add(stu2);
        users.add(stu3);
        users.add(stu4);
        users.add(stu5);
        users.add(stu6);
        users.add(stu7);
        users.add(stu8);
        users.add(stu9);
        users.add(stu10);
        System.out.println("✓ Created 10 students");

        // ============================================================
        // CREATE MODULES - LINK TO FIELDS & LECTURERS
        // ============================================================

        // COMPUTER SCIENCE MODULES (4)
        Module mod1 = new Module("MOD001", "Python Programming", "CT091-3-2",
                "Learn Python programming with focus on data structures, algorithms, and practical applications");
        mod1.setLecturerId("LEC001");
        mod1.setFieldId("FLD001");
        field1.addModule("MOD001");
        lec1.addModule("MOD001");
        modules.add(mod1);

        Module mod2 = new Module("MOD002", "Object-Oriented with Java", "CT092-3-2",
                "Learn Java programming with OOP concepts including inheritance, polymorphism, and encapsulation");
        mod2.setLecturerId("LEC002");
        mod2.setFieldId("FLD001");
        field1.addModule("MOD002");
        lec2.addModule("MOD002");
        modules.add(mod2);

        Module mod3 = new Module("MOD003", "Database Management", "CT093-3-2",
                "Learn SQL, database design, normalization, and relational database management systems");
        mod3.setLecturerId("LEC003");
        mod3.setFieldId("FLD001");
        field1.addModule("MOD003");
        lec3.addModule("MOD003");
        modules.add(mod3);

        Module mod4 = new Module("MOD004", "Information System", "CT094-3-2",
                "Learn about enterprise information systems, business processes, and IT management");
        mod4.setLecturerId("LEC004");
        mod4.setFieldId("FLD001");
        field1.addModule("MOD004");
        lec4.addModule("MOD004");
        modules.add(mod4);

        // BUSINESS MODULES (4)
        Module mod5 = new Module("MOD005", "Customer Relationship Management", "BU091-3-2",
                "Learn CRM strategies, customer service excellence, and relationship building techniques");
        mod5.setLecturerId("LEC005");
        mod5.setFieldId("FLD002");
        field2.addModule("MOD005");
        lec5.addModule("MOD005");
        modules.add(mod5);

        Module mod6 = new Module("MOD006", "Business Analytics", "BU092-3-2",
                "Learn data analysis, business intelligence, and decision-making with data-driven insights");
        mod6.setLecturerId("LEC006");
        mod6.setFieldId("FLD002");
        field2.addModule("MOD006");
        lec6.addModule("MOD006");
        modules.add(mod6);

        Module mod7 = new Module("MOD007", "E-commerce", "BU093-3-2",
                "Learn online business models, digital marketing, and e-commerce platform management");
        mod7.setLecturerId("LEC007");
        mod7.setFieldId("FLD002");
        field2.addModule("MOD007");
        lec7.addModule("MOD007");
        modules.add(mod7);

        Module mod8 = new Module("MOD008", "Business Economics", "BU094-3-2",
                "Learn economic principles, market analysis, and their application to business decisions");
        mod8.setLecturerId("LEC008");
        mod8.setFieldId("FLD002");
        field2.addModule("MOD008");
        lec8.addModule("MOD008");
        modules.add(mod8);

        // ENGINEERING MODULES (4)
        Module mod9 = new Module("MOD009", "Robotics & Automation", "EN091-3-2",
                "Learn robotics principles, automation systems, and control mechanisms");
        mod9.setLecturerId("LEC009");
        mod9.setFieldId("FLD003");
        field3.addModule("MOD009");
        lec9.addModule("MOD009");
        modules.add(mod9);

        Module mod10 = new Module("MOD010", "Engineering Science", "EN092-3-2",
                "Learn fundamental engineering principles including mechanics, materials, and thermodynamics");
        mod10.setLecturerId("LEC010");
        mod10.setFieldId("FLD003");
        field3.addModule("MOD010");
        lec10.addModule("MOD010");
        modules.add(mod10);

        Module mod11 = new Module("MOD011", "Engineering Mathematics", "EN093-3-2",
                "Learn advanced mathematics for engineering including calculus, linear algebra, and differential equations");
        mod11.setLecturerId("LEC011");
        mod11.setFieldId("FLD003");
        field3.addModule("MOD011");
        lec11.addModule("MOD011");
        modules.add(mod11);

        Module mod12 = new Module("MOD012", "Energy Technologies", "EN094-3-2",
                "Learn renewable energy systems, power generation, and sustainable energy solutions");
        mod12.setLecturerId("LEC012");
        mod12.setFieldId("FLD003");
        field3.addModule("MOD012");
        lec12.addModule("MOD012");
        modules.add(mod12);

        System.out.println("✓ Created 12 modules (4 per field)");
        System.out.println("  - Computer Science: Python, Java OOP, Database, Info Systems");
        System.out.println("  - Business: CRM, Analytics, E-commerce, Economics");
        System.out.println("  - Engineering: Robotics, Eng Science, Eng Math, Energy Tech");

        // ============================================================
        // CREATE CLASSES (2 per module = 24 classes)
        // ============================================================

        // Computer Science Classes
        ClassGroup cls1 = new ClassGroup("CLS001", "Python Programming Class A", "MOD001",
                "Monday", "09:00", "11:00", "Room 301", 30);
        ClassGroup cls2 = new ClassGroup("CLS002", "Python Programming Class B", "MOD001",
                "Wednesday", "14:00", "16:00", "Room 302", 30);
        ClassGroup cls3 = new ClassGroup("CLS003", "OOP with Java Class A", "MOD002",
                "Tuesday", "09:00", "11:00", "Room 303", 30);
        ClassGroup cls4 = new ClassGroup("CLS004", "OOP with Java Class B", "MOD002",
                "Thursday", "14:00", "16:00", "Room 304", 30);
        ClassGroup cls5 = new ClassGroup("CLS005", "Database Management Class A", "MOD003",
                "Monday", "14:00", "16:00", "Lab 101", 25);
        ClassGroup cls6 = new ClassGroup("CLS006", "Database Management Class B", "MOD003",
                "Friday", "09:00", "11:00", "Lab 102", 25);
        ClassGroup cls7 = new ClassGroup("CLS007", "Information System Class A", "MOD004",
                "Wednesday", "09:00", "11:00", "Room 305", 30);
        ClassGroup cls8 = new ClassGroup("CLS008", "Information System Class B", "MOD004",
                "Friday", "14:00", "16:00", "Room 306", 30);

        // Business Classes
        ClassGroup cls9 = new ClassGroup("CLS009", "CRM Class A", "MOD005",
                "Monday", "09:00", "11:00", "Room 401", 35);
        ClassGroup cls10 = new ClassGroup("CLS010", "CRM Class B", "MOD005",
                "Thursday", "14:00", "16:00", "Room 402", 35);
        ClassGroup cls11 = new ClassGroup("CLS011", "Business Analytics Class A", "MOD006",
                "Tuesday", "09:00", "11:00", "Lab 201", 30);
        ClassGroup cls12 = new ClassGroup("CLS012", "Business Analytics Class B", "MOD006",
                "Friday", "09:00", "11:00", "Lab 202", 30);
        ClassGroup cls13 = new ClassGroup("CLS013", "E-commerce Class A", "MOD007",
                "Monday", "14:00", "16:00", "Room 403", 30);
        ClassGroup cls14 = new ClassGroup("CLS014", "E-commerce Class B", "MOD007",
                "Wednesday", "14:00", "16:00", "Room 404", 30);
        ClassGroup cls15 = new ClassGroup("CLS015", "Business Economics Class A", "MOD008",
                "Tuesday", "14:00", "16:00", "Room 405", 35);
        ClassGroup cls16 = new ClassGroup("CLS016", "Business Economics Class B", "MOD008",
                "Thursday", "09:00", "11:00", "Room 406", 35);

        // Engineering Classes
        ClassGroup cls17 = new ClassGroup("CLS017", "Robotics & Automation Class A", "MOD009",
                "Monday", "09:00", "12:00", "Lab 301", 20);
        ClassGroup cls18 = new ClassGroup("CLS018", "Robotics & Automation Class B", "MOD009",
                "Wednesday", "14:00", "17:00", "Lab 302", 20);
        ClassGroup cls19 = new ClassGroup("CLS019", "Engineering Science Class A", "MOD010",
                "Tuesday", "09:00", "11:00", "Room 501", 30);
        ClassGroup cls20 = new ClassGroup("CLS020", "Engineering Science Class B", "MOD010",
                "Thursday", "14:00", "16:00", "Room 502", 30);
        ClassGroup cls21 = new ClassGroup("CLS021", "Engineering Mathematics Class A", "MOD011",
                "Monday", "14:00", "16:00", "Room 503", 30);
        ClassGroup cls22 = new ClassGroup("CLS022", "Engineering Mathematics Class B", "MOD011",
                "Friday", "09:00", "11:00", "Room 504", 30);
        ClassGroup cls23 = new ClassGroup("CLS023", "Energy Technologies Class A", "MOD012",
                "Wednesday", "09:00", "11:00", "Lab 303", 25);
        ClassGroup cls24 = new ClassGroup("CLS024", "Energy Technologies Class B", "MOD012",
                "Friday", "14:00", "16:00", "Lab 304", 25);

        // Distribute students to classes (keeping your existing distribution)
        cls1.addStudent("STU001"); cls1.addStudent("STU002"); cls1.addStudent("STU003");
        cls2.addStudent("STU004"); cls2.addStudent("STU005");
        cls3.addStudent("STU001"); cls3.addStudent("STU003"); cls3.addStudent("STU005");
        cls4.addStudent("STU002"); cls4.addStudent("STU004");
        cls5.addStudent("STU001"); cls5.addStudent("STU002"); cls5.addStudent("STU003");
        cls6.addStudent("STU004"); cls6.addStudent("STU005");
        cls7.addStudent("STU001"); cls7.addStudent("STU004");
        cls8.addStudent("STU002"); cls8.addStudent("STU005");

        cls9.addStudent("STU003"); cls9.addStudent("STU006"); cls9.addStudent("STU007");
        cls10.addStudent("STU008"); cls10.addStudent("STU009");
        cls11.addStudent("STU006"); cls11.addStudent("STU007");
        cls12.addStudent("STU008"); cls12.addStudent("STU009");
        cls13.addStudent("STU003"); cls13.addStudent("STU007");
        cls14.addStudent("STU006"); cls14.addStudent("STU009");
        cls15.addStudent("STU006"); cls15.addStudent("STU008");
        cls16.addStudent("STU007"); cls16.addStudent("STU009");

        cls17.addStudent("STU010");
        cls18.addStudent("STU010");
        cls19.addStudent("STU010");
        cls20.addStudent("STU010");
        cls21.addStudent("STU010");
        cls22.addStudent("STU010");
        cls23.addStudent("STU010");
        cls24.addStudent("STU010");

        // Register students to classes (keeping your existing registrations)
        stu1.registerClass("CLS001"); stu1.registerClass("CLS003"); stu1.registerClass("CLS005"); stu1.registerClass("CLS007");
        stu2.registerClass("CLS001"); stu2.registerClass("CLS004"); stu2.registerClass("CLS005"); stu2.registerClass("CLS008");
        stu3.registerClass("CLS001"); stu3.registerClass("CLS003"); stu3.registerClass("CLS005"); stu3.registerClass("CLS009"); stu3.registerClass("CLS013");
        stu4.registerClass("CLS002"); stu4.registerClass("CLS004"); stu4.registerClass("CLS006"); stu4.registerClass("CLS007");
        stu5.registerClass("CLS002"); stu5.registerClass("CLS003"); stu5.registerClass("CLS006"); stu5.registerClass("CLS008");
        stu6.registerClass("CLS009"); stu6.registerClass("CLS011"); stu6.registerClass("CLS014"); stu6.registerClass("CLS015");
        stu7.registerClass("CLS009"); stu7.registerClass("CLS011"); stu7.registerClass("CLS013"); stu7.registerClass("CLS016");
        stu8.registerClass("CLS010"); stu8.registerClass("CLS012"); stu8.registerClass("CLS015");
        stu9.registerClass("CLS010"); stu9.registerClass("CLS012"); stu9.registerClass("CLS014"); stu9.registerClass("CLS016");
        stu10.registerClass("CLS017"); stu10.registerClass("CLS018"); stu10.registerClass("CLS019"); stu10.registerClass("CLS020");
        stu10.registerClass("CLS021"); stu10.registerClass("CLS022"); stu10.registerClass("CLS023"); stu10.registerClass("CLS024");

        classes.add(cls1); classes.add(cls2); classes.add(cls3); classes.add(cls4);
        classes.add(cls5); classes.add(cls6); classes.add(cls7); classes.add(cls8);
        classes.add(cls9); classes.add(cls10); classes.add(cls11); classes.add(cls12);
        classes.add(cls13); classes.add(cls14); classes.add(cls15); classes.add(cls16);
        classes.add(cls17); classes.add(cls18); classes.add(cls19); classes.add(cls20);
        classes.add(cls21); classes.add(cls22); classes.add(cls23); classes.add(cls24);
        System.out.println("✓ Created 24 classes with schedules (2 per module)");

        // ============================================================
        // CREATE ASSESSMENTS (3 per module = 36 assessments)
        // ============================================================

        // Computer Science Assessments
        assessments.add(new Assessment("ASS001", "Python Basics Assignment", "Assignment", 100, 30, "MOD001"));
        assessments.add(new Assessment("ASS002", "Python Data Structures Test", "Test", 50, 30, "MOD001"));
        assessments.add(new Assessment("ASS003", "Python Final Project", "Project", 100, 40, "MOD001"));

        assessments.add(new Assessment("ASS004", "Java Fundamentals Quiz", "Quiz", 20, 15, "MOD002"));
        assessments.add(new Assessment("ASS005", "OOP Design Project", "Project", 100, 50, "MOD002"));
        assessments.add(new Assessment("ASS006", "Java Final Exam", "Exam", 100, 35, "MOD002"));

        assessments.add(new Assessment("ASS007", "SQL Basics Assignment", "Assignment", 100, 25, "MOD003"));
        assessments.add(new Assessment("ASS008", "Database Design Test", "Test", 50, 35, "MOD003"));
        assessments.add(new Assessment("ASS009", "Database Final Project", "Project", 100, 40, "MOD003"));

        assessments.add(new Assessment("ASS010", "IS Analysis Assignment", "Assignment", 100, 30, "MOD004"));
        assessments.add(new Assessment("ASS011", "Enterprise Systems Test", "Test", 50, 30, "MOD004"));
        assessments.add(new Assessment("ASS012", "IS Implementation Project", "Project", 100, 40, "MOD004"));

        // Business Assessments
        assessments.add(new Assessment("ASS013", "CRM Strategy Assignment", "Assignment", 100, 35, "MOD005"));
        assessments.add(new Assessment("ASS014", "Customer Service Test", "Test", 50, 30, "MOD005"));
        assessments.add(new Assessment("ASS015", "CRM Implementation Project", "Project", 100, 35, "MOD005"));

        assessments.add(new Assessment("ASS016", "Data Analysis Quiz", "Quiz", 20, 20, "MOD006"));
        assessments.add(new Assessment("ASS017", "Business Intelligence Test", "Test", 50, 30, "MOD006"));
        assessments.add(new Assessment("ASS018", "Analytics Case Study", "Project", 100, 50, "MOD006"));

        assessments.add(new Assessment("ASS019", "E-commerce Platform Assignment", "Assignment", 100, 30, "MOD007"));
        assessments.add(new Assessment("ASS020", "Digital Marketing Test", "Test", 50, 30, "MOD007"));
        assessments.add(new Assessment("ASS021", "E-commerce Website Project", "Project", 100, 40, "MOD007"));

        assessments.add(new Assessment("ASS022", "Economics Principles Assignment", "Assignment", 100, 30, "MOD008"));
        assessments.add(new Assessment("ASS023", "Market Analysis Test", "Test", 50, 30, "MOD008"));
        assessments.add(new Assessment("ASS024", "Business Economics Case Study", "Project", 100, 40, "MOD008"));

        // Engineering Assessments
        assessments.add(new Assessment("ASS025", "Robotics Fundamentals Assignment", "Assignment", 100, 30, "MOD009"));
        assessments.add(new Assessment("ASS026", "Automation Systems Test", "Test", 50, 30, "MOD009"));
        assessments.add(new Assessment("ASS027", "Robotics Design Project", "Project", 100, 40, "MOD009"));

        assessments.add(new Assessment("ASS028", "Engineering Principles Assignment", "Assignment", 100, 30, "MOD010"));
        assessments.add(new Assessment("ASS029", "Materials Science Test", "Test", 50, 30, "MOD010"));
        assessments.add(new Assessment("ASS030", "Engineering Science Project", "Project", 100, 40, "MOD010"));

        assessments.add(new Assessment("ASS031", "Calculus Assignment", "Assignment", 100, 30, "MOD011"));
        assessments.add(new Assessment("ASS032", "Linear Algebra Test", "Test", 50, 30, "MOD011"));
        assessments.add(new Assessment("ASS033", "Mathematics Final Exam", "Exam", 100, 40, "MOD011"));

        assessments.add(new Assessment("ASS034", "Renewable Energy Assignment", "Assignment", 100, 30, "MOD012"));
        assessments.add(new Assessment("ASS035", "Power Systems Test", "Test", 50, 30, "MOD012"));
        assessments.add(new Assessment("ASS036", "Energy Technologies Project", "Project", 100, 40, "MOD012"));
        System.out.println("✓ Created 36 assessments (3 per module)");

        // ============================================================
        // CREATE SAMPLE RESULTS
        // ============================================================

        // STU001 - Computer Science student
        createResult("RES001", "STU001", "ASS001", 85, "A", "Excellent Python skills. Clean code with proper documentation.");
        createResult("RES002", "STU001", "ASS002", 42, "B+", "Good understanding of data structures. Practice more on algorithms.");
        createResult("RES003", "STU001", "ASS004", 18, "A", "Perfect understanding of Java fundamentals.");
        createResult("RES004", "STU001", "ASS007", 90, "A+", "Outstanding SQL queries with excellent database design.");
        createResult("RES005", "STU001", "ASS008", 45, "A", "Excellent normalization and ER diagrams.");

        // STU002 - Computer Science student
        createResult("RES006", "STU002", "ASS001", 78, "B+", "Good Python programming. Improve on error handling.");
        createResult("RES007", "STU002", "ASS007", 88, "A", "Very good database implementation.");
        createResult("RES008", "STU002", "ASS008", 40, "B+", "Solid database knowledge. Review indexing concepts.");

        // STU003 - Mixed CS and Business student
        createResult("RES009", "STU003", "ASS007", 75, "B+", "Good SQL skills. Need more practice with complex queries.");
        createResult("RES010", "STU003", "ASS013", 82, "A", "Excellent CRM strategy with clear implementation plan.");
        createResult("RES011", "STU003", "ASS016", 16, "B+", "Good data analysis. Improve visualization techniques.");

        // STU006 - Business student
        createResult("RES012", "STU006", "ASS013", 88, "A", "Outstanding CRM strategy. Well-researched and practical.");
        createResult("RES013", "STU006", "ASS016", 17, "A", "Excellent analytics skills with insightful findings.");
        createResult("RES014", "STU006", "ASS019", 80, "A", "Good e-commerce platform design with modern features.");

        // STU010 - Engineering student
        createResult("RES015", "STU010", "ASS025", 92, "A+", "Exceptional robotics design with innovative automation solutions.");
        createResult("RES016", "STU010", "ASS028", 87, "A", "Excellent engineering principles application.");
        createResult("RES017", "STU010", "ASS031", 85, "A", "Very good mathematical skills with clear problem-solving.");
        createResult("RES018", "STU010", "ASS034", 90, "A+", "Outstanding energy systems analysis with sustainable solutions.");
        System.out.println("✓ Created 18 sample assessment results");

        // ============================================================
        // INITIALIZATION COMPLETE
        // ============================================================
        System.out.println("\n=== INITIALIZATION SUMMARY ===");
        System.out.println("Fields: 3 (Computer Science, Business, Engineering)");
        System.out.println("Users: " + users.size() + " (2 Admins, 3 Leaders, 12 Lecturers, 10 Students)");
        System.out.println("Modules: " + modules.size() + " (4 per field)");
        System.out.println("Classes: " + classes.size() + " (2 per module)");
        System.out.println("Assessments: " + assessments.size() + " (3 per module)");
        System.out.println("Results: " + results.size() + " sample results");
        System.out.println("===============================\n");
    }
}



import java.util.List;
import java.util.ArrayList;

class Lecturer extends User {
    private List<String> assignedModules;
    private String academicLeaderId;
    private String fieldId; // NEW: Field assignment

    public Lecturer(String userId, String username, String password, String fullName, String email) {
        super(userId, username, password, fullName, email, "LECTURER");
        this.assignedModules = new ArrayList<>();
    }

    @Override
    public String getAccessLevel() {
        return "LECTURER";
    }

    public List<String> getAssignedModules() { return assignedModules; }
    public void addModule(String moduleId) {
        if (!assignedModules.contains(moduleId)) {
            assignedModules.add(moduleId);
        }
    }
    public String getAcademicLeaderId() { return academicLeaderId; }
    public void setAcademicLeaderId(String academicLeaderId) { this.academicLeaderId = academicLeaderId; }

    // Field methods
    public String getFieldId() { return fieldId; }
    public void setFieldId(String fieldId) { this.fieldId = fieldId; }
}
import java.util.List;
import java.util.ArrayList;

class AcademicLeader extends User {
    private List<String> managedLecturers;
    private String fieldId; // NEW: Field they manage

    public AcademicLeader(String userId, String username, String password, String fullName, String email) {
        super(userId, username, password, fullName, email, "ACADEMIC_LEADER");
        this.managedLecturers = new ArrayList<>();
    }

    @Override
    public String getAccessLevel() {
        return "ACADEMIC_LEADER";
    }

    public List<String> getManagedLecturers() { return managedLecturers; }
    public void addLecturer(String lecturerId) {
        if (!managedLecturers.contains(lecturerId)) {
            managedLecturers.add(lecturerId);
        }
    }

    // Field methods
    public String getFieldId() { return fieldId; }
    public void setFieldId(String fieldId) { this.fieldId = fieldId; }
}
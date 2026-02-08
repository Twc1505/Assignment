import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

class Module implements Serializable {
    private String moduleId;
    private String moduleName;
    private String moduleCode;
    private String description;
    private String lecturerId;
    private String fieldId; // NEW: Field assignment
    private List<Assessment> assessments;

    public Module(String moduleId, String moduleName, String moduleCode, String description) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.moduleCode = moduleCode;
        this.description = description;
        this.assessments = new ArrayList<>();
    }

    // Getters
    public String getModuleId() { return moduleId; }
    public String getModuleName() { return moduleName; }
    public String getModuleCode() { return moduleCode; }
    public String getDescription() { return description; }
    public String getLecturerId() { return lecturerId; }
    public String getFieldId() { return fieldId; } // NEW
    public List<Assessment> getAssessments() { return assessments; }

    // Setters
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public void setDescription(String description) { this.description = description; }
    public void setLecturerId(String lecturerId) { this.lecturerId = lecturerId; }
    public void setFieldId(String fieldId) { this.fieldId = fieldId; } // NEW
    public void addAssessment(Assessment assessment) { assessments.add(assessment); }

    @Override
    public String toString() {
        return moduleId + "," + moduleName + "," + moduleCode + "," + description + "," +
                (lecturerId != null ? lecturerId : "") + "," +
                (fieldId != null ? fieldId : "");
    }
}
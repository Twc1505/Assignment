import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Field class - represents an academic field/department
 * Demonstrates: Encapsulation, Association
 */
class Field implements Serializable {
    private String fieldId;
    private String fieldName;
    private String academicLeaderId;
    private List<String> moduleIds;

    public Field(String fieldId, String fieldName) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.moduleIds = new ArrayList<>();
    }

    public String getFieldId() { return fieldId; }
    public String getFieldName() { return fieldName; }
    public String getAcademicLeaderId() { return academicLeaderId; }
    public List<String> getModuleIds() { return moduleIds; }

    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public void setAcademicLeaderId(String academicLeaderId) { this.academicLeaderId = academicLeaderId; }

    public void addModule(String moduleId) {
        if (!moduleIds.contains(moduleId)) {
            moduleIds.add(moduleId);
        }
    }

    public void removeModule(String moduleId) {
        moduleIds.remove(moduleId);
    }

    @Override
    public String toString() {
        return fieldId + "," + fieldName + "," +
                (academicLeaderId != null ? academicLeaderId : "") + "," +
                String.join(";", moduleIds);
    }
}
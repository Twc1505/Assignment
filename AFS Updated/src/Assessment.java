import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

class Assessment implements Serializable {
    private String assessmentId;
    private String assessmentName;
    private String assessmentType; // Assignment, Test, Quiz, etc.
    private double totalMarks;
    private double weightage;
    private String moduleId;

    public Assessment(String assessmentId, String assessmentName, String assessmentType,
                      double totalMarks, double weightage, String moduleId) {
        this.assessmentId = assessmentId;
        this.assessmentName = assessmentName;
        this.assessmentType = assessmentType;
        this.totalMarks = totalMarks;
        this.weightage = weightage;
        this.moduleId = moduleId;
    }

    public String getAssessmentId() { return assessmentId; }
    public String getAssessmentName() { return assessmentName; }
    public String getAssessmentType() { return assessmentType; }
    public double getTotalMarks() { return totalMarks; }
    public double getWeightage() { return weightage; }
    public String getModuleId() { return moduleId; }

    @Override
    public String toString() {
        return assessmentId + "," + assessmentName + "," + assessmentType + "," +
                totalMarks + "," + weightage + "," + moduleId;
    }
}

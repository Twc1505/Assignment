import java.io.Serializable;
import java.util.Date;

class AssessmentResult implements Serializable {
    private String resultId;
    private String studentId;
    private String assessmentId;
    private double marksObtained;
    private String feedback;
    private String grade;
    private Date submissionDate;

    public AssessmentResult(String resultId, String studentId, String assessmentId,
                            double marksObtained, String feedback) {
        this.resultId = resultId;
        this.studentId = studentId;
        this.assessmentId = assessmentId;
        this.marksObtained = marksObtained;
        this.feedback = feedback;
        this.submissionDate = new Date();
    }

    public String getResultId() { return resultId; }
    public String getStudentId() { return studentId; }
    public String getAssessmentId() { return assessmentId; }
    public double getMarksObtained() { return marksObtained; }
    public String getFeedback() { return feedback; }
    public String getGrade() { return grade; }
    public Date getSubmissionDate() { return submissionDate; }

    public void setMarksObtained(double marksObtained) { this.marksObtained = marksObtained; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public void setGrade(String grade) { this.grade = grade; }

    @Override
    public String toString() {
        return resultId + "," + studentId + "," + assessmentId + "," + marksObtained + "," +
                (grade != null ? grade : "") + "," + feedback;
    }
}

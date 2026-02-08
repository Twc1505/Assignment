import java.io.Serializable;
import java.util.Date;

/**
 * ModuleFeedback class - represents student feedback on modules and lecturers
 */
class ModuleFeedback implements Serializable {
    private String feedbackId;
    private String studentId;
    private String moduleId;
    private String lecturerId;

    // Rating scales (1-5)
    private int contentQualityRating;      // How good is the course content
    private int teachingEffectivenessRating; // How effective is the lecturer
    private int materialClarityRating;      // How clear are the materials
    private int responsivenesRating;        // How responsive is the lecturer
    private int overallRating;              // Overall satisfaction

    // Text feedback
    private String strengths;               // What went well
    private String improvements;            // What could be improved
    private String additionalComments;      // Any other comments

    private Date submissionDate;
    private boolean isAnonymous;

    public ModuleFeedback(String feedbackId, String studentId, String moduleId, String lecturerId) {
        this.feedbackId = feedbackId;
        this.studentId = studentId;
        this.moduleId = moduleId;
        this.lecturerId = lecturerId;
        this.submissionDate = new Date();
        this.isAnonymous = true; // Always anonymous when viewed by lecturers
    }

    // Getters
    public String getFeedbackId() { return feedbackId; }
    public String getStudentId() { return studentId; }
    public String getModuleId() { return moduleId; }
    public String getLecturerId() { return lecturerId; }
    public int getContentQualityRating() { return contentQualityRating; }
    public int getTeachingEffectivenessRating() { return teachingEffectivenessRating; }
    public int getMaterialClarityRating() { return materialClarityRating; }
    public int getResponsivenessRating() { return responsivenesRating; }
    public int getOverallRating() { return overallRating; }
    public String getStrengths() { return strengths; }
    public String getImprovements() { return improvements; }
    public String getAdditionalComments() { return additionalComments; }
    public Date getSubmissionDate() { return submissionDate; }
    public boolean isAnonymous() { return isAnonymous; }

    // Setters
    public void setContentQualityRating(int rating) { this.contentQualityRating = rating; }
    public void setTeachingEffectivenessRating(int rating) { this.teachingEffectivenessRating = rating; }
    public void setMaterialClarityRating(int rating) { this.materialClarityRating = rating; }
    public void setResponsivenessRating(int rating) { this.responsivenesRating = rating; }
    public void setOverallRating(int rating) { this.overallRating = rating; }
    public void setStrengths(String strengths) { this.strengths = strengths; }
    public void setImprovements(String improvements) { this.improvements = improvements; }
    public void setAdditionalComments(String additionalComments) { this.additionalComments = additionalComments; }

    // Calculate average rating
    public double getAverageRating() {
        return (contentQualityRating + teachingEffectivenessRating +
                materialClarityRating + responsivenesRating + overallRating) / 5.0;
    }

    @Override
    public String toString() {
        return feedbackId + "," + studentId + "," + moduleId + "," + lecturerId + "," +
                contentQualityRating + "," + teachingEffectivenessRating + "," +
                materialClarityRating + "," + responsivenesRating + "," + overallRating + "," +
                (strengths != null ? strengths.replace(",", "~~COMMA~~") : "") + "," +
                (improvements != null ? improvements.replace(",", "~~COMMA~~") : "") + "," +
                (additionalComments != null ? additionalComments.replace(",", "~~COMMA~~") : "") + "," +
                submissionDate.getTime();
    }
}
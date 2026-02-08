import java.util.Map;
import java.util.HashMap;

class GradingSystem {
    private static Map<String, GradeRange> gradeRanges = new HashMap<>();

    static class GradeRange {
        String grade;
        double minPercentage;
        double maxPercentage;
        String description;

        GradeRange(String grade, double minPercentage, double maxPercentage, String description) {
            this.grade = grade;
            this.minPercentage = minPercentage;
            this.maxPercentage = maxPercentage;
            this.description = description;
        }
    }

    static {
        // APU Default Grading System
        gradeRanges.put("A+", new GradeRange("A+", 90, 100, "Excellent"));
        gradeRanges.put("A", new GradeRange("A", 80, 89, "Very Good"));
        gradeRanges.put("B+", new GradeRange("B+", 75, 79, "Good"));
        gradeRanges.put("B", new GradeRange("B", 70, 74, "Above Average"));
        gradeRanges.put("C+", new GradeRange("C+", 65, 69, "Average"));
        gradeRanges.put("C", new GradeRange("C", 60, 64, "Pass"));
        gradeRanges.put("D", new GradeRange("D", 50, 59, "Marginal Pass"));
        gradeRanges.put("F", new GradeRange("F", 0, 49, "Fail"));
    }

    public static String calculateGrade(double percentage) {
        for (GradeRange range : gradeRanges.values()) {
            if (percentage >= range.minPercentage && percentage <= range.maxPercentage) {
                return range.grade;
            }
        }
        return "F";
    }

    public static Map<String, GradeRange> getGradeRanges() {
        return gradeRanges;
    }
}

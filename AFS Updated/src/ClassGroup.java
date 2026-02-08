import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

class ClassGroup implements Serializable {
    private String classId;
    private String className;
    private String moduleId;
    private List<String> studentIds;

    // Schedule information
    private String dayOfWeek;  // e.g., "Monday", "Tuesday"
    private String startTime;  // e.g., "09:00"
    private String endTime;    // e.g., "11:00"
    private String room;       // e.g., "Room 301"
    private int capacity;      // Maximum number of students

    public ClassGroup(String classId, String className, String moduleId) {
        this.classId = classId;
        this.className = className;
        this.moduleId = moduleId;
        this.studentIds = new ArrayList<>();
        this.capacity = 30; // Default capacity
    }

    // Constructor with schedule
    public ClassGroup(String classId, String className, String moduleId,
                      String dayOfWeek, String startTime, String endTime, String room, int capacity) {
        this.classId = classId;
        this.className = className;
        this.moduleId = moduleId;
        this.studentIds = new ArrayList<>();
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.capacity = capacity;
    }

    // Getters
    public String getClassId() { return classId; }
    public String getClassName() { return className; }
    public String getModuleId() { return moduleId; }
    public List<String> getStudentIds() { return studentIds; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }

    // Setters
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setRoom(String room) { this.room = room; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public void addStudent(String studentId) {
        if (!studentIds.contains(studentId) && studentIds.size() < capacity) {
            studentIds.add(studentId);
        }
    }

    public void removeStudent(String studentId) {
        studentIds.remove(studentId);
    }

    public boolean isFull() {
        return studentIds.size() >= capacity;
    }

    public int getAvailableSlots() {
        return capacity - studentIds.size();
    }

    public String getScheduleInfo() {
        if (dayOfWeek == null || startTime == null) {
            return "Schedule not set";
        }
        return dayOfWeek + " " + startTime + "-" + endTime + " (" + room + ")";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(classId).append(",");
        sb.append(className).append(",");
        sb.append(moduleId).append(",");
        sb.append(String.join(";", studentIds)).append(",");
        sb.append(dayOfWeek != null ? dayOfWeek : "").append(",");
        sb.append(startTime != null ? startTime : "").append(",");
        sb.append(endTime != null ? endTime : "").append(",");
        sb.append(room != null ? room : "").append(",");
        sb.append(capacity);
        return sb.toString();
    }
}
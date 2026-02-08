import java.util.List;
import java.util.ArrayList;

class Student extends User {
    private List<String> registeredClasses;

    public Student(String userId, String username, String password, String fullName, String email) {
        super(userId, username, password, fullName, email, "STUDENT");
        this.registeredClasses = new ArrayList<>();
    }

    @Override
    public String getAccessLevel() {
        return "STUDENT";
    }

    public List<String> getRegisteredClasses() { return registeredClasses; }
    public void registerClass(String classId) {
        if (!registeredClasses.contains(classId)) {
            registeredClasses.add(classId);
        }
    }
}
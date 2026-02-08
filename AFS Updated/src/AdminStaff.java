class AdminStaff extends User {
    public AdminStaff(String userId, String username, String password, String fullName, String email) {
        super(userId, username, password, fullName, email, "ADMIN");
    }

    @Override
    public String getAccessLevel() {
        return "ADMINISTRATOR";
    }
}
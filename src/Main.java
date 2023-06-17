import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.sql.*;

public class Main {
    private static AttendanceTracker attendanceTracker;
    private static Scanner scanner;
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";


    public static void main(String[] args) {
        attendanceTracker = new AttendanceTracker();
        scanner = new Scanner(System.in);

        System.out.println("Welcome to Attendance Tracker!");

        while (true) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Add student");
            System.out.println("2. Remove student");
            System.out.println("3. Update student");
            System.out.println("4. Mark attendance");
            System.out.println("5. Display attendance");
            System.out.println("6. Fetch Student data");
            System.out.println("7. Exit");

            System.out.print("Enter your choice: ");
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                switch (choice) {
                    case 1:
                        addStudent();
                        break;
                    case 2:
                        removeStudent();
                        break;
                    case 3:
                        updateStudent();
                        break;
                    case 4:
                        markAttendance();
                        break;
                    case 5:
                        displayAttendance();
                        break;
                    case 6:
                        System.out.println("Fetching data from the database...");
                        fetchStudentInfo();
                        break;
                    case 7:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a valid integer choice.");
                scanner.nextLine();
            }
        }
    }

    private static void fetchStudentInfo() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String SQL = "SELECT students.first_name, students.last_name, class.year, mgmt_teams.name, students.ispresent " +
                    "FROM students " +
                    "JOIN class ON students.class_id = class.class_id " +
                    "JOIN mgmt_teams ON students.mgmt_team_id = mgmt_teams.team_id";
            PreparedStatement statement = conn.prepareStatement(SQL);
            ResultSet rs = statement.executeQuery();
            System.out.println("Student Information:");
            System.out.println("--------------------");
            while (rs.next()) {
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String className = rs.getString("year");
                String teamName = rs.getString("name");
                boolean isPresent = rs.getBoolean("ispresent");

                System.out.println("Name: " + firstName + " " + lastName);
                System.out.println("Class: " + className);
                System.out.println("Management Team: " + teamName);
                System.out.println("Attendance: " + (isPresent ? "Present" : "Absent"));
                System.out.println("--------------------");
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static void addStudentToDatabase(String firstName, String lastName, Integer classYear, String mgmtTeamName, boolean isPresent) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String SQL_CLASS_ID = "SELECT class_id FROM \"public\".\"class\" WHERE year = ?";
            String SQL_MANAGEMENT_ID = "SELECT team_id FROM \"public\".\"mgmt_teams\" WHERE name = ?";
            PreparedStatement statementClassId = conn.prepareStatement(SQL_CLASS_ID);
            statementClassId.setInt(1, classYear);
            ResultSet resultClassId = statementClassId.executeQuery();
            int classId = 0;
            if (resultClassId.next()) {
                classId = resultClassId.getInt("class_id");
            }
            PreparedStatement statementMgmtId = conn.prepareStatement(SQL_MANAGEMENT_ID);
            statementMgmtId.setString(1, mgmtTeamName);
            ResultSet resultMgmtId = statementMgmtId.executeQuery();
            int mgmtTeamId = 0;
            if (resultMgmtId.next()) {
                mgmtTeamId = resultMgmtId.getInt("team_id");

            }

            String SQL = "INSERT INTO \"public\".\"students\" (first_name, last_name, class_id, mgmt_team_id, ispresent) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(SQL);
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setInt(3, classId);
            statement.setInt(4, mgmtTeamId);
            statement.setBoolean(5, isPresent);
            int affectedRows = statement.executeUpdate();
            System.out.println("Student added to the database successfully.");
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static void addStudent() {
        System.out.println("Enter the student's information:");
        System.out.print("First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();
        Integer classYear = classYearValidation(scanner);
        String mgmtTeamId = managementTeamValidation(scanner);
        System.out.print("Is Present (true/false): ");
        boolean isPresent = scanner.nextBoolean();

        addStudentToDatabase(firstName, lastName, classYear, mgmtTeamId, isPresent);
    }

    private static void removeStudentFromDatabase(String firstName, String lastName) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String SQL = "DELETE FROM \"public\".\"students\" WHERE first_name = ? AND last_name = ?";
            PreparedStatement statement = conn.prepareStatement(SQL);
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Student removed from the database successfully.");
            } else {
                System.out.println("Student doesn't exist.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static void removeStudent() {
        System.out.println("Enter the student's information:");
        System.out.print("First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();

        removeStudentFromDatabase(firstName, lastName);
    }

    private static void updateStudentInDatabase(String firstName, String lastName, String classId, String mgmtTeamId, boolean isPresent) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String SQL = "UPDATE \"public\".\"students\" SET class_id = ?, mgmt_team_id = ?, ispresent = ? WHERE first_name = ? AND last_name = ?";
            PreparedStatement statement = conn.prepareStatement(SQL);
            statement.setInt(1, Integer.parseInt(classId));
            statement.setInt(2, Integer.parseInt(mgmtTeamId));
            statement.setBoolean(3, isPresent);
            statement.setString(4, firstName);
            statement.setString(5, lastName);
            int affectedRows = statement.executeUpdate();
            System.out.println("Student updated in the database successfully. Affected rows: " + affectedRows);
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static void updateStudentInDatabase(String firstName, String lastName, boolean isPresent) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String SQL = "UPDATE \"public\".\"students\" SET ispresent = ? WHERE first_name = ? AND last_name = ?";
            PreparedStatement statement = conn.prepareStatement(SQL);
            statement.setBoolean(1, isPresent);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Student updated from the database successfully.");
            } else {
                System.out.println("Student doesn't exist.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static void updateStudent() {
        System.out.println("Enter the student's information:");
        System.out.print("First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();
        System.out.print("Is Present (true/false): ");
        boolean isPresent = scanner.nextBoolean();

        updateStudentInDatabase(firstName, lastName,  isPresent);
    }

    private static void markAttendance() {
        System.out.println("Enter the student's information:");
        System.out.print("First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();
        System.out.print("Is Present (true/false): ");
        String input = scanner.nextLine().toLowerCase();
        boolean isPresent = input.equals("true");

        updateStudentInDatabase(firstName, lastName, isPresent);
    }

    private static String managementTeamValidation(Scanner scanner) {
        ArrayList<String> mgmt_teams = new ArrayList<String>();
        String userResponse = "";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String SQL_QUERY = "SELECT name FROM \"public\".\"mgmt_teams\"";
            PreparedStatement statement = conn.prepareStatement(SQL_QUERY);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                mgmt_teams.add(result.getString("name"));
            }

            System.out.println("Management Team Name: ");
            userResponse = scanner.nextLine();
            while (!mgmt_teams.contains(userResponse)) {
                System.out.println("Not a valid Management Team.");
                System.out.println("The team choices are: ");
                for (int i = 0; i < mgmt_teams.size(); i ++) {
                    System.out.println(mgmt_teams.get(i));
                }
                System.out.println("Management Team Name: ");
                userResponse = scanner.nextLine();
            }


        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
        return userResponse;
    }

    private static Integer classYearValidation(Scanner scanner) {
        ArrayList<Integer> classYears = new ArrayList<Integer>();
        Integer userResponse = 0;
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String SQL_QUERY = "SELECT year FROM \"public\".\"class\"";
            PreparedStatement statement = conn.prepareStatement(SQL_QUERY);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                classYears.add(result.getInt("year"));
            }

            System.out.println("Class Year: ");
            try {
                String attempt = scanner.nextLine();
                userResponse = Integer.parseInt(attempt);
                while (userResponse > 1900 && userResponse < 9999) {
                    System.out.println("Invalid Response.");
                    System.out.println("Class Year: ");
                    attempt = scanner.nextLine();
                    userResponse = Integer.parseInt(attempt);
                }
            } catch (NumberFormatException e) {
                System.out.println("An error occurred" + e.getMessage());
            }
            if (!classYears.contains(userResponse)) {
                String SQL_QUERY2 = "INSERT INTO \"public\".\"class\" (year) VALUES (?)";
                PreparedStatement stmt = conn.prepareStatement(SQL_QUERY2);
                stmt.setInt(1, userResponse);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0){
                    System.out.println("Didn't work.");
                }
            }

        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
        return userResponse;
    }

    private static void displayAttendance() {
        System.out.println("Enter the student's information:");
        System.out.print("First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String SQL = "SELECT ispresent FROM \"public\".\"students\" WHERE first_name = ? AND last_name = ?";
            PreparedStatement statement = conn.prepareStatement(SQL);
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                boolean isPresent = result.getBoolean("ispresent");
                System.out.println("Attendance for " + firstName + " " + lastName + ": " + (isPresent ? "Present" : "Absent"));
            } else {
                System.out.println("Student not found in the database.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}




class AttendanceTracker {
    private Map<String, Student> students;

    public AttendanceTracker() {
        students = new LinkedHashMap<>();
    }



    class Student {
        private String firstName;
        private String lastName;
        private String classYear;
        private String mgmtTeam;
        private boolean isPresent;

        public Student(String firstName, String lastName, String classYear, String mgmtTeam) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.classYear = classYear;
            this.mgmtTeam = mgmtTeam;
            this.isPresent = false;
        }

    }
}
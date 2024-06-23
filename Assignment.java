// https://github.com/jeel8003/Exam-Management-System.git

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

abstract class Question {
    protected String text;
    protected String answer;

    public Question(String text, String answer) {
        this.text = text;
        this.answer = answer;
    }

    public abstract boolean checkAnswer(String userAnswer);

    public String getText() {
        return text;
    }

    public String getAnswer() {
        return answer;
    }

    public abstract String toFormattedString();
}

class MCQQuestion extends Question {
    private List<String> choices;

    public MCQQuestion(String text, List<String> choices, String answer) {
        super(text, answer);
        this.choices = choices;
    }

    @Override
    public boolean checkAnswer(String userAnswer) {
        return userAnswer.equalsIgnoreCase(answer);
    }

    public List<String> getChoices() {
        return choices;
    }

    @Override
    public String toFormattedString() {
        StringBuilder formatted = new StringBuilder();
        formatted.append("MCQ\n");
        formatted.append(text).append("\n");
        for (String choice : choices) {
            formatted.append(choice).append("\n");
        }
        formatted.append(answer).append("\n");
        return formatted.toString();
    }
}

class TrueFalseQuestion extends Question {
    public TrueFalseQuestion(String text, String answer) {
        super(text, answer);
    }

    @Override
    public boolean checkAnswer(String userAnswer) {
        return userAnswer.equalsIgnoreCase(answer);
    }

    @Override
    public String toFormattedString() {
        return "TrueFalse\n" + text + "\n" + answer + "\n";
    }
}

interface Quiz {
    void addQuestion(Question question);

    void start();

    void gradeQuiz();
}

class Student {
    private String username;
    private String password;
    private int score;

    public Student(String username, String password) {
        this.username = username;
        this.password = password;
        this.score = 0;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getScore() {
        return score;
    }

    public void increaseScore(int points) {
        score += points;
    }

    public void saveStudentDataToFile(String file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            writer.println("Username: " + username);
            writer.println("Password: " + password);
            writer.println("Score: " + score);
            writer.println();
            System.out.println("Student data saved to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Student> loadStudentDataFromFile(String file) {
        ArrayList<Student> students = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String username = null;
            String password = null;
            int score = 0;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Username: ")) {
                    username = line.substring(10);
                } else if (line.startsWith("Password: ")) {
                    password = line.substring(10);
                } else if (line.startsWith("Score: ")) {
                    score = Integer.parseInt(line.substring(7));
                } else if (line.isEmpty()) {
                    if (username != null && password != null) {
                        Student student = new Student(username, password);
                        student.score = score;
                        students.add(student);
                    }
                    username = null;
                    password = null;
                    score = 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return students;
    }
}

class Admin {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";
    private static final String QUESTIONS_FILE = "questions.txt";

    private List<Question> questions = new ArrayList<>();

    public Admin() {
        loadQuestionsFromFile();
    }

    private void loadQuestionsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(QUESTIONS_FILE))) {
            String line;
            String text = null;
            String answer = null;
            List<String> choices = new ArrayList<>();
            boolean isMCQ = false;

            while ((line = reader.readLine()) != null) {
                if (line.equals("MCQ")) {
                    text = reader.readLine();
                    choices.clear();
                    String choice;
                    while ((choice = reader.readLine()) != null && !choice.isEmpty()) {
                        choices.add(choice);
                    }
                    if ((answer = reader.readLine()) == null) {
                        break;
                    }
                    isMCQ = true;
                } else if (line.equals("TrueFalse")) {
                    text = reader.readLine();
                    if ((answer = reader.readLine()) == null) {
                        break;
                    }
                    isMCQ = false;
                }

                if (text != null && answer != null) {
                    if (isMCQ) {
                        MCQQuestion mcqQuestion = new MCQQuestion(text, choices, answer);
                        questions.add(mcqQuestion);
                    } else {
                        TrueFalseQuestion trueFalseQuestion = new TrueFalseQuestion(text, answer);
                        questions.add(trueFalseQuestion);
                    }
                    text = null;
                    answer = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveQuestionsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(QUESTIONS_FILE))) {
            for (Question question : questions) {
                writer.print(question.toFormattedString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addQuestion(Question question) {
        questions.add(question);
        saveQuestionsToFile();
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
        saveQuestionsToFile();
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public static boolean authenticateAdmin(String username, String password) {
        return ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password);
    }
}

class QuizManager implements Quiz {
    private Student student;
    private List<Question> questions = new ArrayList<>();
    private int score = 0;

    public QuizManager(Student student, List<Question> questions) {
        this.student = student;
        this.questions.addAll(questions);
    }

    @Override
    public void addQuestion(Question question) {
        questions.add(question);
    }

    @Override
    public void start() {
        Scanner scanner = new Scanner(System.in);
        for (Question question : questions) {
            System.out.println(question.getText());

            if (question instanceof MCQQuestion) {
                MCQQuestion mcqQuestion = (MCQQuestion) question;
                List<String> choices = mcqQuestion.getChoices();
                for (int i = 0; i < choices.size(); i++) {
                    System.out.println((char) ('A' + i) + ". " + choices.get(i));
                }
            }

            System.out.print("Your answer: ");
            String userAnswer = scanner.nextLine();
            if (question.checkAnswer(userAnswer)) {
                System.out.println("Correct!");
                score++;
            } else {
                System.out.println("Incorrect. The correct answer is: " + question.getAnswer());
            }
        }
    }

    @Override
    public void gradeQuiz() {
        System.out.println("Quiz Complete!");
        System.out.println("Your Score: " + score + "/" + questions.size());
    }
}

public class Assignment {
    public static void main(String[] args) {
        ArrayList<Student> students = Student.loadStudentDataFromFile("students.txt");
        Admin admin = new Admin();
        System.out.println("Welcome to the Quiz Management System");

        while (true) {
            System.out.println("Select your role:");
            System.out.println("1. Student Signup");
            System.out.println("2. Student Login");
            System.out.println("3. Admin");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter your name: ");
                    String studentName = scanner.nextLine();
                    System.out.print("Enter a password: ");
                    String password = scanner.nextLine();
                    Student student = new Student(studentName, password);
                    students.add(student);
                    student.saveStudentDataToFile("students.txt");
                    System.out.println("Student " + studentName + " signed up successfully.");
                    break;

                case 2:
                    System.out.print("Enter your name: ");
                    String loginName = scanner.nextLine();
                    System.out.print("Enter your password: ");
                    String loginPassword = scanner.nextLine();
                    boolean found = false;
                    Student student1 = null;

                    for (Student s : students) {
                        if (s.getUsername().equals(loginName) && s.getPassword().equals(loginPassword)) {
                            found = true;
                            student1 = s;
                            break;
                        }
                    }

                    if (found) {
                        QuizManager quizManager = new QuizManager(student1, admin.getQuestions());
                        quizManager.start();
                        quizManager.gradeQuiz();
                        System.out.println("Thank you, " + loginName + ", for taking the quiz!");
                    } else {
                        System.out.println("Invalid username or password. Please check your credentials.");
                    }
                    break;

                case 3:
                    System.out.print("Admin Username: ");
                    String adminUsername = scanner.nextLine();
                    System.out.print("Admin Password: ");
                    String adminPassword = scanner.nextLine();

                    if (Admin.authenticateAdmin(adminUsername, adminPassword)) {
                        adminOptions(admin);
                    } else {
                        System.out.println("Admin login failed. Please check your credentials.");
                    }
                    break;

                case 4:
                    System.out.println("Exiting the Quiz Management System.");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void adminOptions(Admin admin) {
        while (true) {
            System.out.println("\nAdmin Options:");
            System.out.println("1. Add Question");
            System.out.println("2. Remove Question");
            System.out.println("3. List Questions");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");

            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter the question text: ");
                    String questionText = scanner.nextLine();

                    System.out.print("Enter the answer: ");
                    String answer = scanner.nextLine();

                    System.out.print("Is it a Multiple Choice Question? (true/false): ");
                    boolean isMCQ = scanner.nextBoolean();

                    if (isMCQ) {
                        List<String> choices = new ArrayList<>();
                        scanner.nextLine();
                        System.out.print("Enter the choices (comma-separated): ");
                        String choicesStr = scanner.nextLine();
                        String[] choiceArray = choicesStr.split(",");
                        for (String choicea : choiceArray) {
                            choices.add(choicea);
                        }
                        MCQQuestion mcqQuestion = new MCQQuestion(questionText, choices, answer);
                        admin.addQuestion(mcqQuestion);
                    } else {
                        TrueFalseQuestion trueFalseQuestion = new TrueFalseQuestion(questionText, answer);
                        admin.addQuestion(trueFalseQuestion);
                    }
                    System.out.println("Question added successfully!");
                    break;

                case 2:
                    System.out.println("Select a question to remove:");
                    List<Question> questions = admin.getQuestions();
                    for (int i = 0; i < questions.size(); i++) {
                        Question question = questions.get(i);
                        String questionType = (question instanceof MCQQuestion) ? "MCQ" : "TrueFalse";
                        System.out.println((i + 1) + ". [" + questionType + "] " + question.getText());
                    }
                    int removeChoice = scanner.nextInt();
                    if (removeChoice >= 1 && removeChoice <= questions.size()) {
                        Question removedQuestion = questions.get(removeChoice - 1);
                        admin.removeQuestion(removedQuestion);
                        System.out.println("Question removed successfully!");
                    } else {
                        System.out.println("Invalid choice. Please try again.");
                    }
                    break;

                case 3:
                    List<Question> listedQuestions = admin.getQuestions();
                    System.out.println("\nList of Questions:");
                    for (int i = 0; i < listedQuestions.size(); i++) {
                        Question q = listedQuestions.get(i);
                        String questionType = (q instanceof MCQQuestion) ? "MCQ" : "TrueFalse";
                        System.out.println((i + 1) + ". [" + questionType + "] " + q.getText() + " (Answer: " + q.getAnswer() + ")");
                    }
                    break;

                case 4:
                    System.out.println("Logging out as admin.");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
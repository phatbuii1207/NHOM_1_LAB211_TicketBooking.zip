package view;

import controller.LoginController;
import controller.LoginController.LoginResult;

import java.util.Optional;
import java.util.Scanner;

public class LoginView {

    private final LoginController loginController;
    private final Scanner scanner;
    private boolean isLoggedIn;
    private String currentUserId;
    private boolean isAdmin;

    public LoginView() {
        this.loginController = new LoginController();
        this.scanner = new Scanner(System.in);
        this.isLoggedIn = false;
    }

    public void displayLoginScreen() {
        System.out.println("\n========================================");
        System.out.println("        USER LOGIN SYSTEM");
        System.out.println("========================================");

        System.out.print("Enter User ID (or type 'exit' to quit): ");
        String userId = scanner.nextLine();

        if (userId.equalsIgnoreCase("exit")) {
            isLoggedIn = false;
            System.out.println("Thank you for using the system!");
            return;
        }

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        login(userId, password);
    }

    private void login(String id, String password) {
        LoginResult result = loginController.login(id, password);

        if (!result.isSuccess()) {
            System.out.println("Error: " + result.getMessage());
            return;
        }

        isLoggedIn = true;
        currentUserId = result.getFan().getId();
        isAdmin = result.isAdmin();

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + result.getFan().getName() + "!");
        System.out.println("Role: " + (isAdmin ? "ADMIN" : "USER"));
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void close() {
        scanner.close();
    }
}

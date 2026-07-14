package controller;

import model.Fan;
import repository.FanRepository;
import java.util.Optional;

public class LoginController {
    private final FanRepository fanRepository;

    public LoginController() {
        this.fanRepository = new FanRepository();
    }

    public LoginController(FanRepository fanRepo) {
        this.fanRepository = fanRepo;
    }

    public LoginResult login(String id, String password) {
        if (id == null || id.isBlank()) {
            return LoginResult.fail("ID cannot be empty.");
        }
        if (password == null || password.isBlank()) {
            return LoginResult.fail("Password cannot be empty.");
        }

        String trimmedId = id.trim();
        boolean isAdmin = checkAdminPermission(trimmedId);

        // Find the user in FanRepository (acting as the unified User database for this
        // simulation)
        Optional<Fan> fanOpt = fanRepository.findById(trimmedId);
        if (fanOpt.isEmpty()) {
            return LoginResult.fail("User ID does not exist.");
        }

        Fan fan = fanOpt.get();

        // Verify password
        if (!verifyPassword(password, fan.getPasswordHash())) {
            return LoginResult.fail("Incorrect password.");
        }

        return LoginResult.success(fan, isAdmin);
    }

    /**
     * Verifies if ID starts with "AD" (case-insensitive) for admin check.
     *
     * @param id The ID to check
     * @return true if ID starts with "AD"
     */
    public boolean checkAdminPermission(String id) {
        return id != null && id.trim().toUpperCase().startsWith("AD");
    }

    private boolean verifyPassword(String enteredPassword, String storedHash) {
        // Simulated hash check for standard LAB211 test data
        if ("$2a$10$FAKEHASHFORLAB211PURPOSEONLYXXXXXX".equals(storedHash)) {
            return "password123".equals(enteredPassword);
        }
        return enteredPassword.equals(storedHash);
    }

    /*
     * public void updateFan(String fanId){
     * Optional<Fan> found = fanRepository.findById(fanId);
     * if(found.isEmpty()){
     * return;
     * }
     * 
     * }
     */

    // ================================================================
    // INNER CLASS: Login Result Wrapper
    // ================================================================
    public static class LoginResult {
        private final boolean success;
        private final Fan fan;
        private final boolean isAdmin;
        private final String message;

        private LoginResult(boolean success, Fan fan, boolean isAdmin, String message) {
            this.success = success;
            this.fan = fan;
            this.isAdmin = isAdmin;
            this.message = message;
        }

        public static LoginResult success(Fan fan, boolean isAdmin) {
            return new LoginResult(true, fan, isAdmin, "Login successful.");
        }

        public static LoginResult fail(String reason) {
            return new LoginResult(false, null, false, reason);
        }

        public boolean isSuccess() {
            return success;
        }

        public Fan getFan() {
            return fan;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public String getMessage() {
            return message;
        }
    }
}
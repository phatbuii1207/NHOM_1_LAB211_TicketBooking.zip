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

    /**
     * Updates fan data with authorization checks.
     * Admin can update any fan by ID.
     * Fan can only update their own profile (operatorId must equal targetFanId).
     *
     * @param operatorId  ID of the user performing the change
     * @param targetFanId ID of the fan to be modified
     * @param newName     New name (ignored if null/blank)
     * @param newEmail    New email (ignored if null/blank)
     * @param newPhone    New phone (ignored if null/blank)
     * @param newPassword New password (ignored if null/blank)
     * @return boolean true if update was successful
     */
    public boolean updateFanData(String operatorId, String targetFanId, String newName, String newEmail,
            String newPhone, String newPassword) {
        if (operatorId == null || operatorId.isBlank() || targetFanId == null || targetFanId.isBlank()) {
            System.out.println("  [!] Operator ID and Target Fan ID cannot be empty.");
            return false;
        }

        boolean operatorIsAdmin = checkAdminPermission(operatorId);
        if (!operatorIsAdmin && !operatorId.trim().equalsIgnoreCase(targetFanId.trim())) {
            System.out.println("  [!] Permission denied. Fans can only change their own data.");
            return false;
        }

        Optional<Fan> targetFanOpt = fanRepository.findById(targetFanId.trim());
        if (targetFanOpt.isEmpty()) {
            System.out.println("  [!] Fan not found with ID: " + targetFanId);
            return false;
        }

        Fan fan = targetFanOpt.get();
        boolean modified = false;

        if (newName != null && !newName.isBlank()) {
            fan.setName(newName.trim());
            modified = true;
        }
        if (newEmail != null && !newEmail.isBlank()) {
            fan.setEmail(newEmail.trim());
            modified = true;
        }
        if (newPhone != null && !newPhone.isBlank()) {
            fan.setPhone(newPhone.trim());
            modified = true;
        }
        if (newPassword != null && !newPassword.isBlank()) {
            fan.setPasswordHash(newPassword.trim());
            modified = true;
        }

        if (modified) {
            return fanRepository.save(fan);
        }

        return false;
    }

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
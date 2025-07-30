package pl.where2play.w2papi.service;

import pl.where2play.w2papi.model.User;

/**
 * Service for Multi-Factor Authentication (MFA) operations.
 */
public interface MfaService {

    /**
     * Generate a new MFA secret for a user.
     *
     * @param user the user
     * @return the generated secret
     */
    String generateSecret(User user);

    /**
     * Enable MFA for a user.
     *
     * @param user the user
     * @param secret the MFA secret
     * @return the updated user
     */
    User enableMfa(User user, String secret);

    /**
     * Disable MFA for a user.
     *
     * @param user the user
     * @return the updated user
     */
    User disableMfa(User user);

    /**
     * Verify an MFA code.
     *
     * @param user the user
     * @param code the code to verify
     * @return true if the code is valid, false otherwise
     */
    boolean verifyCode(User user, String code);

    /**
     * Generate a QR code for MFA setup.
     *
     * @param user the user
     * @param secret the MFA secret
     * @return the QR code as a base64-encoded string
     */
    String generateQrCode(User user, String secret);
}
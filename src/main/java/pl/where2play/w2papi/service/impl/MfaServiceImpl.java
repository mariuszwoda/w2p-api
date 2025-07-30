package pl.where2play.w2papi.service.impl;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.where2play.w2papi.model.User;
import pl.where2play.w2papi.repository.UserRepository;
import pl.where2play.w2papi.service.MfaService;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

/**
 * Implementation of the MfaService interface.
 * This service provides methods for Multi-Factor Authentication (MFA) operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MfaServiceImpl implements MfaService {

    private final UserRepository userRepository;
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(
            new DefaultCodeGenerator(),
            timeProvider
    );

    @Override
    @Transactional
    public String generateSecret(User user) {
        log.info("Generating MFA secret for user: {}", user.getEmail());
        return secretGenerator.generate();
    }

    @Override
    @Transactional
    public User enableMfa(User user, String secret) {
        log.info("Enabling MFA for user: {}", user.getEmail());
        user.setMfaEnabled(true);
        user.setMfaSecret(secret);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User disableMfa(User user) {
        log.info("Disabling MFA for user: {}", user.getEmail());
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        return userRepository.save(user);
    }

    @Override
    public boolean verifyCode(User user, String code) {
        log.info("Verifying MFA code for user: {}", user.getEmail());
        if (!user.isMfaEnabled() || user.getMfaSecret() == null) {
            log.warn("MFA is not enabled for user: {}", user.getEmail());
            return false;
        }
        return codeVerifier.isValidCode(user.getMfaSecret(), code);
    }

    @Override
    public String generateQrCode(User user, String secret) {
        log.info("Generating QR code for MFA setup for user: {}", user.getEmail());
        QrData data = new QrData.Builder()
                .label(user.getEmail())
                .secret(secret)
                .issuer("W2P Calendar")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        try {
            byte[] qrCodeImage = qrGenerator.generate(data);
            return getDataUriForImage(qrCodeImage, qrGenerator.getImageMimeType());
        } catch (QrGenerationException e) {
            log.error("Error generating QR code for user: {}", user.getEmail(), e);
            throw new RuntimeException("Error generating QR code", e);
        }
    }
}
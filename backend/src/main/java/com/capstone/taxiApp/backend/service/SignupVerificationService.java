package com.capstone.taxiApp.backend.service;

import com.capstone.taxiApp.backend.dto.SendSignupVerificationCodeRequest;
import com.capstone.taxiApp.backend.dto.SignupVerificationResponse;
import com.capstone.taxiApp.backend.dto.VerifySignupCodeRequest;
import com.capstone.taxiApp.backend.entity.SignupEmailVerification;
import com.capstone.taxiApp.backend.entity.University;
import com.capstone.taxiApp.backend.repository.SignupEmailVerificationRepository;
import com.capstone.taxiApp.backend.repository.UniversityRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Random;

@Service
public class SignupVerificationService {

    private static final int VERIFIED_FALSE = 0;
    private static final int VERIFIED_TRUE = 1;
    private static final String PURPOSE_SIGN_UP = "SIGN_UP";
    private static final int CODE_EXPIRE_MINUTES = 10;

    private final SignupEmailVerificationRepository signupEmailVerificationRepository;
    private final UniversityRepository universityRepository;
    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from:${APP_MAIL_FROM:}}")
    private String mailFromAddress;

    public SignupVerificationService(
            SignupEmailVerificationRepository signupEmailVerificationRepository,
            UniversityRepository universityRepository,
            JavaMailSender javaMailSender
    ) {
        this.signupEmailVerificationRepository = signupEmailVerificationRepository;
        this.universityRepository = universityRepository;
        this.javaMailSender = javaMailSender;
    }

    @Transactional
    public SignupVerificationResponse sendSignupVerificationCode(SendSignupVerificationCodeRequest request) {
        String normalizedStudentId = normalizeText(request.studentId());
        String normalizedEmail = normalizeEmail(request.email());
        validateSchoolEmail(normalizedEmail);

        String verificationCode = generateVerificationCode();
        LocalDateTime now = LocalDateTime.now();

        SignupEmailVerification verification = new SignupEmailVerification();
        verification.setStudentId(normalizedStudentId);
        verification.setEmail(normalizedEmail);
        verification.setVerificationCode(verificationCode);
        verification.setVerificationPurpose(PURPOSE_SIGN_UP);
        verification.setVerified(VERIFIED_FALSE);
        verification.setCreatedAt(now);
        verification.setExpiresAt(now.plusMinutes(CODE_EXPIRE_MINUTES));
        signupEmailVerificationRepository.save(verification);

        sendVerificationEmail(normalizedEmail, verificationCode);
        return new SignupVerificationResponse(false, "인증코드를 발송했습니다.");
    }

    @Transactional
    public SignupVerificationResponse verifySignupCode(VerifySignupCodeRequest request) {
        String normalizedStudentId = normalizeText(request.studentId());
        String normalizedEmail = normalizeEmail(request.email());
        validateSchoolEmail(normalizedEmail);

        SignupEmailVerification verification = signupEmailVerificationRepository
                .findFirstByStudentIdAndEmailOrderByCreatedAtDesc(normalizedStudentId, normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("발급된 인증코드를 찾을 수 없습니다."));

        if (verification.getVerified() == VERIFIED_TRUE) {
            return new SignupVerificationResponse(true, "이미 인증이 완료되었습니다.");
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증코드가 만료되었습니다. 다시 요청해 주세요.");
        }

        if (!verification.getVerificationCode().equals(request.verificationCode().trim())) {
            throw new IllegalArgumentException("인증코드가 일치하지 않습니다.");
        }

        verification.setVerified(VERIFIED_TRUE);
        verification.setVerifiedAt(LocalDateTime.now());
        signupEmailVerificationRepository.save(verification);

        return new SignupVerificationResponse(true, "학생 메일 인증이 완료되었습니다.");
    }

    private void validateSchoolEmail(String email) {
        String domain = extractEmailDomain(email);
        universityRepository.findBySchoolEmailDomainIgnoreCase(domain)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 학교 이메일 도메인입니다."));
    }

    private void sendVerificationEmail(String recipientEmail, String verificationCode) {
        if (mailFromAddress == null || mailFromAddress.isBlank()) {
            throw new IllegalStateException("APP_MAIL_FROM 또는 app.mail.from 설정이 필요합니다.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFromAddress);
        message.setTo(recipientEmail);
        message.setSubject("[SU TAXI] 학생 인증 코드");
        message.setText("인증코드는 [" + verificationCode + "] 입니다. 10분 이내에 입력해 주세요.");
        javaMailSender.send(message);
    }

    private String generateVerificationCode() {
        return String.format(Locale.getDefault(), "%06d", new Random().nextInt(1_000_000));
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String extractEmailDomain(String email) {
        int separatorIndex = email.indexOf('@');
        if (separatorIndex < 0 || separatorIndex == email.length() - 1) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
        return email.substring(separatorIndex + 1);
    }
}

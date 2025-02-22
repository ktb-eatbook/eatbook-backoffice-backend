package com.eatbook.backoffice.domain.member.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailVerificationService {

    private final JavaMailSender mailSender;
    // 이메일과 인증번호를 저장하는 메모리 저장소
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    @Autowired
    public EmailVerificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 해당 이메일로 인증번호를 생성하여 전송합니다.
     * @param email 인증할 이메일 주소
     */
    public void sendVerificationCode(String email) {
        // 이메일 정규화: 앞뒤 공백 제거 및 소문자 변환
        String normalizedEmail = email.trim().toLowerCase();
        String code = generateRandomCode();
        verificationCodes.put(normalizedEmail, code);
        log.info("저장된 인증코드: {} → {}", normalizedEmail, code);
        sendEmail(normalizedEmail, code);
    }

    /**
     * 이메일로 전송한 인증번호와 입력받은 코드가 일치하는지 검증합니다.
     * 검증 성공 시 해당 이메일의 인증번호는 삭제됩니다.
     * @param email 이메일 주소
     * @param code  사용자가 입력한 인증번호
     * @return 인증 성공 여부
     */
    public boolean verifyCode(String email, String code) {
        String normalizedEmail = email.trim().toLowerCase();
        if (verificationCodes.containsKey(normalizedEmail)) {
            if (verificationCodes.get(normalizedEmail).equals(code)) {
                verificationCodes.remove(normalizedEmail);
                return true;
            } else {
                log.info(normalizedEmail + "로 전송된 인증번호 " + code
                        + "가 " + verificationCodes.get(normalizedEmail) + "와 일치하지 않습니다.");
            }
        } else {
            log.info(normalizedEmail + "로 전송된 인증번호가 존재하지 않습니다.");
            return false;
        }
        return false;
    }

    // 6자리 난수 생성 (100000 ~ 999999)
    private String generateRandomCode() {
        int randomNumber = new Random().nextInt(900000) + 100000;
        return String.valueOf(randomNumber);
    }

    // 간단한 텍스트 이메일 전송
    private void sendEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("이메일 인증번호 안내");
        message.setText("안녕하세요.\n인증번호는 " + code + " 입니다.\n입력 후 인증을 완료해주세요.");
        mailSender.send(message);
    }
}
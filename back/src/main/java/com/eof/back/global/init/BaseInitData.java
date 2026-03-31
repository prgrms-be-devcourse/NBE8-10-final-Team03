package com.eof.back.global.init;

import com.eof.back.domain.quiz.entity.Quiz;
import com.eof.back.domain.quiz.repository.QuizRepository;
import com.eof.back.domain.quizset.entity.QuizSet;
import com.eof.back.domain.quizset.repository.QuizSetRepository;
import com.eof.back.domain.user.user.entity.AuthProvider;
import com.eof.back.domain.user.user.entity.Role;
import com.eof.back.domain.user.user.entity.User;
import com.eof.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class BaseInitData implements ApplicationRunner {

    private final UserRepository userRepository;
    private final QuizSetRepository quizSetRepository;
    private final QuizRepository quizRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("이미 초기 데이터가 존재합니다. 스킵합니다.");
            return;
        }

        log.info("초기 데이터 생성 시작...");

        // ===== 유저 생성 =====
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin1234"))
                .nickname("관리자")
                .role(Role.ADMIN)
                .provider(AuthProvider.LOCAL)  // ← 추가
                .build();
        userRepository.save(admin);

        User testUser = User.of("testuser", passwordEncoder.encode("test1234"), "테스트맨");
        userRepository.save(testUser);

        // ===== 퀴즈셋 1: 세계 수도 =====
        QuizSet capitalQuizSet = QuizSet.of("세계 수도 퀴즈", "나라의 수도를 맞춰보세요!", admin);
        quizSetRepository.save(capitalQuizSet);

        saveQuiz(capitalQuizSet, "대한민국의 수도는?", "서울", "서울", "부산", "대구", "인천");
        saveQuiz(capitalQuizSet, "일본의 수도는?", "도쿄", "도쿄", "오사카", "교토", "나고야");
        saveQuiz(capitalQuizSet, "미국의 수도는?", "워싱턴 D.C.", "뉴욕", "워싱턴 D.C.", "로스앤젤레스", "시카고");
        saveQuiz(capitalQuizSet, "프랑스의 수도는?", "파리", "파리", "마르세유", "리옹", "니스");
        saveQuiz(capitalQuizSet, "영국의 수도는?", "런던", "런던", "맨체스터", "버밍엄", "리버풀");
        saveQuiz(capitalQuizSet, "독일의 수도는?", "베를린", "베를린", "뮌헨", "함부르크", "프랑크푸르트");
        saveQuiz(capitalQuizSet, "중국의 수도는?", "베이징", "베이징", "상하이", "광저우", "선전");
        saveQuiz(capitalQuizSet, "호주의 수도는?", "캔버라", "시드니", "멜버른", "캔버라", "브리즈번");
        saveQuiz(capitalQuizSet, "브라질의 수도는?", "브라질리아", "상파울루", "리우데자네이루", "브라질리아", "살바도르");
        saveQuiz(capitalQuizSet, "이탈리아의 수도는?", "로마", "로마", "밀라노", "나폴리", "피렌체");

        // ===== 퀴즈셋 2: 사칙연산 =====
        QuizSet mathQuizSet = QuizSet.of("사칙연산 퀴즈", "기본 사칙연산 문제들입니다!", admin);
        quizSetRepository.save(mathQuizSet);

        saveQuiz(mathQuizSet, "15 + 27 = ?", "42", "40", "41", "42", "43");
        saveQuiz(mathQuizSet, "100 - 37 = ?", "63", "53", "63", "73", "83");
        saveQuiz(mathQuizSet, "8 × 9 = ?", "72", "63", "72", "81", "56");
        saveQuiz(mathQuizSet, "144 ÷ 12 = ?", "12", "10", "11", "12", "13");
        saveQuiz(mathQuizSet, "25 + 38 = ?", "63", "61", "62", "63", "64");
        saveQuiz(mathQuizSet, "200 - 85 = ?", "115", "105", "110", "115", "120");
        saveQuiz(mathQuizSet, "7 × 8 = ?", "56", "48", "54", "56", "63");
        saveQuiz(mathQuizSet, "81 ÷ 9 = ?", "9", "7", "8", "9", "10");
        saveQuiz(mathQuizSet, "45 + 55 = ?", "100", "90", "95", "100", "105");
        saveQuiz(mathQuizSet, "6 × 7 = ?", "42", "36", "40", "42", "48");

        // ===== 퀴즈셋 3: 쉬운 영단어 =====
        QuizSet englishQuizSet = QuizSet.of("쉬운 영단어 퀴즈", "기초 영단어를 맞춰보세요!", admin);
        quizSetRepository.save(englishQuizSet);

        saveQuiz(englishQuizSet, "'사과'를 영어로 하면?", "Apple", "Apple", "Banana", "Orange", "Grape");
        saveQuiz(englishQuizSet, "'고양이'를 영어로 하면?", "Cat", "Dog", "Cat", "Bird", "Fish");
        saveQuiz(englishQuizSet, "'학교'를 영어로 하면?", "School", "School", "Hospital", "Library", "Market");
        saveQuiz(englishQuizSet, "'행복한'을 영어로 하면?", "Happy", "Sad", "Angry", "Happy", "Tired");
        saveQuiz(englishQuizSet, "'물'을 영어로 하면?", "Water", "Water", "Fire", "Earth", "Wind");
        saveQuiz(englishQuizSet, "'책'을 영어로 하면?", "Book", "Pen", "Book", "Desk", "Chair");
        saveQuiz(englishQuizSet, "'빨간색'을 영어로 하면?", "Red", "Blue", "Red", "Green", "Yellow");
        saveQuiz(englishQuizSet, "'친구'를 영어로 하면?", "Friend", "Family", "Enemy", "Friend", "Teacher");
        saveQuiz(englishQuizSet, "'태양'을 영어로 하면?", "Sun", "Moon", "Star", "Sun", "Cloud");
        saveQuiz(englishQuizSet, "'집'을 영어로 하면?", "House", "House", "Car", "Tree", "Road");

        log.info("초기 데이터 생성 완료!");
    }

    private void saveQuiz(QuizSet quizSet, String content, String answer,
                          String choice1, String choice2, String choice3, String choice4) {
        Quiz quiz = Quiz.builder()
                .quizSet(quizSet)
                .content(content)
                .answer(answer)
                .choice1(choice1)
                .choice2(choice2)
                .choice3(choice3)
                .choice4(choice4)
                .build();
        quizRepository.save(quiz);
        quizSet.increaseQuizCount(); // ← 퀴즈 수 증가
    }
}
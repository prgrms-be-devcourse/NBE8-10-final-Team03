package com.eof.back.global.init;

import com.eof.back.domain.quiz.entity.AnswerType;
import com.eof.back.domain.quiz.entity.QuestionType;
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
                .provider(AuthProvider.LOCAL)
                .build();
        userRepository.save(admin);

        User testUser = User.of("testuser", passwordEncoder.encode("test1234"), "테스트맨");
        userRepository.save(testUser);

        // ===== 퀴즈셋 1: 세계 수도 (텍스트 + 객관식) =====
        QuizSet capitalQuizSet = QuizSet.of("세계 수도 퀴즈", "나라의 수도를 맞춰보세요!", admin);
        quizSetRepository.save(capitalQuizSet);

        saveMultipleChoiceQuiz(capitalQuizSet, "대한민국의 수도는?", "서울", "서울", "부산", "대구", "인천");
        saveMultipleChoiceQuiz(capitalQuizSet, "일본의 수도는?", "도쿄", "도쿄", "오사카", "교토", "나고야");
        saveMultipleChoiceQuiz(capitalQuizSet, "미국의 수도는?", "워싱턴 D.C.", "뉴욕", "워싱턴 D.C.", "로스앤젤레스", "시카고");
        saveMultipleChoiceQuiz(capitalQuizSet, "프랑스의 수도는?", "파리", "파리", "마르세유", "리옹", "니스");
        saveMultipleChoiceQuiz(capitalQuizSet, "영국의 수도는?", "런던", "런던", "맨체스터", "버밍엄", "리버풀");
        saveMultipleChoiceQuiz(capitalQuizSet, "독일의 수도는?", "베를린", "베를린", "뮌헨", "함부르크", "프랑크푸르트");
        saveMultipleChoiceQuiz(capitalQuizSet, "중국의 수도는?", "베이징", "베이징", "상하이", "광저우", "선전");
        saveMultipleChoiceQuiz(capitalQuizSet, "호주의 수도는?", "캔버라", "시드니", "멜버른", "캔버라", "브리즈번");
        saveMultipleChoiceQuiz(capitalQuizSet, "브라질의 수도는?", "브라질리아", "상파울루", "리우데자네이루", "브라질리아", "살바도르");
        saveMultipleChoiceQuiz(capitalQuizSet, "이탈리아의 수도는?", "로마", "로마", "밀라노", "나폴리", "피렌체");

        // ===== 퀴즈셋 2: 상식 및 미디어 (다양한 유형) =====
        QuizSet generalKnowledgeQuizSet = QuizSet.of("상식 및 미디어 퀴즈", "다양한 유형의 문제입니다!", admin);
        quizSetRepository.save(generalKnowledgeQuizSet);

        // 1. 이미지 + 객관식
        saveImageQuiz(generalKnowledgeQuizSet, "이 동물은 무엇인가요?", "코끼리", "https://example.com/elephant.jpg", "코끼리", "사자", "기린", "하마");

        // 2. 텍스트 + 주관식
        saveShortAnswerQuiz(generalKnowledgeQuizSet, "대한민국의 국화는 무엇인가요?", "무궁화");

        // 3. 영상 + 객관식 (유튜브 시작/종료 시간 추가)
        saveVideoQuiz(generalKnowledgeQuizSet, "영상 속의 노래 제목은?", "Dynamite", "https://www.youtube.com/embed/KhZ5DCd7m6s", 10, 40, "Dynamite", "Butter", "Boy with Luv", "Idol");

        // 4. 음성 + 주관식 (유튜브 시작 시간 추가)
        saveAudioQuiz(generalKnowledgeQuizSet, "들려오는 소리의 악기는?", "피아노", "https://www.youtube.com/embed/WJ3-F02-F_Y", 60, null);

        // 5. 텍스트 + 객관식
        saveMultipleChoiceQuiz(generalKnowledgeQuizSet, "지구에서 가장 높은 산은?", "에베레스트", "백두산", "에베레스트", "후지산", "킬리만자로");


        // ===== 퀴즈셋 3: 쉬운 영단어 =====
        QuizSet englishQuizSet = QuizSet.of("쉬운 영단어 퀴즈", "기초 영단어를 맞춰보세요!", admin);
        quizSetRepository.save(englishQuizSet);

        saveMultipleChoiceQuiz(englishQuizSet, "'사과'를 영어로 하면?", "Apple", "Apple", "Banana", "Orange", "Grape");
        saveMultipleChoiceQuiz(englishQuizSet, "'고양이'를 영어로 하면?", "Cat", "Dog", "Cat", "Bird", "Fish");
        saveMultipleChoiceQuiz(englishQuizSet, "'학교'를 영어로 하면?", "School", "School", "Hospital", "Library", "Market");
        saveMultipleChoiceQuiz(englishQuizSet, "'행복한'을 영어로 하면?", "Happy", "Sad", "Angry", "Happy", "Tired");
        saveMultipleChoiceQuiz(englishQuizSet, "'물'을 영어로 하면?", "Water", "Water", "Fire", "Earth", "Wind");
        saveMultipleChoiceQuiz(englishQuizSet, "'책'을 영어로 하면?", "Book", "Pen", "Book", "Desk", "Chair");
        saveMultipleChoiceQuiz(englishQuizSet, "'빨간색'을 영어로 하면?", "Red", "Blue", "Red", "Green", "Yellow");
        saveMultipleChoiceQuiz(englishQuizSet, "'친구'를 영어로 하면?", "Friend", "Family", "Enemy", "Friend", "Teacher");
        saveMultipleChoiceQuiz(englishQuizSet, "'태양'을 영어로 하면?", "Sun", "Moon", "Star", "Sun", "Cloud");
        saveMultipleChoiceQuiz(englishQuizSet, "'집'을 영어로 하면?", "House", "House", "Car", "Tree", "Road");

        // ===== 퀴즈셋 4: 사칙연산 =====
        QuizSet mathQuizSet = QuizSet.of("사칙연산 퀴즈", "기본 사칙연산 문제들입니다!", admin);
        quizSetRepository.save(mathQuizSet);

        saveMultipleChoiceQuiz(mathQuizSet, "15 + 27 = ?", "42", "40", "41", "42", "43");
        saveMultipleChoiceQuiz(mathQuizSet, "100 - 37 = ?", "63", "53", "63", "73", "83");
        saveMultipleChoiceQuiz(mathQuizSet, "8 × 9 = ?", "72", "63", "72", "81", "56");
        saveMultipleChoiceQuiz(mathQuizSet, "144 ÷ 12 = ?", "12", "10", "11", "12", "13");
        saveMultipleChoiceQuiz(mathQuizSet, "25 + 38 = ?", "63", "61", "62", "63", "64");
        saveMultipleChoiceQuiz(mathQuizSet, "200 - 85 = ?", "115", "105", "110", "115", "120");
        saveMultipleChoiceQuiz(mathQuizSet, "7 × 8 = ?", "56", "48", "54", "56", "63");
        saveMultipleChoiceQuiz(mathQuizSet, "81 ÷ 9 = ?", "9", "7", "8", "9", "10");
        saveMultipleChoiceQuiz(mathQuizSet, "45 + 55 = ?", "100", "90", "95", "100", "105");
        saveMultipleChoiceQuiz(mathQuizSet, "6 × 7 = ?", "42", "36", "40", "42", "48");

        log.info("초기 데이터 생성 완료!");
    }

    private void saveMultipleChoiceQuiz(QuizSet quizSet, String content, String answer,
                                       String choice1, String choice2, String choice3, String choice4) {
        Quiz quiz = Quiz.builder()
                .quizSet(quizSet)
                .questionType(QuestionType.TEXT)
                .answerType(AnswerType.MULTIPLE_CHOICE)
                .content(content)
                .answer(answer)
                .choice1(choice1)
                .choice2(choice2)
                .choice3(choice3)
                .choice4(choice4)
                .build();
        quizRepository.save(quiz);
        quizSet.increaseQuizCount();
    }

    private void saveImageQuiz(QuizSet quizSet, String content, String answer, String imageUrl,
                              String choice1, String choice2, String choice3, String choice4) {
        Quiz quiz = Quiz.builder()
                .quizSet(quizSet)
                .questionType(QuestionType.IMAGE)
                .answerType(AnswerType.MULTIPLE_CHOICE)
                .content(content)
                .answer(answer)
                .imageUrl(imageUrl)
                .choice1(choice1)
                .choice2(choice2)
                .choice3(choice3)
                .choice4(choice4)
                .build();
        quizRepository.save(quiz);
        quizSet.increaseQuizCount();
    }

    private void saveShortAnswerQuiz(QuizSet quizSet, String content, String answer) {
        Quiz quiz = Quiz.builder()
                .quizSet(quizSet)
                .questionType(QuestionType.TEXT)
                .answerType(AnswerType.SHORT_ANSWER)
                .content(content)
                .answer(answer)
                .build();
        quizRepository.save(quiz);
        quizSet.increaseQuizCount();
    }

    private void saveVideoQuiz(QuizSet quizSet, String content, String answer, String videoUrl,
                              Integer startTime, Integer endTime,
                              String choice1, String choice2, String choice3, String choice4) {
        Quiz quiz = Quiz.builder()
                .quizSet(quizSet)
                .questionType(QuestionType.VIDEO)
                .answerType(AnswerType.MULTIPLE_CHOICE)
                .content(content)
                .answer(answer)
                .videoUrl(videoUrl)
                .startTime(startTime)
                .endTime(endTime)
                .choice1(choice1)
                .choice2(choice2)
                .choice3(choice3)
                .choice4(choice4)
                .build();
        quizRepository.save(quiz);
        quizSet.increaseQuizCount();
    }

    private void saveAudioQuiz(QuizSet quizSet, String content, String answer, String videoUrl, Integer startTime, Integer endTime) {
        Quiz quiz = Quiz.builder()
                .quizSet(quizSet)
                .questionType(QuestionType.AUDIO)
                .answerType(AnswerType.SHORT_ANSWER)
                .content(content)
                .answer(answer)
                .videoUrl(videoUrl)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        quizRepository.save(quiz);
        quizSet.increaseQuizCount();
    }
}

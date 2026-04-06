package com.eof.back.domain.quiz.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 퀴즈 문제의 형태를 정의하는 열거형입니다.
 *
 * @author MintyU
 * @since 2026-04-06
 */
@Getter
@RequiredArgsConstructor
public enum QuestionType {
    /**
     * 텍스트 형태의 문제
     */
    TEXT("텍스트"),

    /**
     * 이미지 기반의 문제
     */
    IMAGE("이미지"),

    /**
     * 유튜브 영상 기반의 문제 (화면 보임)
     */
    VIDEO("유튜브 영상"),

    /**
     * 유튜브 음성 기반의 문제 (화면 숨김, 소리만 재생)
     */
    AUDIO("유튜브 음성");

    private final String description;
}

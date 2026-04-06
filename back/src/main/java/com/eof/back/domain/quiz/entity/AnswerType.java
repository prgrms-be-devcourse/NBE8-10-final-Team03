package com.eof.back.domain.quiz.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 퀴즈의 정답(제출) 유형을 정의하는 열거형입니다.
 *
 * @author MintyU
 * @since 2026-04-06
 */
@Getter
@RequiredArgsConstructor
public enum AnswerType {
    /**
     * 객관식 (사지선다형)
     */
    MULTIPLE_CHOICE("객관식"),

    /**
     * 주관식 (단답형)
     */
    SHORT_ANSWER("주관식");

    private final String description;
}

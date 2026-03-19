package com.eof.back.domain.user.service;

import com.eof.back.domain.user.dto.UserSignupRequest;
import com.eof.back.domain.user.dto.UserSignupResponse;
import com.eof.back.domain.user.entity.User;
import com.eof.back.domain.user.repository.UserRepository;
import com.eof.back.global.exception.errorCode.AuthErrorCode;
import com.eof.back.global.exception.exceptions.AuthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * UserService의 비즈니스 로직을 검증하는 단위 테스트 클래스입니다.
 *
 * <p>Mockito를 사용하여 의존 객체(UserRepository, PasswordEncoder)를 Mock 처리하고
 * 서비스 계층의 로직이 의도한 대로 동작하는지 검증합니다.</p>
 *
 * <p>현재는 회원가입(signup) 기능을 중심으로 테스트를 작성하며,
 * 향후 사용자 관련 비즈니스 로직이 추가될 경우 해당 테스트가 확장될 수 있습니다.</p>
 *
 * @author 5h6vm
 * @since 2026-03-18
 */

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        UserSignupRequest req = new UserSignupRequest(
                "testUser",
                "password123",
                "tester"
        );

        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userRepository.existsByNickname("tester")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        User savedUser = User.of("testUser", "encodedPassword", "tester");

        when(userRepository.saveAndFlush(any(User.class))).thenReturn(savedUser);

        // when
        UserSignupResponse result = userService.signup(req);

        // then
        assertThat(result.username()).isEqualTo("testUser");
        assertThat(result.nickname()).isEqualTo("tester");

        verify(userRepository).saveAndFlush(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("회원가입 실패 - 아이디 중복")
    void signup_fail_duplicateLoginId() {
        // given
        UserSignupRequest req = new UserSignupRequest(
                "testUser",
                "password123",
                "tester"
        );

        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(req))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.USER_ALREADY_EXIST));

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복")
    void signup_fail_duplicateNickname() {
        // given
        UserSignupRequest req = new UserSignupRequest(
                "testUser",
                "password123",
                "tester"
        );

        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userRepository.existsByNickname("tester")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(req))
                .isInstanceOf(AuthException.class)
                .satisfies(e -> assertThat(((AuthException) e).getErrorCode())
                        .isEqualTo(AuthErrorCode.NICKNAME_ALREADY_EXIST));

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("회원가입 동시 요청 - 아이디 중복 시 한 건만 성공하고 나머지는 SIGNUP_FAIL")
    void signup_concurrent_duplicateUsername() throws InterruptedException {
        // given
        UserSignupRequest req = new UserSignupRequest("testUser", "password123", "tester");
        User savedUser = User.of("testUser", "encodedPassword", "tester");

        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userRepository.existsByNickname("tester")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenReturn(savedUser)
                .thenThrow(new DataIntegrityViolationException("username unique constraint violation"));

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        List<AuthErrorCode> errorCodes = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    userService.signup(req);
                    successCount.incrementAndGet();
                } catch (AuthException e) {
                    errorCodes.add((AuthErrorCode) e.getErrorCode());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(errorCodes).containsExactly(AuthErrorCode.SIGNUP_FAIL);
    }

    @Test
    @DisplayName("회원가입 동시 요청 - 닉네임 중복 시 한 건만 성공하고 나머지는 SIGNUP_FAIL")
    void signup_concurrent_duplicateNickname() throws InterruptedException {
        // given
        UserSignupRequest req = new UserSignupRequest("testUser", "password123", "tester");
        User savedUser = User.of("testUser", "encodedPassword", "tester");

        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userRepository.existsByNickname("tester")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenReturn(savedUser)
                .thenThrow(new DataIntegrityViolationException("nickname unique constraint violation"));

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        List<AuthErrorCode> errorCodes = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    userService.signup(req);
                    successCount.incrementAndGet();
                } catch (AuthException e) {
                    errorCodes.add((AuthErrorCode) e.getErrorCode());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(errorCodes).containsExactly(AuthErrorCode.SIGNUP_FAIL);
    }
}

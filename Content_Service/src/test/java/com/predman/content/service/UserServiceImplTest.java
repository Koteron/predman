package com.predman.content.service;

import com.predman.content.auth.CustomUserDetails;
import com.predman.content.auth.JwtTokenProvider;
import com.predman.content.auth.PasswordUtil;
import com.predman.content.dto.user.auth.UserAuthResponseDto;
import com.predman.content.dto.user.auth.UserLoginDto;
import com.predman.content.dto.user.auth.UserRegisterDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.ProjectMember;
import com.predman.content.entity.User;
import com.predman.content.exception.ForbiddenException;
import com.predman.content.exception.NotFoundException;
import com.predman.content.exception.UnauthorizedException;
import com.predman.content.mapper.UserMapper;
import com.predman.content.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordUtil passwordUtil;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private EntityManager entityManager;
    @Mock private ProjectService projectService;
    @Mock private ProjectMemberService projectMemberService;

    private UserServiceImpl userService;
    private final String TEST_JWT = "nonex@email.em";

    private final User TEST_USER = User.builder()
            .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            .login("TEST_LOGIN")
            .email("test_email@email.com")
            .updatedDate(LocalDateTime.of(2023, 1, 1, 0, 0))
            .createdDate(LocalDateTime.of(2023, 1, 1, 0, 0))
            .passwordHash("test_hash")
            .build();

    private final UserDto TEST_USER_DTO = UserDto.builder()
            .id(TEST_USER.getId())
            .login(TEST_USER.getLogin())
            .email(TEST_USER.getEmail())
            .build();

    private final UserRegisterDto TEST_USER_REGISTER_DTO = UserRegisterDto.builder()
            .email(TEST_USER.getEmail())
            .login(TEST_USER.getLogin())
            .password("test_user_password")
            .build();

    private final UserLoginDto TEST_USER_LOGIN_DTO = UserLoginDto.builder()
            .email(TEST_USER.getEmail())
            .password("test_user_password")
            .build();

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository,
                userMapper,
                passwordUtil,
                jwtTokenProvider,
                entityManager,
                projectService,
                projectMemberService);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getEntityById_ReturnsUser() {
        when(entityManager.find(User.class, TEST_USER.getId())).thenReturn(TEST_USER);
        User user = userService.getEntityById(TEST_USER.getId());
        assertEquals(TEST_USER, user);
    }

    @Test
    void getEntityById_ThrowsException_WhenUserDoesNotExist() {
        when(entityManager.find(User.class, TEST_USER.getId())).thenReturn(null);
        assertThrows(NotFoundException.class, () -> userService.getEntityById(TEST_USER.getId()));
    }

    @Test
    void getEntityByEmail_ReturnsUser() {
        when(entityManager.find(User.class, TEST_USER.getId())).thenReturn(null);
        assertThrows(NotFoundException.class, () -> userService.getEntityById(TEST_USER.getId()));
    }

    @Test
    void getEntityByEmail_ThrowsException_WhenUserDoesNotExist() {
        when(entityManager.find(User.class, TEST_USER.getId())).thenReturn(null);
        assertThrows(NotFoundException.class, () -> userService.getEntityById(TEST_USER.getId()));
    }

    @Test
    void getEntityByEmail_ShouldReturnUser_WhenUserExists() {
        @SuppressWarnings("unchecked")
        TypedQuery<User> mockedQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(mockedQuery);
        when(mockedQuery.setParameter(eq("email"), eq(TEST_USER.getEmail()))).thenReturn(mockedQuery);

        when(mockedQuery.getSingleResult()).thenReturn(TEST_USER);

        User result = userService.getEntityByEmail(TEST_USER.getEmail());

        assertNotNull(result);
        assertEquals(TEST_USER.getEmail(), result.getEmail());

        verify(entityManager).createQuery(anyString(), eq(User.class));
        verify(mockedQuery).setParameter("email", TEST_USER.getEmail());
        verify(mockedQuery).getSingleResult();
    }

    @Test
    void getEntityByEmail_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        @SuppressWarnings("unchecked")
        TypedQuery<User> mockedQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(mockedQuery);
        when(mockedQuery.setParameter(eq("email"), eq(TEST_USER.getEmail()))).thenReturn(mockedQuery);
        when(mockedQuery.getSingleResult()).thenThrow(new NoResultException());

        assertThrows(NotFoundException.class, () -> userService.getEntityByEmail(TEST_USER.getEmail()));

        verify(entityManager).createQuery(anyString(), eq(User.class));
        verify(mockedQuery).setParameter("email", TEST_USER.getEmail());
        verify(mockedQuery).getSingleResult();
    }

    @Test
    void getById_ReturnsUser() {
        when(userRepository.findById(TEST_USER.getId())).thenReturn(Optional.of(TEST_USER));
        when(userMapper.convertToUserDto(TEST_USER)).thenReturn(TEST_USER_DTO);
        UserDto userDto = userService.getById(TEST_USER.getId());
        assertEquals(TEST_USER.getId(), userDto.id());
    }

    @Test
    void getById_ThrowsException_WhenUserDoesNotExist() {
        when(userRepository.findById(TEST_USER.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getById(TEST_USER.getId()));
    }

    @Test
    void getByEmail_ReturnsUser() {
        when(userRepository.findByEmail(TEST_USER.getEmail())).thenReturn(Optional.of(TEST_USER));
        when(userMapper.convertToUserDto(TEST_USER)).thenReturn(TEST_USER_DTO);
        UserDto userDto = userService.getByEmail(TEST_USER.getEmail());
        assertEquals(TEST_USER.getId(), userDto.id());
    }

    @Test
    void getByEmail_ThrowsException_WhenUserDoesNotExist() {
        when(userRepository.findByEmail(TEST_USER.getEmail())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getByEmail(TEST_USER.getEmail()));
    }

    @Test
    void registerUser_ReturnsAuthResponse() {
        when(passwordUtil.encrypt(TEST_USER_REGISTER_DTO.password())).thenReturn(TEST_USER.getPasswordHash());
        when(userRepository.save(any(User.class))).thenReturn(TEST_USER);
        when(jwtTokenProvider.generateToken(TEST_USER.getEmail())).thenReturn(TEST_JWT);
        assertEquals(userService.register(TEST_USER_REGISTER_DTO), UserAuthResponseDto.builder()
                .id(TEST_USER.getId())
                .email(TEST_USER.getEmail())
                .login(TEST_USER.getLogin())
                .token(TEST_JWT)
                .build());
    }

    @Test
    void registerUser_ThrowsException_WhenUserExists() {
        when(passwordUtil.encrypt(TEST_USER_REGISTER_DTO.password())).thenReturn(TEST_USER.getPasswordHash());
        when(userRepository.save(any(User.class))).thenThrow(new EntityExistsException());
        assertThrows(EntityExistsException.class, () -> userService.register(TEST_USER_REGISTER_DTO));
    }

    @Test
    void loginUser_ReturnsAuthResponse() {
        when(userRepository.findByEmail(TEST_USER.getEmail())).thenReturn(Optional.of(TEST_USER));
        when(passwordUtil.matches(TEST_USER_REGISTER_DTO.password(), TEST_USER.getPasswordHash()))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken(TEST_USER.getEmail())).thenReturn(TEST_JWT);
        assertEquals(userService.login(TEST_USER_LOGIN_DTO), UserAuthResponseDto.builder()
                .id(TEST_USER.getId())
                .email(TEST_USER.getEmail())
                .login(TEST_USER.getLogin())
                .token(TEST_JWT)
                .build());
    }

    @Test
    void loginUser_ThrowsException_WhenUserDoesNotExist() {
        when(userRepository.findByEmail(TEST_USER.getEmail())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.login(TEST_USER_LOGIN_DTO));
    }

    @Test
    void loginUser_ThrowsException_WhenPasswordIsIncorrect() {
        when(userRepository.findByEmail(TEST_USER.getEmail())).thenReturn(Optional.of(TEST_USER));
        when(passwordUtil.matches(TEST_USER_REGISTER_DTO.password(), TEST_USER.getPasswordHash()))
                .thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> userService.login(TEST_USER_LOGIN_DTO));
    }

    @Test
    void deleteUser_CallsDeleteUserMethod() {
        when(projectService.getAllEntitiesByOwnerId(TEST_USER.getId())).thenReturn(new ArrayList<>());
        ArrayList<ProjectMember> members = new ArrayList<>();
        when(projectMemberService.findAllByProjectIds(ArgumentMatchers.anyList())).thenReturn(members);
        userService.deleteById(TEST_USER.getId());
        verify(projectMemberService).deleteAllByUserId(TEST_USER.getId());
        verify(userRepository).deleteById(TEST_USER.getId());
    }

    @Test
    void deleteUser_DeletesProjectsIfNoOtherMembers() {
        Project project = Project.builder().id(UUID.randomUUID()).owner(TEST_USER).build();
        when(projectService.getAllEntitiesByOwnerId(TEST_USER.getId()))
                .thenReturn(List.of(project));
        when(projectMemberService.findAllByProjectIds(List.of(project.getId())))
                .thenReturn(List.of());

        userService.deleteById(TEST_USER.getId());

        verify(projectMemberService).deleteAllByProjectId(project.getId());
        verify(projectService).delete(project);
        verify(projectMemberService).deleteAllByUserId(TEST_USER.getId());
        verify(userRepository).deleteById(TEST_USER.getId());
    }

    @Test
    void deleteUser_ReassignsProjectOwnerIfMembersExist() {
        UUID projectId = UUID.randomUUID();
        Project project = Project.builder().id(projectId).owner(TEST_USER).build();

        User anotherUser = User.builder().id(UUID.randomUUID()).build();
        ProjectMember member = ProjectMember.builder().user(anotherUser).project(project).build();

        when(projectService.getAllEntitiesByOwnerId(TEST_USER.getId()))
                .thenReturn(List.of(project));
        when(projectMemberService.findAllByProjectIds(List.of(projectId)))
                .thenReturn(List.of(member));

        userService.deleteById(TEST_USER.getId());

        verify(projectService).changeOwnerUnchecked(eq(project), eq(anotherUser));
        verify(projectMemberService).deleteAllByUserId(TEST_USER.getId());
        verify(userRepository).deleteById(TEST_USER.getId());
    }

    @Test
    void deleteUser_ThrowsException_WhenGetProjectsFails() {
        when(projectService.getAllEntitiesByOwnerId(TEST_USER.getId()))
                .thenThrow(new RuntimeException("DB failure"));

        assertThrows(RuntimeException.class, () -> userService.deleteById(TEST_USER.getId()));

        verify(projectService, times(1)).getAllEntitiesByOwnerId(TEST_USER.getId());
        verifyNoMoreInteractions(projectService, projectMemberService, userRepository);
    }

    @Test
    void getAuthenticatedUser_ReturnsUser_WhenAuthenticated() {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userMapper.convertToUserEntity(userDetails)).thenReturn(TEST_USER);

        assertEquals(TEST_USER, userService.getAuthenticatedUser());
    }

    @Test
    void getAuthenticatedUser_ThrowsForbidden_WhenAuthenticationIsNull() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(ForbiddenException.class, () -> userService.getAuthenticatedUser());
    }

    @Test
    void getAuthenticatedUser_ThrowsForbidden_WhenNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(ForbiddenException.class, () -> userService.getAuthenticatedUser());
    }

    @Test
    void getAuthenticatedUser_ThrowsForbidden_WhenPrincipalIsInvalid() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("someString");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(ForbiddenException.class, () -> userService.getAuthenticatedUser());
    }

    @Test
    void checkAuthenticatedUser_DoesNotThrow_WhenUserIdMatches() {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userMapper.convertToUserEntity(userDetails)).thenReturn(TEST_USER);

        assertDoesNotThrow(() -> userService.checkAuthenticatedUser(TEST_USER.getId()));
    }

    @Test
    void checkAuthenticatedUser_ThrowsForbidden_WhenUserIdDoesNotMatch() {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userMapper.convertToUserEntity(userDetails)).thenReturn(TEST_USER);

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> userService.checkAuthenticatedUser(
                        UUID.fromString("11111111-2222-1111-1111-111111111111"))
        );

        assertEquals("User id mismatch!", exception.getMessage());
    }

}
package com.predman.content.service;

import com.predman.content.auth.CustomUserDetails;
import com.predman.content.auth.JwtTokenProvider;
import com.predman.content.auth.PasswordUtil;
import com.predman.content.dto.user.auth.UserAuthResponseDto;
import com.predman.content.dto.user.auth.UserLoginDto;
import com.predman.content.dto.user.detailed.UserDto;
import com.predman.content.dto.user.auth.UserRegisterDto;
import com.predman.content.entity.Project;
import com.predman.content.entity.ProjectMember;
import com.predman.content.entity.User;
import com.predman.content.exception.ForbiddenException;
import com.predman.content.exception.NotFoundException;
import com.predman.content.exception.UnauthorizedException;
import com.predman.content.mapper.UserMapper;
import com.predman.content.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordUtil passwordUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private final EntityManager entityManager;
    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;


    @Override
    public User getEntityById(UUID id)
    {
        User user = entityManager.find(User.class, id);
        if (user == null)
        {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    @Override
    public User getEntityByEmail(String email)
    {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.email = :email", User.class);
        query.setParameter("email", email);
        User user = query.getSingleResult();
        if (user == null)
        {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    @Override
    public UserDto getById(UUID id)
    {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found!"));
        return userMapper.convertToUserDto(user);
    }

    @Override
    public UserDto getByEmail(String email)
    {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found!"));
        return userMapper.convertToUserDto(user);
    }

    @Override
    public UserAuthResponseDto register(UserRegisterDto userRegisterDto)
    {
        User user = userRepository.save(User.builder()
                .email(userRegisterDto.email())
                .login(userRegisterDto.login())
                .passwordHash(passwordUtil.encrypt(userRegisterDto.password()))
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build());
        return UserAuthResponseDto.builder()
                .id(user.getId())
                .login(user.getLogin())
                .email(user.getEmail())
                .token(jwtTokenProvider.generateToken(user.getEmail()))
                .build();
    }

    @Override
    public UserAuthResponseDto login(UserLoginDto userLoginDto)
    {
        User user = userRepository.findByEmail(userLoginDto.email())
                .orElseThrow(() -> new NotFoundException("User not found!"));
        if (passwordUtil.matches(userLoginDto.password(), user.getPasswordHash()))
        {
            return UserAuthResponseDto
                    .builder()
                    .id(user.getId())
                    .token(jwtTokenProvider.generateToken(user.getEmail()))
                    .login(user.getLogin())
                    .email(user.getEmail())
                    .build();
        }
        throw new UnauthorizedException("Wrong password!");
    }

    @Override
    @Transactional
    public void deleteById(UUID userId) {
        List<Project> projects = projectService.getAllEntitiesByOwnerId(userId);
        List<UUID> projectIds = projects.stream().map(Project::getId).toList();
        List<ProjectMember> members = projectMemberService.findAllByProjectIds(projectIds);
        for (Project project : projects) {
            List<User> alternativeOwners = members.stream()
                    .map(ProjectMember::getUser)
                    .filter(u -> !u.getId().equals(userId))
                    .toList();

            if (!alternativeOwners.isEmpty()) {
                User newOwner = alternativeOwners.get(ThreadLocalRandom.current().nextInt(alternativeOwners.size()));
                projectService.changeOwnerUnchecked(project, newOwner);
            } else {
                projectService.delete(project);
            }
        }
        userRepository.deleteById(userId);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return userMapper.convertToUserEntity((CustomUserDetails) principal);
        }
        throw new ForbiddenException("Invalid authentication principal");
    }

    @Override
    public void checkAuthenticatedUser(UUID userId) {
        User requestSender = getAuthenticatedUser();
        if (requestSender.getId().equals(userId)) {
            return;
        }
        throw new ForbiddenException("User id mismatch!");
    }
}

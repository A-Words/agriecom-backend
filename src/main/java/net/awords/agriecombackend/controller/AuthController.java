package net.awords.agriecombackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.awords.agriecombackend.dto.ApiResponseDTO;
import net.awords.agriecombackend.dto.auth.AuthDtos;
import net.awords.agriecombackend.entity.Role;
import net.awords.agriecombackend.entity.User;
import net.awords.agriecombackend.repository.RoleRepository;
import net.awords.agriecombackend.repository.UserRepository;
import net.awords.agriecombackend.security.JwtUtil;
import net.awords.agriecombackend.security.RoleConstants;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "认证相关接口")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponseDTO<AuthDtos.UserInfo> login(@Valid @RequestBody AuthDtos.LoginRequest req, HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.username, req.password));
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
    String[] roleArray = user.getRoles().stream().map(Role::getName).toArray(String[]::new);
    List<String> roleNames = Arrays.stream(roleArray).toList();
    String token = jwtUtil.generateToken(auth.getName(), roleNames);

        Cookie cookie = new Cookie(jwtUtil.getCookieName(), token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        // 可按需设置 SameSite/secure
        response.addCookie(cookie);

        AuthDtos.UserInfo info = new AuthDtos.UserInfo();
        info.id = user.getId();
        info.username = user.getUsername();
        info.roles = roleArray;
        return ApiResponseDTO.success(info);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDTO<AuthDtos.UserInfo> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        if (userRepository.findByUsername(req.username).isPresent()) {
            return ApiResponseDTO.error(400, "用户名已存在");
        }
        User u = new User();
        u.setUsername(req.username);
        u.setPassword(passwordEncoder.encode(req.password));
        Role role = roleRepository.findByName(RoleConstants.USER).orElseGet(() -> {
            Role r = new Role(); r.setName(RoleConstants.USER); return roleRepository.save(r);
        });
        u.setRoles(Set.of(role));
        userRepository.save(u);

        AuthDtos.UserInfo info = new AuthDtos.UserInfo();
        info.id = u.getId();
        info.username = u.getUsername();
        info.roles = new String[]{RoleConstants.USER};
        return ApiResponseDTO.success(info);
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public ApiResponseDTO<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(jwtUtil.getCookieName(), "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ApiResponseDTO.success(null);
    }

    @Operation(summary = "当前用户")
    @GetMapping("/me")
    public ApiResponseDTO<AuthDtos.UserInfo> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ApiResponseDTO.error(401, "未登录");
        }
        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return ApiResponseDTO.error(404, "用户不存在");
        }
        AuthDtos.UserInfo info = new AuthDtos.UserInfo();
        info.id = user.getId();
        info.username = user.getUsername();
        info.roles = user.getRoles().stream().map(Role::getName).toArray(String[]::new);
        return ApiResponseDTO.success(info);
    }
}

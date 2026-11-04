package com.velas.transitAds.security.authFilter;

import com.velas.transitAds.entity.User;
import com.velas.transitAds.exception.ApiException;
import com.velas.transitAds.entity.AuthenticatedUser;
import com.velas.transitAds.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class EmailVerificationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public EmailVerificationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.matches("/auth/verify/\\d{6}")
                || path.equals("/auth/resend")
                || path.equals("/auth/login/driver")
                || path.equals("/auth/register/driver")
                || path.equals("/auth/login/company")
                || path.equals("/auth/register/company")
                || path.equals("/auth/refresh/company")
                || path.equals("/auth/refresh")
                || path.equals("/auth/logout");
    }

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {

            filterChain.doFilter(request, response);
            return;
        }

        boolean verified = userRepository.findById(new ObjectId(user.id()))
                .map(User::isVerified)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "ACCOUNT_NOT_FOUND",
                        "Authenticated account no longer exists",
                        Map.of()
                ));

        if (!verified) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "EMAIL_VERIFICATION_REQUIRED",
                    "Please verify your email before accessing this resource",
                    Map.of()
            );
        }

        filterChain.doFilter(request, response);
    }
}
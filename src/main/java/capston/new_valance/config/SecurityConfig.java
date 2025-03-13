package capston.new_valance.config;

import capston.new_valance.jwt.JwtFilter;
import capston.new_valance.jwt.JwtUtil;
import capston.new_valance.oauth2.CustomFailHandler;
import capston.new_valance.oauth2.CustomOAuth2UserService;
import capston.new_valance.oauth2.CustomSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Lazy
    private final CustomOAuth2UserService customOAuth2UserService;

    private final JwtUtil jwtUtil;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomFailHandler customFailHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager() {
        OAuth2AuthorizedClientService clientService =
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        OAuth2AuthorizedClientRepository clientRepository =
                new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(clientService);

        OAuth2AuthorizedClientProvider provider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .build();

        DefaultOAuth2AuthorizedClientManager clientManager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, clientRepository);
        clientManager.setAuthorizedClientProvider(provider);
        return clientManager;
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfig()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAfter(new JwtFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class) // 필터 순서 보장
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(endpoint -> endpoint
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                        .failureHandler(customFailHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    // CORS 설정
    private CorsConfigurationSource corsConfig() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                config.setAllowedMethods(Collections.singletonList("*"));
                config.setAllowCredentials(true);
                config.setExposedHeaders(Collections.singletonList("Authorization"));
                config.setMaxAge(3600L);
                return config;
            }
        };
    }
}

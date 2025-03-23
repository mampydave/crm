package site.easy.to.build.crm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import site.easy.to.build.crm.config.oauth2.CustomOAuth2UserService;
import site.easy.to.build.crm.config.oauth2.OAuthLoginSuccessHandler;
import site.easy.to.build.crm.service.user.OAuthUserService;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final OAuthLoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomOAuth2UserService oauthUserService;
    private final CrmUserDetails crmUserDetails;
    private final CustomerUserDetails customerUserDetails;
    private final Environment environment;

    @Autowired
    public SecurityConfig(OAuthLoginSuccessHandler oAuth2LoginSuccessHandler, CustomOAuth2UserService oauthUserService, CrmUserDetails crmUserDetails,
                          CustomerUserDetails customerUserDetails, Environment environment) {
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.oauthUserService = oauthUserService;
        this.crmUserDetails = crmUserDetails;
        this.customerUserDetails = customerUserDetails;
        this.environment = environment;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
        httpSessionCsrfTokenRepository.setParameterName("csrf");

        http.csrf((csrf) -> csrf
                .ignoringRequestMatchers("/api/**")
                .csrfTokenRepository(httpSessionCsrfTokenRepository)
        );

        http.cors(cors -> cors.configurationSource(corsConfigurationSource())); // Appliquer la configuration CORS

        http.authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/register/**").permitAll()
                        .requestMatchers("/set-employee-password/**").permitAll()
                        .requestMatchers("/change-password/**").permitAll()
                        .requestMatchers("/font-awesome/**").permitAll()
                        .requestMatchers("/fonts/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/save").permitAll()
                        .requestMatchers("/js/**").permitAll()
                        .requestMatchers("/css/**").permitAll()
<<<<<<< Updated upstream
=======
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/config/**").permitAll()
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/**/manager/**")).hasRole("MANAGER")
                        .requestMatchers("/employee/**").hasAnyRole("MANAGER", "EMPLOYEE")
                        .requestMatchers("/customer/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login")
                        .permitAll()
                ).userDetailsService(crmUserDetails)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauthUserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                ).logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll())
                .exceptionHandling(exception -> {
                    exception.accessDeniedHandler(accessDeniedHandler());
                });

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5175")); // Autoriser l'origine de votre frontend
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Autoriser les méthodes HTTP
        configuration.setAllowedHeaders(List.of("*")); // Autoriser tous les headers
        configuration.setAllowCredentials(true); // Autoriser les credentials

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Appliquer à tous les endpoints
        return source;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain customerSecurityFilterChain(HttpSecurity http) throws Exception {
        HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
        httpSessionCsrfTokenRepository.setParameterName("csrf");

        http.csrf((csrf) -> csrf
                .ignoringRequestMatchers("/api/**")
                .csrfTokenRepository(httpSessionCsrfTokenRepository)
        );

        http.cors(cors -> cors.configurationSource(corsConfigurationSource())); // Appliquer la configuration CORS

        http.securityMatcher("/customer-login/**")
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/set-password/**").permitAll()
                        .requestMatchers("/font-awesome/**").permitAll()
                        .requestMatchers("/fonts/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/js/**").permitAll()
                        .requestMatchers("/css/**").permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/**/manager/**")).hasRole("MANAGER")
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/customer-login")
                        .loginProcessingUrl("/customer-login")
                        .failureUrl("/customer-login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()).userDetailsService(customerUserDetails)
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/customer-login")
                        .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
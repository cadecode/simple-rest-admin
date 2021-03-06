package top.cadecode.sra.framework.config.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity.IgnoredRequestConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.cadecode.sra.common.enums.AuthModelEnum;
import top.cadecode.sra.framework.security.LoginSuccessHandler;
import top.cadecode.sra.framework.security.TokenAuthFilter;
import top.cadecode.sra.framework.security.handler.LoginFailureHandler;
import top.cadecode.sra.framework.security.handler.NoAuthenticationHandler;
import top.cadecode.sra.framework.security.handler.NoAuthorityHandler;
import top.cadecode.sra.framework.security.handler.SignOutSuccessHandler;
import top.cadecode.sra.framework.security.voter.DataBaseRoleVoter;

import java.util.Arrays;
import java.util.List;

/**
 * @author Cade Li
 * @date 2022/5/27
 * @description Security ??????
 */
@Slf4j
@Data
@RequiredArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
@ConfigurationProperties("sra.security")
public class SecurityConfig {

    /**
     * ????????????
     */
    private AuthModelEnum authModel;

    /**
     * ??????????????? url
     */
    private List<String> ignoreUrls;

    /**
     * ????????????
     */
    public static final String LOGIN_URL = "/login";

    /**
     * ????????????
     */
    public static final String LOGOUT_URL = "/logout";

    /**
     * ?????????????????????
     */
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final NoAuthenticationHandler noAuthenticationHandler;
    private final NoAuthorityHandler noAuthorityHandler;
    private final SignOutSuccessHandler signOutSuccessHandler;

    /**
     * ?????? Token ?????????
     */
    private final TokenAuthFilter tokenAuthFilter;

    /**
     * ???????????????
     */
    private final DataBaseRoleVoter dataBaseRoleVoter;

    /**
     * ?????? UserDetailsService
     */
    private final UserDetailsService userDetailsService;

    /**
     * ???????????????
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security ??????
     */
    @Bean
    public WebSecurityConfigurerAdapter webSecurityConfigurer(PasswordEncoder passwordEncoder) {
        return new WebSecurityConfigurerAdapter() {

            @Override
            protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
            }

            @Override
            protected void configure(HttpSecurity http) throws Exception {
                // ?????? csrf
                http.csrf().disable();
                // ?????? session ??????
                http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                // ??????????????????
                http.authorizeRequests()
                        // ????????????????????????
                        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated();
                // ?????????????????????
                http.logout().permitAll()
                        .logoutUrl(LOGOUT_URL)
                        .logoutSuccessHandler(signOutSuccessHandler);
                // ??????????????????
                http.exceptionHandling()
                        // ????????????????????????
                        .authenticationEntryPoint(noAuthenticationHandler)
                        // ????????????????????????
                        .accessDeniedHandler(noAuthorityHandler);
                // ???????????? accessDecisionManager
                http.authorizeRequests()
                        .accessDecisionManager(new UnanimousBased(
                                Arrays.asList(new WebExpressionVoter(), dataBaseRoleVoter)));
                // ?????????????????????
                http.formLogin().permitAll()
                        .loginProcessingUrl(LOGIN_URL)
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler);
                // ?????? Token ???????????????
                http.addFilterBefore(tokenAuthFilter, UsernamePasswordAuthenticationFilter.class);
                log.info("?????? Security ????????????????????? {}", authModel);
            }

            @Override
            public void configure(WebSecurity web) {
                // ???????????????
                IgnoredRequestConfigurer ignoring = web.ignoring();
                // ?????? swagger knife ??????
                ignoring.antMatchers("/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs/**");
                // ?????????????????????
                if (CollUtil.isNotEmpty(ignoreUrls)) {
                    log.info("?????? Security ?????? {}", ignoreUrls);
                    ignoring.antMatchers(ArrayUtil.toArray(ignoreUrls, String.class));
                }
            }
        };
    }
}

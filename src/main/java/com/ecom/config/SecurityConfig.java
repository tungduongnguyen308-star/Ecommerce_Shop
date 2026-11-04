package com.ecom.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
@Configuration
public class SecurityConfig {
    @Bean public PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
    @Bean public UserDetailsService userDetailsService(JdbcTemplate jdbc){
        return email->{
            var matches=jdbc.query("SELECT email,password_hash,role,enabled FROM app_users WHERE email=?",(r,i)->
                    User.withUsername(r.getString("email")).password(r.getString("password_hash"))
                            .roles(r.getString("role")).disabled(!r.getBoolean("enabled")).build(),email);
            if(matches.isEmpty())throw new org.springframework.security.core.userdetails.UsernameNotFoundException("Unknown user");
            return matches.get(0);
        };
    }
    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a->a
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/**").permitAll())
            .formLogin(f->f.loginPage("/login").loginProcessingUrl("/login")
                    .defaultSuccessUrl("/admin",true).failureUrl("/login?error").permitAll())
            .logout(l->l.logoutUrl("/logout").logoutSuccessUrl("/"));
        // CSRF remains enabled. All HTML mutating requests are POST with CSRF token.
        return http.build();
    }
}

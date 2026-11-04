package com.ecom.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
public class AdminSeeder {
    @Bean public ApplicationRunner seedAdmin(JdbcTemplate jdbc,PasswordEncoder encoder,
        @Value("${demo.admin-email}") String email,@Value("${demo.admin-password}") String password){
        return args->{
            Integer exists=jdbc.queryForObject("SELECT COUNT(*) FROM app_users WHERE email=?",Integer.class,email);
            if(exists!=null&&exists==0)
                jdbc.update("INSERT INTO app_users(email,password_hash,role,enabled) VALUES(?,?,'ADMIN',TRUE)",email,encoder.encode(password));
        };
    }
}

package com.project.medium.configuration.security;

import com.project.medium.configuration.custom.CustomAccessDeniedHandler;
import com.project.medium.configuration.custom.RestAuthenticationEntryPoint;
import com.project.medium.configuration.filter.JwtAuthenticationFilter;
import com.project.medium.model.auth.Account;
import com.project.medium.model.auth.Role;
import com.project.medium.services.account.AccountService;
import com.project.medium.services.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {
    @Autowired
    private AccountService accountService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String BLOG_UNAUTHENTICATED_ROUTES = "/api/blogs/get-blog/?{[0-9]+}";

    private static final String COMMENT_UNAUTHENTICATED_ROUTES = "/api/comments/?{[0-9]+}/blog";

    private static final String LIKE_UNAUTHENTICATED_ROUTES = "api/likes/?{[0-9]+}/blog";
    private static final String LIST_IMAGES = "/api/v1/images/album/?{[0-9]+}";

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public RestAuthenticationEntryPoint restServicesEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }


    @Bean
    public CustomAccessDeniedHandler customAccessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @PostConstruct
    public void init() {


        List<Account> accounts = (List<Account>) accountService.findAll();
        List<Role> roles = (List<Role>) roleService.findAll();
        if (roles.isEmpty()){
            Role roleAdmin = new Role();
            roleAdmin.setId(1L);
            roleAdmin.setName("ROLE_ADMIN");
            roleService.save(roleAdmin);

            Role roleCoach = new Role();
            roleCoach.setId(2L);
            roleCoach.setName("ROLE_USER");
            roleService.save(roleCoach);
        }

        if (accounts.isEmpty()){
            Account admin = new Account();
            Set<Role> roleList = new HashSet<>();
            roleList.add(new Role(1L,"ROLE_ADMIN"));
            roleList.add(new Role(2L,"ROLE_USER"));
            admin.setEmail("admin@gmail.com");
            admin.setNickName("admin");
            admin.setPassword("admin");
            admin.setPhoneNumber("0972522048");
            //thuy them code defaul avatar cho admin
            admin.setAvatar("https://ramenparados.com/wp-content/uploads/2019/03/no-avatar-png-8.png");

            admin.setRoles(roleList);
            accountService.save(admin);
        }
    }

    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        this.blogId=String.valueOf(this.blogController.getBlogId());

        http.csrf().ignoringAntMatchers("/**");
        http.httpBasic().authenticationEntryPoint(restServicesEntryPoint());
        http.authorizeRequests()
                //thêm đường dẫn api/blogdedtail là permit all
                .antMatchers("/api/blogs/list", "/login", "/api/accounts/create","/sendSimpleEmail",LIST_IMAGES,this.LIKE_UNAUTHENTICATED_ROUTES , this.BLOG_UNAUTHENTICATED_ROUTES, this.COMMENT_UNAUTHENTICATED_ROUTES).permitAll()
                .anyRequest().authenticated()
                .and().csrf().disable()
                .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling().accessDeniedHandler(customAccessDeniedHandler());
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors();
    }

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping( "/**" )
                .allowedOrigins( "http://localhost:4200" )
                .allowedMethods( "GET", "POST", "DELETE" )
                .allowedHeaders( "*" )
                .allowCredentials( true )
                .exposedHeaders( "Authorization" )
                .maxAge( 3600 );
    }
}

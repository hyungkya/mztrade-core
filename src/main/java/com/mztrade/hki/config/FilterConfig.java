package com.mztrade.hki.config;

import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class FilterConfig {
    private final FirebaseAuth firebaseAuth;

    @Autowired
    public FilterConfig(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }
    // uncomment this and comment the @Component in the filter class definition to register only for a url pattern
    @Bean
    public FilterRegistrationBean<FirebaseTokenFilter> firebaseTokenFilterFilterRegistrationBean() {
        FilterRegistrationBean<FirebaseTokenFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new FirebaseTokenFilter(firebaseAuth));
        //registrationBean.addUrlPatterns("/**");
        registrationBean.setOrder(1);

        return registrationBean;
    }
}



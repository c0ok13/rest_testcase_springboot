package com.example.rest_testcase_springboot.config;

import com.example.rest_testcase_springboot.service.model.ClientBank;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//т.к. наш клиент банка будет представлен в рамках некоторого класса, нам требуется сделать внедрение его в контекст спринга, для этого мы используем конфигуратор
@Configuration
public class ConfigurationOfClient {
    //благодаря аннотации @bean мы имеем возможность внедрить наш сторонний класс в контекст спринга и работать как с обычным бином спринга
    @Bean
    ClientBank clientBlank() {
        return new ClientBank();
    }
}

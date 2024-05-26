package com.gwang.todo.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .version("0.0.1")
                .title("ToDo List API")
                .description("TODO를 등록하고 관리하는 API 입니다.\n서버 환경에 따라 아래의 url을 선택해주세요.");
        
        
        List<Server> serverList = new ArrayList<>();
        
        Server server = new Server();
        server.setUrl("https://todo.shellofmagic.com");
        serverList.add(server);
        
        Server local = new Server();
        local.setUrl("http://localhost:9999");
        serverList.add(local);
     
        return new OpenAPI()
                .info(info)
                .servers(serverList);
    }

}
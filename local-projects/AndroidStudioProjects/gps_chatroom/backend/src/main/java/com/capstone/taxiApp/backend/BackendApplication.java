package com.capstone.taxiApp.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 택시 합승 앱 백엔드 Spring Boot 애플리케이션 진입점.
// 이 클래스가 실행되면 내장 톰캣 서버가 8080 포트에서 기동된다.
@SpringBootApplication
public class BackendApplication {

	// 애플리케이션을 시작한다. 컴포넌트 스캔·자동 설정·빈 등록이 모두 여기서 시작된다.
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}

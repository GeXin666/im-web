package com.dxxt.im;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class App {

	public static void main(String[] args) {
        new SpringApplicationBuilder(App.class).run(args);
	}

}


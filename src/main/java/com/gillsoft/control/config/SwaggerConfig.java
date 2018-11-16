package com.gillsoft.control.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
public class SwaggerConfig implements WebMvcConfigurer {

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("com.gillsoft.api")).paths(PathSelectors.any())
				.build()
				.apiInfo(metaData())
				.tags(new Tag("Localities", "Methods to get information about available cities", 0),
						new Tag("Trip search", "Methods to search trips and to get all information about selected trip", 1),
						new Tag("Order", "Methods to create and handle orders and tickets", 2),
						new Tag("Schedule", "Methods to get information about schedule", 3));
	}
	
	private ApiInfo metaData() {
        return new ApiInfoBuilder()
                .title("GDS REST API")
                .description("\"GDS service API for sale tickets\"")
                .version("1.0.0")
                .contact(new Contact("Artem Kashpur", "http://gillsoft.tech", "artem.kashpur@gillsoft.tech"))
                .build();
    }

}

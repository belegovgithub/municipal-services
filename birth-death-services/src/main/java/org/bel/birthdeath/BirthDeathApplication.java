package org.bel.birthdeath;

import org.bel.birthdeath.birth.repository.BirthRepository;
import org.bel.birthdeath.birth.service.BirthService;
import org.bel.birthdeath.common.services.CommonService;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication(scanBasePackages = "org.bel.birthdeath")
@EnableAutoConfiguration
@Import({TracerConfiguration.class})
public class BirthDeathApplication {

	@Autowired
	BirthService birthService;
	
	@Autowired
	CommonService commonService;
	
	@Autowired
	BirthRepository br;
	
	public static void main(String[] args) {
		SpringApplication.run(BirthDeathApplication.class, args);
	}
	
	@Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper()
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }
	
}

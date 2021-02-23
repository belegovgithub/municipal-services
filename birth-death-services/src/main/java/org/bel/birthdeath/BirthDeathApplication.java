package org.bel.birthdeath;

import javax.annotation.PostConstruct;

import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.birth.service.BirthService;
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
import com.google.gson.Gson;

@SpringBootApplication(scanBasePackages = "org.bel.birthdeath")
@EnableAutoConfiguration
@Import({TracerConfiguration.class})
public class BirthDeathApplication {

	@Autowired
	BirthService birthService;
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
	
	/*@PostConstruct
	private void start() {
		System.out.println("in");
		SearchCriteria criteria = new SearchCriteria();
		criteria.setTenantId("pb.testing");
		criteria.setMotherName("a");
		criteria.setDateofbirth("22-02-2021");
		System.out.println(new Gson().toJson(birthService.search(criteria).get(0)));
	}*/
}

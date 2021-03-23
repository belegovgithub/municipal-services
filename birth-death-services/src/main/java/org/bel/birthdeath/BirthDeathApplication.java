package org.bel.birthdeath;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.bel.birthdeath.birth.repository.BirthRepository;
import org.bel.birthdeath.birth.service.BirthService;
import org.bel.birthdeath.common.services.CommonService;
import org.egov.encryption.config.EncryptionConfiguration;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication(scanBasePackages = "org.bel.birthdeath" )
@EnableAutoConfiguration
//@Import({TracerConfiguration.class})
@Import({TracerConfiguration.class,EncryptionConfiguration.class})
public class BirthDeathApplication {

	@Autowired
	BirthService birthService;
	
	@Autowired
	CommonService commonService;
	
	@Autowired
	BirthRepository br;
	
	@Value("${app.timezone}")
    private String timeZone;
	
	private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
	
	public static void main(String[] args) {
		SpringApplication.run(BirthDeathApplication.class, args);
	}
	
	@Bean
    public ObjectMapper objectMapperBnd(){
        return new ObjectMapper()
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }
	
	@Bean
    public MappingJackson2HttpMessageConverter jacksonConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH));
        mapper.setTimeZone(TimeZone.getTimeZone(timeZone));
        converter.setObjectMapper(mapper);
        return converter;
    }
	
	@PostConstruct
    public void initialize() {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
    }
}

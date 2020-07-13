package org.egov.pg.web.contract;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PgDetail {

    private Long id;
    private String tenantId;
    private String merchantId;
    private String secretKey;
    private String userName;
    private String password;
    private Date lastModifiedDate;
	private Date createdDate; 
	private Long createdBy;
	private Long lastModifiedBy; 
}

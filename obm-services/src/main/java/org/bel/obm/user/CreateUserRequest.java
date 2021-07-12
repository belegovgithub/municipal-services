package org.bel.obm.user;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class CreateUserRequest {

	private RequestInfo requestInfo;
	
    private User userInfo;
}



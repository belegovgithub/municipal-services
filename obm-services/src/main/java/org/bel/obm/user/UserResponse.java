package org.bel.obm.user;

import java.util.List;

import org.egov.common.contract.request.User;
import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserResponse {
	
    @JsonProperty("responseInfo")
	ResponseInfo responseInfo;

    List<User> user;
}

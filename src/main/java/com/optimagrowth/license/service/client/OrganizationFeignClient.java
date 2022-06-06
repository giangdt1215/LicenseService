package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("organization-service")
public interface OrganizationFeignClient {

    @RequestMapping(value = "/v1/organization/{organizationId}",
                    method = RequestMethod.GET,
                    consumes = "application/json")
    Organization getOrganization(@PathVariable("organizationId") String organizationId);
}

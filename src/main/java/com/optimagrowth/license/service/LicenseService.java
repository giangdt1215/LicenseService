package com.optimagrowth.license.service;

import com.optimagrowth.license.config.ServiceConfig;
import com.optimagrowth.license.model.License;
import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.repository.LicenseRepository;
import com.optimagrowth.license.service.client.OrganizationDiscoveryClient;
import com.optimagrowth.license.service.client.OrganizationFeignClient;
import com.optimagrowth.license.service.client.OrganizationRestTemplateClient;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeoutException;

@Service
public class LicenseService {

    @Autowired
    MessageSource messages;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    ServiceConfig config;

    @Autowired
    private OrganizationDiscoveryClient organizationDiscoveryClient;

    @Autowired
    private OrganizationRestTemplateClient organizationRestTemplateClient;

    @Autowired
    private OrganizationFeignClient organizationFeignClient;

    public License getLicense(String licenseId, String organizationId) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        if(license == null){
            throw new IllegalArgumentException(
                    String.format(messages.getMessage("license.search.error.message", null, null),
                            licenseId, organizationId)
            );
        }
        return license.withComment(config.getProperty());
    }

    public License getLicense(String licenseId, String organizationId, String clientType){
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        if(license == null){
            throw new IllegalArgumentException(String.format(
               messages.getMessage("license.search.error.message", null, null),
               licenseId, organizationId));
        }

        Organization organization = retrieveOrganizationInfo(organizationId, clientType);
        if(organization != null){
            license.setOrganizationName(organization.getName());
            license.setContactName(organization.getContactName());
            license.setContactEmail(organization.getContactEmail());
            license.setContactPhone(organization.getContactPhone());
        }

        return license.withComment(config.getProperty());
    }

    private Organization retrieveOrganizationInfo(String organizationId, String clientType) {
        Organization organization = null;

        switch(clientType){
            case "feign":
                System.out.println("I am using feign client");
                organization = organizationFeignClient.getOrganization(organizationId);
                break;
            case "rest":
                System.out.println("I am using rest client");
                organization = organizationRestTemplateClient.getOrganization(organizationId);
                break;
            case "discovery":
                System.out.println("I am using discovery client");
                organization = organizationDiscoveryClient.getOrganization(organizationId);
                break;
            default:
                organization = organizationRestTemplateClient.getOrganization(organizationId);
        }

        return organization;
    }

    private void randomlyRunLong() throws TimeoutException {
        Random rand = new Random();
        int randNum = rand.nextInt(3) + 1;
        if(randNum == 3) sleep();
    }

    private void sleep() throws TimeoutException {
        try {
            Thread.sleep(5000);
            throw new TimeoutException();
        } catch (InterruptedException e){
            System.out.println(e.getMessage());
        }
    }

    @CircuitBreaker(name = "licenseService",
                fallbackMethod = "buildFallbackLicenseList")
    @RateLimiter(name="licenseService",
                fallbackMethod = "buildFallbackLicenseList")
    @Retry(name="retryLicenseService",
                fallbackMethod = "buildFallbackLicenseList")
    @Bulkhead(name = "bulkheadLicenseService",
                fallbackMethod = "buildFallbackLicenseList")
    public List<License> getLicenseByOrganization(String organizationId) throws TimeoutException {
        randomlyRunLong();
        return licenseRepository.findByOrganizationId(organizationId);
    }

    private List<License> buildFallbackLicenseList(String organizationId, Throwable t){
        List<License> fallbackList = new ArrayList<>();
        License license = new License();
        license.setLicenseId("0000000-00-00000");
        license.setOrganizationId(organizationId);
        license.setProductName("Sorry no licensing information currently available");
        fallbackList.add(license);
        return fallbackList;
    }

    public License createLicense(License license){
        license.setLicenseId(UUID.randomUUID().toString());
        licenseRepository.save(license);
        return license.withComment(config.getProperty());
    }

    public License updateLicense(License license){
        licenseRepository.save(license);
        return license.withComment(config.getProperty());
    }

    public String deleteLicense(String licenseId, Locale locale){
        String responseMessage = null;
        License license = new License();
        license.setLicenseId(licenseId);
        licenseRepository.delete(license);
        responseMessage = String.format(messages.getMessage("license.delete.message", null, locale),
                licenseId);
        return responseMessage;
    }

}

package com.optimagrowth.license;

import com.optimagrowth.license.model.License;
import com.optimagrowth.license.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping(value ="v1/organization/{organizationId}/license")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;

    @GetMapping(value="/{licenseId}")
    public ResponseEntity<License> getLicense(@PathVariable("organizationId") String organizationId,
                                              @PathVariable("licenseId") String licenseId){
        License license = licenseService.getLicense(licenseId, organizationId);

        //Add link HATEOAS
        license.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LicenseController.class)
                .getLicense(organizationId, license.getLicenseId()))
                .withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LicenseController.class)
                .createLicense(organizationId, license, null))
                .withRel("createLicense"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LicenseController.class)
                .updateLicense(organizationId, license, null))
                .withRel("updateLicense"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LicenseController.class)
                .deleteLicense(organizationId, license.getLicenseId(), null))
                .withRel("deleteLicense")
        );
        return ResponseEntity.ok(license);
    }

    @PutMapping
    public ResponseEntity<License> updateLicense(@PathVariable("organizationId") String organizationId,
                                                @RequestBody License request,
                                                @RequestHeader(value = "Accept-Language", required = false)
                                                    Locale locale){
        return ResponseEntity.ok(licenseService.updateLicense(request));
    }

    @PostMapping
    public ResponseEntity<License> createLicense(@PathVariable("organizationId") String organizationId,
                                                @RequestBody License request,
                                                @RequestHeader(value = "Accept-Language", required = false)
                                                            Locale locale){
        return ResponseEntity.ok(licenseService.createLicense(request));
    }

    @DeleteMapping(value="/{licenseId}")
    public ResponseEntity<String> deleteLicense(@PathVariable("organizationId") String organizationId,
                                                @PathVariable("licenseId") String licenseId,
                                                @RequestHeader(value = "Accept-Language", required = false)
                                                    Locale locale){
        return ResponseEntity.ok(licenseService.deleteLicense(licenseId, locale));
    }

}

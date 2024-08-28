package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.api.model.Preferences;
import com.ericsson.cifwk.tdm.application.preferences.PreferencesService;
import com.ericsson.cifwk.tdm.presentation.validation.contexts.ContextIdExists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.ResponseEntity.ok;

@Validated
@RestController
@RequestMapping(PreferencesController.REQUEST_MAPPING)
public class PreferencesController {

    public static final String REQUEST_MAPPING = "/api/preferences";

    @Autowired
    private PreferencesService service;

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public ResponseEntity<Preferences> getByUserId(@PathVariable("userId") String userId) {
        Optional<Preferences> preferences = service.findByUserId(userId);
        if (preferences.isPresent()) {
            return ok(preferences.get());
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<Void> save(@RequestBody @Valid @ContextIdExists Preferences preferences) {
        boolean updated = service.update(preferences);
        if (updated) {
            return new ResponseEntity<>(NO_CONTENT);
        }
        return new ResponseEntity<>(CREATED);
    }
}

package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.api.model.Preferences;
import com.ericsson.cifwk.tdm.application.preferences.PreferencesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.ericsson.cifwk.tdm.api.model.Preferences.PreferencesBuilder.aPreferences;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PreferencesControllerTest {

    @InjectMocks
    private PreferencesController controller;

    @Mock
    private PreferencesService service;

    @Test
    public void getByUserId_shouldReturn_preferences_andStatus_OK() throws Exception {
        Preferences preferences = aPreferences().build();
        doReturn(of(preferences)).when(service).findByUserId(anyString());

        ResponseEntity<Preferences> result = controller.getByUserId("userId");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isSameAs(preferences);
        verify(service).findByUserId("userId");
    }

    @Test
    public void getByUserId_shouldReturn_emptyBody_andStatus_NOT_FOUND() throws Exception {
        doReturn(empty()).when(service).findByUserId(anyString());

        ResponseEntity<Preferences> result = controller.getByUserId("userId");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
        verify(service).findByUserId("userId");
    }

    @Test
    public void save_shouldReturn_emptyBody_andStatus_CREATED_whenSaved_newPreferences() throws Exception {
        Preferences preferences = aPreferences().build();
        doReturn(false).when(service).update(preferences);

        ResponseEntity result = controller.save(preferences);

        assertThat(result.getStatusCode()).isSameAs(HttpStatus.CREATED);
        assertThat(result.getBody()).isNull();
        verify(service, only()).update(preferences);
    }

    @Test
    public void save_shouldReturn_emptyBody_andStatus_NO_CONTENT_whenUpdated_existingPreferences() throws Exception {
        Preferences preferences = aPreferences().build();
        doReturn(true).when(service).update(preferences);

        ResponseEntity result = controller.save(preferences);

        assertThat(result.getStatusCode()).isSameAs(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();
        verify(service, only()).update(preferences);
    }
}

package com.ericsson.cifwk.tdm.resources;

import com.ericsson.cifwk.tdm.model.testware.ArtifactItems;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Produces({APPLICATION_XML})
public interface TestwareResource {

    @GET
    ArtifactItems getTestware();
}

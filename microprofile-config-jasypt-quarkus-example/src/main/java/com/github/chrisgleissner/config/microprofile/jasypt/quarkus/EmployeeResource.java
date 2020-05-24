package com.github.chrisgleissner.config.microprofile.jasypt.quarkus;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/employee")
@Produces(MediaType.APPLICATION_JSON)
public class EmployeeResource {
    // Can't use constructor injection since JaCoCo requires implicit constructor to report correct coverage, see https://quarkus.io/guides/tests-with-coverage
    @Inject private EntityManager em;

    @GET @Path("/{id}")
    public Employee findById(@PathParam("id") Long id) {
        return Optional.ofNullable(em.find(Employee.class, id))
                .orElseThrow(() -> new WebApplicationException("Can't find employee by ID " + id, Response.Status.NOT_FOUND));
    }
}
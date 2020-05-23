package com.github.chrisgleissner.config.microprofile.jasypt.quarkus;

import lombok.RequiredArgsConstructor;

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
@Produces(MediaType.APPLICATION_JSON) @RequiredArgsConstructor
public class EmployeeResource {
    private final EntityManager em;

    @GET @Path("/{id}")
    public Employee findById(@PathParam("id") Long id) {
        return Optional.ofNullable(em.find(Employee.class, id))
                .orElseThrow(() -> new WebApplicationException("Can't find employee by ID " + id, Response.Status.NOT_FOUND));
    }
}
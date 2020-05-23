package com.github.chrisgleissner.config.microprofile.jasypt.quarkus;

import com.github.chrisgleissner.config.microprofile.jasypt.JasyptConfigSource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;
import static org.assertj.core.api.Assertions.assertThat;

@SystemProperty(key = JasyptConfigSource.JASYPT_PASSWORD, value = "pwd")
@QuarkusTest
class EmployeeResourceTest {

    @Test
    void findById() {
        Employee employee = get("/api/employee/1").then()
                .assertThat()
                .statusCode(200)
                .extract().as(Employee.class);
        assertThat(employee.getFirstname()).isEqualTo("John");
    }
}
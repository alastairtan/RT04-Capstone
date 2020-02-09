package capstone.rt04.retailbackend.controllers;

import capstone.rt04.retailbackend.entities.Customer;
import capstone.rt04.retailbackend.request.customer.CustomerChangePasswordRequest;
import capstone.rt04.retailbackend.request.customer.CustomerEmailRequest;
import capstone.rt04.retailbackend.request.customer.CustomerLoginRequest;
import capstone.rt04.retailbackend.util.ErrorMessages;

import static capstone.rt04.retailbackend.util.routeconstants.CustomerControllerRoutes.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class CustomerControllerTest {

    private static final String VALID_CUST_EMAIL = "tonystark@gmail.com";
    private static final String VALID_CUST_PASSWORD = "spiderman";

    private static Long createdCustomerId;

    @Before
    public void setUp() throws Exception {
        Customer validCustomer = new Customer("Tony", "Stark", VALID_CUST_EMAIL, VALID_CUST_PASSWORD);
        Customer createdCustomer = given().
                contentType("application/json").
                body(validCustomer).
                when().post(CUSTOMER_BASE_ROUTE + CREATE_NEW_CUSTOMER).
                then().statusCode(HttpStatus.CREATED.value()).extract().body().as(Customer.class);
        //body("email", equalTo(validCustomer.getEmail()));
        assertThat(createdCustomer.getCustomerId().equals(validCustomer.getCustomerId()));
        assertThat(createdCustomer.getOnlineShoppingCart()).isNotNull();
        assertThat(createdCustomer.getInStoreShoppingCart()).isNotNull();
        createdCustomerId = createdCustomer.getCustomerId();
    }

    @After
    public void tearDown() throws Exception {
        Customer deletedCustomer = given().
                pathParam("customerId", createdCustomerId).
                when().delete(CUSTOMER_BASE_ROUTE + DELETE_CUSTOMER).
                then().statusCode(HttpStatus.OK.value()).extract().body().as(Customer.class);

        assertThat(deletedCustomer.getCustomerId().equals(createdCustomerId));
        createdCustomerId = null;
    }

    @Test
    public void createInvalidCustomer() {
        Customer invalidCustomer = new Customer("Steve", "Rogers", "steve@tony@bucky", "blablabla");
        given().
                contentType("application/json").
                body(invalidCustomer).
                when().post(CUSTOMER_BASE_ROUTE + CREATE_NEW_CUSTOMER).
                then().statusCode(HttpStatus.BAD_REQUEST.value()).
                body("email", equalTo(ErrorMessages.EMAIL_INVALID));
    }

    @Test
    public void deleteCustomerThatDoesNotExist() {
        given().
                pathParam("customerId", 91293129).
                when().delete(CUSTOMER_BASE_ROUTE + DELETE_CUSTOMER).
                then().statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void getCustomerByEmail() {
        CustomerEmailRequest req = new CustomerEmailRequest(VALID_CUST_EMAIL);
        Customer customer = given()
                .contentType("application/json")
                .body(req)
                .when().post(CUSTOMER_BASE_ROUTE + GET_CUSTOMER_BY_EMAIL)
                .then().statusCode(HttpStatus.OK.value()).extract().body().as(Customer.class);
        assertThat(customer.getCustomerId()).isEqualTo(createdCustomerId);
    }

    @Test
    public void login() {
        CustomerLoginRequest req = new CustomerLoginRequest(VALID_CUST_EMAIL, VALID_CUST_PASSWORD);
        Customer customer = given()
                .contentType("application/json")
                .body(req)
                .when().post(CUSTOMER_BASE_ROUTE + LOGIN)
                .then().statusCode(HttpStatus.OK.value()).extract().body().as(Customer.class);
        assertThat(customer.getCustomerId()).isEqualTo(createdCustomerId);
    }

    @Test
    public void changePassword(){
        CustomerChangePasswordRequest req = new CustomerChangePasswordRequest(createdCustomerId, "wrongPw", "newPassword");
        //wrong password
        given()
                .contentType("application/json")
                .body(req)
                .when().post(CUSTOMER_BASE_ROUTE + CHANGE_PASSWORD)
                .then().statusCode(HttpStatus.UNAUTHORIZED.value());

        //invalid id
        req.setCustomerId(991239129L);
        given()
                .contentType("application/json")
                .body(req)
                .when().post(CUSTOMER_BASE_ROUTE + CHANGE_PASSWORD)
                .then().statusCode(HttpStatus.NOT_FOUND.value());

        req.setCustomerId(createdCustomerId);
        req.setOldPassword(VALID_CUST_PASSWORD);

        Customer customer = given()
                .contentType("application/json")
                .body(req)
                .when().post(CUSTOMER_BASE_ROUTE + CHANGE_PASSWORD)
                .then().statusCode(HttpStatus.OK.value()).extract().body().as(Customer.class);

        assertThat(customer.getCustomerId()).isEqualTo(createdCustomerId);
    }

}
package com.reliaquest.api.service;

import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.DeleteEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.Response;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {
    private static final String BASE_URL = "http://localhost:8112/api/v1/employee";

    private final RestTemplate restTemplate;

    public List<Employee> getAllEmployees() {
        log.debug("Fetching all employees from server");
        try {
            ResponseEntity<Response<List<Employee>>> response = restTemplate.exchange(
                    BASE_URL, HttpMethod.GET, null, new ParameterizedTypeReference<Response<List<Employee>>>() {});

            Response<List<Employee>> responseWrapper = response.getBody();
            if (responseWrapper == null || responseWrapper.data() == null) {
                log.error("Failed to retrieve employees - response was null");
                throw new RuntimeException("Failed to retrieve employees - response was null");
            }

            log.info(
                    "Successfully retrieved {} employees",
                    responseWrapper.data().size());
            return responseWrapper.data();
        } catch (HttpClientErrorException e) {
            handleHttpClientErrorException(e);
            return null;
        } catch (RestClientException e) {
            log.error("Error fetching all employees from server", e);
            throw new RuntimeException("Error fetching all employees from server", e);
        }
    }

    public Employee getEmployeeById(String id) {
        log.debug("Fetching employee by ID: {}", id);
        try {
            ResponseEntity<Response<Employee>> response = restTemplate.exchange(
                    BASE_URL + "/" + id, HttpMethod.GET, null, new ParameterizedTypeReference<Response<Employee>>() {});

            Response<Employee> responseWrapper = response.getBody();
            if (responseWrapper == null || responseWrapper.data() == null) {
                log.error("Failed to retrieve employee by id - response was null");
                throw new RuntimeException("Failed to retrieve employee by id - response was null");
            }

            log.info("Successfully retrieved employee");
            return responseWrapper.data();
        } catch (HttpClientErrorException e) {
            handleHttpClientErrorException(e);
            return null;
        } catch (RestClientException e) {
            log.error("Error fetching employee by ID: {}", id, e);
            throw new RuntimeException("Error fetching employee by ID: " + id, e);
        }
    }

    public Employee create(@NonNull CreateEmployeeInput input) {
        log.debug("Creating new employee");
        try {
            ResponseEntity<Response<Employee>> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(input),
                    new ParameterizedTypeReference<Response<Employee>>() {});
            Response<Employee> responseWrapper = response.getBody();
            if (responseWrapper == null || responseWrapper.data() == null) {
                log.error("Failed to create employee - response was null");
                throw new RuntimeException("Failed to create employee - response was null");
            }

            Employee created = responseWrapper.data();
            log.info("Successfully created employee: {} with ID: {}", created.getName(), created.getId());
            return created;
        } catch (HttpClientErrorException e) {
            handleHttpClientErrorException(e);
            return null;
        } catch (RestClientException e) {
            log.error("Error creating employee", e);
            throw new RuntimeException("Failed to create employee", e);
        }
    }

    public String delete(@NonNull DeleteEmployeeInput input) {
        log.debug("Deleting employee");
        try {
            ResponseEntity<Response<Boolean>> response = restTemplate.exchange(
                    BASE_URL,
                    HttpMethod.DELETE,
                    new HttpEntity<>(input),
                    new ParameterizedTypeReference<Response<Boolean>>() {});

            Response<Boolean> responseWrapper = response.getBody();
            if (responseWrapper == null || responseWrapper.data() == null || !responseWrapper.data()) {
                log.error("Failed to delete employee - response was null");
                throw new RuntimeException("Failed to delete employee - response was null");
            }

            log.info("Successfully deleted employee: {}", input.getName());
            return input.getName();
        } catch (HttpClientErrorException e) {
            handleHttpClientErrorException(e);
            return null;
        } catch (RestClientException e) {
            log.error("Error deleting employee", e);
            throw new RuntimeException("Failed to delete employee", e);
        }
    }

    private void handleHttpClientErrorException(HttpClientErrorException e) {
        if (e.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            log.warn("Rate limited by server (429 Too Many Requests)");
            throw new RuntimeException("Server rate limit exceeded. Please try again later.", e);
        }
        throw e;
    }
}

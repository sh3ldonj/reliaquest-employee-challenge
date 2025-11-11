package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.DeleteEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.Response;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "null"}) // using Mockito
class EmployeeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee1;
    private Employee employee2;
    private List<Employee> employees;

    @BeforeEach
    void setUp() {
        employee1 = Employee.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .salary(75000)
                .age(30)
                .title("Software Engineer")
                .email("johndoe@company.com")
                .build();

        employee2 = Employee.builder()
                .id(UUID.randomUUID())
                .name("Jane Smith")
                .salary(95000)
                .age(28)
                .title("Senior Developer")
                .email("janesmith@company.com")
                .build();

        employees = Arrays.asList(employee1, employee2);
    }

    @Test
    void getAllEmployees_ShouldReturnListOfEmployees() {
        Response<List<Employee>> responseWrapper = Response.handledWith(employees);
        ResponseEntity<Response<List<Employee>>> responseEntity = ResponseEntity.ok(responseWrapper);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        List<Employee> result = employeeService.getAllEmployees();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());
    }

    @Test
    void getAllEmployees_ShouldThrowException_WhenResponseIsNull() {
        ResponseEntity<Response<List<Employee>>> responseEntity = ResponseEntity.ok(null);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> employeeService.getAllEmployees());
        assertEquals("Failed to retrieve employees - response was null", exception.getMessage());
    }

    @Test
    void getAllEmployees_ShouldHandleRateLimitError() {
        HttpClientErrorException rateLimitException =
                new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limited");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(rateLimitException);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> employeeService.getAllEmployees());
        assertEquals("Server rate limit exceeded. Please try again later.", exception.getMessage());
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee() {
        String employeeId = employee1.getId().toString();
        Response<Employee> responseWrapper = Response.handledWith(employee1);
        ResponseEntity<Response<Employee>> responseEntity = ResponseEntity.ok(responseWrapper);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        Employee result = employeeService.getEmployeeById(employeeId);

        assertNotNull(result);
        assertEquals(employee1.getId(), result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void getEmployeeById_ShouldThrowException_WhenEmployeeNotFound() {
        String employeeId = UUID.randomUUID().toString();
        HttpClientErrorException notFoundException =
                new HttpClientErrorException(HttpStatus.NOT_FOUND, "Employee not found");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(notFoundException);

        assertThrows(HttpClientErrorException.class, () -> employeeService.getEmployeeById(employeeId));
    }

    @Test
    void create_ShouldReturnCreatedEmployee() {
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("Alice Brown");
        input.setSalary(80000);
        input.setAge(32);
        input.setTitle("Product Manager");

        Employee created = Employee.builder()
                .id(UUID.randomUUID())
                .name("Alice Brown")
                .salary(80000)
                .age(32)
                .title("Product Manager")
                .email("alicebrown@company.com")
                .build();

        Response<Employee> responseWrapper = Response.handledWith(created);
        ResponseEntity<Response<Employee>> responseEntity = ResponseEntity.ok(responseWrapper);

        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        Employee result = employeeService.create(input);

        assertNotNull(result);
        assertEquals("Alice Brown", result.getName());
        assertEquals(80000, result.getSalary());
        verify(restTemplate)
                .exchange(
                        anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    void create_ShouldHandleRateLimitError() {
        CreateEmployeeInput input = new CreateEmployeeInput();
        input.setName("Alice Brown");
        input.setSalary(80000);
        input.setAge(32);
        input.setTitle("Product Manager");

        HttpClientErrorException rateLimitException =
                new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limited");

        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(rateLimitException);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> employeeService.create(input));
        assertEquals("Server rate limit exceeded. Please try again later.", exception.getMessage());
    }

    @Test
    void delete_ShouldReturnDeletedEmployeeName() {
        DeleteEmployeeInput input = new DeleteEmployeeInput();
        input.setName("John Doe");

        Response<Boolean> responseWrapper = Response.handledWith(true);
        ResponseEntity<Response<Boolean>> responseEntity = ResponseEntity.ok(responseWrapper);

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        String result = employeeService.delete(input);

        assertEquals("John Doe", result);
        verify(restTemplate)
                .exchange(
                        anyString(),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class));
    }

    @Test
    void delete_ShouldThrowException_WhenDeleteFails() {
        DeleteEmployeeInput input = new DeleteEmployeeInput();
        input.setName("John Doe");

        Response<Boolean> responseWrapper = Response.handledWith(false);
        ResponseEntity<Response<Boolean>> responseEntity = ResponseEntity.ok(responseWrapper);

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> employeeService.delete(input));
        assertEquals("Failed to delete employee - response was null", exception.getMessage());
    }

    @Test
    void delete_ShouldHandleRateLimitError() {
        DeleteEmployeeInput input = new DeleteEmployeeInput();
        input.setName("John Doe");

        HttpClientErrorException rateLimitException =
                new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limited");

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(rateLimitException);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> employeeService.delete(input));
        assertEquals("Server rate limit exceeded. Please try again later.", exception.getMessage());
    }

    @Test
    void getAllEmployees_ShouldHandleRestClientException() {
        RestClientException restException = new RestClientException("Connection failed");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(restException);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> employeeService.getAllEmployees());
        assertEquals("Error fetching all employees from server", exception.getMessage());
    }
}

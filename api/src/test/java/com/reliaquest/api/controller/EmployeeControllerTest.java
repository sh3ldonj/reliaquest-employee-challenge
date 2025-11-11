package com.reliaquest.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private Employee employee1;
    private Employee employee2;
    private Employee employee3;
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

        employee3 = Employee.builder()
                .id(UUID.randomUUID())
                .name("Bob Johnson")
                .salary(50000)
                .age(25)
                .title("Junior Developer")
                .email("bobjohnson@company.com")
                .build();

        employees = Arrays.asList(employee1, employee2, employee3);
    }

    @Test
    void getAllEmployees_ShouldReturnListOfEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].employee_name").value("John Doe"))
                .andExpect(jsonPath("$[1].employee_name").value("Jane Smith"))
                .andExpect(jsonPath("$[2].employee_name").value("Bob Johnson"));
    }

    @Test
    void getEmployeesByNameSearch_ShouldReturnMatchingEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/v1/employee/search/John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].employee_name").value("John Doe"))
                .andExpect(jsonPath("$[1].employee_name").value("Bob Johnson"));
    }

    @Test
    void getEmployeesByNameSearch_ShouldReturnEmptyList_WhenNoMatches() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/v1/employee/search/Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getEmployeesByNameSearch_ShouldBeCaseInsensitive() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/v1/employee/search/jane"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employee_name").value("Jane Smith"));
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee() throws Exception {
        String employeeId = employee1.getId().toString();
        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee1);

        mockMvc.perform(get("/api/v1/employee/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId))
                .andExpect(jsonPath("$.employee_name").value("John Doe"))
                .andExpect(jsonPath("$.employee_salary").value(75000));
    }

    @Test
    void getHighestSalaryOfEmployees_ShouldReturnHighestSalary() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("95000"));
    }

    @Test
    void getHighestSalaryOfEmployees_ShouldReturnZero_WhenNoEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ShouldReturnTopEarners() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("Jane Smith"))
                .andExpect(jsonPath("$[1]").value("John Doe"))
                .andExpect(jsonPath("$[2]").value("Bob Johnson"));
    }

    @Test
    void createEmployee_ShouldReturnCreatedEmployee() throws Exception {
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

        when(employeeService.create(any(CreateEmployeeInput.class))).thenReturn(created);

        mockMvc.perform(
                        post("/api/v1/employee")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                    "name": "Alice Brown",
                                    "salary": 80000,
                                    "age": 32,
                                    "title": "Product Manager"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employee_name").value("Alice Brown"))
                .andExpect(jsonPath("$.employee_salary").value(80000));
    }

    @Test
    void deleteEmployeeById_ShouldReturnDeletedEmployeeName() throws Exception {
        String employeeId = employee1.getId().toString();
        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee1);
        when(employeeService.delete(any())).thenReturn("John Doe");

        mockMvc.perform(delete("/api/v1/employee/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(content().string("John Doe"));
    }

    @Test
    void deleteEmployeeById_ShouldReturn404_WhenEmployeeNotFound() throws Exception {
        String employeeId = UUID.randomUUID().toString();
        when(employeeService.getEmployeeById(employeeId))
                .thenThrow(new RuntimeException("Employee not found with ID: " + employeeId));

        mockMvc.perform(delete("/api/v1/employee/{id}", employeeId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Employee not found with ID: " + employeeId));
    }
}

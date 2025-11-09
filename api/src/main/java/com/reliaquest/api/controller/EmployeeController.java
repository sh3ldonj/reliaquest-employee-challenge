package com.reliaquest.api.controller;

import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeInput> {

    private final EmployeeService employeeService;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        return ResponseEntity.ok(employeeService.getAllEmployees().stream()
                .filter(employee -> Objects.nonNull(employee.getName())
                        && employee.getName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        List<Employee> allEmployees = employeeService.getAllEmployees();
        int highestSalary = allEmployees.stream()
                .filter(employee -> Objects.nonNull(employee.getSalary()))
                .mapToInt(Employee::getSalary)
                .max()
                .orElse(0);
        return ResponseEntity.ok(highestSalary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        List<String> topTenHighestEarningEmployeeNames = employeeService.getAllEmployees().stream()
                .filter(employee -> Objects.nonNull(employee.getSalary()) && Objects.nonNull(employee.getName()))
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .collect(Collectors.toList());
        return ResponseEntity.ok(topTenHighestEarningEmployeeNames);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@RequestBody CreateEmployeeInput input) {
        Employee created = employeeService.create(input);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(created);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        // TODO: Implement delete by ID
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

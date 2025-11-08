package com.reliaquest.api.controller;

import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
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
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        throw new UnsupportedOperationException();
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

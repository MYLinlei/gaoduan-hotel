package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "鍛樺伐鐩稿叧鎺ュ彛")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    @ApiOperation(value = "鍛樺伐鐧诲綍")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("鍛樺伐鐧诲綍: {}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());

        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    @PostMapping("/logout")
    @ApiOperation(value = "鍛樺伐閫€鍑?")
    public Result<String> logout() {
        return Result.success();
    }

    @PostMapping
    @ApiOperation("鏂板鍛樺伐")
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("鏂板鍛樺伐: {}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("鍛樺伐鍒嗛〉鏌ヨ")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("鍛樺伐鍒嗛〉鏌ヨ: {}", employeePageQueryDTO);
        return Result.success(employeeService.pageQuery(employeePageQueryDTO));
    }

    @PostMapping("/status/{status}")
    @ApiOperation("鍚敤绂佺敤鍛樺伐璐﹀彿")
    public Result startOrStop(@PathVariable("status") Integer status, Long id) {
        log.info("鍚敤绂佺敤鍛樺伐璐﹀彿: {}, {}", status, id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("鏍规嵁id鏌ヨ鍛樺伐淇℃伅")
    public Result<Employee> getById(@PathVariable("id") Long id) {
        log.info("鏍规嵁鍛樺伐id鏌ヨ淇℃伅: {}", id);
        return Result.success(employeeService.getById(id));
    }

    @PutMapping
    @ApiOperation("缂栬緫鍛樺伐淇℃伅")
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("缂栬緫鍛樺伐淇℃伅: {}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }

    @PutMapping("/editPassword")
    @ApiOperation("淇敼鍛樺伐瀵嗙爜")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        log.info("淇敼鍛樺伐瀵嗙爜");
        employeeService.editPassword(passwordEditDTO);
        return Result.success();
    }
}

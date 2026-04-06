package com.sky.controller.user;

import com.sky.dto.UserDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Api(tags = "用户登录接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${sky.jwt.user-token-name:authentication}")
    private String userTokenName;

    @PostMapping("/sendCode")
    @ApiOperation("发送短信验证码")
    public Result<String> sendCode(@RequestParam String phone) {
        log.info("send login code, phone={}", phone);
        userService.sendCode(phone);
        return Result.success("验证码已发送");
    }

    @PostMapping("/login")
    @ApiOperation("手机号验证码登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("sms login, phone={}", userLoginDTO.getPhone());
        return Result.success(userService.login(userLoginDTO));
    }

    @PostMapping("/logout")
    @ApiOperation("退出登录")
    public Result logout(@RequestHeader(value = "authentication", required = false) String token) {
        log.info("logout request, headerName={}", userTokenName);
        userService.logout(token);
        return Result.success();
    }

    @GetMapping("/me")
    @ApiOperation("当前登录用户")
    public Result<UserDTO> currentUser() {
        return Result.success(userService.currentUser());
    }
}

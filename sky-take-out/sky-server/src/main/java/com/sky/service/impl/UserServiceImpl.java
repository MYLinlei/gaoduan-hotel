package com.sky.service.impl;

import com.sky.constant.RedisLoginConstant;
import com.sky.context.UserHolder;
import com.sky.dto.UserDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.service.UserService;
import com.sky.utils.CodeUtil;
import com.sky.utils.RegexUtils;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendCode(String phone) {
        String safePhone = trim(phone);
        if (RegexUtils.isPhoneInvalid(safePhone)) {
            throw new LoginFailedException("手机号格式不正确");
        }

        String code = CodeUtil.randomCode();
        String redisKey = RedisLoginConstant.LOGIN_CODE_KEY + safePhone;
        stringRedisTemplate.opsForValue().set(
                redisKey,
                code,
                RedisLoginConstant.LOGIN_CODE_TTL_MINUTES,
                TimeUnit.MINUTES
        );
        log.info("sms login code generated, phone={}, code={}", safePhone, code);
    }

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        String phone = trim(userLoginDTO.getPhone());
        String code = trim(userLoginDTO.getCode());

        if (RegexUtils.isPhoneInvalid(phone)) {
            throw new LoginFailedException("手机号格式不正确");
        }
        if (RegexUtils.isCodeInvalid(code)) {
            throw new LoginFailedException("验证码格式不正确");
        }

        String redisCodeKey = RedisLoginConstant.LOGIN_CODE_KEY + phone;
        String cachedCode = stringRedisTemplate.opsForValue().get(redisCodeKey);
        if (cachedCode == null) {
            throw new LoginFailedException("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(code)) {
            throw new LoginFailedException("验证码错误");
        }

        User user = userMapper.getByPhone(phone);
        if (user == null) {
            user = User.builder()
                    .openid("sms-" + phone)
                    .name(CodeUtil.defaultNickname())
                    .phone(phone)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        stringRedisTemplate.delete(redisCodeKey);

        String token = CodeUtil.randomToken();
        String tokenKey = RedisLoginConstant.LOGIN_TOKEN_KEY + token;
        Map<String, String> tokenUserMap = new HashMap<>();
        tokenUserMap.put("id", String.valueOf(user.getId()));
        tokenUserMap.put("phone", phone);
        tokenUserMap.put("nickname", trim(user.getName()) == null ? CodeUtil.defaultNickname() : trim(user.getName()));
        stringRedisTemplate.opsForHash().putAll(tokenKey, tokenUserMap);
        stringRedisTemplate.expire(
                tokenKey,
                RedisLoginConstant.LOGIN_TOKEN_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        return UserLoginVO.builder()
                .id(user.getId())
                .phone(phone)
                .nickname(tokenUserMap.get("nickname"))
                .token(token)
                .build();
    }

    @Override
    public void logout(String token) {
        String safeToken = trim(token);
        if (safeToken != null) {
            stringRedisTemplate.delete(RedisLoginConstant.LOGIN_TOKEN_KEY + safeToken);
        }
    }

    @Override
    public UserDTO currentUser() {
        UserDTO userDTO = UserHolder.getUser();
        if (userDTO == null) {
            throw new LoginFailedException("未登录");
        }
        return userDTO;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }
}

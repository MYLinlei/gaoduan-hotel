package com.sky.interceptor;

import com.sky.constant.RedisLoginConstant;
import com.sky.context.BaseContext;
import com.sky.context.UserHolder;
import com.sky.dto.UserDTO;
import com.sky.properties.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getUserTokenName());
        if (token == null || token.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String redisKey = RedisLoginConstant.LOGIN_TOKEN_KEY + token.trim();
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(redisKey);
        if (entries == null || entries.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(Long.valueOf(entries.get("id").toString()));
        userDTO.setPhone(String.valueOf(entries.get("phone")));
        userDTO.setNickname(String.valueOf(entries.get("nickname")));

        UserHolder.saveUser(userDTO);
        BaseContext.setCurrentId(userDTO.getId());
        stringRedisTemplate.expire(
                redisKey,
                RedisLoginConstant.LOGIN_TOKEN_TTL_MINUTES,
                TimeUnit.MINUTES
        );
        log.info("user token check success, userId={}", userDTO.getId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
        UserHolder.removeUser();
    }
}

package com.sky.service;

import com.sky.dto.UserDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.vo.UserLoginVO;

public interface UserService {

    void sendCode(String phone);

    UserLoginVO login(UserLoginDTO userLoginDTO);

    void logout(String token);

    UserDTO currentUser();
}

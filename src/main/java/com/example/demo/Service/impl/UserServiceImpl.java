package com.example.demo.Service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.Email;

import com.example.demo.Dto.*;
import com.example.demo.common.config.jwt.JwtUtil;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.User;
import com.example.demo.ModelMapper.UserMapper;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Service.UserService;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

//	@Autowired
//	private MapperDto mapperDto;

    //@Autowired
    private UserMapper userMapper;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MapperDto mapperDto;

    /**
     * User login
     *
     * @param input LoginDto
     * @return
     */
    @Override
    public UserJwtDto userLogin(LoginDto input) {
        UserDetails userDetails = this.userDetailService.loadUserByUsername(input.getUserName());

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDetails, input.getPassword(), userDetails.getAuthorities()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = this.userRepository.findUserByUserName(userDetails.getUsername());

        return this.getUserJwt(authentication, user);
    }

    @Override
    public UserJwtDto userRegister(RegisterDto input) {
        User user = this.mapperDto.convertUser(input);
        user.setUserName(input.getUserName());
        user.setPassword(this.passwordEncoder.encode(input.getPassword()));
        user.setLevel(input.getLevel());
        user.setCreateAt(LocalDateTime.now());
        this.userRepository.save(user);

        return this.userLogin(input);
    }

    @Override
    public boolean isCheckEmail(@Email String email) {
        if (Strings.isNotEmpty(email)) {
            return this.userRepository.existsByEmail(email);
        }
        return false;
    }

    @Override
    public boolean isCheckUserName(String userName) {
        if (Strings.isNotEmpty(userName)) {
            return this.userRepository.existsByUserName(userName);
        }
        return false;
    }

    @Override
    public UserDto updateUser(UserDto userDto, int idUser) {
        User user = userRepository.findById(idUser).get();
        if (user == null)
            return null;
        user.setEmail(userDto.getEmail());
        user.setAddress(userDto.getAddress());
        user.setBirth(userDto.getBirth());
        user.setFullName(userDto.getFullName());
        user.setSex(userDto.isSex());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setUpdateAt(LocalDateTime.now());
        return this.userMapper.getInstance().toDto(userRepository.save(user));
    }

    @Override
    public List<UserDto> getAllUser() {
        List<UserDto> list = new ArrayList<>();
        list = userRepository.findAll().stream().map(i -> this.userMapper.getInstance().toDto(i)).collect(Collectors.toList());
        return list;
    }

    @Override
    public UserDto findUserByUserName(String userName) {
        return this.userMapper.getInstance().toDto(userRepository.findAllByUserName(userName).get(0));
    }

    @Override
    public List<UserDto> getAllUserByLevel(int level) {
        return this.getAllUser().stream().filter(i -> i.getLevel() == level).collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(int id) {
        UserDto userDto = this.userMapper.getInstance().toDto(this.userRepository.findById(id).get());
        return userDto == null ? null : userDto;
    }

    private UserJwtDto getUserJwt(Authentication authentication, User user) {
        String jwtToken = jwtUtil.generateJwtToken(authentication);
        String role = String.valueOf(user.getLevel()) ;

        return new UserJwtDto(jwtToken, role, this.mapperDto.convertUserDto(user));
    }
}

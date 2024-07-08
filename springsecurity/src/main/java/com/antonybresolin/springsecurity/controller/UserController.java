package com.antonybresolin.springsecurity.controller;

import com.antonybresolin.springsecurity.controller.dto.CreateUserDto;
import com.antonybresolin.springsecurity.entities.Role;
import com.antonybresolin.springsecurity.entities.User;
import com.antonybresolin.springsecurity.repository.RoleRepository;
import com.antonybresolin.springsecurity.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
public class UserController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @PostMapping("/users")
    public ResponseEntity<Void> newUser(@RequestBody CreateUserDto dto) {

        var basicRole = roleRepository.findByName(Role.Values.BASIC.name());
        var userFromDB = userRepository.findByUsername(dto.username());

        userFromDB.ifPresentOrElse(
                user -> {
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY);
                },
                () -> {
                    var user = new User();
                    user.setUsername(dto.username());
                    user.setPassword(passwordEncoder.encode(dto.password()));
                    user.setRoles(Set.of(basicRole));
                    userRepository.save(user);
                }
        );

        return ResponseEntity.ok().build();
    }
}

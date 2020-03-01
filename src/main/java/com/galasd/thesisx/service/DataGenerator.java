package com.galasd.thesisx.service;

import com.galasd.thesisx.security.RoleEntity;
import com.galasd.thesisx.security.UserEntity;
import com.galasd.thesisx.security.UserRepository;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;

@SpringComponent
public class DataGenerator {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public DataGenerator(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void loadData() {
        // Use existing database
        if (userRepository.count() != 0L) {
            return;
        }

        createCommon(userRepository, passwordEncoder);
        createAdmin(userRepository, passwordEncoder);
        // A set of products without constrains that can be deleted
        createDeletableUsers(userRepository, passwordEncoder);
    }

    private UserEntity createCommon(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return userRepository.save(
                createUser("common@vaadin.com", "Jo", "Carter",
                        passwordEncoder.encode("common"), RoleEntity.COMMON, false));
    }

    private UserEntity createAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return userRepository.save(
                createUser("admin@vaadin.com", "John", "Smith",
                        passwordEncoder.encode("admin"), RoleEntity.ADMIN, true));
    }

    private void createDeletableUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        userRepository.save(createUser("adam@vaadin.com", "Adam", "Hamilton",
                        passwordEncoder.encode("adam"), RoleEntity.COMMON, false));
        userRepository.save(createUser("david@vaadin.com", "David", "Miller",
                passwordEncoder.encode("david"), RoleEntity.COMMON, false));
    }

    private UserEntity createUser(String email, String firstName, String lastName, String passwordHash, String role,
                                  boolean locked) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setLocked(locked);
        return user;
    }
}
package com.findit.service;

import com.findit.model.Role;
import com.findit.model.User;
import com.findit.repository.UserRepository;
import com.findit.util.Validator;
import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository userRepository;
    
     

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        seedAdmin();
    }

    private void seedAdmin() {
        User admin = new User(userRepository.generateId(), "Admin",
                "admin@findit.com", "admin123", "9000000000", Role.ADMIN);
        userRepository.save(admin);
    }

    public User register(String name, String email, String password, String phone) {
        if (!Validator.isNotBlank(name)) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (!Validator.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (!Validator.isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (!Validator.isValidPhone(phone)) {
            throw new IllegalArgumentException("Phone must be exactly 10 digits.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered.");
        }

        User user = new User(userRepository.generateId(), name.trim(),
                email.trim().toLowerCase(), password, phone, Role.USER);
        userRepository.save(user);
        return user;
    }

    public Optional<User> login(String email, String password) {
        Optional<User> found = userRepository.findByEmail(email);
        if (found.isPresent() && found.get().getPassword().equals(password)) {
            return found;
        }
        return Optional.empty();
    }

    public void updatePhone(User user, String newPhone) {
        if (!Validator.isValidPhone(newPhone)) {
            throw new IllegalArgumentException("Phone must be exactly 10 digits.");
        }
        user.setPhone(newPhone);
    }
    public void changeEmail(User user, String newEmail) {
        if (!Validator.isValidEmail(newEmail)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        Optional<User> existing = userRepository.findByEmail(newEmail);
        if (existing.isPresent() && existing.get().getId() != user.getId()) {
            throw new IllegalArgumentException("Email already used by another account.");
        }
        user.setEmail(newEmail.trim().toLowerCase());
    }
    

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    private static final ItemMenu itemMenu = new ItemMenu(scanner,
            new ItemService(new ItemRepository<LostItem>(), new ItemRepository<FoundItem>()));
}
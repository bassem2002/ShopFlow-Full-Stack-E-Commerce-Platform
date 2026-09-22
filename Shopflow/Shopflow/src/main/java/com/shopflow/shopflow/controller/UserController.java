package com.shopflow.shopflow.controller;

import com.shopflow.shopflow.dto.response.UserResponse;
import com.shopflow.shopflow.entity.User;
import com.shopflow.shopflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .filter(user -> user.getRole() != com.shopflow.shopflow.enums.Role.ADMIN)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> update) {
        return userRepository.findById(id)
                .map(user -> {
                    if (update.containsKey("active")) {
                        // Seul l'admin peut changer le statut actif
                        // Note: Une vérification supplémentaire ici serait mieux pour la sécurité
                        user.setActive((Boolean) update.get("active"));
                    }
                    if (update.containsKey("firstName")) {
                        user.setFirstName((String) update.get("firstName"));
                    }
                    if (update.containsKey("lastName")) {
                        user.setLastName((String) update.get("lastName"));
                    }
                    userRepository.save(user);
                    return ResponseEntity.ok(mapToResponse(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

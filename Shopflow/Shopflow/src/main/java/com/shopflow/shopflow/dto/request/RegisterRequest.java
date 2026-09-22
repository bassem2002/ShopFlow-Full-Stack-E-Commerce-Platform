package com.shopflow.shopflow.dto.request;

import com.shopflow.shopflow.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * DTO utilisé pour recevoir les données du frontend
 * lors de l'inscription.
 * Bonne pratique :
 * on ne reçoit pas directement une Entity dans le Controller.
 */
@Data
public class RegisterRequest {
    /*
     * @NotBlank -> champ obligatoire et non vide
     * @Email -> vérifie le format email
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;
    /*
     * Mot de passe obligatoire
     * On pourra plus tard ajouter une règle de complexité
     */
    @NotBlank(message = "Password is required")
    @jakarta.validation.constraints.Size(min = 8, message = "Password must be at least 8 characters long")
    @jakarta.validation.constraints.Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
        message = "Password must contain at least one digit, one lowercase and one uppercase letter"
    )
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    /*
     * Le rôle sera maintenant envoyé par le client.
     * Valeurs attendues : CUSTOMER ou SELLER
     *
     * On évite ADMIN à l'inscription publique.
     */
    private Role role;

    /*
     * Champs utiles si role = SELLER
     */
    private String storeName;
    private String storeDescription;
    private String storeLogo;

}
package pl.where2play.w2papi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.where2play.w2papi.model.User;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for User entity.
 * Used for transferring user data between client and server.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private String pictureUrl;
    private String provider;
    private LocalDateTime createdAt;

    /**
     * Converts a User entity to a UserDTO.
     *
     * @param user the User entity to convert
     * @return the UserDTO
     */
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .pictureUrl(user.getPictureUrl())
                .provider(user.getProvider() != null ? user.getProvider().name() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
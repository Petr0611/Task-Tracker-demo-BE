package de.upteams.tasktracker.user.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Oleg Mordkovich
 * {@code @date} 02.11.2025
 */

public class ChangePasswordRequestDTO {
    @Schema(
            description = "old User password",
            example = "letmein"
    )
    private String oldPassword;
    @Schema(
            description = "new User password",
            example = "letmein!!!"
    )
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
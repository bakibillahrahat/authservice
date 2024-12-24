package org.mbr.authservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.mbr.authservice.entities.UserInfo;

@JsonNaming (PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto extends UserInfo {
    @NonNull
    private String username;
    @NonNull
    private String lastname;
    private Long phoneNumber;
    private String email;
}

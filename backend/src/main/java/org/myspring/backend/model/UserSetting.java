package org.myspring.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_settings")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Column(name = "api_key_encrypted", columnDefinition = "TEXT")
    private String apiKey;

    @Builder.Default
    @Column(name = "app_theme", nullable = false)
    private String appTheme = "dark";

    @JsonIgnore // prevents JSON recursion
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}

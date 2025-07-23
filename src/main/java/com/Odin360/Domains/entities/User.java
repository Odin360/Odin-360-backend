package com.Odin360.Domains.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Set;


@Entity

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String username;
    @Column(unique = true,nullable = false)
    private String email;
    private String profileImage;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private boolean enabled;
    @ManyToMany
    @JoinTable(name = "user_team",
            joinColumns = @JoinColumn(name = "user_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "team_id",referencedColumnName = "id")
           )
    private Set<Team> teams = new HashSet<>();
    @Column(name = "verificationCodeTime")
    private LocalDateTime verificationCodeExpiresAt;
    private String verificationCode;
}

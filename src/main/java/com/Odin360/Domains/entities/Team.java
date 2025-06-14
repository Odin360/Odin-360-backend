package com.Odin360.Domains.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.UUID;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of="id")
@Table(name = "team")
public class Team {
    private String name;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(columnDefinition = "Text")
    private String description;
    private String drive;
    @ManyToMany(mappedBy = "teams",fetch = FetchType.EAGER)
    private Set <User> users = new HashSet<>();
}

package com.Odin360.Domains.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "team")
public class Team {
    private String name;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(columnDefinition = "Text")
    private String description;
    private String drive;
    @ManyToMany(mappedBy = "teams")
    private List <User> users = new ArrayList<>();
}

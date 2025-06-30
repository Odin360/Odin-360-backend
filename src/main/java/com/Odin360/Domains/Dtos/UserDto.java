package com.Odin360.Domains.Dtos;

import com.Odin360.Domains.entities.Team;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class UserDto {
    private String email;
    private String username;
    private UUID id;
    private Set<TeamResponse>teams=new HashSet<>();

}

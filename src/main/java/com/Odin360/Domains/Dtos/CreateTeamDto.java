package com.Odin360.Domains.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTeamDto {
    private UUID uuid;
    private String name;
    private String description;
    private String drive;

}

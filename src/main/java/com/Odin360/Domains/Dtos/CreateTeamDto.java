package com.Odin360.Domains.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTeamDto {
    private String name;
    private String description;
    private String drive;

}

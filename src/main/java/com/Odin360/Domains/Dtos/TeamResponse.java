package com.Odin360.Domains.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamResponse {
    private UUID UUID;
    private String name;
    private String drive;
}

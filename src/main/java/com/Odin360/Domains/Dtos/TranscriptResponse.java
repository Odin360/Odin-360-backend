package com.Odin360.Domains.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptResponse {
    private UUID teamId;
    private String transcript;
}

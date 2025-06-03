package com.Odin360.mappers;

import com.Odin360.Domains.Dtos.CreateTeamDto;
import com.Odin360.Domains.Dtos.TeamResponse;
import com.Odin360.Domains.entities.Team;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamMapper {
    Team fromCreateTeamDto (CreateTeamDto createTeamDto);
    TeamResponse toTeamResponse (Team team);
}

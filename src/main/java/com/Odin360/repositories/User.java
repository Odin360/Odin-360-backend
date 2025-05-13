package com.Odin360.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface User extends JpaRepository<User, UUID> {
}

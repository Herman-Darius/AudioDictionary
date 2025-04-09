package com.example.SCDProiectv2.Repositories;

import com.example.SCDProiectv2.Models.Courier;
import com.example.SCDProiectv2.Models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Integer> {
    Optional<Courier> findByUsername(String username);
    boolean existsByEmail(String email);
    Optional<List<Courier>> findAllByManagerId(int managerId);
    List<Courier> findAllByRole(Role role);
    @Query("SELECT c FROM Courier c LEFT JOIN FETCH c.manager WHERE c.id = :id")
    Optional<Courier> findByIdWithManager(@Param("id") Integer id);
    @Query("SELECT c FROM Courier c LEFT JOIN FETCH c.manager")
    Optional<List<Courier>> findAllWithManagers();

    boolean existsByUsername(String username);

}

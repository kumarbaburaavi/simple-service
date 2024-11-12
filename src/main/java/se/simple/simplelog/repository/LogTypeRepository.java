package se.simple.simplelog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import se.simple.simplelog.model.LogType;

public interface LogTypeRepository extends JpaRepository<LogType, Long> {

    @EntityGraph(value = "LogType.name")
    Optional<LogType> findByName(String name);
}

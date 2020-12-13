package dev.yxy.reactive.dao.repository;

import dev.yxy.reactive.model.entity.Human;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HumanRepository extends R2dbcRepository<Human, Long> {
}

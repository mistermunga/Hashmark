package xyz.ryansbeanfactory.hashmark.hashmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.ryansbeanfactory.hashmark.hashmark.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}

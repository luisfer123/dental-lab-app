package com.dental.lab.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dental.lab.model.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	Optional<User> findByUsername(String username);
	
	boolean existsByUsername(String username);
	
	boolean existsByEmail(String email);
	
	@Query("select u from User u where u.username like %:pattern%")
	List<User> searchByUsernameLike(@Param("pattern") String pattern);
	
	@Query("select u from User u where u.email like %:pattern%")
	List<User> searchByEmailLike(@Param("pattern") String pattern);
	
	@Query("select u from User u where u.firstName like %:pattern% or "
			+ "u.firstLastName like  %:pattern% or u.secondLastName like  %:pattern%")
	List<User> searchByNameLike(@Param("pattern") String pattern);

}

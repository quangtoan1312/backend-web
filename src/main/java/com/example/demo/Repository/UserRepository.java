package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.User;
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	User findUserByLevel (int level);
	User findUserByUserName (String userName);
	boolean existsByEmail(String email);
	boolean existsByUserName(String userName);
	List<User> findAllByUserName(String userName);
	List<User> findAllByLevel(int level);

}

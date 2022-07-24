package com.ranjit.contactmanager.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ranjit.contactmanager.entities.Contact;
import com.ranjit.contactmanager.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	
	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pageable);
	
	public List<Contact> findByNameContainingAndUser(String nameKeyword,User user);
	
}

package com.example.viegymapp.repository;

import com.example.viegymapp.entity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostMediaRepository extends JpaRepository<PostMedia, UUID> {
}

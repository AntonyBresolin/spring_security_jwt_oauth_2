package com.antonybresolin.springsecurity.repository;

import com.antonybresolin.springsecurity.entities.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TweetRepository extends JpaRepository<Tweet, Long> {
}

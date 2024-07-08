package com.antonybresolin.springsecurity.controller;

import com.antonybresolin.springsecurity.controller.dto.CreateTweetDto;
import com.antonybresolin.springsecurity.controller.dto.FeedDto;
import com.antonybresolin.springsecurity.controller.dto.FeedItemDto;
import com.antonybresolin.springsecurity.entities.Role;
import com.antonybresolin.springsecurity.entities.Tweet;
import com.antonybresolin.springsecurity.entities.User;
import com.antonybresolin.springsecurity.repository.TweetRepository;
import com.antonybresolin.springsecurity.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/tweets")
public class TweetController {
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    public TweetController(TweetRepository tweetRepository, UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Void> createTweet(@RequestBody CreateTweetDto dto,
                                            JwtAuthenticationToken token) {
        String username = token.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var user = userOptional.get();

        var tweet = new Tweet();
        tweet.setUser(user);
        tweet.setContent(dto.content());

        tweetRepository.save(tweet);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTweet(@PathVariable("id") Long tweetId,
                                            JwtAuthenticationToken token) {
        String username = token.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var user = userOptional.get();
        var tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var isAdmin = user.getRoles()
                .stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        if (isAdmin || tweet.getUser().getId().equals(user.getId())) {
            tweetRepository.deleteById(tweetId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDto> feed(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        var tweets = tweetRepository.findAll(
                        PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
                .map(tweet ->
                        new FeedItemDto(
                                tweet.getTweetId(),
                                tweet.getContent(),
                                tweet.getUser().getUsername())
                );

        return ResponseEntity.ok(new FeedDto(
                tweets.getContent(), page, pageSize, tweets.getTotalPages(), tweets.getTotalElements()));
    }
}

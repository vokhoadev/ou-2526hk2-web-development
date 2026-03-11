package com.ou.springcode.repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.ou.springcode.model.User;

import jakarta.annotation.PostConstruct;

@Repository
public class UserRepository implements IUserRepository {
    private final Map<Long, User> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostConstruct
    public void init(){
        User u1 = new User(null, "nva", "nva@gmail.com", "Nguyễn Văn A", null);
        User u2 = new User(null, "nvb", "nvb@gmail.com", "Nguyễn Văn B", null);
        User u3 = new User(null, "nvc", "nvc@gmail.com", "Nguyễn Văn C", null);
        
        save(u1);
        save(u2);
        save(u3);
    }

    public List<User> findAll() {
        return new ArrayList<>(store.values());
    }

    public User save(User user) {
        if(user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
            user.setCreatedAt(java.time.LocalDateTime.now());
        }
        store.put(user.getId(), user);
        return user;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
}

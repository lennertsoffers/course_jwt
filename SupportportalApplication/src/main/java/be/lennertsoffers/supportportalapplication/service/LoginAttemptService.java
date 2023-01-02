package be.lennertsoffers.supportportalapplication.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    private static final int MAXIMUM_NUMBER_OF_ATTEMPTS = 5;
    private static final int ATTEMPT_INCREMENT = 1;

    private final LoadingCache<String, Integer> loginAttemptCache;

    public LoginAttemptService() {
        super();
        this.loginAttemptCache = CacheBuilder
                .newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public void evictUserFromLoginAttemptCache(String username) {
        this.loginAttemptCache.invalidate(username);
    }

    public void addUserToLoginAttemptCache(String username) {
        int attempts;

        try {
            attempts = this.loginAttemptCache.get(username) + ATTEMPT_INCREMENT;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        this.loginAttemptCache.put(username, attempts);
    }

    public boolean hasExceededMaxAttempts(String username) {
        try {
            return this.loginAttemptCache.get(username) <= MAXIMUM_NUMBER_OF_ATTEMPTS;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}

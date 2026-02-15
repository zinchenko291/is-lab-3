package me.zinch.is.islab2.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import me.zinch.is.islab2.exceptions.AuthException;
import me.zinch.is.islab2.exceptions.ConflictException;
import me.zinch.is.islab2.exceptions.ResourceNotFoundException;
import me.zinch.is.islab2.models.dao.implementations.UserDao;
import me.zinch.is.islab2.models.dto.user.UserDto;
import me.zinch.is.islab2.models.dto.user.UserMapper;
import me.zinch.is.islab2.models.entities.User;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AuthService {
    private static final int CHALLENGE_BYTES = 32;
    private static final long CHALLENGE_TTL_MS = 2 * 60 * 1000L;

    private final Map<String, PendingRegistration> registrationChallenges = new ConcurrentHashMap<>();
    private final Map<String, PendingLogin> loginChallenges = new ConcurrentHashMap<>();

    private UserDao userDao;
    private UserMapper userMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService() { }

    @Inject
    public AuthService(UserDao userDao, UserMapper userMapper) {
        this.userDao = userDao;
        this.userMapper = userMapper;
    }

    public String startRegistration(String name, String pubkey, String email) {
        if (userDao.findByPubkey(pubkey).isPresent()) {
            throw new ConflictException("Public key already registered");
        }
        parsePublicKey(pubkey);

        String challenge = generateChallenge();
        registrationChallenges.put(pubkey, new PendingRegistration(name, pubkey, email, challenge, expiresAt()));
        return challenge;
    }

    @Transactional
    public UserDto finishRegistration(String pubkey, String signatureBase64) {
        PendingRegistration pending = getValidRegistration(pubkey);
        if (pending == null) {
            throw new AuthException("Registration challenge not found");
        }

        if (!verifySignature(pending.pubkey, pending.challenge, signatureBase64)) {
            throw new AuthException("Invalid signature");
        }

        User user = new User();
        user.setName(pending.name);
        user.setPubkey(pending.pubkey);
        user.setEmail(pending.email);
        user.setIsAdmin(userDao.count() == 0);
        user = userDao.create(user);
        registrationChallenges.remove(pubkey);
        return userMapper.entityToDto(user);
    }

    public String startLogin(String pubkey) {
        parsePublicKey(pubkey);
        User user = userDao.findByPubkey(pubkey)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String challenge = generateChallenge();
        loginChallenges.put(user.getPubkey(), new PendingLogin(challenge, expiresAt()));
        return challenge;
    }

    public UserDto finishLogin(String pubkey, String signatureBase64) {
        User user = userDao.findByPubkey(pubkey)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        PendingLogin pending = getValidLogin(user.getPubkey());
        String challenge = pending == null ? null : pending.challenge;
        if (challenge == null) {
            throw new AuthException("Login challenge not found");
        }

        if (!verifySignature(user.getPubkey(), challenge, signatureBase64)) {
            throw new AuthException("Invalid signature");
        }

        loginChallenges.remove(user.getPubkey());
        return userMapper.entityToDto(user);
    }

    private String generateChallenge() {
        byte[] challengeBytes = new byte[CHALLENGE_BYTES];
        secureRandom.nextBytes(challengeBytes);
        return Base64.getEncoder().encodeToString(challengeBytes);
    }

    private boolean verifySignature(String pubkeyBase64, String challengeBase64, String signatureBase64) {
        PublicKey publicKey = parsePublicKey(pubkeyBase64);
        try {
            byte[] challengeBytes = Base64.getDecoder().decode(challengeBase64);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            Signature signature = Signature.getInstance("Ed25519");
            signature.initVerify(publicKey);
            signature.update(challengeBytes);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

    private PublicKey parsePublicKey(String pubkeyBase64) {
        try {
            byte[] pubkeyBytes = Base64.getDecoder().decode(pubkeyBase64);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
            return keyFactory.generatePublic(new X509EncodedKeySpec(pubkeyBytes));
        } catch (IllegalArgumentException e) {
            throw new AuthException("Invalid public key base64");
        } catch (Exception e) {
            throw new AuthException("Invalid public key X509");
        }
    }

    private long expiresAt() {
        return System.currentTimeMillis() + CHALLENGE_TTL_MS;
    }

    private PendingRegistration getValidRegistration(String pubkey) {
        PendingRegistration pending = registrationChallenges.get(pubkey);
        if (pending == null) {
            return null;
        }
        if (System.currentTimeMillis() > pending.expiresAt) {
            registrationChallenges.remove(pubkey);
            return null;
        }
        return pending;
    }

    private PendingLogin getValidLogin(String pubkey) {
        PendingLogin pending = loginChallenges.get(pubkey);
        if (pending == null) {
            return null;
        }
        if (System.currentTimeMillis() > pending.expiresAt) {
            loginChallenges.remove(pubkey);
            return null;
        }
        return pending;
    }

    private record PendingRegistration(String name, String pubkey, String email, String challenge, long expiresAt) {
    }

    private record PendingLogin(String challenge, long expiresAt) {
    }
}

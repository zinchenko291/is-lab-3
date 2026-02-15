package me.zinch.is.islab2.models.dao.implementations;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import me.zinch.is.islab2.models.dao.helpers.UserFieldConverter;
import me.zinch.is.islab2.models.entities.User;
import me.zinch.is.islab2.models.fields.UserField;

import java.util.Optional;

@ApplicationScoped
public class UserDao extends AbstractDao<User, UserField> {
    public UserDao() {
        super(User.class);
    }

    @Inject
    public UserDao(UserFieldConverter converter) {
        super(User.class, converter);
    }

    public Optional<User> findByName(String name) {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.name = :name",
                User.class
        );
        query.setParameter("name", name);
        return query.getResultStream().findFirst();
    }

    public Optional<User> findByPubkey(String pubkey) {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.pubkey = :pubkey",
                User.class
        );
        query.setParameter("pubkey", pubkey);
        return query.getResultStream().findFirst();
    }
}

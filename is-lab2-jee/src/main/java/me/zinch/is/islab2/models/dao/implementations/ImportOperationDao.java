package me.zinch.is.islab2.models.dao.implementations;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import me.zinch.is.islab2.Config;
import me.zinch.is.islab2.models.entities.ImportOperation;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ImportOperationDao {
    @PersistenceContext(unitName = Config.UNIT_NAME)
    private EntityManager em;

    public Optional<ImportOperation> findById(Integer id) {
        return Optional.ofNullable(em.find(ImportOperation.class, id));
    }

    public ImportOperation create(ImportOperation operation) {
        em.persist(operation);
        em.flush();
        return operation;
    }

    public ImportOperation update(ImportOperation operation) {
        return em.merge(operation);
    }

    public List<ImportOperation> findAll() {
        return em.createQuery(
                        "SELECT i FROM ImportOperation i ORDER BY i.id DESC",
                        ImportOperation.class
                )
                .getResultList();
    }

    public List<ImportOperation> findAllByUser(Integer userId) {
        return em.createQuery(
                        "SELECT i FROM ImportOperation i WHERE i.user.id = :userId ORDER BY i.id DESC",
                        ImportOperation.class
                )
                .setParameter("userId", userId)
                .getResultList();
    }
}

package me.zinch.is.islab2.models.dao.implementations;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import me.zinch.is.islab2.Config;
import me.zinch.is.islab2.models.entities.ImportConflict;
import me.zinch.is.islab2.models.entities.ImportConflictResolution;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ImportConflictDao {
    @PersistenceContext(unitName = Config.UNIT_NAME)
    private EntityManager em;

    public Optional<ImportConflict> findById(Integer id) {
        return Optional.ofNullable(em.find(ImportConflict.class, id));
    }

    public ImportConflict create(ImportConflict conflict) {
        em.persist(conflict);
        em.flush();
        return conflict;
    }

    public ImportConflict update(ImportConflict conflict) {
        return em.merge(conflict);
    }

    public List<ImportConflict> findByOperation(Integer operationId) {
        return em.createQuery(
                        "SELECT c FROM ImportConflict c WHERE c.operation.id = :operationId ORDER BY c.id ASC",
                        ImportConflict.class
                )
                .setParameter("operationId", operationId)
                .getResultList();
    }

    public List<ImportConflict> findUnresolvedByOperation(Integer operationId) {
        return em.createQuery(
                        "SELECT c FROM ImportConflict c WHERE c.operation.id = :operationId AND c.resolution = :resolution ORDER BY c.id ASC",
                        ImportConflict.class
                )
                .setParameter("operationId", operationId)
                .setParameter("resolution", ImportConflictResolution.UNRESOLVED)
                .getResultList();
    }
}

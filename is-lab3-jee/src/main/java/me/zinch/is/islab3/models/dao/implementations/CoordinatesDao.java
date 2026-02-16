package me.zinch.is.islab3.models.dao.implementations;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import me.zinch.is.islab3.models.entities.Coordinates;
import me.zinch.is.islab3.models.fields.Filter;
import me.zinch.is.islab3.models.fields.CoordinatesField;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CoordinatesDao extends AbstractDao<Coordinates, CoordinatesField> {
    public CoordinatesDao() {
        super(Coordinates.class);
    }

    public List<Coordinates> findAllPagedByOwner(Integer page, Integer pageSize, Filter<CoordinatesField> filter, Integer ownerId) {
        return readThroughCache(
                "coordinates:findAllPagedByOwner:" + page + ":" + pageSize + ":" + ownerId + ":" + filterKey(filter),
                () -> {
                    CriteriaBuilder cb = em.getCriteriaBuilder();
                    CriteriaQuery<Coordinates> cq = cb.createQuery(Coordinates.class);
                    Root<Coordinates> root = cq.from(Coordinates.class);

                    List<Predicate> predicates = new ArrayList<>();
                    List<Order> orders = new ArrayList<>();

                    applyPredicate(predicates, filter, cb, root);
                    predicates.add(cb.equal(root.get("owner").get("id"), ownerId));
                    applyOrder(orders, filter, cb, root);

                    cq.select(root)
                            .where(predicates.toArray(new Predicate[0]))
                            .orderBy(orders);

                    TypedQuery<Coordinates> q = cacheQuery(em.createQuery(cq));
                    q.setMaxResults(pageSize);
                    q.setFirstResult(page * pageSize);
                    return q.getResultList();
                }
        );
    }

    public Long countPagedByOwner(Filter<CoordinatesField> filter, Integer ownerId) {
        return readThroughCache(
                "coordinates:countPagedByOwner:" + ownerId + ":" + filterKey(filter),
                () -> {
                    CriteriaBuilder cb = em.getCriteriaBuilder();
                    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
                    Root<Coordinates> root = cq.from(Coordinates.class);

                    List<Predicate> predicates = new ArrayList<>();
                    applyPredicate(predicates, filter, cb, root);
                    predicates.add(cb.equal(root.get("owner").get("id"), ownerId));

                    cq.select(cb.count(root))
                            .where(predicates.toArray(new Predicate[0]));

                    return cacheQuery(em.createQuery(cq)).getSingleResult();
                }
        );
    }

    public Optional<Coordinates> findByXY(double x, Double y) {
        return readThroughCache(
                "coordinates:findByXY:" + x + ":" + y,
                () -> {
                    List<Coordinates> results = cacheQuery(em.createQuery(
                                    "SELECT c FROM Coordinates c WHERE c.x = :x AND c.y = :y",
                                    Coordinates.class
                            ))
                            .setParameter("x", x)
                            .setParameter("y", y)
                            .setMaxResults(1)
                            .getResultList();
                    return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
                }
        );
    }
}

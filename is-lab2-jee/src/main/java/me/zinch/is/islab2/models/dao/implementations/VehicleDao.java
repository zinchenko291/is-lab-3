package me.zinch.is.islab2.models.dao.implementations;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.validation.ConstraintViolationException;
import me.zinch.is.islab2.exceptions.ConstraintException;
import me.zinch.is.islab2.models.dao.helpers.VehicleFieldConverter;
import me.zinch.is.islab2.models.dao.interfaces.IVehicleDao;
import me.zinch.is.islab2.models.entities.Coordinates;
import me.zinch.is.islab2.models.entities.FuelType;
import me.zinch.is.islab2.models.entities.Vehicle;
import me.zinch.is.islab2.models.fields.Filter;
import me.zinch.is.islab2.models.fields.Range;
import me.zinch.is.islab2.models.fields.VehicleField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class VehicleDao extends AbstractDao<Vehicle, VehicleField> implements IVehicleDao {
    public VehicleDao() {
        super(Vehicle.class);
    }

    @Inject
    public VehicleDao(VehicleFieldConverter converter) {
        super(Vehicle.class, converter);
    }

    @Override
    public Vehicle create(Vehicle entity) {
        try {
            Coordinates coordinates = em.getReference(Coordinates.class, entity.getCoordinates().getId());
            entity.setCoordinates(coordinates);
            em.persist(entity);
            em.flush();
            em.refresh(entity);
            return entity;
        } catch (ConstraintViolationException e) {
            throw new ConstraintException(String.format("Ошибка валидации значения(ий). %s", getConstraintMessage(e)));
        }
        catch (NullPointerException e) {
            throw new ConstraintException("Проставлены значения null в недопустимые поля.");
        }
    }

    @Override
    public Vehicle update(Vehicle entity) {
        Vehicle vehicle = em.find(Vehicle.class, entity.getId());
        entity.setCreationDate(vehicle.getCreationDate());
        em.merge(entity);
        return entity;
    }

    @Override
    public Optional<Vehicle> findMinEnginePower() {
        try {
            Vehicle result = (Vehicle) em.createNativeQuery(
                    "SELECT * FROM get_vehicle_with_min_engine_power()",
                    Vehicle.class
            ).getSingleResult( );
            return Optional.ofNullable(result);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<Vehicle> findMinEnginePowerByOwner(Integer ownerId) {
        List<Vehicle> results = em.createQuery(
                        "SELECT v FROM Vehicle v WHERE v.owner.id = :ownerId ORDER BY v.enginePower ASC, v.id ASC",
                        Vehicle.class
                )
                .setParameter("ownerId", ownerId)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    public Long countGtFuelTypeByOwner(Integer ownerId, FuelType fuelType) {
        List<FuelType> greaterFuelTypes = Arrays.stream(FuelType.values())
                .filter(type -> type.compareTo(fuelType) > 0)
                .toList();
        if (greaterFuelTypes.isEmpty()) {
            return 0L;
        }

        return em.createQuery(
                        "SELECT COUNT(v) FROM Vehicle v WHERE v.owner.id = :ownerId AND v.fuelType IN :fuelTypes",
                        Long.class
                )
                .setParameter("ownerId", ownerId)
                .setParameter("fuelTypes", greaterFuelTypes)
                .getSingleResult();
    }

    @Override
    public Long countGtFuelType(FuelType fuelType) {
        Object result = em.createNativeQuery(
                        "SELECT count_vehicles_with_fuel_type_greater_than(?1)"
                )
                .setParameter(1, fuelType.name())
                .getSingleResult();

        return ((Number) result).longValue();
    }

    @Override
    public Optional<Vehicle> resetDistanceTravelledById(Integer id) {
        try {
            em.createNativeQuery(
                            "SELECT reset_vehicle_distance(?1)"
                    )
                    .setParameter(1, id)
                    .getResultList();

            Vehicle updated = em.find(Vehicle.class, id);
            em.refresh(updated);
            return Optional.ofNullable(updated);
        } catch (PersistenceException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<Vehicle> findByNameSubstring(Integer page, Integer pageSize, String name) {
        @SuppressWarnings("unchecked")
        List<Vehicle> result = em.createNativeQuery(
                        "SELECT * FROM get_vehicles_with_name_containing(?1)",
                        Vehicle.class
                )
                .setParameter(1, name)
                .setFirstResult(page * pageSize)
                .setMaxResults(pageSize)
                .getResultList();

        return result;
    }

    public List<Vehicle> findByNameSubstringByOwner(Integer page, Integer pageSize, String name, Integer ownerId) {
        String pattern = "%" + name.toLowerCase() + "%";
        return em.createQuery(
                        "SELECT v FROM Vehicle v WHERE v.owner.id = :ownerId AND LOWER(v.name) LIKE :pattern ORDER BY v.id ASC",
                        Vehicle.class
                )
                .setParameter("ownerId", ownerId)
                .setParameter("pattern", pattern)
                .setFirstResult(page * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public List<Vehicle> findByEnginePowerRange(Integer page, Integer pageSize, Range<Integer> range) {
        @SuppressWarnings("unchecked")
        List<Vehicle> result = em.createNativeQuery(
                        "SELECT * FROM get_vehicles_in_engine_power_range(?1, ?2)",
                        Vehicle.class
                )
                .setParameter(1, range.getMin())
                .setParameter(2, range.getMax())
                .setFirstResult(page * pageSize)
                .setMaxResults(pageSize)
                .getResultList();

        return result;
    }

    public List<Vehicle> findByEnginePowerRangeByOwner(Integer page, Integer pageSize, Range<Integer> range, Integer ownerId) {
        return em.createQuery(
                        "SELECT v FROM Vehicle v WHERE v.owner.id = :ownerId AND v.enginePower BETWEEN :min AND :max ORDER BY v.enginePower ASC, v.id ASC",
                        Vehicle.class
                )
                .setParameter("ownerId", ownerId)
                .setParameter("min", range.getMin())
                .setParameter("max", range.getMax())
                .setFirstResult(page * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public Long countByNameSubstring(String name) {
        Object result = em.createNativeQuery(
                        "SELECT COUNT(*) FROM get_vehicles_with_name_containing(?1)"
                )
                .setParameter(1, name)
                .getSingleResult();

        return ((Number) result).longValue();
    }

    public Long countByNameSubstringByOwner(String name, Integer ownerId) {
        String pattern = "%" + name.toLowerCase() + "%";
        return em.createQuery(
                        "SELECT COUNT(v) FROM Vehicle v WHERE v.owner.id = :ownerId AND LOWER(v.name) LIKE :pattern",
                        Long.class
                )
                .setParameter("ownerId", ownerId)
                .setParameter("pattern", pattern)
                .getSingleResult();
    }

    @Override
    public Long countByEnginePowerRange(Range<Integer> range) {
        Object result = em.createNativeQuery(
                        "SELECT COUNT(*) FROM get_vehicles_in_engine_power_range(?1, ?2)"
                )
                .setParameter(1, range.getMin())
                .setParameter(2, range.getMax())
                .getSingleResult();

        return ((Number) result).longValue();
    }

    public Long countByEnginePowerRangeByOwner(Range<Integer> range, Integer ownerId) {
        return em.createQuery(
                        "SELECT COUNT(v) FROM Vehicle v WHERE v.owner.id = :ownerId AND v.enginePower BETWEEN :min AND :max",
                        Long.class
                )
                .setParameter("ownerId", ownerId)
                .setParameter("min", range.getMin())
                .setParameter("max", range.getMax())
                .getSingleResult();
    }

    public Optional<Vehicle> findFirstByCoordinatesId(Integer coordinatesId) {
        List<Vehicle> results = em.createQuery(
                        "SELECT v FROM Vehicle v WHERE v.coordinates.id = :coordinatesId ORDER BY v.id ASC",
                        Vehicle.class
                )
                .setParameter("coordinatesId", coordinatesId)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    public List<Vehicle> findAllPagedByOwner(Integer page, Integer pageSize, Filter<VehicleField> filter, Integer ownerId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Vehicle> cq = cb.createQuery(Vehicle.class);
        Root<Vehicle> root = cq.from(Vehicle.class);

        List<Predicate> predicates = new ArrayList<>();
        List<Order> orders = new ArrayList<>();

        applyPredicate(predicates, filter, cb, root);
        predicates.add(cb.equal(root.get("owner").get("id"), ownerId));
        applyOrder(orders, filter, cb, root);

        cq.select(root)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(orders);

        TypedQuery<Vehicle> q = em.createQuery(cq);
        q.setMaxResults(pageSize);
        q.setFirstResult(page * pageSize);
        return q.getResultList();
    }

    public Long countPagedByOwner(Filter<VehicleField> filter, Integer ownerId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Vehicle> root = cq.from(Vehicle.class);

        List<Predicate> predicates = new ArrayList<>();
        applyPredicate(predicates, filter, cb, root);
        predicates.add(cb.equal(root.get("owner").get("id"), ownerId));

        cq.select(cb.count(root))
                .where(predicates.toArray(new Predicate[0]));

        return em.createQuery(cq).getSingleResult();
    }
}

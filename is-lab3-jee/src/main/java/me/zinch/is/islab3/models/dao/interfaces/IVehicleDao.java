package me.zinch.is.islab3.models.dao.interfaces;

import me.zinch.is.islab3.models.entities.FuelType;
import me.zinch.is.islab3.models.entities.Vehicle;
import me.zinch.is.islab3.models.fields.Range;
import me.zinch.is.islab3.models.fields.VehicleField;

import java.util.List;
import java.util.Optional;

public interface IVehicleDao extends IDao<Vehicle, VehicleField> {
    Optional<Vehicle> findMinEnginePower();
    Long countGtFuelType(FuelType fuelType);
    List<Vehicle> findByNameSubstring(Integer page, Integer pageSize, String name);
    List<Vehicle> findByEnginePowerRange(Integer page, Integer pageSize, Range<Integer> range);
    Optional<Vehicle> resetDistanceTravelledById(Integer id);

    Long countByNameSubstring(String name);
    Long countByEnginePowerRange(Range<Integer> range);
}

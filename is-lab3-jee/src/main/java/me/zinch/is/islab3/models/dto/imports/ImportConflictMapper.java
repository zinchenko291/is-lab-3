package me.zinch.is.islab3.models.dto.imports;

import jakarta.enterprise.context.ApplicationScoped;
import me.zinch.is.islab3.models.entities.ImportConflict;

@ApplicationScoped
public class ImportConflictMapper {
    public ImportConflictDto entityToDto(ImportConflict conflict) {
        if (conflict == null) return null;
        ImportConflictDto dto = new ImportConflictDto();
        dto.setId(conflict.getId());
        dto.setResolution(conflict.getResolution());
        dto.setVehicleIndex(conflict.getVehicleIndex());
        dto.setExistingVehicleId(conflict.getExistingVehicleId());
        dto.setCoordinateX(conflict.getCoordinateX());
        dto.setCoordinateY(conflict.getCoordinateY());
        dto.setUserId(conflict.getOperation().getUser().getId());
        dto.setCreatedAt(conflict.getCreatedAt());
        return dto;
    }
}

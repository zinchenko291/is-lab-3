package me.zinch.is.islab3.models.dto.coordinates;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import me.zinch.is.islab3.models.dto.IMapper;
import me.zinch.is.islab3.models.dto.user.UserMapper;
import me.zinch.is.islab3.models.entities.Coordinates;


@ApplicationScoped
public class CoordinatesMapper implements IMapper<Coordinates, CoordinatesDto, CoordinatesWithoutIdDto> {
    private UserMapper userMapper;

    public CoordinatesMapper() {}

    @Inject
    public CoordinatesMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Coordinates dtoToEntity(CoordinatesDto coordinatesDto) {
        if (coordinatesDto == null) return null;
        Coordinates coordinates = new Coordinates();
        coordinates.setId(coordinatesDto.getId());
        coordinates.setX(coordinatesDto.getX());
        coordinates.setY(coordinatesDto.getY());
        return coordinates;
    }

    @Override
    public Coordinates idLessDtoToEntity(CoordinatesWithoutIdDto coordinatesDto) {
        if (coordinatesDto == null) return null;
        Coordinates coordinates = new Coordinates();
        coordinates.setX(coordinatesDto.getX());
        coordinates.setY(coordinatesDto.getY());
        return coordinates;
    }

    @Override
    public CoordinatesDto entityToDto(Coordinates coordinates) {
        if (coordinates == null) return null;
        CoordinatesDto coordinatesDto = new CoordinatesDto();
        coordinatesDto.setId(coordinates.getId());
        coordinatesDto.setX(coordinates.getX());
        coordinatesDto.setY(coordinates.getY());
        coordinatesDto.setOwner(userMapper == null ? null : userMapper.entityToShortDto(coordinates.getOwner()));
        return coordinatesDto;
    }
}

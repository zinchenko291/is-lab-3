package me.zinch.is.islab2.models.dto.user;

import jakarta.enterprise.context.ApplicationScoped;
import me.zinch.is.islab2.models.dto.IMapper;
import me.zinch.is.islab2.models.entities.User;

@ApplicationScoped
public class UserMapper implements IMapper<User, UserDto, UserWithoutIdDto> {
    public UserShortDto entityToShortDto(User user) {
        if (user == null) return null;
        UserShortDto dto = new UserShortDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        return dto;
    }

    @Override
    public UserDto entityToDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setPubkey(user.getPubkey());
        dto.setEmail(user.getEmail());
        dto.setIsAdmin(user.getIsAdmin());
        return dto;
    }

    @Override
    public User idLessDtoToEntity(UserWithoutIdDto userDto) {
        if (userDto == null) return null;
        User user = new User();
        user.setName(userDto.getName());
        user.setPubkey(userDto.getPubkey());
        user.setEmail(userDto.getEmail());
        user.setIsAdmin(userDto.getIsAdmin());
        return user;
    }

    @Override
    public User dtoToEntity(UserDto userDto) {
        if (userDto == null) return null;
        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setPubkey(userDto.getPubkey());
        user.setEmail(userDto.getEmail());
        user.setIsAdmin(userDto.getIsAdmin());
        return user;
    }
}

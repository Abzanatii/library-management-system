package com.abz.library.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.abz.library.user.dto.UserResponse;
import com.abz.library.user.entity.User;

@Mapper (componentModel = "Spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(User user);

}

package com.noom.interview.fullstack.sleep.mapper;

import com.noom.interview.fullstack.sleep.api.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.api.SleepLogResponse;
import com.noom.interview.fullstack.sleep.models.SleepLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper: request/entity to SleepLog, entity to SleepLogResponse.
 */
@Mapper(componentModel = "spring")
public interface SleepLogMapper {

    @Mapping(target = "id", source = "id", defaultValue = "0L")
    @Mapping(target = "userId", source = "userId", defaultValue = "0L")
    @Mapping(target = "totalTimeInBedMinutes", source = "totalTimeInBedMinutes", defaultValue = "0")
    SleepLogResponse toResponse(SleepLog entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "createdAt", ignore = true)
    SleepLog toEntity(CreateSleepLogRequest request, Long userId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "sleepDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateFromRequest(@MappingTarget SleepLog target, CreateSleepLogRequest source);
}

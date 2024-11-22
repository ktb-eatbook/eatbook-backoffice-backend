package com.eatbook.backoffice.entity.constant;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AgeGroupConverter implements AttributeConverter<AgeGroup, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AgeGroup ageGroup) {
        return ageGroup != null ? ageGroup.getValue() : null;
    }

    @Override
    public AgeGroup convertToEntityAttribute(Integer dbData) {
        return dbData != null ? AgeGroup.fromValue(dbData) : null;
    }
}
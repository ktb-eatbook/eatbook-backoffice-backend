package com.eatbook.backoffice.entity.idClass;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class NovelCategoryId implements Serializable {
    private static final long serialVersionUID = 2L;

    private String novelId;
    private String categoryId;

}

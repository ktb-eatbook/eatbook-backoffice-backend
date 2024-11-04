package com.eatbook.backoffice.entity.idClass;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class NovelCategoryId implements Serializable {
    private static final long serialVersionUID = 2L;

    private String novel;
    private String category;

}

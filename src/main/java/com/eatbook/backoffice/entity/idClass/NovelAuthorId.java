package com.eatbook.backoffice.entity.idClass;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class NovelAuthorId implements Serializable {
    private static final long serialVersionUID = 1L;

    private String novel;
    private String author;
}

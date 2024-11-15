// NovelInfo.java
package com.eatbook.backoffice.domain.novel.dto;

import java.util.List;

public record NovelInfo(
        String id,
        String title,
        List<String> authorList,
        List<String> categoryList,
        String coverImageUrl
) {
    public static NovelInfo of(
            final String id,
            final String title,
            final List<String> authorList,
            final List<String> categoryList,
            final String coverImageUrl
    ) {
        return new NovelInfo(id, title, authorList, categoryList, coverImageUrl);
    }
}
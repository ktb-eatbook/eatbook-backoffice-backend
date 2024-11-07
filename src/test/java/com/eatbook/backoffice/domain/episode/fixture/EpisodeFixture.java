package com.eatbook.backoffice.domain.episode.fixture;

import com.eatbook.backoffice.entity.Episode;
import com.eatbook.backoffice.entity.FileMetadata;
import com.eatbook.backoffice.entity.Novel;
import com.eatbook.backoffice.entity.constant.ReleaseStatus;

import java.lang.reflect.Field;

public class EpisodeFixture {

    public static final String episodeId = "3ed5d018-1499-407f-a73f-23ab142ba593";
    public static final String novelId = "2ed5d018-1499-407f-a73f-23ab142ba593";
    public static final String fileMetadataId = "4ed5d018-1499-407f-a73f-23ab142ba593";
    public static final String episodeTitle = "Unique Episode Title";
    public static final String episodeTitleDuplicated = "Duplicated Episode Title";
    public static final String novelTitle = "Title";
    public static final String summary = "Valid Episode Summary";
    public static final int publicationYear = 1800;
    public static final ReleaseStatus releaseStatus = ReleaseStatus.PUBLIC;

    // 헬퍼 메서드: FileMetadata ID 설정
    public static FileMetadata createFileMetadataWithId(String id) {
        FileMetadata fileMetadata = FileMetadata.builder().build();

        // 리플렉션을 사용하여 id 설정
        try {
            Field idField = FileMetadata.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(fileMetadata, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return fileMetadata;
    }

    // 헬퍼 메서드: 테스트용 Novel ID 설정
    public static Novel createNovelWithId(String id, String title, String summary, int publicationYear) {
        Novel novel = Novel.builder()
                .title(title)
                .summary(summary)
                .publicationYear(publicationYear)
                .build();

        // 리플렉션을 사용하여 id 설정
        try {
            Field idField = Novel.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(novel, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return novel;
    }

    // 헬퍼 메서드: 테스트용 Episode ID 설정
    public static Episode createEpisodeWithId(String id, String title, ReleaseStatus releaseStatus) {
        Episode episode = Episode.builder()
                .title(title)
                .releaseStatus(releaseStatus)
                .build();

        // 리플렉션을 사용하여 id 설정
        try {
            Field idField = Episode.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(episode, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return episode;
    }
}

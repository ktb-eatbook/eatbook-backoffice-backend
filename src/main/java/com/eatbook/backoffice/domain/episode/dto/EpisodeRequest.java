package com.eatbook.backoffice.domain.episode.dto;

import com.eatbook.backoffice.entity.constant.ReleaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

public record EpisodeRequest (
    @NotBlank(message = "소설 ID는 필수입니다.")
    String novelId,

    @NotBlank(message = "에피소드 제목은 필수입니다.")
    String title,

    LocalDateTime scheduledReleaseDate,

    LocalDateTime releasedDate,

    @NotNull(message = "공개 상태는 필수입니다.")
    ReleaseStatus releaseStatus
) {
    @Builder
    public EpisodeRequest(
            String novelId,
            String title,
            LocalDateTime scheduledReleaseDate,
            LocalDateTime releasedDate,
            ReleaseStatus releaseStatus
    ) {
        this.novelId = novelId;
        this.title = title;
        this.scheduledReleaseDate = scheduledReleaseDate;
        this.releasedDate = releasedDate;
        this.releaseStatus = releaseStatus;
    }
}

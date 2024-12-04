package com.eatbook.backoffice.entity;

import com.eatbook.backoffice.entity.base.SoftDeletableEntity;
import com.eatbook.backoffice.entity.constant.AgeGroup;
import com.eatbook.backoffice.entity.constant.AgeGroupConverter;
import com.eatbook.backoffice.entity.constant.Gender;
import com.eatbook.backoffice.entity.constant.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends SoftDeletableEntity {

    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column
    private LocalDateTime lastLogin;

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, length = 100)
    @NotNull
    private String nickname;

    @Column
    private String passwordHash;

    @Column
    private String email;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull
    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    @Convert(converter = AgeGroupConverter.class)
    private AgeGroup ageGroup;

    @Column
    private String profileImageUrl;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Bookmark> bookmarks = new ArrayList<>();

    @Builder
    public Member(LocalDateTime lastLogin, Role role, String nickname, String passwordHash, String email, Gender gender, AgeGroup ageGroup, String profileImageUrl) {
        this.lastLogin = lastLogin;
        this.role = role;
        this.nickname = nickname;
        this.passwordHash = passwordHash;
        this.email = email;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.profileImageUrl = profileImageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return id.equals(member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}

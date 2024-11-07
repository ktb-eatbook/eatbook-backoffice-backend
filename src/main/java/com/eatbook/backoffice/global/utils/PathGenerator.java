package com.eatbook.backoffice.global.utils;

import org.springframework.web.util.UriComponentsBuilder;

public class PathGenerator {

    /**
     * 여러 문자열을 입력받아 '/'로 조합된 경로를 생성합니다.
     *
     * @param paths 경로를 구성할 여러 문자열
     * @return '/'로 조합된 경로 문자열
     */
    public static String generateRelativePath(String... paths) {
        return String.join("/", paths);
    }

    /**
     * S3 버킷의 경로를 생성합니다.
     *
     * @param bucketName S3 버킷 이름
     * @param region S3 버킷이 위치한 AWS 리전
     * @param paths S3 버킷 경로를 구성할 여러 문자열
     * @return S3 버킷 경로 (https://[bucketName].s3.[region].amazonaws.com/[paths])
     */
    public static String getFilePath(String bucketName, String region, String... paths) {
        String relativePath = generateRelativePath(paths);

        String https = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(bucketName + ".s3." + region + ".amazonaws.com")
                .path(relativePath)
                .build()
                .toUriString();

        return https;
    }
}

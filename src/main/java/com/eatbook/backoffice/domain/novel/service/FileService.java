package com.eatbook.backoffice.domain.novel.service;

import com.eatbook.backoffice.domain.novel.exception.PresignedUrlGenerationException;
import com.eatbook.backoffice.entity.constant.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

import static com.amazonaws.services.s3.internal.BucketNameUtils.validateBucketName;
import static com.eatbook.backoffice.domain.novel.response.NovelErrorCode.S3_PRE_SIGNED_URL_GENERATION_FAILED;

/**
 * S3 버킷의 파일을 관리하기 위한 서비스입니다.
 * @author lavin
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private final S3Presigner s3Presigner;
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    
    @Value("${presigned.url.expiration}")
    private int presignedUrlExpiration;
    
    /**
     * 지정된 S3 버킷에 객체를 업로드하기 위해 preSignedURL을 생성합니다.
     *
     * @param objectKey     S3 버킷에 저장될 객체의 키
     * @param contentType   업로드할 객체의 MIME 유형
     * @return              preSignedURL URL을 문자열로 반환
     * @throws PresignedUrlGenerationException  preSignedURL 생성 중에 예외가 발생할 경우
     */
    public String getPresignUrl(String objectKey, ContentType contentType) {
        PutObjectRequest putObjectRequest = createPutObjectRequest(objectKey, contentType.getMimeType());

        PresignedPutObjectRequest presignedPutObjectRequest;
        try {
            validateBucketName(bucketName);
            presignedPutObjectRequest = createPresignedPutObjectRequest(putObjectRequest);
        } catch (Exception e) {
            throw new PresignedUrlGenerationException(S3_PRE_SIGNED_URL_GENERATION_FAILED, e.getMessage());
        }

        return presignedPutObjectRequest.url().toString();
    }

    /**
     * S3 버킷에 저장할 객체(파일)의 키(key)를 생성합니다.
     *
     * @param objectId       객체(파일)의 ID
     * @param directoryPath  객체(파일)가 저장될 디렉토리 경로
     * @return 생성된 객체(파일) 키
     */
    private String createObjectKey(String objectId, String directoryPath) {
        log.info(directoryPath + "/" + objectId);
        return directoryPath + "/" + objectId;
    }

    /**
     * S3 PutObject 요청 객체를 생성합니다.
     *
     * @param objectKey     객체(파일) 키
     * @param contentType    객체(파일)의 콘텐츠 타입
     * @return 생성된 PutObject 요청 객체
     */
    private PutObjectRequest createPutObjectRequest(String objectKey, String contentType) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();
    }

    /**
     * S3 PutObject 요청에 대한 presigned URL을 생성합니다.
     *
     * @param putObjectRequest  S3 PutObject 요청 객체
     * @return 생성된 presigned PutObject 요청 객체
     */
    private PresignedPutObjectRequest createPresignedPutObjectRequest(PutObjectRequest putObjectRequest) {
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlExpiration))
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest);
    }
}
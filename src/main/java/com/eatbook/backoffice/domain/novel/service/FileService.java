package com.eatbook.backoffice.domain.novel.service;

import com.eatbook.backoffice.domain.novel.exception.PresignedUrlGenerationException;
import com.eatbook.backoffice.entity.constant.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

import static com.amazonaws.services.s3.internal.BucketNameUtils.validateBucketName;
import static com.eatbook.backoffice.domain.novel.response.NovelErrorCode.S3_PRE_SIGNED_URL_GENERATION_FAILED;
import static com.eatbook.backoffice.global.utils.PathGenerator.getFilePath;

/**
 * S3 버킷의 파일을 관리하기 위한 서비스입니다.
 * @author lavin
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private final S3Client s3Client;

    private final S3Presigner s3Presigner;
    
    @Value("${cloud.aws.s3.bucket.public}")
    private String publicBucket;

    @Value("${cloud.aws.s3.bucket.private}")
    private String privateBucket;
    
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
    public String getPresignUrl(String objectKey, ContentType contentType, String bucketName) {
        PutObjectRequest putObjectRequest = createPutObjectRequest(objectKey, contentType.getMimeType(), bucketName);

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
     * 지정된 S3 버킷에 파일을 업로드하고, 업로드된 파일에 대한 preSigned URL을 생성합니다.
     *
     * @param objectKey     S3 버킷에 저장될 객체(파일)의 키
     * @param file          업로드할 파일
     * @param contentType   업로드할 파일의 MIME 유형
     * @param bucketName    S3 버킷 이름
     * @return              업로드된 파일의 preSigned URL
     * @throws RuntimeException  publicBucket에 파일 업로드 실패 시 발생
     */
    public String uploadFileToBucket(String objectKey, MultipartFile file, String contentType, String bucketName) {
        try {
            validateBucketName(bucketName);

            PutObjectRequest putObjectRequest = createPutObjectRequest(
                    objectKey,
                    contentType,
                    bucketName
            );

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            String fileUrl = generatePresignedGetUrl(objectKey);

            return fileUrl;
        } catch (Exception e) {
            log.error("publicBucket에 파일 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("publicBucket에 파일 업로드 실패", e);
        }
    }
    
    /**
     * 지정된 S3 버킷에 프로필 이미지를 업로드합니다.
     *
     * @param objectKey   S3 버킷에 저장될 객체의 키
     * @param fileBytes   업로드할 파일의 바이트 배열
     * @param contentType 파일의 MIME 유형
     * @return 업로드된 파일의 URL
     */
    public String uploadProfileImage(String objectKey, byte[] fileBytes, ContentType contentType) {
        try {
            validateBucketName(publicBucket);

            PutObjectRequest putObjectRequest = createPutObjectRequest(objectKey, contentType.getMimeType(), publicBucket);

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
            String profileImageUrl = getFilePath(publicBucket, objectKey);
            return profileImageUrl;
        } catch (Exception e) {
            log.error("S3 파일 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("S3 파일 업로드 실패", e);
        }
    }

    /**
     * S3 PutObject 요청 객체를 생성합니다.
     *
     * @param objectKey     객체(파일) 키
     * @param contentType    객체(파일)의 콘텐츠 타입
     * @return 생성된 PutObject 요청 객체
     */
    private PutObjectRequest createPutObjectRequest(String objectKey, String contentType, String bucketName) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();
    }

    /**
     S3 GetObject 요청에 대한 presigned URL을 생성합니다.
     @param objectKey S3 버킷에 저장된 객체(파일)의 키
     @return 생성된 presigned GET URL. URL을 통해 S3 버킷의 객체를 읽기 위해 사용할 수 있습니다.
     @throws RuntimeException presigned GET URL 생성 중에 예외가 발생할 경우
     */
    private String generatePresignedGetUrl(String objectKey) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(presignedUrlExpiration))
                    .getObjectRequest(b -> b.bucket(privateBucket).key(objectKey))
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

            String presignedUrl = presignedRequest.url().toString();
            log.info("Presigned GET URL 생성됨: {}", presignedUrl);

            return presignedUrl;
        } catch (Exception e) {
            log.error("Presigned GET URL 생성 실패: {}", e.getMessage());
            throw new RuntimeException("Presigned GET URL 생성 실패", e);
        }
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
package com.example.viegymapp.service.Impl;

import com.example.viegymapp.dto.PagingResponse;
import com.example.viegymapp.dto.request.ExerciseMediaRequest;
import com.example.viegymapp.dto.request.ExerciseRequest;
import com.example.viegymapp.dto.response.ExerciseMediaResponse;
import com.example.viegymapp.dto.response.ExerciseResponse;
import com.example.viegymapp.entity.Enum.DifficultyLevel;
import com.example.viegymapp.entity.Exercise;
import com.example.viegymapp.entity.ExerciseMedia;
import com.example.viegymapp.entity.Tag;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.exception.AppException;
import com.example.viegymapp.exception.ErrorCode;
import com.example.viegymapp.mapper.ExerciseMapper;
import com.example.viegymapp.mapper.ExerciseMediaMapper;
import com.example.viegymapp.repository.ExerciseMediaRepository;
import com.example.viegymapp.repository.ExerciseRepository;
import com.example.viegymapp.repository.TagRepository;
import com.example.viegymapp.service.ExerciseService;
import com.example.viegymapp.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExerciseServiceImpl implements ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final ExerciseMediaRepository exerciseMediaRepository;
    private final ExerciseMapper exerciseMapper;
    private final ExerciseMediaMapper exerciseMediaMapper;
    private final UserService userService;
    private final TagRepository tagRepository;



    public PagingResponse<ExerciseResponse> getExercises(
            String tag,
            String difficulty,
            String muscleGroup,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Exercise> exercisePage;

        // Convert difficulty string sang enum
        DifficultyLevel difficultyEnum = null;
        if (difficulty != null && !difficulty.isEmpty()) {
            difficultyEnum = DifficultyLevel.valueOf(difficulty.toUpperCase());
        }

        boolean hasTag = tag != null && !tag.isEmpty();
        boolean hasDifficulty = difficultyEnum != null;
        boolean hasMuscleGroup = muscleGroup != null && !muscleGroup.isEmpty();

        if (hasTag && hasDifficulty && hasMuscleGroup) {
            exercisePage = exerciseRepository.findDistinctByTags_NameAndDifficultyAndMuscleGroup(
                    tag, difficultyEnum, muscleGroup, pageable
            );
        } else if (hasTag && hasDifficulty) {
            exercisePage = exerciseRepository.findDistinctByTags_NameAndDifficulty(
                    tag, difficultyEnum, pageable
            );
        } else if (hasTag && hasMuscleGroup) {
            exercisePage = exerciseRepository.findDistinctByTags_NameAndMuscleGroup(
                    tag, muscleGroup, pageable
            );
        } else if (hasDifficulty && hasMuscleGroup) {
            exercisePage = exerciseRepository.findByDifficultyAndMuscleGroup(
                    difficultyEnum, muscleGroup, pageable
            );
        } else if (hasTag) {
            exercisePage = exerciseRepository.findDistinctByTags_Name(tag, pageable);
        } else if (hasDifficulty) {
            exercisePage = exerciseRepository.findByDifficulty(difficultyEnum, pageable);
        } else if (hasMuscleGroup) {
            exercisePage = exerciseRepository.findByMuscleGroup(muscleGroup, pageable);
        } else {
            exercisePage = exerciseRepository.findAll(pageable);
        }

        // map entity → DTO bằng mapper bạn gửi
        List<ExerciseResponse> dtoList = exercisePage
                .getContent()
                .stream()
                .map(exerciseMapper::toResponseDTO)
                .toList();

        return new PagingResponse<>(
                dtoList,
                exercisePage.getTotalElements(),
                page,
                size
        );
    }

    private DifficultyLevel parseDifficulty(String difficulty) {
        try {
            return DifficultyLevel.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
    }

    @Override
    public ExerciseResponse getExerciseById(UUID id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));
        return exerciseMapper.toResponseDTO(exercise);
    }

    @Override
    public ExerciseResponse createExercise(ExerciseRequest createRequest) {
        if (createRequest == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }

        Exercise exercise = exerciseMapper.toEntity(createRequest);

        // Xử lý createdBy mặc định = currentUser
        if (exercise.getCreatedBy() == null || exercise.getCreatedBy().getId() == null) {
            var currentUser = userService.getCurrentUser();
            if (currentUser != null && currentUser.getId() != null) {
                User userRef = new User();
                userRef.setId(currentUser.getId());
                exercise.setCreatedBy(userRef);
            }
        }

        // Xử lý tags
        if (createRequest.getTags() != null && !createRequest.getTags().isEmpty()) {
            Set<Tag> tagEntities = createRequest.getTags().stream()
                    .map(tagName -> tagRepository.findByNameIgnoreCase(tagName)
                            .orElseGet(() -> {
                                Tag newTag = new Tag();
                                newTag.setName(tagName.trim());
                                return tagRepository.save(newTag);
                            })
                    ).collect(Collectors.toSet());
            exercise.setTags(tagEntities);
        }

        List<ExerciseMedia> tempMediaList = exercise.getMediaList();
        exercise.setMediaList(null);
        
        // Lưu exercise trước để có id
        Exercise savedExercise = exerciseRepository.save(exercise);

        // Xử lý media nếu có - ưu tiên mediaList field, nếu không có thì check metadata
        List<ExerciseMedia> savedMediaList = new ArrayList<>();
        
        // Ưu tiên xử lý từ field mediaList trực tiếp
        if (createRequest.getMediaList() != null && !createRequest.getMediaList().isEmpty()) {
            for (ExerciseMediaRequest mediaRequest : createRequest.getMediaList()) {
                ExerciseMedia media = exerciseMediaMapper.toEntity(mediaRequest);
                media.setExercise(savedExercise);  // ✅ BẮT BUỘC gán exercise trước khi save
                savedMediaList.add(exerciseMediaRepository.save(media));
            }
        } 
        // Fallback: xử lý từ metadata.mediaList (để tương thích với code cũ)
        else if (createRequest.getMetadata() != null && createRequest.getMetadata().has("mediaList")) {
            var mediaNodes = createRequest.getMetadata().get("mediaList");
            if (mediaNodes.isArray()) {
                for (var node : mediaNodes) {
                    ExerciseMediaRequest mediaRequest = new ExerciseMediaRequest();
                    mediaRequest.setMediaType(node.get("mediaType").asText());
                    mediaRequest.setUrl(node.get("url").asText());
                    if (node.has("caption")) mediaRequest.setCaption(node.get("caption").asText());
                    if (node.has("orderNo")) mediaRequest.setOrderNo(node.get("orderNo").asInt());

                    ExerciseMedia media = exerciseMediaMapper.toEntity(mediaRequest);
                    media.setExercise(savedExercise);  // ✅ BẮT BUỘC gán exercise
                    savedMediaList.add(exerciseMediaRepository.save(media));
                }
            }
        }

        if (!savedMediaList.isEmpty()) {
            savedExercise.setMediaList(savedMediaList);
        }

        return exerciseMapper.toResponseDTO(savedExercise);
    }


    @Override
    public ExerciseResponse updateExercise(UUID id, ExerciseRequest updateRequest) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));

        if (updateRequest == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }

        // Update các field cơ bản
        exerciseMapper.updateExercise(exercise, updateRequest);

        // Update tags
        if (updateRequest.getTags() != null) {
            Set<Tag> tagEntities = updateRequest.getTags().stream()
                    .map(tagName -> tagRepository.findByNameIgnoreCase(tagName)
                            .orElseGet(() -> {
                                Tag newTag = new Tag();
                                newTag.setName(tagName.trim());
                                return tagRepository.save(newTag);
                            })
                    ).collect(Collectors.toSet());
            exercise.setTags(tagEntities);
        }

        // Update mediaList an toàn với orphanRemoval
        if ((updateRequest.getMediaList() != null && !updateRequest.getMediaList().isEmpty())
                || (updateRequest.getMetadata() != null && updateRequest.getMetadata().has("mediaList"))) {

            // Xóa media cũ bằng cách clear collection, Hibernate sẽ xóa orphan tự động
            exercise.getMediaList().clear();

            List<ExerciseMediaRequest> mediaRequests = new ArrayList<>();

            // Lấy media từ mediaList field
            if (updateRequest.getMediaList() != null && !updateRequest.getMediaList().isEmpty()) {
                mediaRequests.addAll(updateRequest.getMediaList());
            }
            // Fallback: lấy từ metadata.mediaList
            else if (updateRequest.getMetadata() != null && updateRequest.getMetadata().has("mediaList")) {
                var mediaNodes = updateRequest.getMetadata().get("mediaList");
                if (mediaNodes.isArray()) {
                    for (var node : mediaNodes) {
                        ExerciseMediaRequest mediaRequest = new ExerciseMediaRequest();
                        mediaRequest.setMediaType(node.get("mediaType").asText());
                        mediaRequest.setUrl(node.get("url").asText());
                        if (node.has("caption")) mediaRequest.setCaption(node.get("caption").asText());
                        if (node.has("orderNo")) mediaRequest.setOrderNo(node.get("orderNo").asInt());
                        mediaRequests.add(mediaRequest);
                    }
                }
            }

            // Thêm media mới vào collection
            for (ExerciseMediaRequest mediaRequest : mediaRequests) {
                ExerciseMedia media = exerciseMediaMapper.toEntity(mediaRequest);
                media.setExercise(exercise);
                exercise.getMediaList().add(media);
            }
        }

        // Lưu exercise cuối cùng
        Exercise updatedExercise = exerciseRepository.save(exercise);

        return exerciseMapper.toResponseDTO(updatedExercise);
    }


    @Override
    public void deleteExercise(UUID id) {
        if (!exerciseRepository.existsById(id)) {
            throw new AppException(ErrorCode.EXERCISE_NOT_FOUND);
        }
        exerciseRepository.deleteById(id);
    }

    @Override
    public List<ExerciseMediaResponse> getMedia(UUID exerciseId) {
        if (!exerciseRepository.existsById(exerciseId)) {
            throw new AppException(ErrorCode.EXERCISE_NOT_FOUND);
        }

        List<ExerciseMedia> mediaList = exerciseMediaRepository.findByExerciseId(exerciseId);

        if (mediaList.isEmpty()) {
            throw new AppException(ErrorCode.MEDIA_NOT_FOUND);
        }

        // Map sang DTO bằng MapStruct
        return mediaList.stream()
                .map(exerciseMediaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }



    @Override
    public ExerciseMediaResponse addMedia(UUID exerciseId, ExerciseMediaRequest mediaCreateRequest) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));

        if (mediaCreateRequest == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }

        ExerciseMedia media = exerciseMediaMapper.toEntity(mediaCreateRequest);
        media.setId(null);
        media.setExercise(exercise);

        ExerciseMedia saved = exerciseMediaRepository.save(media);
        return exerciseMediaMapper.toResponseDTO(saved);
    }

    @Override
    public void deleteMedia(UUID mediaId) {
        ExerciseMedia media = exerciseMediaRepository.findById(mediaId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        exerciseMediaRepository.delete(media);
    }

    @Override
    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(exerciseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

}

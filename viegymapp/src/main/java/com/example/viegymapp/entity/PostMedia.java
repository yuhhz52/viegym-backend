package com.example.viegymapp.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import com.example.viegymapp.entity.BaseEntity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "post_media")
@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor 
@Builder
public class PostMedia extends BaseEntity{
    @Id
    @UuidGenerator
    @Column(name = "post_media_id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @Column(name = "media_type")
    private String mediaType;
    private String url;
    
    @Column(name = "order_no")
    private Integer orderNo = 0;
}

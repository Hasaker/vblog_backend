package com.hasaker.post.entity;

import com.hasaker.common.base.BaseEntity;
import lombok.*;

/**
 * @author 余天堂
 * @since 2019/10/31 22:48
 * @description 评论实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Comment extends BaseEntity {

    @NonNull
    private Long postId;

    private Long commentId;

    @NonNull
    private String content;
}

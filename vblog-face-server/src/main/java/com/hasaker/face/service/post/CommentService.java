package com.hasaker.face.service.post;

import com.hasaker.face.vo.response.ResponsePostCommentVo;

import java.util.List;

/**
 * @package com.hasaker.face.service.post.impl
 * @author 余天堂
 * @create 2020/3/28 02:03
 * @description CommentService
 */
public interface CommentService {

    List<ResponsePostCommentVo> listByPostId(Long postId);
}

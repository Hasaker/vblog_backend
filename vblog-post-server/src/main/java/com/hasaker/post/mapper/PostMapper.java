package com.hasaker.post.mapper;

import com.hasaker.common.base.BaseMapper;
import com.hasaker.post.entity.Post;
import org.apache.ibatis.annotations.Mapper;

/**
 * @package com.hasaker.vblog.mapper
 * @author 余天堂
 * @create 2019/12/24 18:07
 * @description PostMapper
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
}

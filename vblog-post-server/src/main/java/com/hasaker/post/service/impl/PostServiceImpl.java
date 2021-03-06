package com.hasaker.post.service.impl;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.hasaker.common.base.impl.BaseServiceImpl;
import com.hasaker.common.consts.Consts;
import com.hasaker.common.exception.enums.CommonExceptionEnums;
import com.hasaker.component.elasticsearch.service.EsService;
import com.hasaker.post.document.ImageDoc;
import com.hasaker.post.document.PostDoc;
import com.hasaker.post.document.TopicDoc;
import com.hasaker.post.entity.*;
import com.hasaker.post.exception.enums.PostExceptionEnum;
import com.hasaker.post.mapper.PostMapper;
import com.hasaker.post.service.*;
import com.hasaker.post.vo.request.RequestPostTopicVo;
import com.hasaker.post.vo.request.RequestPostVo;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @package com.hasaker.post.service.impl
 * @author 余天堂
 * @create 2020/3/22 19:43
 * @description PostServiceImpl
 */
@Service
public class PostServiceImpl extends BaseServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private PostImageService postImageService;
    @Autowired
    private PostTopicService postTopicService;
    @Autowired
    private TopicService topicService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private VoteService voteService;
    @Autowired
    private EsService esService;

    /**
     * Create a new post
     * @param postVo
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long post(RequestPostVo postVo) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(postVo);

        filterTopic(postVo);

        Post post = Convert.convert(Post.class, postVo);
        post = this.saveId(post);
        final Long postId = post.getId();
        PostDoc postDoc = Convert.convert(PostDoc.class, post);
        postDoc.setPoster(post.getCreateUser());
        postDoc.setPostTime(post.getCreateTime());
        postDoc.setTopics(Collections.emptyList());

        // Save images of this post
        if (ObjectUtils.isNotNull(postVo.getImages())) {
            List<PostImage> images = postVo.getImages().stream()
                    .map(o -> Convert.convert(PostImage.class, o)).collect(Collectors.toList());
            images.forEach(o -> o.setPostId(postId));

            List<ImageDoc> imageDocs = images.stream().map(o -> postImageService.saveId(o)).map(o -> {
                ImageDoc imageDoc = Convert.convert(ImageDoc.class, o);
                imageDoc.setUploader(o.getCreateUser());
                imageDoc.setUploadTime(o.getCreateTime());
                return imageDoc;
            }).collect(Collectors.toList());
            esService.index(imageDocs);
        }

        // Save topics of this post
        if (ObjectUtils.isNotNull(postVo.getTopics())) {
            // Save new topic to es
            List<TopicDoc> newTopics = postVo.getTopics().stream()
                    .filter(o -> ObjectUtils.isNull(o.getTopicId()))
                    .map(o -> {
                        Topic topic = Convert.convert(Topic.class, o);
                        topic.setDescription(Consts.TOPIC_NO_DESC);
                        topic.setBackground(Consts.DEFAULT_TOPIC_BACKGROUND);
                        topic = topicService.saveId(topic);
                        o.setTopicId(topic.getId());
                        return Convert.convert(TopicDoc.class, topic);
                    }).collect(Collectors.toList());
            if (ObjectUtils.isNotNull(newTopics)) {
                esService.index(newTopics);
            }

            // Save post topic relationship to database
            List<PostTopic> topics = postVo.getTopics().stream()
                    .map(o -> Convert.convert(PostTopic.class, o))
                    .collect(Collectors.toList());
            topics.forEach(o -> o.setPostId(postId));
            postTopicService.save(topics);

            // Fill topic ID to post's topics
            postDoc.setTopics(topics.stream().map(PostTopic::getTopicId).distinct().collect(Collectors.toList()));
        }

        // Save post document to es
        esService.index(postDoc);

        return post.getId();
    }

    /**
     * Delete a post by ID
     * Delete related images, topics, comments and votes
     * @param postId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long postId) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(postId);

        Post post = this.getById(postId);
        PostExceptionEnum.POST_NOT_EXISTS.assertNotEmpty(post);
        this.removeById(postId);

        // Delete images belong to this post
        QueryWrapper<PostImage> postImageQueryWrapper = new QueryWrapper<>();
        postImageQueryWrapper.eq(PostImage.POST_ID, postId);
        postImageService.remove(postImageQueryWrapper);

        // Delete topics belong to this post
        QueryWrapper<PostTopic> postTopicQueryWrapper = new QueryWrapper<>();
        postTopicQueryWrapper.eq(PostTopic.POST_ID, postId);
        postTopicService.remove(postTopicQueryWrapper);

        // Delete comments belong to this post
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq(Comment.POST_ID, postId);
        commentService.remove(commentQueryWrapper);

        // Delete votes belong to this post
        QueryWrapper<Vote> voteQueryWrapper = new QueryWrapper<>();
        voteQueryWrapper.eq(Vote.POST_ID, postId);
        voteService.remove(voteQueryWrapper);

        // Delete from es
        esService.delete(postId, PostDoc.class);
    }

    /**
     * Index all posts to es for dev use
     */
    @Override
    public void indexAllPosts() {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        List<Post> posts = this.list(queryWrapper);

        QueryWrapper<PostTopic> postTopicQueryWrapper = new QueryWrapper<>();
        List<PostTopic> topics = postTopicService.list(postTopicQueryWrapper);
        Map<Long, List<Long>> topicMap = topics.stream()
                .collect(Collectors.groupingBy(PostTopic::getPostId,
                        Collectors.mapping(PostTopic::getTopicId, Collectors.toList())));

        List<PostDoc> postDocs = posts.stream().map(o -> {
            PostDoc postDoc = Convert.convert(PostDoc.class, o);
            postDoc.setPoster(o.getCreateUser());
            postDoc.setPostTime(o.getCreateTime());
            postDoc.setTopics(ObjectUtils.isNotNull(topicMap.get(o.getId())) ? topicMap.get(o.getId()) : Collections.emptyList());
            return postDoc;
        }).collect(Collectors.toList());

        esService.index(postDocs);
    }

    /**
     * Filter topics from post content
     * @param postVo
     * @return
     */
    private void filterTopic(RequestPostVo postVo) {
        CommonExceptionEnums.NOT_NULL_ARG.assertNotEmpty(postVo);

        // filter topics from content rounded by '#'
        List<String> topics = new ArrayList<>();
        String content = postVo.getContent();
        StringBuilder replacedContent = new StringBuilder();
        String topic;
        if (content.contains("#")) {
            char[] chars = content.toCharArray();
            for (int prefix = 0; prefix < chars.length - 1; prefix++) {
                if (chars[prefix] == '#') {
                    for (int suffix = prefix + 1; suffix < chars.length; suffix++) {
                        if (chars[suffix] == '#') {
                            // ignore continuous situation like '##' and '###'
                            if (suffix - prefix > 1) {
                                topic = content.substring(prefix + 1, suffix);
                                replacedContent.append(content, replacedContent.length() + topics.size() * 2, prefix).append(topic);
                                topics.add(topic);
                                prefix = suffix;
                            }
                            break;
                        }
                    }
                }
            }
        }

        // set topicId for existed topics
        if (ObjectUtils.isNotNull(topics)) {
            SearchQuery searchQuery = new NativeSearchQuery(QueryBuilders.termsQuery(TopicDoc.NAME, topics));
            List<TopicDoc> existedTopics = esService.list(searchQuery, TopicDoc.class);

            postVo.setTopics(topics.stream().map(o -> {
                RequestPostTopicVo topicVo = new RequestPostTopicVo();
                topicVo.setName(o);
                if (ObjectUtils.isNotNull(existedTopics)) {
                    for (TopicDoc topicDoc : existedTopics) {
                        if (topicDoc.getName().equals(o)) {
                            topicVo.setTopicId(topicDoc.getId());
                        }
                    }
                }
                return topicVo;
            }).collect(Collectors.toList()));

            postVo.setContent(replacedContent.toString());
        }
    }
}

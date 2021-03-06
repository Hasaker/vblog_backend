package com.hasaker.face.controller.post;

import com.hasaker.common.vo.Ajax;
import com.hasaker.common.vo.PageInfo;
import com.hasaker.face.controller.base.BaseController;
import com.hasaker.face.service.post.CommentService;
import com.hasaker.face.service.post.PostService;
import com.hasaker.face.service.post.TopicService;
import com.hasaker.face.service.post.VoteService;
import com.hasaker.face.vo.request.RequestAggregationVo;
import com.hasaker.face.vo.request.RequestPostSearchVo;
import com.hasaker.face.vo.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @package com.hasaker.face.controller.post
 * @author 余天堂
 * @create 2020/3/26 14:41
 * @description OpenController
 */
@RestController
@RequestMapping("/open/post")
public class OpenPostController extends BaseController {

    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private VoteService voteService;
    @Autowired
    private TopicService topicService;

    @PostMapping("/list")
    Ajax<PageInfo<ResponsePostVo>> listPost(@RequestBody RequestPostSearchVo searchVo) {
        searchVo.setUserId(getUserId());

        return Ajax.getInstance().successT(postService.page(searchVo));
    }

    @PostMapping("/hot-words")
    Ajax<List<ResponseHotWordsAggVo>> listHotWords(@RequestBody RequestAggregationVo aggregationVo) {
        return Ajax.getInstance().successT(postService.getHotWords(aggregationVo));
    }

    @PostMapping("/hot-topics")
    Ajax<List<ResponseHotTopicsAggVo>> listHotTopics(@RequestBody RequestAggregationVo aggregationVo) {
        return Ajax.getInstance().successT(postService.getHotTopics(aggregationVo));
    }

    @PostMapping("/hot-users")
    Ajax<List<ResponseHotUsersAggVo>> listHotUsers(@RequestBody RequestAggregationVo aggregationVo) {
        return Ajax.getInstance().successT(postService.getHotUsers(aggregationVo));
    }

    @GetMapping("/{postId}")
    Ajax<ResponsePostVo> get(@PathVariable Long postId) {
        return Ajax.getInstance().successT(postService.getById(postId));
    }

    @GetMapping("/voters/{postId}")
    Ajax<List<ResponseUserInfoVo>> listVoters(@PathVariable("postId") Long postId) {
        return Ajax.getInstance().successT(voteService.list(postId));
    }

    @GetMapping("/comments/{postId}")
    Ajax<List<ResponsePostCommentVo>> listComments(@PathVariable("postId") Long postId) {
        return Ajax.getInstance().successT(commentService.listByPostId(postId));
    }

    @GetMapping("/topic/{topicId}")
    Ajax<ResponseTopicDetailVo> topicDetail(@PathVariable("topicId") Long topicId) {
        return Ajax.getInstance().successT(topicService.detail(topicId));
    }

    @GetMapping("/index-all")
    Ajax indexAll() {
        postService.indexAll();
        return Ajax.success();
    }
}

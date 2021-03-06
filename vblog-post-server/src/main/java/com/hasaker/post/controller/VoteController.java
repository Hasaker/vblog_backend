package com.hasaker.post.controller;

import com.hasaker.common.vo.Ajax;
import com.hasaker.post.service.VoteService;
import com.hasaker.post.vo.request.RequestVoteVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @package com.hasaker.post.controller
 * @author 余天堂
 * @create 2020/3/22 19:52
 * @description VoteController
 */
@RestController
@RequestMapping(value = "/vote")
public class VoteController {

    @Autowired
    private VoteService voteService;

    @ApiOperation(value = "Vote a post or a comment")
    @PostMapping(value = "/vote")
    Ajax vote(@RequestBody RequestVoteVo voteVo) {
        voteService.vote(voteVo);
        return Ajax.success();
    }

    @ApiOperation(value = "Index all topics to es")
    @GetMapping(value = "/index-all")
    Ajax indexAllVotes() {
        voteService.indexAllVotes();
        return Ajax.success();
    }
}

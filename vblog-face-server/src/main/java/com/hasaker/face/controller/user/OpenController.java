package com.hasaker.face.controller.user;

import com.hasaker.common.vo.Ajax;
import com.hasaker.common.vo.PageInfo;
import com.hasaker.face.service.user.UserService;
import com.hasaker.face.vo.request.RequestUserSearchVo;
import com.hasaker.face.vo.response.ResponseUserDetailVo;
import com.hasaker.face.vo.response.ResponseUserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @package com.hasaker.face.controller.user
 * @author 余天堂
 * @create 2020/2/27 09:53
 * @description OpenUserController
 */
@RestController
@RequestMapping(value = "/open/user")
public class OpenController {

    @Autowired
    private UserService userService;

    @PostMapping("/search")
    public Ajax<PageInfo<ResponseUserInfoVo>> search(@RequestBody RequestUserSearchVo searchVo) {
        return Ajax.getInstance().successT(userService.search(searchVo));
    }

    @GetMapping("/detail/{userId}")
    public Ajax<ResponseUserDetailVo> detail(@PathVariable("userId") String userId) {
        return Ajax.getInstance().successT(userService.detail(userId));
    }
}

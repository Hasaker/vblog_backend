package com.hasaker.account.service;

import com.hasaker.account.entity.Friend;
import com.hasaker.account.vo.request.RequestFriendDeleteVo;
import com.hasaker.account.vo.request.RequestFriendRemarkVo;
import com.hasaker.account.vo.request.RequestFriendVisibilityVo;
import com.hasaker.common.base.BaseService;

/**
 * @package com.hasaker.account.service
 * @author 余天堂
 * @create 2020/3/2 10:14
 * @description FriendService
 */
public interface FriendService extends BaseService<Friend> {

    void add(Long userId, Long friendId, String remark, Integer visibility);

    void delete(RequestFriendDeleteVo deleteVo);

    void changeRemark(RequestFriendRemarkVo remarkVo);

    void changeVisibility(RequestFriendVisibilityVo visibilityVo);

    void indexAll();
}

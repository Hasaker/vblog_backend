package com.hasaker.vblog.component.elasticsearch.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.Date;
import java.util.List;

/**
 * @author 余天堂
 * @since 2019/11/3 17:42
 * @description 
 */
@Document(indexName = "vblog-post")
public class PostDocument {

    @Id
    private String postId;

    private String postUserId;

    private String postUserAvatar;

    private String postUserNickname;

    private String content;

    private List<String> images;

    private Integer visibility;

    private List<VoteUser> votes;

    private List<PostComment> comments;

    private Date postTime;

    @GeoPointField
    private GeoPoint location;

    private static class VoteUser {

        private String userId;

        private String userAvatar;

        private String userNickname;

        private String voteTime;
    }

    private static class PostComment {

        private String userId;

        private String userAvatar;

        private String userNickname;

        private String targetUserId;

        private String targetUserAvatar;

        private String targetUserNickname;

        private String commentContent;

        private Date commentTime;
    }
}

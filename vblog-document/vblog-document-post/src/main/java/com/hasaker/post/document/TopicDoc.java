package com.hasaker.post.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @package com.hasaker.post.document
 * @author 余天堂
 * @create 2020/3/24 18:18
 * @description TopicDoc
 */
@Data
@Document(indexName = "vblog-post-topic")
public class TopicDoc {

    @Id
    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Text, analyzer = "ik_smart", searchAnalyzer = "ik_smart", fielddata = true)
    private String description;

    @Field(type = FieldType.Keyword)
    private String background;

    @Field(type = FieldType.Long)
    private Long createUser;

    @Field(type = FieldType.Date)
    private Long createTime;

    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String BACKGROUND = "background";
    public static final String CREATE_USER = "createUser";
    public static final String CREATE_TIME = "createTime";
}

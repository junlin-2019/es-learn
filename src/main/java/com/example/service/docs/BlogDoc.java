package com.example.service.docs;

import com.example.service.es.EsDoc;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description:
 * @Author: admin
 * @Date: 2020/10/16 14:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogDoc implements EsDoc {

    private Long blogId;

    private String blogName;

    private String author;

    private String content;

    @Override
    public String docId() {
        return this.blogId.toString();
    }
}

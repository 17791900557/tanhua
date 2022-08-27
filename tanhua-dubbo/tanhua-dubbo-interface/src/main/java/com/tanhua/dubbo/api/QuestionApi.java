package com.tanhua.dubbo.api;

import com.tabhua.model.domain.Question;

public interface QuestionApi {
    //获取陌生人问题
    Question getQuestion(Long id);

    //保存陌生人问题
    void save(Question question);

    //更新陌生人问题
    void update(Question question);
}

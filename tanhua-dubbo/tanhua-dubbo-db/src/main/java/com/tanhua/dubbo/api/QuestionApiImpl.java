package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tabhua.model.domain.Question;
import com.tanhua.dubbo.mappers.QuestionMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class QuestionApiImpl implements QuestionApi {

    @Autowired
    private QuestionMapper questionMapper;

    //获取陌生人问题
    @Override
    public Question getQuestion(Long id) {
        LambdaQueryWrapper<Question> qw = new LambdaQueryWrapper<>();
        qw.eq(Question::getUserId, id);
        Question question = questionMapper.selectOne(qw);
        return question;
    }
    //保存陌生人问题
    @Override
    public void save(Question question) {
        questionMapper.insert(question);
    }

    //更新陌生人问题
    @Override
    public void update(Question question) {
        questionMapper.updateById(question);
    }

}

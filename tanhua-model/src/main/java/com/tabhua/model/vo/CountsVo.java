package com.tabhua.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountsVo implements Serializable {
    private Integer eachLoveCount;//互相喜欢
    private Integer loveCount;//喜欢
    private Integer fanCount;//粉丝
}

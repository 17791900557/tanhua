package com.tabhua.model.vo;

import com.tabhua.model.domain.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class fangKeVo {
    private Long id; //用户id
    private String nickname; //昵称
    private String avatar; //用户头像
    private String gender; //性别
    private Integer age; //年龄
    private String city; //城市
    private String education; //学历
    private Integer marriage; //婚姻状态
    private Integer matchRate;//匹配度==缘分值
    private Boolean alreadyLove;//是否已经喜欢

        /**
         * 在vo对象中，补充一个工具方法，封装转化过程
         */
        public static fangKeVo init(UserInfo userInfo) {
            fangKeVo vo = new fangKeVo();
            BeanUtils.copyProperties(userInfo,vo);
            return vo;
        }
    }

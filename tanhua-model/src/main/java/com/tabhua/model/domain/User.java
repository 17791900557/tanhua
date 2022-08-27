package com.tabhua.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor//无参构造
public class User extends BasePojo{
    private Long id;
    private String phone;
    private String password;

    //环信账户密码
    private String hxUser;
    private String hxPassword;

}

package com.tanhua.server.interceptor;

import com.tabhua.model.domain.User;

public class UserHolder {
    private static ThreadLocal<User> tl = new ThreadLocal<>();

    //将用户对象存入tl
    public static void setTl(User user){
        tl.set(user);
    }
    //从当前线程获取User
    public static User getTl(){
        return tl.get();
    }
    //获取Id
    public static Long getId() {
        return tl.get().getId();
    }
    //获取Phone
    public static String getPhone(){
        return tl.get().getPhone();
    }

    //清空数据
    public static void remove(){
        tl.remove();
    }
}


package com.tanhua.server.interceptor;

import com.tabhua.model.domain.User;
import com.tanhua.commoms.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TokenInterceptor implements HandlerInterceptor {
    /**
     * 请求之前验证用户
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
//        boolean verifyToken = JwtUtils.verifyToken(token);
//        if (!verifyToken) {
//            return false;
//        }   由网关代替
        //token有效
        Claims claims = JwtUtils.getClaims(token);
        String phone = (String) claims.get("phone");
        Integer Id = (Integer) claims.get("id");

        User user = new User();
        user.setId(Long.valueOf(Id));
        user.setPhone(phone);

        UserHolder.setTl(user);

        return true;

    }

    /**
     * 请求后删除ThreadLocal中的数据
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.remove();
    }
}

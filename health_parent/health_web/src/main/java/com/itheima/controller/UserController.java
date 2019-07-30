package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.constant.MessageConstant;
import com.itheima.constant.RedisMessageConstant;
import com.itheima.entity.Result;
import com.itheima.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;

import java.util.jar.JarEntry;

/**
 * 用户控制层
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;

    @Autowired
    private JedisPool jedisPool;

    @RequestMapping(value = "/getUsername", method = RequestMethod.GET)
    public Result getUsername() {
        //认证成功后用户信息 就放入springsecurity上下文(SecurityContextHolder)中
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();//getPrincipal():用户对象 getAuthentication():认证对象

        //String password = user.getPassword();//spring securtiy：不能获取密码 shiro:可以获取
        String username = user.getUsername();
        //Collection<GrantedAuthority> authorities = user.getAuthorities();/
        return new Result(true, MessageConstant.GET_USERNAME_SUCCESS, username);
    }

    @RequestMapping(value = "/changePassword")
    public Result changePassword(@RequestParam("password") String password,@RequestParam("telephone") String telephone) {
        try {
            Integer id = Integer.parseInt(jedisPool.getResource().get(RedisMessageConstant.SENDTYPE_CHANGE_PASSWORD_VALID_SUCCESS + "_" + telephone));
            userService.updatePassword(id,password);
            jedisPool.getResource().del(RedisMessageConstant.SENDTYPE_CHANGE_PASSWORD_VALID_SUCCESS + "_" + telephone);
            return new Result(true, MessageConstant.CHANGGE_PASSWORD_SUCCESS);
        } catch (NumberFormatException e) {
            return new Result(false, MessageConstant.CHANGGE_PASSWORD_FAIL);
        }
    }
}

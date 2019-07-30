package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.aliyuncs.exceptions.ClientException;
import com.itheima.constant.MessageConstant;
import com.itheima.constant.RedisMessageConstant;
import com.itheima.entity.Result;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import com.itheima.utils.SMSUtils;
import com.itheima.utils.ValidateCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;

/**
 * 验证码发送
 */
@RestController
@RequestMapping("/validateCode")
public class ValidateCodeController {

    @Reference
    private UserService userService;

    @Autowired
    private JedisPool jedisPool;
    /**
     * 快速登录的验证码发送
     * @return
     */
    @RequestMapping(value = "/webChangePasswordSend6Order")
    public Result webChangePasswordSend4Order(@RequestParam("telephone") String telephone) {
        //1.生成验证码
        Integer number = ValidateCodeUtils.generateValidateCode(6);
        //2.调用短信接口发送验证码
        try {
            SMSUtils.sendShortMessage(SMSUtils.VALIDATE_CODE,telephone,number.toString());
            //3.将验证码存入redis
            jedisPool.getResource().setex(RedisMessageConstant.SENDTYPE_CHANGE_PASSWORD+"_"+telephone,5*60,number.toString());
            //4.返回结果给页面
            return new Result(true, MessageConstant.SEND_VALIDATECODE_SUCCESS);
        } catch (ClientException e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.SEND_VALIDATECODE_FAIL);
        }
    }
    /**
     * 验证码验证
     * @return
     */
    @RequestMapping(value = "/checkoutValid")
    public Result checkoutValid(@RequestParam("telephone") String telephone,@RequestParam("validateCode") String validateCode) {
        String redisCode = jedisPool.getResource().get(RedisMessageConstant.SENDTYPE_CHANGE_PASSWORD + "_" + telephone);
        if (StringUtils.isEmpty(redisCode) || StringUtils.isEmpty(validateCode) || !redisCode.equals(validateCode)) {
            //验证码错误
            return new Result(false, MessageConstant.VALIDATECODE_ERROR);
        } else {
            User user = userService.findUserByTelephone(telephone);
            jedisPool.getResource().setex(RedisMessageConstant.SENDTYPE_CHANGE_PASSWORD_VALID_SUCCESS+"_"+telephone,5*60,user.getId().toString());
            jedisPool.getResource().del(RedisMessageConstant.SENDTYPE_CHANGE_PASSWORD + "_" + telephone);
            return new Result(true, MessageConstant.VALIDATECODE_SUCCESS);
        }
    }
}

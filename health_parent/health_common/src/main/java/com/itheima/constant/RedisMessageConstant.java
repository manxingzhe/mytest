package com.itheima.constant;

/**
 * 验证码key常量类
 */
public class RedisMessageConstant {
    public static final String SENDTYPE_ORDER = "001";//用于缓存体检预约时发送的验证码
    public static final String SENDTYPE_LOGIN = "002";//用于缓存手机号快速登录时发送的验证码
    public static final String SENDTYPE_GETPWD = "003";//用于缓存找回密码时发送的验证码
    public static final String SETMEAL_REDIES = "004";//用于缓存移动端套餐页面
    public static final String SETMEAL_DETAIL_REDIES = "005";//用于缓存移动端套餐详情页面
    public static final String SENDTYPE_CHANGE_PASSWORD = "006";//用于后端忘记密码发送的验证码
    public static final String SENDTYPE_CHANGE_PASSWORD_VALID_SUCCESS = "007";//用于后端忘记密码发送的验证码验证成功缓存
}
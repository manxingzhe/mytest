package com.itheima.jobs;

import com.itheima.constant.RedisConstant;
import com.itheima.dao.OrderSettingDao;
import com.itheima.pojo.OrderSetting;
import com.itheima.utils.DateUtils;
import com.itheima.utils.QiniuUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.JedisPool;

import java.util.Date;
import java.util.Set;

/**
 * 清理图片任务类
 */
public class ClearOrderSettingJob {

    @Autowired
    private OrderSettingDao orderSettingDao;

    /**
     * 清理图片
     */
    public void clearOrderSetting(){
        System.out.println("定时清理任务运行了。。。。");
        String today = null;
        //第一步：获取当前日期
        try {
            today = DateUtils.parseDate2String(DateUtils.getToday());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("定时清理预约失败!");
        }

        //删除之前的订单
        orderSettingDao.deletePastDay(today);
        System.out.println("定时清理预约成功");
        //3.从redis中将两个集合中的数据相减  传入两个key参数就可以了
        //sdiff:两个相减  srem：删除集合中数据
    }
}

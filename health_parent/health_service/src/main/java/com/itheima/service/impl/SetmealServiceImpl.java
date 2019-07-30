package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.constant.RedisMessageConstant;
import com.itheima.dao.SetmealDao;
import com.itheima.entity.PageResult;
import com.itheima.pojo.Setmeal;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务服务层 套餐管理
 */
@Service(interfaceClass = SetmealService.class)
@Transactional
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private SetmealDao setmealDao;


    /**
     * 新增套餐
     *
     * @param setmeal
     * @param checkgroupIds
     */
    @Override
    public void add(Setmeal setmeal, Integer[] checkgroupIds) {
        //1.保存套餐表数据
        setmealDao.add(setmeal);
        Integer setmealId = setmeal.getId();//套餐id
        //2.根据检查组ids，循环保存套餐检查组中间表
        setSetmealAndCheckGroup(setmealId, checkgroupIds);
    }

    @Override
    public PageResult findPage(Integer currentPage, Integer pageSize, String queryString) {
        PageHelper.startPage(currentPage, pageSize);
        Page<Setmeal> setmealPage = setmealDao.selectByCondition(queryString);
        return new PageResult(setmealPage.getTotal(), setmealPage.getResult());
    }

    /**
     * 查询套餐列表
     */
    @Override
    public List<Setmeal> findAll() {
        String data = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (jedis != null) {
                data = jedis.get(RedisMessageConstant.SETMEAL_REDIES);
            }
            if (data == null) {
                System.out.println("Redis里面是没有数据的,从MySql获得,再存到Redis里面...");
                List<Setmeal> setmeals = setmealDao.findAll();
                data = JSON.toJSONString(setmeals);
                jedis.set(RedisMessageConstant.SETMEAL_REDIES, data);
            }
            System.out.println("从Redis里获得数据");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Redis服务器没开或者Redis服务器挂了, 从MySql获得...");
            if (data == null) {
                System.out.println("Redis里面是没有数据的,从MySql获得,再存到Redis里面...");
                List<Setmeal> setmeals = setmealDao.findAll();
                data = JSON.toJSONString(setmeals);
                jedis.set(RedisMessageConstant.SETMEAL_REDIES, data);
            }
        } finally {
            jedis.close();
            return JSON.parseObject(data, new TypeReference<List<Setmeal>>() {
            });
        }
    }

    /**
     * 查询套餐数据成功
     *
     * @param id
     * @return
     */
    @Override
    public Setmeal findById(Integer id) {
        //方式一：sql
        //setmealDao.findById(id) 套餐表数据
        //checkGroupDao.findById(id)检查组表数据  根据套餐和检查组中间表  查询出所有检查组数据条件套餐id
        //checkItemDao.findById(id)检查项表数据  根据检查组和检查项中间表  查询出所有检查项数据条件检查组id

        //方式二：配置文件方式
        //1.配置套餐sql查询当前套餐数据
        //2.根据套餐中属性 <collection> 设置查询语句 查询检查组数据（最终查询配置是在检查组配置中）
        //3.根据检查组id查询检查项数据 <collection> 设置查询语句 （最终查询配置是在检查项配置中）

        //redis存储:先从缓存区通过id检测哈希表,如果存在该值,直接返回,如果没有该值,从数据库中获得后存入缓存,最后返回

        String data = null;
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (jedis != null) {
                String sid = id.toString();
                data = jedis.hget(RedisMessageConstant.SETMEAL_DETAIL_REDIES,sid);
            }
            if (data == null) {
                Setmeal setmeal = setmealDao.findById(id);
                String sid = setmeal.getId().toString();
                data = JSON.toJSONString(setmeal);
                jedis.hset(RedisMessageConstant.SETMEAL_DETAIL_REDIES, sid, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (data == null) {
                Setmeal setmeal = setmealDao.findById(id);
                String sid = setmeal.getId().toString();
                data = JSON.toJSONString(setmeal);
                jedis.hset(RedisMessageConstant.SETMEAL_DETAIL_REDIES, sid, data);
            }
        } finally {
            jedis.close();
            return JSON.parseObject(data, Setmeal.class);
        }
    }

    /**
     * 查询套餐名称 以及套餐预约数量
     *
     * @return
     */
    @Override
    public List<Map> setmealCount() {
        return setmealDao.setmealCount();
    }

    /**
     * 新增或编辑的套餐的公共方法
     *
     * @param setmealId
     * @param checkgroupIds
     */
    public void setSetmealAndCheckGroup(Integer setmealId, Integer[] checkgroupIds) {
        if (checkgroupIds != null && checkgroupIds.length > 0) {
            for (Integer checkgroupId : checkgroupIds) {
                //方便操作 map
                Map<String, Integer> map = new HashMap<>();
                map.put("setmealId", setmealId);
                map.put("checkgroupId", checkgroupId);
                setmealDao.setSetmealAndCheckGroup(map);
            }
        }
    }
}

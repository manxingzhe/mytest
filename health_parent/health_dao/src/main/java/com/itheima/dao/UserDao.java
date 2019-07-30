package com.itheima.dao;

import com.itheima.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户dao层接口
 */
public interface UserDao {
    /**
     * 根据用户名查询用户对象
     *
     * @param username
     * @return
     */
    User findUserByUsername(String username);

    User findUserByTelephone(String telephone);

    @Update("update t_user set password = #{password} where id = #{id} ")
    void updatePassword(@Param("id") Integer id, @Param("password") String password);
}

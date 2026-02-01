package com.seckill.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.order.dto.LoginDTO;
import com.seckill.order.dto.RegisterDTO;
import com.seckill.order.entity.SeckillUser;
import com.seckill.order.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author seckill
 * @since 1.0.0
 */
public interface UserService extends IService<SeckillUser> {

    /**
     * 用户登录
     *
     * @param dto 登录请求
     * @return 用户信息（含Token）
     */
    UserVO login(LoginDTO dto);

    /**
     * 用户注册
     *
     * @param dto 注册请求
     * @return 用户信息（含Token）
     */
    UserVO register(RegisterDTO dto);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserInfo(Long userId);
}

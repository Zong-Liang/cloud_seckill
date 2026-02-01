package com.seckill.order.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.common.constant.UserStatus;
import com.seckill.common.exception.BusinessException;
import com.seckill.common.result.ResultCode;
import com.seckill.order.dto.LoginDTO;
import com.seckill.order.dto.RegisterDTO;
import com.seckill.order.entity.SeckillUser;
import com.seckill.order.mapper.UserMapper;
import com.seckill.order.security.JwtUtil;
import com.seckill.order.service.UserService;
import com.seckill.order.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现
 *
 * @author seckill
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, SeckillUser> implements UserService {

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserVO login(LoginDTO dto) {
        // 1. 查询用户
        LambdaQueryWrapper<SeckillUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillUser::getUsername, dto.getUsername());
        SeckillUser user = this.getOne(wrapper);

        if (user == null) {
            log.warn("用户不存在: {}", dto.getUsername());
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 2. 验证密码 - 添加调试日志
        log.info("=== 密码验证调试 ===");
        log.info("输入密码: {}", dto.getPassword());
        log.info("数据库哈希: {}", user.getPassword());

        // 测试直接生成的哈希
        String testHash = passwordEncoder.encode("123456");
        log.info("123456 新生成的哈希: {}", testHash);
        log.info("验证输入密码是否匹配数据库哈希: {}", passwordEncoder.matches(dto.getPassword(), user.getPassword()));
        log.info("验证 123456 是否匹配数据库哈希: {}", passwordEncoder.matches("123456", user.getPassword()));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("密码错误: {}", dto.getUsername());
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 3. 检查状态
        if (user.getStatus() == UserStatus.DISABLED) {
            log.warn("用户已禁用: {}", dto.getUsername());
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 4. 更新登录信息
        user.setLoginCount(user.getLoginCount() + 1);
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);

        // 5. 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        log.info("用户登录成功: {}", dto.getUsername());
        return toVO(user, token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO dto) {
        // 1. 检查用户名是否存在
        LambdaQueryWrapper<SeckillUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillUser::getUsername, dto.getUsername());
        if (this.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }

        // 2. 检查手机号是否存在
        if (StrUtil.isNotBlank(dto.getPhone())) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SeckillUser::getPhone, dto.getPhone());
            if (this.count(wrapper) > 0) {
                throw new BusinessException(ResultCode.PHONE_EXIST);
            }
        }

        // 3. 创建用户
        SeckillUser user = new SeckillUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StrUtil.isBlank(dto.getNickname()) ? dto.getUsername() : dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setStatus(UserStatus.NORMAL);
        user.setLoginCount(0);
        this.save(user);

        // 4. 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        log.info("用户注册成功: {}", dto.getUsername());
        return toVO(user, token);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        SeckillUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        return toVO(user, null);
    }

    /**
     * 转换为 VO
     */
    private UserVO toVO(SeckillUser user, String token) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setToken(token);
        return vo;
    }
}

package com.seckill.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.order.entity.SeckillUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 *
 * @author seckill
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<SeckillUser> {
}

package com.seckill.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.common.constant.GoodsStatus;
import com.seckill.common.exception.BusinessException;
import com.seckill.common.result.ResultCode;
import com.seckill.stock.entity.SeckillGoods;
import com.seckill.stock.mapper.GoodsMapper;
import com.seckill.stock.service.GoodsService;
import com.seckill.stock.service.StockCacheService;
import com.seckill.stock.vo.GoodsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 *
 * @author seckill
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, SeckillGoods> implements GoodsService {

    private final GoodsMapper goodsMapper;
    private final StockCacheService stockCacheService;

    @Override
    public List<GoodsVO> listOngoingGoods() {
        LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillGoods::getStatus, GoodsStatus.ONGOING)
                .gt(SeckillGoods::getStockCount, 0)
                .orderByAsc(SeckillGoods::getStartTime);

        List<SeckillGoods> list = this.list(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<GoodsVO> listAllGoods() {
        LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(SeckillGoods::getStatus, GoodsStatus.OFF_SHELF)
                .orderByDesc(SeckillGoods::getCreateTime);

        List<SeckillGoods> list = this.list(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public GoodsVO getGoodsById(Long id) {
        SeckillGoods goods = this.getById(id);
        if (goods == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
        }
        return convertToVO(goods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long goodsId, Integer count) {
        // 先查询商品获取版本号
        SeckillGoods goods = this.getById(goodsId);
        if (goods == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
        }

        // 乐观锁扣减库存
        int rows = goodsMapper.deductStock(goodsId, count, goods.getVersion());
        if (rows == 0) {
            log.warn("扣减库存失败，商品ID: {}, 当前库存: {}, 扣减数量: {}",
                    goodsId, goods.getStockCount(), count);
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }

        log.info("扣减库存成功，商品ID: {}, 扣减数量: {}", goodsId, count);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackStock(Long goodsId, Integer count) {
        // 1. 回滚 Redis 库存
        stockCacheService.rollbackStock(goodsId, count);

        // 2. 回滚 MySQL 库存
        int rows = goodsMapper.rollbackStock(goodsId, count);
        if (rows > 0) {
            log.info("回滚库存成功（Redis + MySQL） - goodsId: {}, count: {}", goodsId, count);
            return true;
        }
        log.warn("回滚 MySQL 库存失败 - goodsId: {}", goodsId);
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductMySQLStock(Long goodsId, Integer count) {
        int rows = goodsMapper.directDeductStock(goodsId, count);
        if (rows > 0) {
            log.info("MySQL 库存同步扣减成功 - goodsId: {}, count: {}", goodsId, count);
            return true;
        }
        log.warn("MySQL 库存同步扣减失败，可能库存不足 - goodsId: {}, count: {}", goodsId, count);
        return false;
    }

    @Override
    public Long getRedisStock(Long goodsId) {
        return stockCacheService.getStock(goodsId);
    }

    @Override
    public SeckillGoods checkSeckillable(Long goodsId) {
        SeckillGoods goods = this.getById(goodsId);

        // 1. 检查商品是否存在
        if (goods == null) {
            throw new BusinessException(ResultCode.GOODS_NOT_EXIST);
        }

        // 2. 检查商品状态
        if (goods.getStatus() == GoodsStatus.OFF_SHELF) {
            throw new BusinessException(ResultCode.GOODS_OFF_SHELF);
        }

        LocalDateTime now = LocalDateTime.now();

        // 3. 检查活动时间
        if (now.isBefore(goods.getStartTime())) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_STARTED);
        }

        if (now.isAfter(goods.getEndTime())) {
            throw new BusinessException(ResultCode.ACTIVITY_ENDED);
        }

        // 4. 检查库存
        if (goods.getStockCount() <= 0) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }

        return goods;
    }

    /**
     * 转换为 VO 对象
     */
    private GoodsVO convertToVO(SeckillGoods goods) {
        GoodsVO vo = new GoodsVO();
        BeanUtils.copyProperties(goods, vo);

        // 设置状态描述
        vo.setStatusDesc(getStatusDesc(goods.getStatus()));

        // 计算折扣
        if (goods.getGoodsPrice() != null && goods.getSeckillPrice() != null
                && goods.getGoodsPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = goods.getSeckillPrice()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(goods.getGoodsPrice(), 0, RoundingMode.HALF_UP);
            vo.setDiscountPercent(discount.intValue());
        }

        return vo;
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case GoodsStatus.NOT_STARTED:
                return "未开始";
            case GoodsStatus.ONGOING:
                return "进行中";
            case GoodsStatus.ENDED:
                return "已结束";
            case GoodsStatus.OFF_SHELF:
                return "已下架";
            default:
                return "未知";
        }
    }
}

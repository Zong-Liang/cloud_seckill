import { useState, useEffect, useMemo } from "react";
import { Button, Modal, message } from "antd";
import { useNavigate } from "react-router-dom";
import { ThunderboltOutlined, LoginOutlined } from "@ant-design/icons";
import { doSeckill, checkKilled } from "@/api";
import { useUserStore, useSeckillStore } from "@/store";
import { canDoSeckill, getRealStatus } from "@/utils";
import type { GoodsVO } from "@/types";
import { GoodsStatus, GoodsStatusText } from "@/types";

interface SeckillButtonProps {
  /** 商品信息 */
  goods: GoodsVO;
  /** 是否禁用 */
  disabled?: boolean;
  /** 自定义类名 */
  className?: string;
  /** 按钮大小 */
  size?: "small" | "middle" | "large";
}

/**
 * 秒杀按钮组件
 */
export default function SeckillButton({
  goods,
  disabled,
  className = "",
  size = "large",
}: SeckillButtonProps) {
  const [loading, setLoading] = useState(false);
  /** 服务端已秒杀标记 */
  const [serverKilled, setServerKilled] = useState(false);
  const navigate = useNavigate();
  const { user, isLoggedIn } = useUserStore();
  const {
    hasSeckilled,
    addSeckillRecord,
    isPending: isPendingFn,
    setPending,
  } = useSeckillStore();

  // 读取 store 中的实际状态值（而非函数引用），确保状态变化触发重渲染
  const pendingGoodsIds = useSeckillStore((s) => s.pendingGoodsIds);
  const seckillRecords = useSeckillStore((s) => s.seckillRecords);

  // 登录后向服务端查询是否已秒杀过该商品
  useEffect(() => {
    if (isLoggedIn && user && goods.id) {
      checkKilled(user.id, goods.id)
        .then((res) => {
          if (res.data === true) {
            setServerKilled(true);
          }
        })
        .catch(() => {
          // 查询失败时不阻断主流程
        });
    }
  }, [isLoggedIn, user, goods.id]);

  // 按钮状态：用 useMemo 代替 useCallback，显式依赖 store 值
  const buttonState = useMemo(() => {
    const status = getRealStatus(goods);

    // 已下架
    if (status === GoodsStatus.OFF_SHELF) {
      return { text: "已下架", disabled: true, type: "default" as const };
    }
    // 未开始
    if (status === GoodsStatus.NOT_STARTED) {
      return { text: "即将开始", disabled: true, type: "default" as const };
    }
    // 已结束
    if (status === GoodsStatus.ENDED) {
      return { text: "已结束", disabled: true, type: "default" as const };
    }
    // 已售罄
    if (goods.stockCount <= 0) {
      return { text: "已售罄", disabled: true, type: "default" as const };
    }
    // 已秒杀过（本地记录 或 服务端记录）
    if (serverKilled || seckillRecords.some((r) => r.goodsId === goods.id)) {
      return { text: "已抢购", disabled: true, type: "default" as const };
    }
    // 正在秒杀中
    if (pendingGoodsIds.includes(goods.id)) {
      return { text: "抢购中...", disabled: true, type: "primary" as const };
    }
    // 未登录时提示需要登录
    if (!isLoggedIn) {
      return { text: "登录后秒杀", disabled: false, type: "primary" as const };
    }
    // 可秒杀
    return { text: "立即秒杀", disabled: false, type: "primary" as const };
  }, [goods, isLoggedIn, serverKilled, seckillRecords, pendingGoodsIds]);

  // 执行秒杀
  const handleSeckill = async () => {
    // 1. 实时校验活动状态（防止按钮渲染后状态变化）
    const currentStatus = getRealStatus(goods);
    if (currentStatus !== GoodsStatus.IN_PROGRESS) {
      message.warning(
        currentStatus === GoodsStatus.NOT_STARTED
          ? "秒杀活动还未开始"
          : currentStatus === GoodsStatus.ENDED
            ? "秒杀活动已结束"
            : GoodsStatusText[currentStatus],
      );
      return;
    }

    // 2. 检查登录状态
    if (!isLoggedIn || !user) {
      Modal.confirm({
        title: "提示",
        content: "请先登录后再参与秒杀",
        okText: "去登录",
        cancelText: "取消",
        onOk: () => {
          navigate("/login", { state: { from: `/goods/${goods.id}` } });
        },
      });
      return;
    }

    // 3. 前端限流（合并防抖 + 限流为一个检查）
    if (!canDoSeckill(goods.id)) {
      message.warning("操作太频繁，请稍后再试");
      return;
    }

    // 4. 已秒杀检查（本地 + 服务端）
    if (serverKilled || hasSeckilled(goods.id)) {
      message.warning("您已参与过该商品秒杀");
      return;
    }

    // 5. 库存前置检查
    if (goods.stockCount <= 0) {
      message.warning("商品已售罄");
      return;
    }

    setLoading(true);
    setPending(goods.id, true);

    try {
      const result = await doSeckill({
        userId: user.id,
        goodsId: goods.id,
        count: 1,
        channel: "PC",
      });

      // 秒杀成功
      const orderNo = result.data;
      addSeckillRecord(goods.id, orderNo);
      setServerKilled(true);
      message.success("恭喜，秒杀成功！");

      // 跳转到结果页
      navigate(`/seckill/result/${orderNo}`);
    } catch (error: unknown) {
      // 业务错误已在 axios 拦截器中处理（会弹 message）
      // 这里兜底处理拦截器未覆盖的场景
      const err = error as { code?: number; message?: string };
      if (err?.code === 1104) {
        // REPEAT_ORDER(1104) 重复下单 → 同步本地状态
        setServerKilled(true);
      }
      console.error("秒杀失败:", error);
    } finally {
      setLoading(false);
      setPending(goods.id, false);
    }
  };

  return (
    <Button
      type={buttonState.type}
      size={size}
      block
      danger={buttonState.type === "primary"}
      loading={loading}
      disabled={disabled || buttonState.disabled}
      onClick={handleSeckill}
      icon={
        !isLoggedIn && buttonState.type === "primary" ? (
          <LoginOutlined />
        ) : buttonState.type === "primary" ? (
          <ThunderboltOutlined />
        ) : undefined
      }
      className={`seckill-btn ${className}`}
    >
      {buttonState.text}
    </Button>
  );
}

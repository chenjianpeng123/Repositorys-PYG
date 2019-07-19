package cn.pinyougou.cart.service;


import cn.pinyougou.pojogroup.Cart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 添加商品到购物车
     * @param cartList 购物车列表 （先拿到购物车列表操作后返回）
     * @param itemId  商品id
     * @param num   数量
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 从redis中查询购物车
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车保存到redis中
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);

    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    public List<Cart> mregeCartList(List<Cart> cartList1,List<Cart> cartList2);
}

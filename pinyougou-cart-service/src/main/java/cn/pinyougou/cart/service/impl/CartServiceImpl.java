package cn.pinyougou.cart.service.impl;


import cn.pinyougou.cart.service.CartService;
import cn.pinyougou.mapper.TbItemMapper;
import cn.pinyougou.pojo.TbItem;
import cn.pinyougou.pojo.TbOrderItem;
import cn.pinyougou.pojogroup.Cart;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务实现类
 * 1.根据商品 SKU ID 查询 SKU 商品信息
 * 2.获取商家 ID
 * 3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
 * 4.如果购物车列表中不存在该商家的购物车
 * 4.1 新建购物车对象
 * 4.2 将新建的购物车对象添加到购物车列表
 * 5.如果购物车列表中存在该商家的购物车
 * 查询购物车明细列表中是否存在该商品
 * 5.1. 如果没有，新增购物车明细
 * 5.2. 如果有，在原购物车明细上添加数量，更改金额
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 添加购物车
     *
     * @param cartList 购物车列表 （先拿到购物车列表操作后返回）
     * @param itemId   商品id
     * @param num      数量
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {


        //1:根据商品 SKU ID 查询 SKU 商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品状态无效");
        }

        //2.获取商家 ID
        String sellerId = item.getSellerId();

        //3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);

        // 4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {
            // 4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);//商家id
            cart.setSellerName(item.getSeller());//商家名称
            List<TbOrderItem> orderItemList = new ArrayList<>(); //创建购物车明细列表
            TbOrderItem orderItem = createOrderItem(item, num);//调用新创建的
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);//购物车明细列表
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);

        } else {// 5.如果购物车列表中存在该商家的购物车
            //判断购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem == null) {

                //5.1. 如果没有，新增购物车明细 并添加到该购物车的明细列表中
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);

            } else {//5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);//更改数量
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));//更改金额

                //当明细列表的数据小于等于0 移除该明细列表
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);
                }

                //当购物车的明细列表等于0  移除该购物车
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        //返回购物车列表
        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 从redis中查询购物车
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从 redis 中提取购物车数据....."+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        //判断cartList是否为空 如果为空让他等于空集合
        if(cartList==null){
            cartList=new ArrayList();
        }
        return cartList;
    }

    /**
     * 将购物车保存到redis
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向 redis 存入购物车数据....."+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    @Override
    public List<Cart> mregeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车");
        //遍历购物车
        for (Cart cart : cartList2) {
            //遍历购物车明细列表
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                //把orderList添加到cartList1中
                cartList1=addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList1;
    }

    /**
     * 根据商家 ID 判断购物车列表中是否存在该商家的购物车
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        //遍历购物车列表
        for (Cart cart : cartList) {
            //判断
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 创建购物车明细对象
     *
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }
        //创建新的购物车明细对象
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setItemId(item.getId());//商品id
        orderItem.setGoodsId(item.getGoodsId());//SPUid
        orderItem.setNum(num);//商品购买数量
        orderItem.setPicPath(item.getImage());//商品图片
        orderItem.setPrice(item.getPrice());//商品单价
        orderItem.setSellerId(item.getSellerId());//商家id
        orderItem.setTitle(item.getTitle());//商品标题
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));//商品总金额

        return orderItem;
    }

    /**
     * 根据skuID在购物车明细列表中查询购物车明细对象
     * 判断购物车中是否有购物车商品明细
     *
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }
}

package cn.pinyougou.cart.controller;

import cn.pinyougou.cart.service.CartService;
import cn.pinyougou.pojogroup.Cart;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import util.CookieUtil;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * （1）从 cookie 中取出购物车
 * （2）向购物车添加商品
 * （3）将购物车存入 cookie
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 6000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 1）从 cookie 中取出购物车
     * 2）向购物车添加商品
     * 3）将购物车存入 cookie
     *
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            // 1）取出购物车
            List<Cart> cartList = findCartList();
            // 2）向购物车添加商品
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if(username.equals("anonymousUser")){//如果是未登录，保存到 cookie
                // 3）将购物车存入 cookie
                String cartListString = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request, response, "cartList", cartListString, 3600 * 24, "UTF-8");
                System.out.println("向cookie中存储数据");
            }else {//如果是已登录，保存到 redis
                cartService.saveCartListToRedis(username,cartList);
                System.out.println("向redis中存储数据");
            }
            return new Result(true, "存入购物车成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, "存入购物车失败");
        }
    }

    /**
     * 查询购物车
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //获取登陆人账号 判断当前是否有登陆人
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("用户：" + username);
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        //当cartListString等于null或者空字符串是 给他赋值为[]
        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";
        }
        //转换为list
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if (username.equals("anonymousUser")) {
            //未登陆 读取本地购物车
            //从cookie中获取购物车
            System.out.println("从cookie中提取购物车");

            //返回cookie中的cartList
            return cartList_cookie;
        } else {
            //已登录 从redis中提取
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            if(cartList_redis.size()>0){//判断 当本地购物车中存在数据执行合并购物车
                //得到合并后的购物车
                List<Cart> cartList = cartService.mregeCartList(cartList_cookie, cartList_redis);
                //将合并后的购物车存入redis
                cartService.saveCartListToRedis(username,cartList);
                //清除本地购物车
                util.CookieUtil.deleteCookie(request,response,"cartList");
                System.out.println("执行合并");
                //返回合并后的购物车
                return cartList;
            }
            return cartList_redis;
        }
    }
}

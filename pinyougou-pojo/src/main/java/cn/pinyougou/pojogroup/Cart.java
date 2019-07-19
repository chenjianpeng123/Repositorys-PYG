package cn.pinyougou.pojogroup;

import cn.pinyougou.pojo.TbOrderItem;

import java.io.Serializable;
import java.util.List;

/**
 * 购物车实体类
 */
public class Cart implements Serializable {

    private String sellerId;//商家id
    private String sellerName;//商家名称
    private List<TbOrderItem> orderItemList;//购物车明细（也是订单明细）

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<TbOrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<TbOrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}

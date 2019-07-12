package cn.pinyougou.page.service;

/**
 * 商品详情页接口
 */
public interface ItemPageService {
    /**
     * 生成商品详情页
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);
}

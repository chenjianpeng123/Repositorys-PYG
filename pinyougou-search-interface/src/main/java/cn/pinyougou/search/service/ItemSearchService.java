package cn.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 搜索方法
     *
     * @param searchMap 前台发送的搜索条件类型可能是多个
     * @return Map        后台返回给前台的搜索结果也包含多种类型
     */
    public Map<String, Object> search(Map searchMap);

    /**
     * 导入商品列表数据
     * @param list
     */
    public void importList(List list);

    /**
     * 删除数据
     * @param goodsIdList
     */
    public void deleteByGoodsIds(List goodsIdList);
}

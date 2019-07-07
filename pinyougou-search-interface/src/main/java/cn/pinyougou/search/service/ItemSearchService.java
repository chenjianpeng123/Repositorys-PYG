package cn.pinyougou.search.service;

import java.util.Map;

public interface ItemSearchService {
    /**
     * 搜索方法
     *
     * @param searchMap 前台发送的搜索条件类型可能是多个
     * @return Map        后台返回给前台的搜索结果也包含多种类型
     */
    public Map<String, Object> search(Map searchMap);
}

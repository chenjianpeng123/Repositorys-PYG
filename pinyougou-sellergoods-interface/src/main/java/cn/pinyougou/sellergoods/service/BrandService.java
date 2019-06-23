package cn.pinyougou.sellergoods.service;

import cn.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;

/**
 * 品牌接口
 */
public interface BrandService {
    /**
     * 查询所有
     * @return
     */
    public List<TbBrand> findAll();

    /**
     * 返回分页列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult findPage(int pageNum,int pageSize);

    /**
     * 添加数据
     * @param brand
     */
    public void add(TbBrand brand);

    /**
     * 根据id查询实体
     * @param id
     * @return
     */
    public TbBrand findOne(Long id);

    /**
     * 修改数据
     * @param brand
     */
    public void update(TbBrand brand);

    /**
     * 删除数据
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 分页查询
     * @param brand
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @return
     */
    public PageResult findPage(TbBrand brand,int pageNum,int pageSize);
}

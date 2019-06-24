package cn.pinyougou.sellergoods.service.impl;

import cn.pinyougou.mapper.TbBrandMapper;
import cn.pinyougou.pojo.TbBrand;
import cn.pinyougou.pojo.TbBrandExample;
import cn.pinyougou.sellergoods.service.BrandService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;

    /**
     * 查询品牌数据
     * @return
     */
    @Override
    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    /**
     * 品牌分页
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 添加数据
     * @param brand
     */
    @Override
    public void add(TbBrand brand) {
        brandMapper.insert(brand);
    }

    /**
     * 根据id查询实体
     * @param id
     * @return
     */
    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 修改数据
     * @param brand
     */
    @Override
    public void update(TbBrand brand) {
     brandMapper.updateByPrimaryKey(brand);
    }

    /**
     * 删除数据
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    /**
     * 分页
     * @param brand
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     * @return
     */
    @Override
    public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
//        PageHelper.startPage(pageNum,pageSize);
//        TbBrandExample example= new TbBrandExample();
//        TbBrandExample.Criteria criteria = example.createCriteria();
//        if (brand != null){
//            if (brand.getName() != null && brand.getName().length()>0){
//                criteria.andNameLike("%"+brand.getName()+"%");
//            }
//            if(brand.getFirstChar()!=null && brand.getFirstChar().length()>0){
//                criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");
//            }
//        }
//        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);
//        return new PageResult(page.getTotal(),page.getResult());
        PageHelper.startPage(pageNum,pageSize);
        TbBrandExample example = new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();
        if(brand.getName() != null && brand.getName().length()>0){
            criteria.andNameLike("%"+brand.getName()+"%");
        }
        if(brand.getFirstChar()!= null&& brand.getFirstChar().length()>0){
            criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");
        }
        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);
        return new PageResult(page.getTotal(),page.getResult());
    }
}

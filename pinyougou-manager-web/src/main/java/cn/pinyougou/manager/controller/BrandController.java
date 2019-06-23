package cn.pinyougou.manager.controller;

import cn.pinyougou.pojo.TbBrand;
import cn.pinyougou.pojo.TbBrandExample;
import cn.pinyougou.sellergoods.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
   private BrandService brandService;

    /**
     * 查询所有数据
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbBrand> findAll(){
    return brandService.findAll();
    }

    /**
     * 分页
     * @param page
     * @param size
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page,int size){
       return brandService.findPage(page,size);
    }

    /**
     * 添加数据
     * @param brand
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.add(brand);
            return new Result(true,"添加成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败!");
        }
    }

    /**
     * 根据id查询数据
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
        return brandService.findOne(id);
    }

    /**
     * 修改数据
     * @param brand
     * @return
     */
   @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改失败");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
    @RequestMapping("/delete")
    public Result dalete(Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败!");
        }
    }

    /**
     * 查询+分页
     * @param brand
     * @param page
     * @param size
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand brand,int page,int size){
    return brandService.findPage(brand,page,size);
   }
}

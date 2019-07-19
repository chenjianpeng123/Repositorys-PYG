package cn.pinyougou.pojogroup;



import cn.pinyougou.pojo.TbTypeTemplate;

import java.io.Serializable;


public class ItemCat implements Serializable {

    private Long id;
    private String name;
    private Long parentId;


    private TbTypeTemplate typeTemplate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public TbTypeTemplate getTypeTemplate() {
        return typeTemplate;
    }

    public void setTypeTemplate(TbTypeTemplate typeTemplate) {
        this.typeTemplate = typeTemplate;
    }
}

//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, itemCatService, uploadService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承
    //定义页面实体结构 和 规格结构
    //$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};
    $scope.entity = {goodsDesc: {itemImages: [], specificationItems: []}};
    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function () {
        //获取商品参数
        var id = $location.search()['id'];
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //向富文本编辑器中添加商品介绍
                editor.html($scope.entity.goodsDesc.introduction);
                //读取图片转换
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //读取扩展属性转换
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //读取规格数据转换
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                //读取SKU列表规格数据集合遍历出每一行数据
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    //逐行转换
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);

                }
            }
        );
    }
       //保存
    $scope.save = function () {
      //提取文本编辑器的值
        $scope.entity.goodsDesc.introduction = editor.html();
        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有 ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert('保存成功');
                    location.href="goods.html";//跳转到商品列表
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    //上传图片
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.success) {//如果文件上传成功 取出url
                    $scope.image_entity.url = response.message;//设置文件地址
                } else {
                    alert(response.message);
                }
            }).error(function () {
            alert('上传时发生错误！');
        });
    }

    //添加图片列表
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //移除图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);

    }
    //显示一级下拉列表数据
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List = response;
            }
        );
    }
    //显示二级下拉列表数据
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
        //根据选择的值查询二级分类
        if (newValue) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat2List = response;
                }
            );
        }

    });
    //显示三级下拉列表数据
    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
        if (newValue) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat3List = response;
                }
            );
        }
    });

    //读取模板ID
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {
        if (newValue) {
            itemCatService.findOne(newValue).success(
                function (response) {
                    $scope.entity.goods.typeTemplateId = response.typeTemplate.id;
                }
            );
        }
    });
    //读取模板id，更新品牌列表
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate = response;//获取类型模板
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//获取品牌列表
                //将扩展属性转换  存入商品说明表中
                if ($location.search()['id'] == null) {//如果id为空就是增加商品
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                }

            }
        );
        //获取规格选项列表
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.specList = response;
            }
        );
    });

    $scope.updateSpecAttribute = function ($event, name, value) {
        //调用通用方法 查询集合中是否存在要查找的值
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', name);
        //判断是否有值
        if (object != null) {
            //判断是否取消勾选
            if ($event.target.checked) {
                //如果有值就追加在变量中
                object.attributeValue.push(value);
            } else {
                //取消勾选
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);//移除选项
                //如果选项都取消了，就将此条记录移除
                if (object.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        } else {
            //没有值 向集合中添加元素
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
        }
    }
    //创建SKU列表
    $scope.createItemList = function () {
        //列表初始化
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];
        //定义规格选项集合
        var items = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }
    //克隆
    addColumn = function (list, columnName, columnValues) {
        //定义新的集合
        var newList = [];
        //循环原本的集合得到每一个元素
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < columnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                //添加属性
                newRow.spec[columnName] = columnValues[j];
                newList.push(newRow);
            }
        }

        //返回新的集合
        return newList;
    }

    //定义状态数组
    $scope.status = ['未审核', '已审核', '审核未通过', '关闭'];//商品状态

    //定义商品分类数组
    $scope.itemCatList = [];
    //获取商品分类列表
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    //将列表id和列表名称添加到商品分类列表
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            }
        );
    }
    //根据规格名称和选项名称返回复选框是否被勾选
    $scope.checkAttributeValue = function (specName, optionName) {
        //规格选项
        var items = $scope.entity.goodsDesc.specificationItems;
        //根据某key的值查询对应的规格选项
        var object = $scope.searchObjectByKey(items, 'attributeName', specName);
        if (object == null) {
            return false;
        } else {
            //判断能查到规格选项
            if (object.attributeValue.indexOf(optionName) >= 0) {
                return true;
            } else {
                return false;
            }
        }
    }
});

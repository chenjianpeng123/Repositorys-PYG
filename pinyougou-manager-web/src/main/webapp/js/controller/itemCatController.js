//控制层
app.controller('itemCatController', function ($scope, $controller, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        itemCatService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        itemCatService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        itemCatService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                $scope.entity.typeId=$scope.entity.typeTemplate.id;
            }
        );
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = itemCatService.update($scope.entity); //修改
        } else {
            $scope.entity.parentId = $scope.parentId;//赋值上级id
            serviceObject = itemCatService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.findByParentId($scope.parentId);//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        itemCatService.dele($scope.selectIds).success(
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
        itemCatService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    //根据上级id查询下级列表
    //查询时记录上级id
    $scope.parentId = 0;//上级id
    $scope.findByParentId = function (parentId) {
        $scope.parentId = parentId;//记住上级id
        itemCatService.findByParentId(parentId).success(
            function (response) {
                $scope.list = response;
            }
        );
    }
    //默认级别为“1”
    $scope.grade = 1;
    //设置级别
    $scope.setGrade = function (value) {
        $scope.grade = value;
    }
    //读取列表
    $scope.selectList = function (p_entity) {
        if ($scope.grade == 1) {
            $scope.entity_1 = null;
            $scope.entity_2 = null;
        }
        if ($scope.grade == 2) {
            $scope.entity_1 = p_entity;
            $scope.entity_2 = null;
        }
        if ($scope.grade == 3) {
            $scope.entity_2 = p_entity;
        }
        $scope.findByParentId(p_entity.id);//查询此级别以下的列表
    }

    //商品分类下拉列表
    $scope.typeTemplateList = {data: []};
    //读取
    $scope.findTypeTemplateList=function () {
        typeTemplateService.selectTypeTemplateList().success(
            function (response) {
                $scope.typeTemplateList={data:response};
            }
        );
    }

    //判断当前分类下时候从在子类
    $scope.checkIsParent=function ($event, id) {
        itemCatService.findByParentId(id).success(
            function (response) {
               // alert(response);
                //判断response不为空 代表有下一级
                if(response!=null && response.length>0){
                    //将id从selectIDS删除
                    var index = $scope.selectIds.indexOf(id);
                    $scope.selectIds.splice(index,1);
                    alert("当前分类存在下级分类，不能删除！")

                    //取消checkBox选中
                    $event.target.checked=false;
                    return;
                }
            }
        )
    }
});

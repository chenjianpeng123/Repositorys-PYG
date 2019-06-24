//定义控制器
app.controller('brandController', function ($scope,$controller,brandService) {

    $controller('baseController',{$scope:$scope});//继承 通过$scope传递
    //查询所有数据
    $scope.findAll = function () {
        brandService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    };

    //分页
    $scope.findPage = function (page, size) {
        brandService.findPage(page, size).success(
            function (response) {
                $scope.list = response.rows;//显示当前页的数据
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };
    //根据id查询
    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    };
    //添加和修改
    $scope.save = function () {
        var Object = null;//定义一个变量
        if ($scope.entity.id != null) {//如果有 ID
            Object = brandService.update($scope.entity);//则执行修改方法
        } else {
            Object = brandService.add($scope.entity);
        }
        Object.success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新
                } else {
                    alert(response.message);
                }
            }
        );
    };


    //删除
    $scope.dele = function () {
        if (confirm("确定要删除吗？")) {
            brandService.dele($scope.selectIds).success(
                function (response) {
                    if (response.success) {
                        $scope.reloadList();
                    } else {
                        alert(response.message);
                    }
                }
            );
        }
    };
    //初始化
    $scope.searchEntity = {};
    //条件查询
    $scope.search = function (page, size) {
        brandService.search(page,size,$scope.searchEntity).success(
            function (respouse) {
                $scope.list = respouse.rows;
                $scope.paginationConf.totalItems = respouse.total;
            }
        )
    }
});
//服务抽取 自定义服务
app.service("brandService", function ($http) {
    //读取数据列表绑定到订单中
    this.findAll = function () {
        return $http.get('../brand/findAll.do');
    };
    //分页服务
    this.findPage = function (page, size) {
        return $http.get('../brand/findPage.do?page=' + page + '&size=' + size);
    };
    //根据id查询
    this.findOne = function (id) {
        return $http.get('../brand/findOne.do?id=' + id);
    };
    //添加数据
    this.add = function (entity) {
        return $http.post('../brand/add.do', entity)
    };
    //修改数据
    this.update = function (entity) {
        return $http.post('../brand/update.do', entity)
    };
    //删除数据
    this.dele = function (ids) {
        return $http.get('../brand/delete.do?ids=' + ids);
    };
    //条件查询
    this.search = function (page, size, searchEntity) {
        return $http.post('../brand/search.do?page=' + page + '&size=' + size, searchEntity);
    };


});
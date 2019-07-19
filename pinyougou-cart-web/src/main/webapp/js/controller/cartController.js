app.controller('cartController', function ($scope, cartService) {
    //查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
               //求和合计数
                $scope.totalValue=cartService.sum($scope.cartList);
            }
        );
    }
    //数量的加减 添加商品到购物车
    $scope.addGoodsToCartList=function (itemId, num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {
                if(response.success){
                   $scope.findCartList();//刷新列表
                }else {
                   alert(response.message);//弹出错误信息
                }
            }
        );
    }
});
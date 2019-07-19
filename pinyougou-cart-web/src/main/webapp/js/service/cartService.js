app.service('cartService', function ($http) {
    //购物车列表
    this.findCartList = function () {
        return $http.get('cart/findCartList.do');
    }
    //数量的加减 添加商品到购物车
    this.addGoodsToCartList = function (itemId, num) {
        return $http.get('cart/addGoodsToCartList.do?itemId=' + itemId + '&num=' + num);
    }

    //求合计
    this.sum = function (cartList) {
        //合计实体
        var totalValue={totalNum:0,totalMoney:0};
        //遍历购物车获取每个购物车对象
        for(var i=0;i<cartList.length;i++){
            var cart=cartList[i];//购物车对象
            //遍历购物车对象得到购物车明细列表
            for(var j=0;j<cart.orderItemList.length;j++){
                 var orderItem=cart.orderItemList[j];//购物车明细
                totalValue.totalNum +=orderItem.num;//累加数量
                totalValue.totalMoney += orderItem.totalFee;//累加金额
            }
        }
     return totalValue;
    }
});
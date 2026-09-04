<template>
  <footer class="site-footer">
    <div class="site-container site-footer__grid">
      <div class="site-footer__brand">
        <strong>筑家优选</strong>
        <p>把线下门店的材料、规格与价格，整理成更容易理解和购买的线上选材体验。</p>
      </div>
      <div><h3>选购入口</h3><RouterLink to="/menu">全部商品</RouterLink><RouterLink to="/coupons">优惠券</RouterLink><RouterLink to="/orders">我的订单</RouterLink></div>
      <div><h3>购买帮助</h3><RouterLink to="/service">配送与安装</RouterLink><RouterLink to="/service">售后咨询</RouterLink><RouterLink to="/service">规格与计价</RouterLink></div>
      <div><h3>联系我们</h3><p>门店地址与电话待运营配置</p><p>在线咨询请进入服务中心</p></div>
    </div>
    <div class="site-container site-footer__bottom"><span>© 2026 筑家优选交易平台</span><span>商品价格与库存以后端实时数据为准</span></div>
  </footer>
</template>

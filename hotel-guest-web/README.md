# 筑家优选用户端前端

基于 `Vue 3 + Vite + Pinia + Vue Router` 的家装建材交易平台用户端单页应用，当前版本优先实现桌面端。

## 技术栈

- Vue 3
- Vite
- Pinia
- Vue Router

## 页面能力

- 欢迎引导页
- 商品首页与搜索
- 商品详情页
- 全局悬浮购物车与购物车抽屉
- 领券中心
- 我的优惠券
- 提交订单页
- 我的订单页
- 极简服务页

## 本地开发

```bash
cd D:\JAVASTUDY\take_out-heima\hotel-guest-web
npm install
npm run dev
```

默认访问地址：
- [http://localhost:5173](http://localhost:5173)

## 构建到后端端口

前端会构建到：

`D:\JAVASTUDY\take_out-heima\sky-take-out\sky-server\src\main\resources\guest`

执行：

```bash
cd D:\JAVASTUDY\take_out-heima\hotel-guest-web
npm run build:backend
```

然后启动后端，访问：

- [http://localhost:8081/guest](http://localhost:8081/guest)

## 当前说明

- 当前为顾客端静态交互版，已正式接入后端端口托管
- 购物车、订单、优惠券领取状态与已选优惠券使用 Pinia + LocalStorage 持久化
- 优惠券抵扣与订单状态展示沿用现有兼容接口，接口不可用时提供清晰的空状态与恢复提示

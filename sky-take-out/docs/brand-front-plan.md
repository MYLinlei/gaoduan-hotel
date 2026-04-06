# Hotel Brand Web Phase 1

## Goal

Build a public-facing hotel brand website on top of the existing `sky-take-out` backend.

This front-end delivery now covers:

- Brand homepage
- Category-based dish browsing
- Dish detail panel
- Note and comment display
- Voucher discovery and seckill entry
- Token-based member actions for like/favorite/seckill
- Shopping cart drawer
- Delivery and dine-in checkout modal
- Address management entry
- Order center with reorder and cancel actions

## Delivery Path

The site is served by the backend at:

- `/brand/index.html`

Static assets are stored under:

- `sky-server/src/main/resources/brand/`

## Public API Dependencies

These routes are treated as public browsing routes:

- `GET /user/shop/status`
- `GET /user/category/list`
- `GET /user/dish/list`
- `GET /user/dish/{id}`
- `GET /user/dishNote/list`
- `GET /user/dishComment/page`
- `GET /user/hotelHighVoucher/list`

Authenticated actions remain protected:

- `POST /user/user/devLogin`
- `POST /user/dish/like/{id}`
- `POST /user/dish/favorite/{id}`
- `POST /user/hotelHighVoucher/seckill/{id}`
- `GET /user/shoppingCart/list`
- `POST /user/shoppingCart/add`
- `POST /user/shoppingCart/sub`
- `DELETE /user/shoppingCart/clean`
- `GET /user/addressBook/list`
- `POST /user/addressBook`
- `PUT /user/addressBook/default`
- `GET /user/hotelTable/list`
- `POST /user/order/submit`
- `PUT /user/order/payment`
- `GET /user/order/historyOrders`
- `PUT /user/order/cancel/{id}`
- `POST /user/order/repetition/{id}`

## Current Scope Boundary

This version still does not yet provide:

- Dedicated multi-page routing
- User login page
- Address edit and delete forms in the brand UI
- Deep order detail page
- Final visual polish for production launch

Those belong to the next front-end expansion round after this integrated single-page transaction shell is stable.

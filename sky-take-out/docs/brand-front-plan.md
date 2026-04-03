# Hotel Brand Web Phase 1

## Goal

Build a public-facing hotel brand website on top of the existing `sky-take-out` backend.

This first front-end delivery focuses on:

- Brand homepage
- Category-based dish browsing
- Dish detail panel
- Note and comment display
- Voucher discovery and seckill entry
- Token-based member actions for like/favorite/seckill

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

- `POST /user/dish/like/{id}`
- `POST /user/dish/favorite/{id}`
- `POST /user/hotelHighVoucher/seckill/{id}`

## Current Scope Boundary

This version does not yet provide:

- Complete cart page
- Address book page
- Full order center page
- End-to-end checkout forms

Those belong to the next front-end expansion round after this visual shell is stable.

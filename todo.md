* implement my api:
  - return a list of categories
  - return some products by category
  - add to cart (first just as a bucket of products..)
  - show cart (first just as a bucket of products...)
* show category tree, allow to select products by category
* separate namespaces for client/server
* use diode/reacto on client side
* calculate totals using the ecom lib
* implement ajax messages during ajax requests (e.g. "updating cart..", "added product $x to cart")

ajax requests:

    client -> [AddToCart] -> api handler -> cart management actor -> cart actor
                                         -> [GetCartView] -> cart managent actor -> cart actor

akka persistence

